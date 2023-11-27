package com.siryus.swisscon.api.auth.usertoken;

import java.util.List;

public interface UserTokenService {
    boolean isTokenValid(Integer userId, UserTokenType type, String rawToken);

    boolean isTokenValid(String externalId, UserTokenType type, String rawToken);

    Integer getUserIdByTokenAndInvalidate(String rawToken);

    void invalidateCurrentToken(Integer userId, UserTokenType type);

    void invalidateToken(String rawToken, UserTokenType type);

    List<UserTokenEntity> findTokensByExternalId(String externalId, UserTokenType userTokenType);

    String getHashValue(String rawValue);

    UserTokenEntity save(UserTokenEntity ute);
    
    UserTokenEntity findToken(String rawToken);    

}
