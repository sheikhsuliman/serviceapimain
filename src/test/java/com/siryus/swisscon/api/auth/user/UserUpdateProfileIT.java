package com.siryus.swisscon.api.auth.user;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.init.InitDTO;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_ADMIN_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_ADMIN_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_ADMIN_LAST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_LAST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_WORKER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_WORKER_LAST_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserUpdateProfileIT extends AbstractMvcTestBase {

    private RequestSpecification asCompanyOwner;
    private RequestSpecification asCompanyAdmin;
    private RequestSpecification asCompanyWorker;


    @BeforeAll
    void initTest() {
        testHelper.signUp(TestBuilder.testSignupDTO(COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME));
        asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);
        TeamUserDTO adminTeamUserDTO = testHelper.inviteUserAndResetPassword(
                asCompanyOwner,
                TestBuilder.testTeamUserAddDTO(COMPANY_NAME, PROJECT_ADMIN_FIRST_NAME, PROJECT_ADMIN_LAST_NAME)
        );
        testHelper.toggleAdmin(asCompanyOwner, adminTeamUserDTO.getId(), true);
        asCompanyAdmin = testHelper.login(PROJECT_ADMIN_EMAIL);

        testHelper.inviteUserAndResetPassword(
                asCompanyOwner,
                TestBuilder.testTeamUserAddDTO(COMPANY_NAME, PROJECT_WORKER_FIRST_NAME, PROJECT_WORKER_LAST_NAME)
        );
        asCompanyWorker = testHelper
                .login(TestHelper.companyEmail(COMPANY_NAME, PROJECT_WORKER_FIRST_NAME, PROJECT_WORKER_LAST_NAME));
    }

    @Test
    void Given_ValidUpdateRequest_When_updateProfile_Then_ProfileIsUpdated() {
        File newProfileImage = testHelper.fileUploadTemporary(asCompanyOwner);

        UserProfileDTO userProfileDTO = TestBuilder.testUserProfileDTO(dto -> dto.toBuilder().profileImageId(newProfileImage.getId()).build());

        testHelper.updateUserProfile(asCompanyOwner, userProfileDTO);

        InitDTO initDTO = testHelper.init(asCompanyOwner);

        assertEquals(userProfileDTO.getProfileImageId(), initDTO.getUser().getPicture().getId());
        assertEquals(userProfileDTO.getTitle(), initDTO.getUser().getTitle());
        assertEquals(userProfileDTO.getAboutMe(), initDTO.getUser().getAboutMe());
        assertEquals(userProfileDTO.getDriversLicense(), initDTO.getUser().getDriversLicense());
        assertEquals(userProfileDTO.getCar(), initDTO.getUser().getCar());
        assertEquals(userProfileDTO.getLicensePlate(), initDTO.getUser().getLicensePlate());
        assertEquals(userProfileDTO.getResponsibility(), initDTO.getUser().getResponsibility());
        assertEquals(userProfileDTO.getGenderId(), initDTO.getUser().getGender().getId());
        assertEquals(userProfileDTO.getFirstName(), initDTO.getUser().getFirstName());
        assertEquals(userProfileDTO.getLastName(), initDTO.getUser().getLastName());
        assertEquals(userProfileDTO.getBirthDate(), initDTO.getUser().getBirthDate());
        assertEquals(userProfileDTO.getSsn(), initDTO.getUser().getSsn());
        assertEquals(userProfileDTO.getCountryOfResidenceId(), initDTO.getUser().getCountryOfResidence());
        assertEquals(userProfileDTO.getNationalityId(), initDTO.getUser().getNationality());
    }

    @Test
    void Given_UnauthenticatedRequest_When_updateProfile_Then_Throw() {
        testHelper.updateUserProfile(defaultSpec(), TestBuilder.testUserProfileDTO(), r -> {
            r.assertThat().statusCode(HttpStatus.UNAUTHORIZED.value());
            return null;
        });
    }

    @Test
    void Given_NotExistingEntities_When_updateProfile_Then_Throw() {
        updateExpectBadRequest(TestBuilder.testUserProfileDTO(dto -> dto.toBuilder().profileImageId(-15).build()));
    }

    @Test
    void Given_NotExistingGenderID_When_updateProfile_Then_Throw() {
        updateExpectBadRequest(TestBuilder.testUserProfileDTO(dto -> dto.toBuilder().genderId(-15).build()));
    }

    @Test
    void Given_NotExistingResidenceId_When_updateProfile_Then_Throw() {
        updateExpectBadRequest(TestBuilder.testUserProfileDTO(dto -> dto.toBuilder().countryOfResidenceId(-15).build()));
    }

    @Test
    void Given_NotExistingNationalityId_When_updateProfile_Then_Throw() {
        updateExpectBadRequest(TestBuilder.testUserProfileDTO(dto -> dto.toBuilder().nationalityId(-15).build()));
    }

    @Test
    void Given_LoggedInAsWorker_When_updateProfile_Then_Throw() {
        testHelper.updateUserProfile(asCompanyWorker, TestBuilder.testUserProfileDTO(),
                r -> {
                    r.assertThat().statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    @Test
    void Given_LoggedInAsAdmin_When_updateProfile_Then_Success() {
        testHelper.updateUserProfile(asCompanyAdmin, TestBuilder.testUserProfileDTO());
    }

    private void updateExpectBadRequest(UserProfileDTO dto) {
        testHelper.updateUserProfile(asCompanyOwner,
                dto,
                r -> {
                    r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
                    return null;
                });
    }


}
