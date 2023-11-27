package com.siryus.swisscon.api.auth;

import com.siryus.swisscon.api.auth.user.UserRequestEntity;
import com.siryus.swisscon.api.auth.user.UserRequestService;
import com.siryus.swisscon.api.auth.user.UserRequestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {
    @Autowired
    UserRequestService userRequestService;

    @EventListener
    public void authenticationFailed(AuthenticationFailureBadCredentialsEvent event) {       
        String ip = ((WebAuthenticationDetails) event.getAuthentication().getDetails()).getRemoteAddress();
        String user = event.getAuthentication().getName();

        // If the attacker intentionally sends a long username it is trimmed to column length size
        userRequestService.addFailedLoginRequestForIp(ip, user.substring(0, Math.min(user.length(), UserRequestEntity.DETAILS_COLUMN_LENGTH)));
    }

    @EventListener
    public void authenticationSuccess(AuthenticationSuccessEvent  event) {
        String ip = ((WebAuthenticationDetails) event.getAuthentication().getDetails()).getRemoteAddress();

        userRequestService.deleteRequests(ip, UserRequestType.LOGIN_ATTEMPT);
    }    
}