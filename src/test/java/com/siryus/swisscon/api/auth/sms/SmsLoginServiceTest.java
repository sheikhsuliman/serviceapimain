package com.siryus.swisscon.api.auth.sms;

import com.siryus.swisscon.api.auth.LemonTemplateUtil;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.auth.usertoken.TokenRequestDTO;
import com.siryus.swisscon.api.auth.usertoken.UserTokenEntity;
import com.siryus.swisscon.api.auth.usertoken.UserTokenRepository;
import com.siryus.swisscon.api.auth.user.UserRequestEntity;
import com.siryus.swisscon.api.auth.user.UserRequestService;
import com.siryus.swisscon.api.auth.user.UserRequestType;
import com.siryus.swisscon.api.auth.usertoken.UserTokenType;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomUtils;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import org.springframework.beans.factory.annotation.Autowired;
import com.siryus.swisscon.api.auth.user.UserRequestRepository;
import org.springframework.boot.test.mock.mockito.SpyBean;

public class SmsLoginServiceTest extends AbstractMvcTestBase{

    @SpyBean
    private SmsService smsService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ExtendedTokenService smsLoginService;
    
    @Autowired
    private UserTokenRepository userTokenRepository;    

    @Autowired
    private UserRequestRepository userRequestRepository;    
    
    @Autowired
    private LemonTemplateUtil lemonTemplateUtil;        
    
    private User selectedUser;
    
    private final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);            
    
    @BeforeAll
    public void doBeforeAll() {
        List<User> usersWithPhone = userRepository.findAll()
                .stream()
                .filter(u -> u.getMobile()!= null && !u.getMobile().isEmpty())
                .collect(Collectors.toList());
        
        selectedUser = usersWithPhone.get(RandomUtils.nextInt(0, usersWithPhone.size()));
        
    }

    @BeforeEach
    public void captureToken() {
        doNothing().when(smsService).sendSmsMessage(tokenCaptor.capture(), anyString());
    }
        
    @Test
    public void Given_userIssuesToken_then_tokenVerifies() {        
        smsLoginService.sendForgotPasswordSmsToken(
            TokenRequestDTO.builder()
                .ip("192.168.0.1")
                .userIdentifier(selectedUser.getFullMobile())
                .build()
        );

        assertNotNull(lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser));

        assertDoesNotThrow(() -> smsLoginService.verifyToken(selectedUser, lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser), UserTokenType.FORGOT_PASSWORD));
    }

    @Test    
    public void Given_userIssuesMultipleTokensSubsequently_then_TokenValuesAreDifferent() {
        smsLoginService.sendForgotPasswordSmsToken(
            TokenRequestDTO.builder()
                .ip("192.168.0.2")
                .userIdentifier(selectedUser.getFullMobile())
                .build()
        );
                
        String token1 = lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser);
        assertNotNull(token1);

        smsLoginService.sendForgotPasswordSmsToken(
            TokenRequestDTO.builder()
                .ip("192.168.0.3")
                .userIdentifier(selectedUser.getFullMobile())
                .build()        
        );
        
        assertNotNull(lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser));

        assertNotEquals(token1, lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser));
    }
    
    @Test    
    public void Given_userIssuesToken_then_oldTokenGetsRemoved() {
        TokenRequestDTO tokenRequest = TokenRequestDTO.builder()
            .ip("192.168.0.4")
            .userIdentifier(selectedUser.getFullMobile())
            .build();
        
        smsLoginService.sendForgotPasswordSmsToken(tokenRequest);
        
        assertNotNull(lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser));
        
        // Expire current token, otherwise we couldn't issue a new one
        Integer oldTokenId = userTokenRepository.getCurrentToken(selectedUser.getId(), UserTokenType.FORGOT_PASSWORD)
                .orElseThrow(() -> new JUnitException("Test failed"))
                .getId();
        
        // Force expire last request
        UserRequestEntity utr = userRequestRepository.getLastRequest(tokenRequest.getIp(), UserRequestType.FORGOT_PASSWORD_REQUEST.name()).orElseThrow();
        utr.setRequestedAt(LocalDateTime.now().minusMinutes(UserRequestService.FORGOT_PASSWORD_MINUTES_TO_WAIT + 1));
        userRequestRepository.save(utr);
        
        smsLoginService.sendForgotPasswordSmsToken(tokenRequest);
        
        assertNotNull(lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser));
        
        assertTrue(userTokenRepository.findById(oldTokenId).isEmpty(), "Old token was not removed");
    }
        
    @Test    
    public void Given_userIssuesToken_then_anotherTokenDoesNotVerify() {
        TokenRequestDTO tokenRequest = TokenRequestDTO.builder()
            .ip("192.168.0.5")
            .userIdentifier(selectedUser.getFullMobile())
            .build();

        smsLoginService.sendForgotPasswordSmsToken(tokenRequest);
        
        assertNotNull(lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser));
        
        String oldToken = lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser);
        
        userTokenRepository.getCurrentToken(selectedUser.getId(), UserTokenType.FORGOT_PASSWORD)
                .orElseThrow(() -> new JUnitException("Test failed"));

        // Force expire last request
        UserRequestEntity utr = userRequestRepository.getLastRequest(tokenRequest.getIp(), UserRequestType.FORGOT_PASSWORD_REQUEST.name()).orElseThrow();
        utr.setRequestedAt(LocalDateTime.now().minusMinutes(UserRequestService.FORGOT_PASSWORD_MINUTES_TO_WAIT + 1));
        userRequestRepository.save(utr);
        
        smsLoginService.sendForgotPasswordSmsToken(tokenRequest);
        
        assertNotNull(lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser));
        
        assertThrows(RuntimeException.class, () -> smsLoginService.verifyToken(selectedUser, oldToken, UserTokenType.FORGOT_PASSWORD));
    }
    
    @Test    
    public void Given_issuedTokenExpires_then_tokenDoesNotVerify() {
        TokenRequestDTO tokenRequest = TokenRequestDTO.builder()
            .ip("192.168.0.6")
            .userIdentifier(selectedUser.getFullMobile())
            .build();

        smsLoginService.sendForgotPasswordSmsToken(tokenRequest);
        
        assertNotNull(lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser));
        
        UserTokenEntity userToken = userTokenRepository.getCurrentToken(selectedUser.getId(), UserTokenType.FORGOT_PASSWORD)
                .orElseThrow(() -> new JUnitException("Test failed"));
        userToken.setExpiresOn(LocalDateTime.now().minusSeconds(ExtendedTokenService.PIN_CODE_EXPIRATION_SECONDS + 2));
        userTokenRepository.save(userToken);
        
        assertThrows(RuntimeException.class, () -> smsLoginService.verifyToken(selectedUser, tokenCaptor.getValue(), UserTokenType.FORGOT_PASSWORD));
    }
}
