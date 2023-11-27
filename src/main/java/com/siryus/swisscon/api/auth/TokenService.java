package com.siryus.swisscon.api.auth;

import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.usertoken.TokenRequestDTO;
import com.siryus.swisscon.api.auth.usertoken.UserTokenEntity;
import com.siryus.swisscon.api.auth.usertoken.UserTokenType;

public interface TokenService {

    void sendForgotPasswordSmsToken(TokenRequestDTO tokenRequest);
    
    void sendChangePhoneSmsToken(String newMobile, Integer newCountryCode);
    
    String issueToken(Integer userId, UserTokenType type);

    String issueToken(Integer userId, UserTokenType type, long expirationSeconds);

    String issueToken(Integer userId, UserTokenType type, String externalId, long expirationSeconds);

    void verifyToken(Integer userId, String rawToken, UserTokenType type, boolean andInvalidate);

    default void verifyToken(User user, String rawToken, UserTokenType type) {
        verifyToken(user.getId(), rawToken, type, false);
    }

    UserTokenEntity verifyToken(String rawToken, UserTokenType type, boolean andInvalidate);

    String getAndIssueUserInvitationToken(Integer userId, long expirationInMillis);

    String getAndIssueToken(UserTokenType type, Integer userId, String externalId, long expirationInMillis);

    @Deprecated //TODO remove after SI-177
    String getAndIssueCompanyInvitationToken(Integer userId, String emailOrPhone, Integer roleId, long expirationInMillis);

    Integer getRoleIdFromInviteCompanyToken(String token);

    boolean companyInvitationTokenIsValid(String emailOrPhone, String rawToken);

}
