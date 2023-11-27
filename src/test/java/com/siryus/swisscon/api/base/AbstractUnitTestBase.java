package com.siryus.swisscon.api.base;

import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import org.junit.jupiter.api.AfterAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractUnitTestBase {

    @AfterAll
    public static void doAfterAll() {
        unMockApplicationUser();
    }

    protected static UserDto mockApplicationUser(Integer userId ) {
        UserDto userDto = new UserDto();
        userDto.setId(userId.toString());
        LemonPrincipal principal = new LemonPrincipal(userDto);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(principal);

        return userDto;
    }

    protected static void unMockApplicationUser() {
        SecurityContextHolder.clearContext();
    }

}
