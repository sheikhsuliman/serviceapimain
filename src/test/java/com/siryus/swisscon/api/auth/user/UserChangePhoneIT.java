package com.siryus.swisscon.api.auth.user;

import com.siryus.swisscon.api.auth.LemonTemplateUtil;
import com.siryus.swisscon.api.auth.sms.SmsService;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestHelper.TestProject;
import org.apache.cxf.common.util.StringUtils;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;

import org.springframework.boot.test.mock.mockito.MockBean;

public class UserChangePhoneIT extends AbstractMvcTestBase {

    private final UserRepository userRepository;    
    private final Integer NEW_COUNTRY_CODE = 44;
    private final String NEW_MOBILE_1 = "731361828";
    private final String NEW_MOBILE_2 = "731361829";
    private final String NEW_MOBILE_3 = "731361830";      

    private static TestProject testProject;    
    private static User testUser;  

    @SpyBean
    private SmsService smsService;    

    @Autowired
    private LemonTemplateUtil lemonTemplateUtil;     

    private final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);        

    @Autowired
    public UserChangePhoneIT(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @BeforeAll
    public void setup() {
        testProject = testHelper.createProject();                
        testUser = userRepository.findById(testProject.contractorCompany.workerId).get();
    }

    @Test
    public void Given_validUser_When_updatesPhoneChangeWithInvalidToken_Then_Success() {     
        captureToken();
                
        testHelper.sendChangeMobileSms(testProject.contractorCompany.asWorker, NEW_COUNTRY_CODE, NEW_MOBILE_1);

        String token = lemonTemplateUtil.extractChangePhoneToken(tokenCaptor.getValue(), testUser);

        assertTrue(token != null && !StringUtils.isEmpty(token), "Token should not be empty");
    }
    
    @Test
    public void Given_validUser_When_updatesPhoneChangeWithInvalidToken_Then_Fail() {
        captureToken();
        
        testHelper.sendChangeMobileSms(testProject.contractorCompany.asWorker, NEW_COUNTRY_CODE, NEW_MOBILE_1);

        String token = lemonTemplateUtil.extractChangePhoneToken(tokenCaptor.getValue(), testUser);

        testHelper.updateMobile(testProject.contractorCompany.asWorker, token + "1", r -> r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void Given_validUser_When_updatesPhoneChangeWithValidToken_Then_Success() {        
        captureToken();
        
        testHelper.sendChangeMobileSms(testProject.contractorCompany.asWorker, NEW_COUNTRY_CODE, NEW_MOBILE_2);

        String token = lemonTemplateUtil.extractChangePhoneToken(tokenCaptor.getValue(), testUser);

        testHelper.updateMobile(testProject.contractorCompany.asWorker, token);

        User u = userRepository.findById(testProject.contractorCompany.workerId).get();
        assertTrue(u.getMobile().equals(NEW_MOBILE_2), "Mobile number was not updated");
        assertTrue(u.getMobileCountryCode().equals(NEW_COUNTRY_CODE), "Country code was not updated");
    }

    @Test
    public void Given_oneUserRequestsPhoneChangeToken_When_anotherUserAttemptsToUseTokenToChangePhone_Then_Fail() {
        captureToken();
        
        User worker = userRepository.findById(testProject.contractorCompany.workerId).get();

        testHelper.sendChangeMobileSms(testProject.contractorCompany.asWorker, NEW_COUNTRY_CODE, NEW_MOBILE_3);

        String token = lemonTemplateUtil.extractChangePhoneToken(tokenCaptor.getValue(), testUser);

        testHelper.updateMobile(testProject.contractorCompany.asOwner, token, r -> r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value()));
    }    
    
    private void captureToken() {
        doNothing().when(smsService).sendSmsMessage(tokenCaptor.capture(), anyString());
    }    
}
