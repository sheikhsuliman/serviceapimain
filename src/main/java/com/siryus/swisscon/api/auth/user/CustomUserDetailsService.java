package com.siryus.swisscon.api.auth.user;

import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.security.LemonUserDetailsService;
import java.util.Optional;

import com.siryus.swisscon.api.auth.AuthException;
import com.siryus.swisscon.api.util.EmailPhoneUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService extends LemonUserDetailsService<User, Integer> {

    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    UserRequestService userRequestService;

    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository, UserService userService) {
        super(null);
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByEmail(username.toLowerCase());
    }

    private Optional<User> findByMobile(String username) {
        return userService
                .findByMobileForLogin(EmailPhoneUtils.toPhoneNumber(username));
    }
    
    @Override
    public LemonPrincipal loadUserByUsername(String username) {
        Optional<User> userOpt = Optional.empty();
        
        String ip = request.getRemoteAddr();
        if (!userRequestService.canAttemptLogin(ip)) {
            throw AuthException.tooManyAttempts();
        }
        
        if(EmailPhoneUtils.isEmail(username)) {
            userOpt = findUserByUsername(username);
        } else if(EmailPhoneUtils.isPhone(username)) {
            userOpt = findByMobile(username);
        }

        User user = userOpt.orElseThrow(()-> AuthException.userWithUsernameNotFound(username));

        return new LemonPrincipal(user.toUserDto());
    }

}
