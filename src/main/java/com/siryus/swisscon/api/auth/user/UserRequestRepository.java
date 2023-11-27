package com.siryus.swisscon.api.auth.user;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository()
public interface UserRequestRepository  extends JpaRepository<UserRequestEntity, Integer> {    
    @Query(
            value = "SELECT req.* FROM user_request req WHERE req.user_ip = :ip AND req.type = :type ORDER BY req.requested_at DESC LIMIT 1",
            nativeQuery = true
    )
    Optional<UserRequestEntity> getLastRequest(@Param("ip") String ip, @Param("type") String type);

    @Query(
        value = "SELECT COUNT(req.*) FROM user_request req WHERE req.user_ip = :ip AND req.type = :type AND req.requested_at >= :startTimeUTC",
        nativeQuery = true
    )
    Long getAttemptsCountInInterval(@Param("ip") String ip, @Param("type") String type, @Param("startTimeUTC") LocalDateTime startTimeUTC);
            
    @Modifying
    @Query(
        value = "DELETE from UserRequestEntity req WHERE req.userIp = :ip AND req.type = :type"
    )
    void deleteRequests(@Param("ip") String ip, @Param("type") UserRequestType type);      
}
