package com.siryus.swisscon.api.auth.usertoken;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository()
public interface UserTokenRepository extends JpaRepository<UserTokenEntity, Integer> {

    @Query("SELECT token FROM UserTokenEntity token WHERE token.userId = :uid AND token.type = :type")
    Optional<UserTokenEntity> getCurrentToken(@Param("uid") Integer userId, @Param("type") UserTokenType tokenType);

    @Query("SELECT token FROM UserTokenEntity token WHERE token.externalId = :externalId AND token.type = :type")
    List<UserTokenEntity> findTokensByExternalId(@Param("externalId") String externalId, @Param("type") UserTokenType tokenType);

    @Query("SELECT token FROM UserTokenEntity token WHERE token.tokenHash = :tokenHash AND token.type = :type")
    Optional<UserTokenEntity> getToken(@Param("tokenHash") String tokenHash, @Param("type") UserTokenType tokenType);

    @Query("SELECT token FROM UserTokenEntity token WHERE token.tokenHash = :tokenHash")
    Optional<UserTokenEntity> getToken(@Param("tokenHash") String tokenHash);    
    
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(
        value = "DELETE from UserTokenEntity token WHERE token.userId = :uid AND token.type = :type"
    )
    void invalidateCurrentToken(@Param("uid") Integer userId, @Param("type") UserTokenType tokenType);

    @Modifying
    @Transactional
    @Query(
            value = "DELETE from UserTokenEntity token WHERE token.tokenHash = :tokenHash AND token.type = :type"
    )
    void invalidateToken(@Param("tokenHash") String tokenHash, @Param("type") UserTokenType tokenType);

}
