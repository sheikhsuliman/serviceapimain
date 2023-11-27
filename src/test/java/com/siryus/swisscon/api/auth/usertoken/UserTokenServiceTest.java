package com.siryus.swisscon.api.auth.usertoken;

import com.siryus.swisscon.api.auth.LemonTemplateUtil;
import com.siryus.swisscon.api.auth.sms.ExtendedTokenService;
import com.siryus.swisscon.api.auth.sms.SmsService;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

public class UserTokenServiceTest extends AbstractMvcTestBase{

    @Autowired
    UserTokenService userTokenService;
    
    @Autowired
    UserTokenRepository userTokenRepository;    

    @Autowired
    ExtendedTokenService smsLoginService;

    @Autowired
    LemonTemplateUtil lemonTemplateUtil;
    
    @SpyBean
    SmsService smsService;    
    
    private final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);    
    
    private User selectedUser;    
    
    @BeforeAll
    public void doBeforeAll() {
        List<User> usersWithPhone = userRepository.findAll()
                .stream()
                .filter(u -> u.getMobile() != null && !u.getMobile().isEmpty())
                .collect(Collectors.toList());
        
        selectedUser = usersWithPhone.get(RandomUtils.nextInt(0, usersWithPhone.size()));        
    }

    @BeforeEach
    public void captureToken() {
        doNothing().when(smsService).sendSmsMessage(tokenCaptor.capture(), anyString());
    }
    
    @Test
    public void Given_issuedToken_tokenIsValid() {
        smsLoginService.sendForgotPasswordSmsToken(
            TokenRequestDTO.builder()
                .ip("192.168.0.1")
                .userIdentifier(selectedUser.getFullMobile())
                .build()
        );
        
        String issuedToken = lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser);
        
        assertTrue(userTokenService.isTokenValid(selectedUser.getId(), UserTokenType.FORGOT_PASSWORD, issuedToken));

        assertFalse(userTokenService.isTokenValid(selectedUser.getId(), UserTokenType.FORGOT_PASSWORD, issuedToken + "a"));        
    }
    
    @Test
    public void Given_tokenIsInvalidated_tokenIsNoLongerValid() {
        smsLoginService.sendForgotPasswordSmsToken(
            TokenRequestDTO.builder()
                .ip("192.168.0.2")
                .userIdentifier(selectedUser.getFullMobile())
                .build()        
        );
        
        String issuedToken = lemonTemplateUtil.extractSmsToken(tokenCaptor.getValue(), selectedUser);  
        
        assertTrue(userTokenService.isTokenValid(selectedUser.getId(), UserTokenType.FORGOT_PASSWORD, issuedToken));

        userTokenService.invalidateCurrentToken(selectedUser.getId(), UserTokenType.FORGOT_PASSWORD);
        
        assertFalse(userTokenService.isTokenValid(selectedUser.getId(), UserTokenType.FORGOT_PASSWORD, issuedToken));
    }
    
}
