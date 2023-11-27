package com.siryus.swisscon.api.general.favorite;

import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.general.reference.ReferenceService;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("favoriteService")
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    private final UserService userService;

    private final ReferenceService referenceService;

    @Autowired
    public FavoriteService(FavoriteRepository favoriteRepository, UserService userService, ReferenceService referenceService) {
        this.favoriteRepository = favoriteRepository;
        this.userService = userService;
        this.referenceService = referenceService;
    }

    @Transactional(readOnly = false)
    public Favorite create(Favorite resource) {

            referenceService.getForeignEntityAndValidateType(
                ReferenceType.valueOf(resource.getReferenceType()),
                resource.getReferenceId()
            );

            // cast the entity, it was checked in the referenceService
            resource.setReferenceId(resource.getReferenceId());

            // set the user
            if(resource.getUser().getId() == null){
                    throw new IllegalArgumentException("A user is required to create a favorite");
            }
            resource.setUser(userService.findById(resource.getUser().getId()));

            return favoriteRepository.save(resource);
    }

    /**
     * Indicates if an user has starred (marked as one of it's favorite items) a certain entity
     * @param userId the id of the user
     * @param entityId the id of the entity
     * @param referenceType the entity type
     * @return if the user favorited this item or not
     */
    public Boolean userStarred(Integer userId, Integer entityId, ReferenceType referenceType) {
            return this.favoriteRepository.exists(userId, entityId, referenceType.toString());
    }
}
