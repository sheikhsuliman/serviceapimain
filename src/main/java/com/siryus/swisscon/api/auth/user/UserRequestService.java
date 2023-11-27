package com.siryus.swisscon.api.auth.user;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRequestService {
    private final UserRequestRepository userRequestRepository;
    public static final long FORGOT_PASSWORD_MINUTES_TO_WAIT = 5;
    public static final long NO_LOGIN_TIMEOUT_SECONDS = 180 * 60;
    public static final long LOGIN_MAX_ATTEMPTS = 3;
    
    public UserRequestService(UserRequestRepository userRequestRepository) {
        this.userRequestRepository = userRequestRepository;
    }
    
    public boolean canRequestToken(String ip) {        
        Optional<UserRequestEntity> utre = userRequestRepository.getLastRequest(ip, UserRequestType.FORGOT_PASSWORD_REQUEST.name());

        return !utre.isPresent() || 
            ChronoUnit.MINUTES.between(utre.get().getRequestedAt(), LocalDateTime.now()) > FORGOT_PASSWORD_MINUTES_TO_WAIT;
    }

    @Transactional
    public void addForgotPasswordRequestForIp(String ip, String details) {
        UserRequestEntity utre = UserRequestEntity.builder()
                .requestedAt(LocalDateTime.now())
                .userIp(ip)
                .details(details)
                .type(UserRequestType.FORGOT_PASSWORD_REQUEST)
                .build();

        userRequestRepository.save(utre);
    }
    
    @Transactional
    public void addFailedLoginRequestForIp(String ip, String details) {           
        UserRequestEntity attempt = UserRequestEntity.builder()
            .userIp(ip)
            .requestedAt(LocalDateTime.now())                    
            .type(UserRequestType.LOGIN_ATTEMPT)
            .details(details)
            .build();
                
        userRequestRepository.save(attempt);
    }
    
    @Transactional
    public boolean canAttemptLogin(String ip) {
        long numberOfAttempts = userRequestRepository.getAttemptsCountInInterval(ip, 
                UserRequestType.LOGIN_ATTEMPT.name(), 
                LocalDateTime.now(ZoneOffset.UTC).minusSeconds(NO_LOGIN_TIMEOUT_SECONDS));

        return numberOfAttempts < LOGIN_MAX_ATTEMPTS;
    }

    @Transactional
    public void deleteRequests(String ip, UserRequestType type) {
        userRequestRepository.deleteRequests(ip, type);
    }    
}
