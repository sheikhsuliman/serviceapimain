/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.auth.user;

import com.naturalprogrammer.spring.lemon.commons.domain.ResetPasswordForm;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.auth.usertoken.UserTokenType;
import com.siryus.swisscon.api.base.TestHelper.ExtendedTestProject;
import com.siryus.swisscon.api.general.langcode.LangCode;
import com.siryus.swisscon.api.general.langcode.LangCodeService;

import static io.restassured.RestAssured.given;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * Groups together all tests around the User class
 *
 * @author Horatiu
 */
public class UserIT extends AbstractMvcTestBase {
    ExtendedTestProject testProject;
    
    private final UserService userService;
    private final LangCodeService langCodeService;
    private final String newPass = "ABC123123";
    private String newLanguageId;
    private final String invalidLanguageId = "-1";
    
    @BeforeAll
    public void setup() {
        testProject = testHelper.createExtendedProject();
        
        String oldLanguageId = userService.findById(testProject.contractorCompany.workerId)
            .getPrefLang()
            .getId();
        
        newLanguageId = langCodeService.findAll()
            .stream()
            .filter(language -> !language.getId().equals(oldLanguageId))
            .findAny()
            .get().getId();
    }
    
    @Autowired
    public UserIT(UserService userService, LangCodeService langCodeService) {
        this.userService = userService;
        this.langCodeService = langCodeService;
    }

    @Test
    void testDynamicUserToken() {
        testHelper.init(testProject.contractorCompany.asWorker);
    }

    @Test
    void Given_validWorkerUser_When_UserChangesLanguageWithValidLanguageId_Then_Success() {               
        testHelper.changeLanguage(testProject.contractorCompany.asWorker, newLanguageId);
        
        User worker = userService.findById(testProject.contractorCompany.workerId);        

        assertEquals(worker.getPrefLang().getId(), newLanguageId, "Language code was not updated");
    }

    @Test
    void Given_validWorkerUser_When_UserChangesLanguageWithInValidLanguageId_Then_Fail() {
        testHelper.changeLanguage(testProject.contractorCompany.asWorker, invalidLanguageId, r -> r.assertThat().statusCode(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void testResetPassword() throws Exception {
        ResetPasswordForm resetPasswordForm = new ResetPasswordForm();
        resetPasswordForm.setCode(extendedTokenService.issueToken(testProject.contractorCompany.workerId, UserTokenType.FORGOT_PASSWORD));
        resetPasswordForm.setNewPassword(newPass);

        // check if reset password was successfully executed
        testHelper.resetPassword(testProject.contractorCompany.asWorker, resetPasswordForm);

        // check if the new password works during login
        testHelper.login(testHelper.PROJECT_WORKER_EMAIL, newPass);
    }

    @Test
    void testChangeAccountDetailsNotAuthenticated() {       
        testHelper.changeLanguage(defaultSpec(), newLanguageId, r -> r.assertThat().statusCode(HttpStatus.UNAUTHORIZED.value()));
    }
}
