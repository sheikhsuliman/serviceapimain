package com.siryus.swisscon.soa;

import com.siryus.swisscon.api.auth.user.UserProfileDTO;
import com.siryus.swisscon.api.auth.user.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/soa/")
public class LemonSoaController {
    private final UserService userService;

    @Autowired
    public LemonSoaController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path="users/{userId}/profile", produces="application/json")
    @ApiOperation(value = "Retrieve other user profile", notes = "This is only for non-sensitive information")
    public UserProfileDTO getOtherUserProfile(@PathVariable Integer userId) {
        return userService.getOtherUserProfile(userId);
    }
}
