package com.siryus.swisscon.api.auth.usertoken;

import com.siryus.swisscon.api.auth.user.UserRequestEntity;
import com.siryus.swisscon.api.auth.user.UserRequestService;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import com.siryus.swisscon.api.auth.user.UserRequestRepository;
import com.siryus.swisscon.api.auth.user.UserRequestType;

@TestMethodOrder(OrderAnnotation.class)
public class UserRequestTest extends AbstractMvcTestBase{
    @Autowired
    UserRequestRepository userRequestRepository;    

    @Autowired
    UserRequestService userRequestService;
    
    private final String USER_IP_1 = "192.168.1.1";
    
    private final String USER_IP_2 = "192.168.1.2";
    
    @Test
    @Order(1)
    public void Given_ipHasNoRequests_ipCanMakeRequest() {
        assertTrue(userRequestService.canRequestToken(USER_IP_1), "User sould be able to perform a request");
    }

    @Test
    public void Given_lastIpRequestExpired_ipCanMakeRequest() {
        UserRequestEntity utr = UserRequestEntity.builder()
                .requestedAt(LocalDateTime.now().minusMinutes(UserRequestService.FORGOT_PASSWORD_MINUTES_TO_WAIT + 2))
                .userIp(USER_IP_1)
                .type(UserRequestType.FORGOT_PASSWORD_REQUEST)                
                .build();
        
        userRequestRepository.save(utr);
        
        assertTrue(userRequestService.canRequestToken(USER_IP_1), "User should be able to perform a request");
    }

    @Test
    public void Given_lastIpRequestDidNotExpire_ipCanNotMakeRequest() {
        UserRequestEntity utr = UserRequestEntity.builder()
                .requestedAt(LocalDateTime.now().minusMinutes(UserRequestService.FORGOT_PASSWORD_MINUTES_TO_WAIT - 2))
                .userIp(USER_IP_2)
                .type(UserRequestType.FORGOT_PASSWORD_REQUEST)                
                .build();
        
        userRequestRepository.save(utr);
        
        assertFalse(userRequestService.canRequestToken(USER_IP_2), "User should be able to perform a request");        
        assertTrue(userRequestService.canRequestToken(USER_IP_1), "User should be able to perform a request");                
    }    
}
