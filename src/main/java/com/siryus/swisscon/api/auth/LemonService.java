package com.siryus.swisscon.api.auth;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemon.commons.domain.ResetPasswordForm;
import com.naturalprogrammer.spring.lemon.commons.mail.LemonMailData;
import com.naturalprogrammer.spring.lemon.commons.mail.MailSender;
import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.GreenTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commonsjpa.LecjUtils;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleService;
import com.siryus.swisscon.api.auth.signup.SignupDTO;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.sms.ExtendedTokenService;
import com.siryus.swisscon.api.auth.sms.SmsService;
import com.siryus.swisscon.api.auth.user.CompanyInviteDTO;
import com.siryus.swisscon.api.auth.user.TeamUserAddDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserProfileDTO;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.auth.usertoken.SimpleUserTokenService;
import com.siryus.swisscon.api.auth.usertoken.UserTokenEntity;
import com.siryus.swisscon.api.auth.usertoken.UserTokenService;
import com.siryus.swisscon.api.auth.usertoken.UserTokenType;
import com.siryus.swisscon.api.auth.validator.PasswordValidator;
import com.siryus.swisscon.api.base.EntityUtil;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companytrade.CompanyTrade;
import com.siryus.swisscon.api.company.companytrade.CompanyTradeRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import com.siryus.swisscon.api.exceptions.BadRequestException;
import com.siryus.swisscon.api.exceptions.NotFoundException;
import com.siryus.swisscon.api.general.langcode.LangCode;
import com.siryus.swisscon.api.general.langcode.LangCodeService;
import com.siryus.swisscon.api.util.CookieService;
import com.siryus.swisscon.api.util.EmailPhoneUtils;
import com.siryus.swisscon.api.util.TranslationUtil;
import com.siryus.swisscon.api.util.ValidationUtils;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.DTOValidator;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@Validated
public class LemonService extends com.naturalprogrammer.spring.lemon.LemonService<User, Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LemonService.class);
    
    public static final String USER_ID_CLAIM = "userId";
    public static final String LEMON_RABBIT_AUDIENCE = "rabbit";

    @Value("${lemon.jwt.signup-link.expiration-millis}")
    private Long signupLinkExpirationMillis;

    @Value("${lemon.default-signup-token}")
    private String defaultSignupToken;

    private final SimpleUserTokenService simpleUserTokenService;
    private final ExtendedTokenService extendedTokenService;
    private final SmsService smsService;
    private final CookieService cookieService;
    private final MailSender mailSender;
    private final GreenTokenService greenTokenService;
    private final CompanyUserRoleService companyUserRoleService;
    private final LangCodeService langCodeService;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final LemonTemplateUtil templateUtil;
    private final EmailLinkUtil emailLinkUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TranslationUtil translationUtil;
    private final CompanyTradeRepository companyTradeRepository;
    private final CompanyRepository companyRepository;
    private final UserTokenService userTokenService;
    private final RoleService roleService;
    private final LemonProperties lemonProperties;
    private final EventsEmitter eventsEmitter;
    private final SecurityHelper securityHelper;

    @Autowired
    public LemonService(
            SimpleUserTokenService simpleUserTokenService,
            ExtendedTokenService extendedTokenService,
            SmsService smsService,
            CookieService cookieService,
            MailSender mailSender,
            GreenTokenService greenTokenService,
            CompanyUserRoleService companyUserRoleService,
            LangCodeService langCodeService,
            CompanyUserRoleRepository companyUserRoleRepository,
            LemonTemplateUtil templateUtil,
            EmailLinkUtil emailLinkUtil,
            UserService userService,
            UserRepository userRepository,
            TranslationUtil translationUtil,
            CompanyTradeRepository companyTradeRepository,
            CompanyRepository companyRepository,
            UserTokenService userTokenService,
            RoleService roleService,
            LemonProperties lemonProperties,
            EventsEmitter eventsEmitter, SecurityHelper securityHelper) {
        this.simpleUserTokenService = simpleUserTokenService;
        this.extendedTokenService = extendedTokenService;
        this.smsService = smsService;
        this.cookieService = cookieService;
        this.mailSender = mailSender;
        this.greenTokenService = greenTokenService;
        this.companyUserRoleService = companyUserRoleService;
        this.langCodeService = langCodeService;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.templateUtil = templateUtil;
        this.emailLinkUtil = emailLinkUtil;
        this.userService = userService;
        this.userRepository = userRepository;
        this.translationUtil = translationUtil;
        this.companyTradeRepository = companyTradeRepository;
        this.companyRepository = companyRepository;
        this.userTokenService = userTokenService;
        this.roleService = roleService;
        this.lemonProperties = lemonProperties;
        this.eventsEmitter = eventsEmitter;
        this.securityHelper = securityHelper;
    }

    @Override
    public User newUser() {
        return new User();
    }

    @Override
    public Integer toId(String id) {
        return Integer.parseInt(id);
    }

    /**
     * Signs up a user.
     */
    @Override
    public void signup(@Valid User user) {
        LangCode lang = user.getPrefLang();
        if (Objects.nonNull(lang) && Objects.nonNull(lang.getId())) {
            lang = langCodeService.findById(lang.getId());
        } else {
            lang = langCodeService.findById(AuthConstants.DEFAULT_LANGUAGE);
        }
        user.setPrefLang(lang);
        super.signup(user);
    }

    @Deprecated //TODO remove after SI-177
    public void verifiySignupCodeDeprecated(String emailOrPhone, String code) {
        if (signupCodeNeedsToBeChecked(code) &&
                !extendedTokenService
                        .companyInvitationTokenIsValid(EmailPhoneUtils
                                .toMailOrPhoneNumber(emailOrPhone), code)) {
            throw AuthException.tokenIsNotValidOrExpired(code);
        }
    }

    public void verifySignup(Integer companyId, String code) {
        if (signupCodeNeedsToBeChecked(code) && !userTokenService.isTokenValid(String.valueOf(companyId),
                UserTokenType.INVITE_COMPANY, code)) {
            throw AuthException.tokenIsNotValidOrExpired(code);
        }
    }

    public SignupDTO verifySignupAndGetSavedCompany(Integer companyId, String code) {
        verifySignup(companyId, code);
        return SignupDTO.from(companyUserRoleService.getCompanyOwner(companyId), code);
    }

    /**
     * If we work on local environment > the defaultSignupToken is set and we can avoid checking the signup token
     * by using a default code defined in the properties
     * Also if no users exists in the Database > we don't have to check the signup token
     */
    private boolean signupCodeNeedsToBeChecked(String code) {
        boolean isNotLocalOrIsNotDefaultToken = AuthConstants.DISABLED.equals(defaultSignupToken) || !code.startsWith(defaultSignupToken);
        boolean activeUsersExist = userRepository.existsAnyActiveUsers();
        return isNotLocalOrIsNotDefaultToken  && activeUsersExist;
    }

    @Transactional
    public TeamUserDTO inviteUser(TeamUserAddDTO teamUserAddDTO) {
        String errorMessage = DTOValidator.validate(teamUserAddDTO);
        if (errorMessage != null) {
            throw new BadRequestException(errorMessage);
        }
        Role role = findAndValidateInvitedRoleId(
                teamUserAddDTO.getRoleId(),
                roleService.findCompanyMemberDefaultRole(), true);

        String emailOrPhone = teamUserAddDTO.getEmailOrPhone();
        Integer countryCode = teamUserAddDTO.getCountryCode();

        validateEmailOrPhoneAlreadyExists(emailOrPhone, countryCode);

        User user = User.from(teamUserAddDTO);
        UserDto invitingUserDTO = LecwUtils.currentUser();
        Integer invitingUserId = Integer.valueOf(invitingUserDTO.getId());
        User invitingUser = userRepository.getOne(invitingUserId);

        // set random password cause its a mandatory field
        user.setPassword(passwordEncoder.encode(RandomStringUtils.random(64, true, true)));

        user.setPrefLang(invitingUser.getPrefLang());
        user.getRoles().add(AuthConstants.ROLE_UNVERIFIED);
        User savedInvitedUser = userRepository.save(user);

        CompanyUserRole companyUserRole = companyUserRoleService
                .linkUserToCurrentUserCompany(invitingUserId, savedInvitedUser, role.getId());

        String formattedEmailOrPhone = EmailPhoneUtils.toMailOrPhoneNumber(teamUserAddDTO.getEmailOrPhone(),
                teamUserAddDTO.getCountryCode());

        createTokenAndSendInvitation(savedInvitedUser, invitingUserDTO, companyUserRole, formattedEmailOrPhone);

        eventsEmitter.emitNotification(NotificationEvent.fromCompany(NotificationType.COMPANY_USER_ADDED,
                companyUserRole.getCompany().getId(),
                securityHelper.currentUserId(), companyUserRole.getCompany().getId(), savedInvitedUser.getId()));

        return TeamUserDTO.fromCompanyUserRole(companyUserRole);
    }

    private Role findAndValidateInvitedRoleId(Integer roleId, Role defaultRole, boolean isInvitedUser) {
        if (roleId != null) {
            Role role = roleService.findById(roleId);
            boolean isUniqueAndMandatory = RoleName.isUniqAndMandatory(role.getName());
            ValidationUtils.throwIf(role.isProjectRole(),
                    () -> AuthException.roleForCompanyUserCannotBeProjectRole(role.getName()));
            ValidationUtils.throwIf(isInvitedUser && isUniqueAndMandatory,
                    () -> AuthException.roleForCompanyCannotBeUniqueAndMandatory(role.getName()));
            ValidationUtils.throwIf(!isInvitedUser && !isUniqueAndMandatory,
                    () -> AuthException.roleForCompanyHasToBeUniqueAndMandatory(role.getName()));
            ValidationUtils.throwIf(RoleName.COMPANY_BOOTSTRAP.name().equals(role.getName()),
                    () -> AuthException.invitedUsersCannotHaveBootstrapRole(role.getName()));
            return role;
        }
        return defaultRole;
    }

    private void validateIsEmailOrPhone(String emailOrPhone, Integer countryCode) {
        if (!EmailPhoneUtils.isEmail(emailOrPhone) &&
                !EmailPhoneUtils.isPhone(countryCode, emailOrPhone)) {
            throw AuthException.emailOrPhoneNotCorrectlyFormatted(emailOrPhone, countryCode);
        }
    }

    private void validateEmailOrPhoneAlreadyExists(String emailOrPhone, Integer countryCode) {
        if (EmailPhoneUtils.isEmail(emailOrPhone)) {
            validateEmailAlreadyExists(emailOrPhone);
        } else if (EmailPhoneUtils.isPhone(countryCode, emailOrPhone)) {
            validateMobileAlreadyExists(countryCode, emailOrPhone);
        } else {
            throw AuthException.emailOrPhoneNotCorrectlyFormatted(emailOrPhone, countryCode);
        }
    }

    private void validateEmailAlreadyExists(String email) {
        if(userService.userWithEmailExists(email)) {
            throw AuthException.emailOrPhoneAlreadyExist(email);
        }
    }

    private void validateMobileAlreadyExists(Integer countryCode, String mobile) {
        if (userService.userWithMobileAlreadyExists(countryCode, EmailPhoneUtils.toPhoneNumber(mobile))) {
            throw AuthException.emailOrPhoneAlreadyExist(EmailPhoneUtils
                    .toFullPhoneNumber(countryCode, mobile));
        }
    }
    
    @Transactional
    public void updatePhoneViaToken(String token) {
        User u = userRepository.findById(Integer.parseInt(LecwUtils.currentUser().getId())).get();

        extendedTokenService.verifyToken(u, token, UserTokenType.CHANGE_PHONE);

        UserTokenEntity ute = userTokenService.findToken(token);

        if (ute.getExternalId() == null || StringUtils.isBlank(ute.getExternalId())) {
            throw AuthException.tokenIsNotValidOrExpired(token);
        }

        Integer countryCode = EmailPhoneUtils.countryCodeFromFullPhoneNumber(ute.getExternalId());
        String mobile = EmailPhoneUtils.mobileFromFullPhoneNumber(ute.getExternalId());

        userTokenService.invalidateToken(token, UserTokenType.CHANGE_PHONE);                

        u.setMobile(mobile);
        u.setMobileCountryCode(countryCode);
        userRepository.save(u);

        eventsEmitter.emitNotification(NotificationEvent.fromCompany(NotificationType.USER_MOBILE_CHANGED,
                companyUserRoleService.findCompanyRoleByUser(u.getId()).getCompany().getId(),
                securityHelper.currentUserId(), u.getId(), u.getId()));

    }

    public Integer inviteCompany(@Valid CompanyInviteDTO companyInviteDTO) {
        validateIsEmailOrPhone(companyInviteDTO.getEmailOrPhone(), companyInviteDTO.getCountryCode());

        String formattedEmailOrPhone = EmailPhoneUtils
                .toMailOrPhoneNumber(companyInviteDTO.getEmailOrPhone(), companyInviteDTO.getCountryCode());

        Optional<User> unverifiedUser = getAndValidateExistingUnverifiedUser(formattedEmailOrPhone);
        User currentUser = userRepository.
                findById(Integer.valueOf(LecwUtils.currentUser().getId()))
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        User invitedUser;
        Company company;

        if(unverifiedUser.isPresent()) {
            invitedUser = unverifiedUser.get();
            company = companyUserRoleService.findCompanyRoleByUser(invitedUser.getId()).getCompany();
            userTokenService.invalidateCurrentToken(invitedUser.getId(), UserTokenType.INVITE_USER);
        } else {
            Role role = findAndValidateInvitedRoleId(
                    companyInviteDTO.getRoleId(),
                    roleService.findCompanyOwnerDefaultRole(), false);

            invitedUser = createNewUserForCompanyInvite(companyInviteDTO.getEmailOrPhone(),
                    companyInviteDTO.getCountryCode(),
                    Optional.ofNullable(currentUser.getPrefLang())
                            .orElse(langCodeService.findById(AuthConstants.DEFAULT_LANGUAGE)));

            company = companyRepository.save(Company.builder()
                    .name(companyInviteDTO.getCompanyName()).build());

            companyUserRoleService.addUserToCompanyTeam(invitedUser, company, role.getName());
        }

        String code = extendedTokenService.getAndIssueToken(UserTokenType.INVITE_COMPANY, invitedUser.getId(),
                        String.valueOf(company.getId()), signupLinkExpirationMillis);

        sendCompanyInvitation(formattedEmailOrPhone, company.getId(), code, currentUser, invitedUser.getPrefLang());

        eventsEmitter.emitNotification(NotificationEvent.fromCompany(NotificationType.COMPANY_ADDED,
                company.getId(), securityHelper.currentUserId(), company.getId(), company.getId()));

        return company.getId();
    }

    private User createNewUserForCompanyInvite(String emailOrPhone, Integer countryCode, LangCode langCode) {
        User user = new User();
        if(EmailPhoneUtils.isEmail(emailOrPhone)) {
            user.setEmail(emailOrPhone);
        } else {
            user.setMobile(EmailPhoneUtils.toPhoneNumber(emailOrPhone));
            user.setMobileCountryCode(countryCode);
            user.setEmail(User.getRandomMail());
        }
        user.setPassword(passwordEncoder.encode(RandomStringUtils.random(64, true, true)));
        user.setPrefLang(langCode);
        user.getRoles().add(AuthConstants.ROLE_UNVERIFIED);
        return userRepository.save(user);
    }

    @Deprecated //TODO remove after SI-177
    public void inviteCompanyDeprecated(CompanyInviteDTO companyInviteDTO) {
        String emailOrPhone = companyInviteDTO.getEmailOrPhone();
        Integer countryCode = companyInviteDTO.getCountryCode();

        validateIsEmailOrPhone(emailOrPhone, countryCode);

        String formattedEmailOrPhone = EmailPhoneUtils.toMailOrPhoneNumber(emailOrPhone, countryCode);

        Integer roleId = findAndValidateInvitedRoleId(
                companyInviteDTO.getRoleId(),
                roleService.findCompanyOwnerDefaultRole(), false).getId();

        Integer currentUserId = Integer.valueOf(LecwUtils.currentUser().getId());
        User currentUser = userRepository.
                findById(currentUserId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        LangCode langcode = Optional.ofNullable(currentUser.getPrefLang())
                .orElse(langCodeService.findById(AuthConstants.DEFAULT_LANGUAGE));

        String code = extendedTokenService
                .getAndIssueCompanyInvitationToken(currentUserId,
                        formattedEmailOrPhone,
                        roleId,
                        signupLinkExpirationMillis);

        sendCompanyInvitationDeprecated(formattedEmailOrPhone, code, currentUser, langcode);
    }

    private Optional<User> getAndValidateExistingUnverifiedUser(String emailOrPhone) {
        Optional<User> userOpt = EmailPhoneUtils.isEmail(emailOrPhone) ?
                userRepository.findByEmail(emailOrPhone) :
                userRepository.findByMobile(emailOrPhone);

        ValidationUtils.throwIf(userOpt.isPresent() && !userOpt.get().hasRole(AuthConstants.ROLE_UNVERIFIED),
                () -> AuthException.emailOrPhoneAlreadyExist(emailOrPhone));

        return userOpt.isPresent() && userOpt.get().hasRole(AuthConstants.ROLE_UNVERIFIED) ?
                userOpt : Optional.empty();
    }

    public void resendInvitation(Integer userId) {
        User invitedUser = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

        ValidationUtils.throwIf(invitedUser.isVerified(),
                () -> AuthException.userIsAlreadyVerified(userId));

        UserDto invitingUser = LecwUtils.currentUser();
        CompanyUserRole companyUserRole = companyUserRoleService.findCompanyRoleByUser(userId);

        // on invite user can have only phone or mail
        String emailOrPhone = invitedUser.getMobile() != null ? invitedUser.getMobile() : invitedUser.getEmail();

        createTokenAndSendInvitation(invitedUser, invitingUser, companyUserRole, emailOrPhone);
    }

    private void createTokenAndSendInvitation(User updatedInvitedUser, UserDto invitingUser, CompanyUserRole companyUserRole, String emailOrPhone) {
        String token = extendedTokenService
                .getAndIssueUserInvitationToken(
                        updatedInvitedUser.getId(),
                        properties.getJwt().getExpirationMillis());
        
        if(EmailPhoneUtils.isEmail(emailOrPhone)) {
            sendInvitationMail(
                    invitingUser.getUsername(),
                    updatedInvitedUser,
                    companyUserRole.getCompany().getName(),
                    token,
                    updatedInvitedUser.getPrefLang());
        } else {
            sendInvitationSms(updatedInvitedUser,
                    companyUserRole.getCompany().getName(),
                    token,
                    updatedInvitedUser.getPrefLang());
        }        
    }

    @Deprecated //TODO remove after SI-177
    @Transactional
    public SignupResponseDTO signupDeprecated(@Valid SignupDTO signupDTO) {
        validateEmailAlreadyExists(signupDTO.getUser().getEmail());
        verifiySignupCodeDeprecated(signupDTO.getLinkEmailOrPhone(), signupDTO.getLinkCode());
        String roleName = getAndValidateSignupRole(signupDTO.getLinkCode());
        userTokenService.invalidateToken(signupDTO.getLinkCode(), UserTokenType.INVITE_COMPANY);

        // create company
        Company tempCompany = new Company().with(signupDTO.getCompany());
        Company company = companyRepository.save(tempCompany);

        // create company trades
        signupDTO.getCompany().getTradeIds().stream()
                .map(tId -> {
                    return CompanyTrade.builder().company(company.getId()).trade(tId).build();
                })
                .forEach(companyTradeRepository::save);

        // create unverified user
        User tempUser = new User().with(signupDTO.getUser());
        User user = signupWithoutLogin(tempUser);

        CompanyUserRole companyUserRole = companyUserRoleService.addUserToCompanyTeam(
                user,
                company,
                roleName
        );

        return SignupResponseDTO.from(companyUserRole);
    }

    @Transactional
    public SignupResponseDTO signup(@Valid SignupDTO signupDTO) {
        verifySignup(signupDTO.getCompany().getCompanyId(), signupDTO.getLinkCode());
        return signupCodeNeedsToBeChecked(signupDTO.getLinkCode()) ?
                signupInvitedCompany(signupDTO) : signupNotInvitedCompany(signupDTO);
    }

    private SignupResponseDTO signupInvitedCompany(SignupDTO signupDTO) {
        Integer userId = userTokenService.getUserIdByTokenAndInvalidate(signupDTO.getLinkCode());
        // update company
        CompanyUserRole companyUserRole = companyUserRoleService.findCompanyRoleByUser(userId);
        Company company = companyRepository.save(companyUserRole.getCompany().with(signupDTO.getCompany()));

        // create company trades
        signupDTO.getCompany().getTradeIds().stream()
                .map(tId -> CompanyTrade.builder()
                        .company(company.getId())
                        .trade(tId)
                        .build())
                .forEach(companyTradeRepository::save);

        // encode pw and verify user
        User user = userService.findById(userId).with(signupDTO.getUser());
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.getRoles().remove(AuthConstants.ROLE_UNVERIFIED);
        user.setCredentialsUpdatedMillis(System.currentTimeMillis());
        userRepository.save(user);

        return SignupResponseDTO.builder()
                .companyId(company.getId())
                .userId(userId)
                .build();
    }

    /** This is only for testing with special token **/
    private SignupResponseDTO signupNotInvitedCompany(SignupDTO signupDTO) {
        String email = StringUtils.substringAfter(signupDTO.getLinkCode(), ".");
        validateEmailOrPhoneAlreadyExists(email, null);

        Company company = companyRepository.save(new Company().with(signupDTO.getCompany()));

        // create company trades
        signupDTO.getCompany().getTradeIds().stream()
                .map(tId -> CompanyTrade.builder()
                        .company(company.getId())
                        .trade(tId)
                        .build())
                .forEach(companyTradeRepository::save);

        // create verified user
        User tempUser = new User().with(signupDTO.getUser());
        tempUser.setEmail(email);
        tempUser.setPassword(this.passwordEncoder.encode(tempUser.getPassword()));
        User user = userRepository.save(tempUser);
        CompanyUserRole companyUserRole = companyUserRoleService
                .addUserToCompanyTeam(user, company, RoleName.COMPANY_OWNER.name());
        return SignupResponseDTO.from(companyUserRole);
    }

    private String getAndValidateSignupRole(String linkCode) {
        if (signupCodeNeedsToBeChecked(linkCode)) {
            Integer roleId = extendedTokenService.getRoleIdFromInviteCompanyToken(linkCode);
            return roleService.findById(roleId)
                    .getName();
        }
        return roleService.findCompanyOwnerDefaultRole().getName();
    }

    /**
     * This will encode the password, make the user unverified and persist the user.
     * It won't login the user like the original method {@link LemonService#signup(User)}
     *
     * @param user the user to save
     * @return the saved user
     */
    private User signupWithoutLogin(@Valid User user) {
        LOGGER.debug("Signing up user: " + user);

        // if no language is given > set english as default language
        if (user.getPrefLang() == null) {
            user.setPrefLang(LangCode.builder().id(AuthConstants.DEFAULT_LANGUAGE).build());
        }

        this.initUser(user);
        return this.userRepository.save(user);
    }

    @Transactional
    @Override
    public void requestEmailChange(User user, User updatedUser) {
        ValidationUtils.throwIfNot(this.passwordEncoder.matches(updatedUser.getPassword(), user.getPassword()),
                AuthException::passwordIsIncorrect);
        validateEmailAlreadyExists(updatedUser.getNewEmail());
        user.setNewEmail(updatedUser.getNewEmail());
        this.userRepository.save(user);
        LecjUtils.afterCommit(() -> this.mailChangeEmailLink(user));
    }

    @Override
    public String changePassword(User user, @Valid ChangePasswordForm changePasswordForm) {
        ValidationUtils.throwIfNot(new PasswordValidator().isValid(changePasswordForm.getPassword(), null),
                AuthException::passwordDoesNotMeetMinimumRequirements);

        Integer currentUserId = toId(LecwUtils.currentUser().getId());
        String oldPassword = this.userRepository.findById(currentUserId)
                .orElseThrow(() -> AuthException.userNotFound(currentUserId))
                .getPassword();

        ValidationUtils.throwIfNot(this.passwordEncoder.matches(changePasswordForm.getOldPassword(), oldPassword),
                AuthException::passwordIsIncorrect);

        user.setPassword(this.passwordEncoder.encode(changePasswordForm.getPassword()));
        user.setCredentialsUpdatedMillis(System.currentTimeMillis());
        this.userRepository.save(user);

        eventsEmitter.emitNotification(NotificationEvent.fromCompany(NotificationType.USER_PASSWORD_CHANGED,
                companyUserRoleService.findCompanyRoleByUser(user.getId()).getCompany().getId(),
                securityHelper.currentUserId(), user.getId(), user.getId()));

        return user.toUserDto().getUsername();
    }

    @Override
    public void forgotPassword(@Valid @Email @NotBlank String email) {
        this.mailForgotPasswordLink(this.userRepository
                .findByEmail(email)
                .orElseThrow(() ->AuthException.userWithUsernameNotFound(email)));
    }

    /**
     * This will only update non-null properties of the input.
     * Properties are further restricted to those of the UserProfileDTO class,
     * although admins can also update app-level roles
     *
     * @param user        the persisted record
     * @param updatedUser the input delta
     * @param currentUser lemon's idea of the current principal
     */
    @Override
    protected void updateUserFields(User user, User updatedUser, UserDto currentUser) {
        LOGGER.debug("updateUserFields, user: {}", user);
        LOGGER.debug("updateUserFields, updatedUser: {}", updatedUser);
        // Trim input down to a user profile
        UserProfileDTO profile = UserProfileDTO.fromUser(updatedUser);
        LOGGER.debug("updateUserFields, profile: {}", profile);
        // Copy non-null to record
        BeanUtils.copyProperties(profile, user, EntityUtil.getNullPropertyNames(profile));

        // Delegate to super for default processing
        super.updateUserFields(user, updatedUser, currentUser);

        LOGGER.debug("updateUserFields, updated to: {}", user);
    }

    /**
     * Verifies an user and sends a welcome email
     */
    @Transactional(
            propagation = Propagation.REQUIRED
    )
    @Override
    public void verifyUser(Integer userId, String verificationCode) {
        User user = this.userRepository.findById(userId).orElseThrow(()->AuthException.userNotFound(userId));

        ValidationUtils.throwIfNot(user.hasRole(AuthConstants.ROLE_UNVERIFIED),
                                   () -> AuthException.signedUpUserShouldHaveRoleUnverified(userId));

        validateTokenAndClaim(verificationCode, AuthConstants.CLAIM_VERIFY, AuthConstants.CLAIM_EMAIL, user.getEmail(), user);

        user.getRoles().remove(AuthConstants.ROLE_UNVERIFIED);
        user.setCredentialsUpdatedMillis(System.currentTimeMillis());
        userRepository.save(user);

        LecjUtils.afterCommit(() -> LemonUtils.login(user));
        CompanyUserRole companyUserRole = companyUserRoleService.findCompanyRoleByUser(userId);

        sendWelcomeMail(user, companyUserRole.getCompany().getName());
    }

    private void validateTokenAndClaim(String verificationCode, String tokenClaim, String authorityClaim, String claimValue, User user) {
        try {
            JWTClaimsSet claims = this.greenTokenService.parseToken(verificationCode, tokenClaim, user.getCredentialsUpdatedMillis());
            LecUtils.ensureAuthority(claims.getSubject().equals(Objects.requireNonNull(user.getId()).toString()) &&
                            claims.getClaim(authorityClaim).equals(claimValue), "com.naturalprogrammer.spring.wrong.verificationCode");

        } catch (BadRequestException | AccessDeniedException e) {
            throw AuthException.tokenOrClaimInvalid(e.getMessage());
        }
    }

    @Override
    public void resetPassword(@Valid ResetPasswordForm form) {
        // Valid token types here can be FORGOT_PASSWORD and INVITE_USER
        // Unless a type parameter is sent along in the request, the hash alone is used to retrieve the right token
        Integer userId = simpleUserTokenService
                .getUserIdByTokenAndInvalidate(form.getCode());
        
        internalResetPassword(form.getNewPassword(), userId);
    }

    private void internalResetPassword(String newPassword, Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        user.setPassword(passwordEncoder.encode(newPassword));

        if(user.hasRole(AuthConstants.ROLE_UNVERIFIED)) {
            user.getRoles().remove(AuthConstants.ROLE_UNVERIFIED);
        }

        user.setCredentialsUpdatedMillis(System.currentTimeMillis());
        this.userRepository.save(user);
        LecjUtils.afterCommit(() -> LemonUtils.login(user));
    }

    /**
     * Sends verification mail to a unverified user.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void sendVerificationMail(User user, String verifyLink) {
        String confirmationLink = emailLinkUtil.getConfirmationLink(verifyLink, user.getId());

        mailSender.send(LemonMailData.of(user.getEmail(),
                translationUtil.get("mail.subject.verify", user.getPrefLang().getId()),
                templateUtil.getConfirmationMailContent(confirmationLink, user)));
    }

    /**
     * Sends an invitation mail to a invited user.
     */
    @SuppressWarnings("unchecked")
    private void sendInvitationMail(String invitingUsername, User invitedUser, String companyName, String code, LangCode lang) {
        String invitationLink = emailLinkUtil.getInvitationLink(code, invitedUser.getId());

        mailSender.send(LemonMailData.of(invitedUser.getEmail(),
                translationUtil.get("mail.subject.invite", lang.getId()),
                templateUtil.getInvitationMailContent(invitationLink, invitingUsername, invitedUser, companyName, lang)));
    }

    private void sendInvitationSms(User invitedUser, String companyName, String code, LangCode lang) {
        String invitationLink = emailLinkUtil.getInvitationLink(code, invitedUser.getId());
        String smsContent = templateUtil.getInvitationSmsContent(invitationLink, invitedUser, companyName, lang);
        String mobile = EmailPhoneUtils.toFullPhoneNumber(invitedUser.getMobileCountryCode(), invitedUser.getMobile());
        smsService.sendSmsMessage(smsContent, mobile);
    }

    private void sendCompanyInvitation(String emailOrPhone, Integer companyId, String code, User invitingUser, LangCode lang) {
        if(EmailPhoneUtils.isEmail(emailOrPhone)) {
            sendCompanyInvitationMail(emailOrPhone, companyId, code, invitingUser, lang);
        } else {
            sendCompanyInvitationSMS(emailOrPhone, companyId, code, invitingUser, lang);
        }
    }

    @Deprecated //TODO remove after SI-177
    private void sendCompanyInvitationDeprecated(String emailOrPhone, String code, User invitingUser, LangCode lang) {
        if(EmailPhoneUtils.isEmail(emailOrPhone)) {
            sendCompanyInvitationMailDeprecated(emailOrPhone, code, invitingUser, lang);
        } else {
            sendCompanyInvitationSMSDeprecated(emailOrPhone, code, invitingUser, lang);
        }
    }

    @SuppressWarnings("unchecked")
    private void sendCompanyInvitationMail(String email, Integer companyId, String code, User invitingUser, LangCode lang) {
        String signupLink = emailLinkUtil.getSignupLink(companyId, code);
        mailSender.send(LemonMailData.of(
                email,
                translationUtil.get("mail.subject.invite_company", lang.getId()),
                templateUtil.getCompanyInvitationMailContent(invitingUser, signupLink, lang)));
    }

    private void sendCompanyInvitationSMS(String phone, Integer companyId, String code, User invitingUser, LangCode lang) {
        String signupLink = emailLinkUtil.getSignupLink(companyId, code);
        smsService.sendSmsMessage(templateUtil.getCompanyInvitationSmsContent(invitingUser, signupLink, lang), phone);
    }

    @SuppressWarnings("unchecked")
    @Deprecated //TODO remove after SI-177
    private void sendCompanyInvitationMailDeprecated(String email, String code, User invitingUser, LangCode lang) {
            String signupLink = emailLinkUtil.getSignupLinkDeprecated(email, code);
            mailSender.send(LemonMailData.of(
                    email,
                    translationUtil.get("mail.subject.invite_company", lang.getId()),
                    templateUtil.getCompanyInvitationMailContent(invitingUser, signupLink, lang)));
    }

    @Deprecated //TODO remove after SI-177
    private void sendCompanyInvitationSMSDeprecated(String phone, String code, User invitingUser, LangCode lang) {
        String signupLink = emailLinkUtil.getSignupLinkDeprecated(phone, code);
        smsService.sendSmsMessage(templateUtil.getCompanyInvitationSmsContent(invitingUser, signupLink, lang), phone);
    }

    /**
     * Sends a welcome email after the user confirms it's account
     */
    @SuppressWarnings("unchecked")
    private void sendWelcomeMail(final User user, final String companyName) {
        mailSender.send(LemonMailData.of(user.getEmail(),
                translationUtil.get("mail.subject.welcome", user.getPrefLang().getId()),
                templateUtil.getWelcomeMailContent(user, companyName)));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void mailForgotPasswordLink(User user) {
        String forgotPasswordLink = properties.getApplicationUrl() + 
                "/reset-password?code=" + 
                extendedTokenService.issueToken(user.getId(), UserTokenType.FORGOT_PASSWORD);

        mailForgotPasswordLink(user, forgotPasswordLink);
    }
    
    /**
     * Mails the forgot password link.
     * <p>
     * Override this method if you're using a different MailData
     */
    @SuppressWarnings("unchecked")
    @Override
    public void mailForgotPasswordLink(User user, String forgotPasswordLink) {
        String resetLink = emailLinkUtil.getResetLink(forgotPasswordLink, user.getId());

        mailSender.send(LemonMailData.of(user.getEmail(),
                translationUtil.get("mail.subject.reset_password", user.getPrefLang().getId()),
                templateUtil.getResetMailContent(resetLink, user)));
    }

    /**
     * Mails the change-email verification link to the user.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void mailChangeEmailLink(User user, String changeEmailLink) {
        String emailChangeLink = emailLinkUtil.getEmailChangeLink(changeEmailLink, user.getId());

        mailSender.send(LemonMailData.of(user.getNewEmail(),
                translationUtil.get("mail.subject.change_email", user.getPrefLang().getId()),
                templateUtil.getChangeEmailMailContent(user, emailChangeLink)));
    }

    /**
     * The changeEmail Endpoint has to be overridden because in the original method
     * there was the annotation that the user has to be authorized.
     * But for changing the mail you don't have to be authorized, cause you already got the code via your email
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void changeEmailUnauthorized(Integer userId, @Valid @NotBlank String changeEmailCode) {

        // validate input
        User user = this.userRepository.findById(userId).orElseThrow(()->AuthException.userNotFound(userId));
        ValidationUtils.throwIf(StringUtils.isBlank(user.getNewEmail()), () -> AuthException.newEmailNotSetOnUser(userId));
        validateEmailAlreadyExists(user.getNewEmail());

        validateTokenAndClaim(changeEmailCode, AuthConstants.CLAIM_CHANGE_EMAIL, AuthConstants.CLAIM_NEW_EAIL, user.getNewEmail(), user);

        // set new mail
        user.setEmail(user.getNewEmail());
        user.setNewEmail(null);
        user.setCredentialsUpdatedMillis(System.currentTimeMillis());

        // remove unverified
        if (user.hasRole(AuthConstants.ROLE_UNVERIFIED)) {
            user.getRoles().remove(AuthConstants.ROLE_UNVERIFIED);
        }

        this.userRepository.save(user);

        eventsEmitter.emitNotification(NotificationEvent.fromCompany(NotificationType.USER_EMAIL_CHANGED,
                companyUserRoleService.findCompanyRoleByUser(user.getId()).getCompany().getId(),
                user.getId(), user.getId(), user.getId()));

        LecjUtils.afterCommit(() -> LemonUtils.login(user));
    }

    /**
     * Here the initial admin user is created by default.
     * But we don't need a lemon admin user for our application
     * Also if we start multiple intances of the spring application > we have duplicate DB entries with the admin user
     * So the server cannot startup
     */
    @Override
    public void onStartup() {
        LOGGER.info("Swisscon Lemon onStartup");
    }

    public String encodePassword(String input) {
        return passwordEncoder.encode(input);
    }

    public Cookie createAuthCookie(String token, Long expirationMillis) {
        String domain = cookieService.getCookieDomain();
        String name = cookieService.getCookieName();
        Integer maxAge = cookieService.getCookieMaxAge(expirationMillis.intValue());
        Boolean secure = cookieService.getCookieIsSecure();

        Cookie cookie = new Cookie(name, token);
        cookie.setDomain(domain);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        
        return cookie;
    }
    
    @Override
    public void addAuthHeader(HttpServletResponse response, String username, Long expirationMillis) {
        UserDto currentUser = LecwUtils.currentUser();

        String token = blueTokenService.createToken(
                BlueTokenService.AUTH_AUDIENCE,
                username,
                expirationMillis,
                Map.of(USER_ID_CLAIM, currentUser.getId())
        );

        response.addCookie(createAuthCookie(token, expirationMillis));
    }

    public String createRabbitJwt(String userName, Integer userId) {
        return blueTokenService.createToken(
                LEMON_RABBIT_AUDIENCE,
                userName,
                lemonProperties.getJwt().getExpirationMillis(),
                Map.of(USER_ID_CLAIM, String.valueOf(userId))
        );
    }
}
