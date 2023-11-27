package com.siryus.swisscon.api.auth;

import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.siryus.swisscon.api.auth.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LemonRepository extends AbstractUserRepository<User, Integer> {

    @Query("select user from User user where lower(user.email) = lower(:email) and user.disabled = null")
    Optional<User> findByEmail(@Param("email") String email);

}
