package com.siryus.swisscon.api.auth.usertoken;

import com.siryus.swisscon.api.auth.AuthException;
import com.siryus.swisscon.api.exceptions.BusinessLogicException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SimpleUserTokenService implements UserTokenService {

    private final UserTokenRepository tokenRepository;

    @Autowired
    public SimpleUserTokenService(
            UserTokenRepository tokenRepository
    ) {
        this.tokenRepository = tokenRepository;
    }
    
    @Override
    public boolean isTokenValid(Integer userId, UserTokenType type, String rawToken) {
        UserTokenEntity userToken = tokenRepository.getCurrentToken(userId, type)
                                                   .orElse(null);

        return userToken != null && 
               userToken.getExpiresOn().isAfter(LocalDateTime.now()) && 
               hashMatches(rawToken, userToken.getTokenHash());
    }

    @Override
    public boolean isTokenValid(String externalId, UserTokenType type, String rawToken) {
        return tokenRepository
                .findTokensByExternalId(externalId, type)
                .stream()
                .anyMatch(t ->
                        t.getExpiresOn().isAfter(LocalDateTime.now()) &&
                                hashMatches(rawToken, t.getTokenHash()));

    }

    @Override
    public Integer getUserIdByTokenAndInvalidate(String rawToken) {
        UserTokenEntity token = tokenRepository.getToken(getHashValue(rawToken))
                .orElseThrow(() -> AuthException.tokenIsNotValidOrExpired(rawToken));

        validateCodeExpiration(token);
        invalidateToken(rawToken, token.getType());

        return token.getUserId();
    }

    private void validateCodeExpiration(UserTokenEntity tokenEntity) {
        if(tokenEntity.getExpiresOn().isBefore(LocalDateTime.now())) {
            throw AuthException.tokenIsNotValidOrExpired("");
        }
    }

    @Override
    public void invalidateCurrentToken(Integer userId, UserTokenType type) {
        tokenRepository.invalidateCurrentToken(userId, type);
    }

    @Override
    public void invalidateToken(String rawToken, UserTokenType type) {
        tokenRepository.invalidateToken(getHashValue(rawToken), type);
    }
    
    @Override
    public UserTokenEntity save(UserTokenEntity ute) {
        return tokenRepository.save(ute);
    }

    @Override
    public List<UserTokenEntity> findTokensByExternalId(String externalId, UserTokenType userTokenType) {
        return tokenRepository.findTokensByExternalId(externalId, userTokenType);
    }

    @Override
    public String getHashValue(String rawValue) {
        if (rawValue == null || rawValue.length() == 0) {
            throw new BusinessLogicException("Invalid argument to hashing function");
        }

        return DigestUtils.sha256Hex(rawValue);
    }
    
    @Override
    public UserTokenEntity findToken(String rawToken) {
        return tokenRepository.getToken(getHashValue(rawToken))
            .orElseThrow(() -> AuthException.tokenIsNotValidOrExpired(rawToken));
    }        

    private boolean hashMatches(String rawValue, String hashValue) {
        return hashValue.equals(getHashValue(rawValue));
    }
}
