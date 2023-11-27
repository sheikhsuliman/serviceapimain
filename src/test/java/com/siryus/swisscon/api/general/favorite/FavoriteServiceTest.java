package com.siryus.swisscon.api.general.favorite;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.auth.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class FavoriteServiceTest extends AbstractMvcTestBase {
    private final FavoriteService favoriteService;
    private final UserService userService;

    @Autowired
    public FavoriteServiceTest(
        FavoriteService favoriteService,
        UserService userService
    ) {
        this.favoriteService = favoriteService;
        this.userService = userService;
    }

    @Test
    public void userStarred() {
        // TODO After a method in the favorite service is added to add favorite items > improve this adding and removing items
        assertEquals(this.favoriteService.userStarred(9999, 1, ReferenceType.PROJECT), false, "Its now possible that a newly created user has liked the project 1");
    }
}
