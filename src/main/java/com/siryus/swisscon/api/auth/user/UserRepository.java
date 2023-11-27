package com.siryus.swisscon.api.auth.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("select case when count(user)> 0 then true else false end from User user where lower(user.email) = lower(?1) and user.disabled = null")
    boolean userWithEmailExists(String email);

    @Query("select case when count(user)> 0 then true else false end from User user where user.mobileCountryCode = :countryCode and user.mobile = :mobile and user.disabled = null")
    boolean userWithMobileExists(@Param("countryCode") Integer countryCode, @Param("mobile") String mobile);

    @Transactional
    @Modifying
    @Query( nativeQuery = true, value = "update \"user\" set disabled = now() AT TIME ZONE 'UTC', password = '' where id = ?1")
    void disable(Integer userId);

    @Query("select case when count(user)> 0 then true else false end from User user where user.disabled = null")
    boolean existsAnyActiveUsers();

    @Query("select user from User user where concat(user.mobileCountryCode, user.mobile) = :mobile and user.disabled = null")
    Optional<User> findByMobile(@Param("mobile") String mobile);

    @Query("select user from User user where concat(user.mobileCountryCode,'0', user.mobile) = :mobile and user.disabled = null")
    Optional<User> findByMobileWithZeroPrefix(@Param("mobile") String mobile);

    @Query("select user from User user where user.mobile = :mobile and user.disabled = null")
    List<User> findMobileWithoutCountryCode(@Param("mobile") String mobile);

    @Query(value = "select * from \"user\" where lower(email) = lower(:email) and disabled is null", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);
    
}
