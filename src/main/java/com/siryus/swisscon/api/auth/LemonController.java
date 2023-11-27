package com.siryus.swisscon.api.auth;


import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.signup.SignupDTO;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.sms.ExtendedTokenService;
import com.siryus.swisscon.api.auth.sms.MobileDTO;
import com.siryus.swisscon.api.auth.user.TeamUserAddDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserAccountDTO;
import com.siryus.swisscon.api.auth.user.UserProfileDTO;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.auth.usertoken.TokenRequestDTO;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.exceptions.BadRequestException;
import com.siryus.swisscon.api.exceptions.NotFoundException;
import com.siryus.swisscon.api.general.langcode.LangCode;
import com.siryus.swisscon.api.general.langcode.LangCodeService;
import com.siryus.swisscon.api.util.CookieService;
import com.siryus.swisscon.api.util.validator.DTOValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@Api(
		tags = "Auth",
		produces = "application/json"
)
@RequestMapping("/api/rest/auth")
public class LemonController extends com.naturalprogrammer.spring.lemon.LemonController<User, Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LemonController.class);

    private static final String[] IP_HEADERS = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR" };    
    
    private final LemonService lemonService;
    private final UserService userService;
    private final LangCodeService langCodeService;
    private final CookieService cookieService;
    private final ExtendedTokenService smsLoginService;
    
    @Autowired
    public LemonController(LemonService lemonService, UserService userService, LangCodeService langCodeService, CookieService cookieService, ExtendedTokenService smsLoginService) {
        this.lemonService = lemonService;
        this.userService = userService;
        this.langCodeService = langCodeService;
        this.cookieService = cookieService;
        this.smsLoginService = smsLoginService;        
    }

    @PostMapping(path = "logout")
    @ApiOperation(value = "Logs out a user", notes = "Returns a Set-Cookie header which forces the browser to remove the cookie")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        // If no cookies are present, there's nothing to reset
        if (cookies == null || cookies.length == 0) {
            LOGGER.error("No cookies present");
            return;
        }

        // Find cookies which has a token in it
        List<Cookie> authCookies = new ArrayList<>();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(cookieService.getCookieName())) {
                authCookies.add(cookie);
            }
        }

        // If login cookies are not present, there's nothing to reset
        if (authCookies.size() == 0) {
            LOGGER.error("No cookies present");
            return;
        }

        for(Cookie c : authCookies) {
            LOGGER.info("Setting cookies");
            Cookie newCookie = lemonService.createAuthCookie(c.getValue(), 0L);
            newCookie.setMaxAge(0);
            response.addCookie(newCookie);
        }
    }

    @Deprecated //TODO remove after SI-177
    @PostMapping(path = "signup", produces = "application/json")
    @ApiOperation(value = "Signup a new user", notes = "No confirmation of the new email is done")
    public SignupResponseDTO signupDeprecated(@RequestBody SignupDTO signupDTO) {
        String errorMessage = DTOValidator.validate(signupDTO);
        if (errorMessage != null) {
            throw new BadRequestException(errorMessage);
        }
        return lemonService.signupDeprecated(signupDTO);
    }

    @PostMapping(path = "signup-from-invite", produces = "application/json")
    @ApiOperation(value = "Signup a company and user", notes = "No confirmation of the new email is done")
    public SignupResponseDTO signup(@RequestBody SignupDTO signupDTO) {
        return lemonService.signup(signupDTO);
    }

    @Deprecated //TODO remove after SI-177
    @PostMapping(path = "verify-signup-code")
    @ApiOperation(value = "Verify the code and email which was sent for the 'company invitation'",
    notes="The codes comes from the company invitation by email")
    public void verifySignupCodeDeprecated(@RequestParam() String emailOrPhone, @RequestParam() String code) {
        lemonService.verifiySignupCodeDeprecated(emailOrPhone, code);
    }

    @PostMapping(path = "verify-signup")
    @ApiOperation(value = "Verify the code and companyId which was sent for the 'company invitation'",
            notes="The codes come with the company invitation by email")
    public SignupDTO verifySignup(@RequestParam() Integer company, @RequestParam() String code) {
        return lemonService.verifySignupAndGetSavedCompany(company, code);
    }

    //TODO refactor this in SIR-1300
    //TODO change phone number should have it's own endpoint
    //TODO in the DTO should be the country code and mobile number separated
    @PostMapping(path="/users/account", produces="application/json")
    @ApiOperation(value = "Update a user's account information", notes = "No confirmation of the new email is done")
    public void updateUserAccount(@RequestBody UserAccountDTO accountInfo, HttpServletResponse response) {

        // Check whether the user making the request to update the profile owns it
        UserDto currentUser = LecwUtils.currentUser();
        if (null == currentUser) {
            throw new NotFoundException("Current user can not be resolved");
        }

        User u = userService.findById(new Integer(currentUser.getId()));
        if (null == u) {
            throw new NotFoundException("User with id " + currentUser.getId() + " was not found");
        }

        LangCode lc = langCodeService.findById(accountInfo.getLanguageId());
        if (null == lc) {
            throw new NotFoundException("Language code with id " + accountInfo.getLanguageId() + " was not found");
        }

        u.setPrefLang(lc);
        userService.update(u);
    }

    @PostMapping(path="/users/phone")
    @ApiOperation(value = "Confirms new number and updates a user's phone number")
    public void updateUserPhone(@RequestParam() String confirmationCode) {
        lemonService.updatePhoneViaToken(confirmationCode);
    }       

    /**
     * Updates non sensitive user data (profile information)
     *
     * Description of POST data can be found here:
     *      https://denkteam.atlassian.net/wiki/spaces/SIR/pages/8945672/User-profile+page
     *
     * @param profileInfo user profile information
     */
    @PostMapping(path="/users/profile", produces="application/json")
    @ApiOperation(value = "Update a user's profile information", notes = "This is only for non-sensitive information")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_PROFILES_EDIT')")
    public UserProfileDTO updateUserProfile(@RequestBody UserProfileDTO profileInfo) {
        return userService.updateUserProfile(profileInfo);
    }

    @GetMapping(path="/users/{userId}/profile", produces="application/json")
    @ApiOperation(value = "Retrieve other user profile", notes = "This is only for non-sensitive information")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_PROFILES_VIEW')")
    public UserProfileDTO getOtherUserProfile(@PathVariable Integer userId) {
        return userService.getOtherUserProfile(userId);
    }

    @PostMapping(path="/users/{userId}/profile", produces="application/json")
    @ApiOperation(value = "Update other user profile information", notes = "This is only for non-sensitive information")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_PROFILES_EDIT')")
    public UserProfileDTO updateOtherUserProfile(@PathVariable Integer userId, @RequestBody UserProfileDTO profileInfo) {
        return userService.updateOtherUserProfile(userId, profileInfo);
    }

    /**
     * Creates a new user with the given input data
     * Sends out an invitation mail with an token which can expire
     * In case the token is expired > the REST Endpoint
     * {@link #resendInvitationMail(Integer)} (Integer)}
     * should be invoked
     *
     * returns added user
     */
    @PostMapping("/invite")
    @ApiOperation(value = "Invite a new user to a company", notes = "Creates a user and send him an email, where he has to reset his password")
    public TeamUserDTO invite(@RequestBody TeamUserAddDTO teamUserAddDTO) {
        return lemonService.inviteUser(teamUserAddDTO);
    }

    /**
     * Sends an invitation mail again
     */
    @Deprecated
    @PostMapping("/users/{id}/resend-invitation-mail")
    @ApiOperation(value = "Resend the invitation mail", notes = "Resend the invitation mail, because it could the user deleted the mail or the token was expired")
    public void resendInvitationMail(@ApiParam(name = "id", required = true) @PathVariable Integer id) {

        if(userService.findById(id) == null) {
            throw new NotFoundException("User with id " + id + " doesn't exist");
        }

        lemonService.resendInvitation(id);
    }

    @PostMapping("/users/{id}/resend-invitation")
    @ApiOperation(value = "Resend the invitation mail or sms. Depends how to the user was invited", notes = "Resend the invitation mail/sms, because it could the user deleted the mail or the token was expired")
    public void resendInvitation(@ApiParam(name = "id", required = true) @PathVariable Integer id) {
        lemonService.resendInvitation(id);
    }

    /**
     * The changeEmail Endpoint has to be overridden because in the original service method
     * there was the annotation that the user has to be authorized.
     * But for changing the mail you don't have to be authorized, cause you already got the code via your email
     */
    @Override
    @PostMapping({"/users/{userId}/email"})
    public UserDto changeEmail(@PathVariable Integer userId, @RequestParam String code, HttpServletResponse response) {
        LOGGER.debug("Changing email of user ...");
        this.lemonService.changeEmailUnauthorized(userId, code);
        return this.userWithToken(response);
    }

    /**
     * Deletes the user from the DB if he isn't assigned to a project. Otherwise he will be disabled
     *
     * @param id the user id
     * @return HttpStatus 200
     */
    @PostMapping("/users/{id}/toggle-admin")
    @ApiOperation(value = "Give or remove company admin rights to a user")
    public TeamUserDTO toggleAdmin(@ApiParam(name = "id", required = true) @PathVariable Integer id,
                                                   @RequestParam("make-admin") Boolean makeAdmin) {

        // TODO permission system
        // TODO check if the current logged in user has permissions to toggle

        if(userService.findById(id) == null) {
            LOGGER.error("User with id {}} doesn't exist", id);
            throw new NotFoundException("User with id " + id + " doesn't exist");
        }

        CompanyUserRole savedCompanyUserRole = userService.toggleAdmin(id, makeAdmin);

        return TeamUserDTO.fromCompanyUserRole(savedCompanyUserRole);
    }
    
    @PostMapping("sms/send-forgot-password")
    @ApiOperation(value = "Generates a SMS token for forgot password workflow")       
    public void forgotPassword(@RequestBody MobileDTO mobile, HttpServletRequest request) {
        String errorMessage = DTOValidator.validate(mobile);
        if (errorMessage != null) {
            throw new BadRequestException(errorMessage);
        }
                
        String ip = getClientIpAddress(request);
        if (ip == null || ip.isEmpty()) {
            throw new BadRequestException("Invalid request");
        }
        
        smsLoginService.sendForgotPasswordSmsToken(
            TokenRequestDTO.builder()
                .ip(ip)
                .userIdentifier(mobile.getMobile())
                .build()
        );
    }   
    
    @PostMapping(path="/sms/send-change-phone")
    @ApiOperation(value = "Sends a SMS token on the new phone number")
    public void requestChangePhoneNumber(@RequestBody MobileDTO mobile, HttpServletRequest request) {    
        DTOValidator.validateAndThrow(mobile);

        smsLoginService.sendChangePhoneSmsToken(mobile.getMobile(), mobile.getCountryCode());
    }
    
    /**
     * Fetches a user by email
     */
    @PostMapping("/users/fetch-by-email")
    @Override
    public User fetchUserByEmail(@RequestParam String email) {

        return null;
    }

    /**
     * Fetches a user by ID
     */
    @GetMapping("/users/{id}")
    @Override
    public User fetchUserById(@PathVariable("id") User user) {
        return null;
    }


    /**
     * Fetch a new token - for session sliding, switch user etc.
     */
    @PostMapping("/fetch-new-auth-token")
    @Override
    public Map<String, String> fetchNewToken(@RequestParam Optional<Long> expirationMillis,
			@RequestParam Optional<String> username,
			HttpServletResponse response) {
        return null;
    }

    /**
     * Fetch a self-sufficient token with embedded UserDto - for interservice communications
     */
    @GetMapping("/fetch-full-token")
    @Override
    public Map<String, String> fetchFullToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }    
        
        return request.getRemoteAddr();
    }
}
