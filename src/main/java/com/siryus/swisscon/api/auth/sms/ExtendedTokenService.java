package com.siryus.swisscon.api.auth.sms;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.AuthException;
import com.siryus.swisscon.api.auth.LemonTemplateUtil;
import com.siryus.swisscon.api.auth.TokenService;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.auth.user.UserRequestService;
import com.siryus.swisscon.api.auth.usertoken.TokenRequestDTO;
import com.siryus.swisscon.api.auth.usertoken.UserTokenEntity;
import com.siryus.swisscon.api.auth.usertoken.UserTokenService;
import com.siryus.swisscon.api.auth.usertoken.UserTokenType;
import com.siryus.swisscon.api.util.CustomStringUtils;
import com.siryus.swisscon.api.util.EmailPhoneUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ExtendedTokenService implements TokenService {

    private static final int PIN_CODE_LENGTH = 6;
    public static final long PIN_CODE_EXPIRATION_SECONDS = 600;

    private final UserTokenService userTokenService;
    private final UserRepository userRepository;
    private final UserRequestService userRequestService;
    private final SmsService smsService;
    private final LemonTemplateUtil lemonUtilTemplate;

    @Autowired
    public ExtendedTokenService(
            UserTokenService userTokenService,
            UserRepository userRepository,
            UserRequestService userRequestService,
            SmsService smsService, LemonTemplateUtil lemonUtilTemplate
    ) {
        this.userTokenService = userTokenService;
        this.userRepository = userRepository;
        this.userRequestService = userRequestService;
        this.smsService = smsService;
        this.lemonUtilTemplate = lemonUtilTemplate;
    }

    @Transactional
    @Override
    public String issueToken(Integer userId, UserTokenType type) {
        return issueToken(userId, type, null, PIN_CODE_EXPIRATION_SECONDS);
    }

    @Transactional
    @Override
    public String issueToken(Integer userId, UserTokenType type, long expirationSeconds ) {
        return issueToken(userId, type, null, expirationSeconds);
    }

    @Transactional
    @Override
    public String issueToken(Integer userId, UserTokenType type, String externalId, long expirationSeconds ) {
        User u =userRepository.findById(userId)
                .orElseThrow(()-> AuthException.userNotFound(userId));

        String rawToken = generateRawToken();

        userTokenService.invalidateCurrentToken(u.getId(), type);
        String effectiveExternalId = Optional.ofNullable(externalId).orElse(u.getUsername());

        UserTokenEntity ute = UserTokenEntity.builder()
                .type(type)
                .userId(u.getId())
                .externalId(effectiveExternalId)
                .tokenHash(userTokenService.getHashValue(rawToken))
                .expiresOn(LocalDateTime.now().plusSeconds(expirationSeconds))
                .build();

        userTokenService.save(ute);

        return rawToken;
    }
    
    @Transactional
    @Override
    public void sendChangePhoneSmsToken(String newMobile, Integer newCountryCode) {
        if (!EmailPhoneUtils.isPhone(newCountryCode, newMobile)) {
            throw AuthException.emailOrPhoneNotCorrectlyFormatted(newMobile, newCountryCode);
        }        

        String newFullMobile = EmailPhoneUtils.toReversibleFullPhoneNumber(newCountryCode, newMobile);

        if (userRepository.userWithMobileExists(newCountryCode, newMobile)) {
            throw AuthException.anotherUserWithMobileExists(newFullMobile);
        }

        User u = userRepository.findById(Integer.parseInt(LecwUtils.currentUser().getId())).get();        

        userTokenService.invalidateCurrentToken(u.getId(), UserTokenType.CHANGE_PHONE);

        String rawToken = generateRawToken();

        UserTokenEntity ute = UserTokenEntity.builder()
                .type(UserTokenType.CHANGE_PHONE)
                .userId(u.getId())
                .tokenHash(userTokenService.getHashValue(rawToken))
                .expiresOn(LocalDateTime.now().plusSeconds(PIN_CODE_EXPIRATION_SECONDS))
                .externalId(newFullMobile) //unless we have a list of all possible country codes, to obtain back components, this is a hack
                .build();

        userTokenService.save(ute);               

        smsService.sendSmsMessage(lemonUtilTemplate.getChangeMobileSmsContent(rawToken, u), newFullMobile);
    }    
    
    @Transactional
    @Override
    public void sendForgotPasswordSmsToken(TokenRequestDTO request) {
        if (!userRequestService.canRequestToken(request.getIp())) {
            throw AuthException.tooManyAttempts();
        }

        userRequestService.addForgotPasswordRequestForIp(request.getIp(), request.getUserIdentifier());

        User u = userRepository.findByMobile(request.getUserIdentifier())
                .orElseThrow(() -> AuthException.userWithMobileNotFound(request.getUserIdentifier()));

        String rawToken = generateRawToken();

        userTokenService.invalidateCurrentToken(u.getId(), UserTokenType.FORGOT_PASSWORD);

        smsService.sendSmsMessage(lemonUtilTemplate.getForgetPasswordSmsContent(rawToken, u), request.getUserIdentifier());

        UserTokenEntity ute = UserTokenEntity.builder()
                .type(UserTokenType.FORGOT_PASSWORD)
                .userId(u.getId())
                .tokenHash(userTokenService.getHashValue(rawToken))
                .expiresOn(LocalDateTime.now().plusSeconds(PIN_CODE_EXPIRATION_SECONDS))
                .build();

        userTokenService.save(ute);
    }

    @Transactional
    @Override
    public void verifyToken(Integer userId, String rawToken, UserTokenType type, boolean andInvalidate) {
        if (null == rawToken || rawToken.isBlank() || !userTokenService.isTokenValid(userId, type, rawToken)) {
            throw AuthException.tokenIsNotValidOrExpired("");
        }
        if (andInvalidate) {
            userTokenService.invalidateToken(rawToken, type);
        }
    }

    @Transactional
    @Override
    public UserTokenEntity verifyToken(String rawToken, UserTokenType type, boolean andInvalidate) {
        var token = userTokenService.findToken(rawToken);

        verifyToken(token.getUserId(), rawToken, type, andInvalidate);

        return token;
    }

    @Transactional
    @Override
    public String getAndIssueUserInvitationToken(Integer userId, long expirationInMillis) {
        userTokenService.invalidateCurrentToken(userId, UserTokenType.INVITE_USER);

        String rawToken = generateRawToken();
        long expirationInSeconds = expirationInMillis / 1000;

        UserTokenEntity userToken = UserTokenEntity.builder()
                .type(UserTokenType.INVITE_USER)
                .userId(userId)
                .tokenHash(userTokenService.getHashValue(rawToken))
                .expiresOn(LocalDateTime.now().plusSeconds(expirationInSeconds))
                .build();

        userTokenService.save(userToken);
        return rawToken;
    }

    @Transactional
    @Override
    public String getAndIssueToken(UserTokenType type, Integer userId, String externalId, long expirationInMillis) {
        String rawToken = generateRawToken();
        long expirationInSeconds = expirationInMillis / 1000;

        UserTokenEntity token = UserTokenEntity.builder()
                .type(type)
                .userId(userId)
                .externalId(externalId)
                .tokenHash(userTokenService.getHashValue(rawToken))
                .expiresOn(LocalDateTime.now().plusSeconds(expirationInSeconds))
                .build();

        userTokenService.save(token);
        return rawToken;
    }

    @Transactional
    @Deprecated //TODO remove after SI-177
    @Override
    public String getAndIssueCompanyInvitationToken(Integer userId, String emailOrPhone, Integer roleId, long expirationInMillis) {
        String rawToken = generateRawToken() + roleId;
        long expirationInSeconds = expirationInMillis / 1000;

        UserTokenEntity token = UserTokenEntity.builder()
                .type(UserTokenType.INVITE_COMPANY)
                .userId(userId)
                .externalId(emailOrPhone)
                .tokenHash(userTokenService.getHashValue(rawToken))
                .expiresOn(LocalDateTime.now().plusSeconds(expirationInSeconds))
                .build();

        userTokenService.save(token);
        return rawToken;
    }

    @Override
    public Integer getRoleIdFromInviteCompanyToken(String token) {
        String roleId = CustomStringUtils.substringAfterIndex(token, PIN_CODE_LENGTH);
        if(!NumberUtils.isParsable(roleId)) {
            throw AuthException.tokenIsNotValidOrExpired(token);
        }
        return Integer.valueOf(roleId);
    }

    @Override
    @Deprecated //TODO remove after SI-177
    public boolean companyInvitationTokenIsValid(String emailOrPhone, String rawToken) {
        return userTokenService.isTokenValid(emailOrPhone, UserTokenType.INVITE_COMPANY, rawToken);
    }

    private String generateRawToken() {
        return RandomStringUtils.randomAlphanumeric(PIN_CODE_LENGTH).toUpperCase();
    }
}
