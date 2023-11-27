package com.siryus.swisscon.api.auth.sms;

import com.naturalprogrammer.spring.lemon.commons.domain.ResetPasswordForm;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomUtils;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import com.siryus.swisscon.api.auth.LemonTemplateUtil;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@TestMethodOrder(OrderAnnotation.class)
public class SmsLoginIT extends AbstractMvcTestBase {

    @SpyBean
    private SmsService smsService;

    private final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
   
    @Autowired
    private UserRepository userRepository;    
    
    @Autowired
    private LemonTemplateUtil lemonTemplateUtil;    
    
    private User selectedUser;
    
    @BeforeAll
    public void baseSetUp() {
        super.baseSetUp();        

        MockitoAnnotations.initMocks(this);               
        
        doNothing().when(smsService).sendSmsMessage(tokenCaptor.capture(), anyString());     
        
        List<User> usersWithPhone = userRepository.findAll()
                .stream()
                .filter(u -> u.getMobile() != null && !u.getMobile().isEmpty())
                .collect(Collectors.toList());
        
        selectedUser = usersWithPhone.get(RandomUtils.nextInt(0, usersWithPhone.size()));            
    }    
    
    @Test
    public void Given_invalidPhoneNumberIsProvided_tokenIsNotIssued() {
        testHelper.sendForgotPasswordSms("0000000", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(1)    
    public void Given_validPhoneNumberIsProvided_passwordCanBeChangedOnce() {
        String password = "newPassword";        

        testHelper.sendForgotPasswordSms(selectedUser.getFullMobile(), HttpStatus.OK);

        assertNotNull(tokenCaptor.getValue());

        String tokenValue = tokenCaptor.getValue().replace(lemonTemplateUtil.getForgetPasswordSmsContent("", selectedUser), "");        

        ResetPasswordForm form = new ResetPasswordForm();
        form.setNewPassword(password);
        form.setCode(tokenValue);

        testHelper.resetPassword(defaultSpec(), form);

        testHelper.login(selectedUser.getEmail(), password);

        testHelper.resetPassword(defaultSpec(), form, r -> r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value()));
    }
    
    @Test
    @Order(2)    
    public void Given_multipleRequests_tokenIsNotIssued() {
        // A second immediate request for a code can not happen from the same IP before a specified number of minutes have passed
        testHelper.sendForgotPasswordSms(selectedUser.getMobile(), HttpStatus.BAD_REQUEST);
    }
}
