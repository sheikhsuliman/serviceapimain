package com.siryus.swisscon.api.general.favorite;

import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.base.PageableUtil;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.project.project.ProjectDTO;
import com.siryus.swisscon.api.project.project.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController("favoriteController")
@Api(
    tags = "Favorites",
    description = "Search or manage Favorite entries",
    produces = "application/json, application/hal+json, application/vnd.api+json"
)
@RequestMapping("/api/rest/favorites")
public class FavoriteController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private FavoriteService favoriteService;

    /**
     * Gets a paginated list of favorite projects
     * @return 
     */
    @RequestMapping(value="/projects", method = {RequestMethod.GET})    
    @ApiOperation(
            value = "Search for resources (paginated).",
            notes = "Find all projects paginated. Properties are _pn (page number), _ps (page size) and sort."
    )
    public ResponseEntity<Page<ProjectDTO>> getProjects(
            @ApiParam(name = "_pn", value = "The page number", allowableValues = "range[0, infinity]", defaultValue = "0")
            @RequestParam(value = "_pn", required = false, defaultValue = "0") Integer page,
            @ApiParam(name = "_ps", value = "The page size", allowableValues = "range[1, infinity]")
            @RequestParam(value = "_ps", required = false, defaultValue = "10") Integer size,
            @ApiParam(name = "sort", value = "Comma separated list of attribute names, descending for each one prefixed with a dash, ascending otherwise")
            @RequestParam(value = "sort", required = false, defaultValue = "id") String sort) {
        
        UserDto user = LecwUtils.currentUser();
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Pageable pageable = PageableUtil.buildPageable(page, size, sort);
        
        // Paged list of favorited project ids of this user
        Page<Project> result = this.favoriteRepository.findFavoriteProjects(new Integer(user.getId()), pageable);

        Page<ProjectDTO> r = result.map(p -> {
            // TODO Nick 30.10.2019 
            // Percentage completed should come from a different entity. There is no model atm which stores it.
            return projectService.dtoFrom(p, Boolean.TRUE);
        });

        return ResponseEntity.ok(r);
    }

    @PostMapping(value = "/toggle")
    public FavoriteStatusDTO toggle(@Valid @NotNull @RequestBody FavoriteStatusDTO status) {
        final Integer userId = Integer.valueOf(LecwUtils.currentUser().getId());
        if (favoriteRepository.exists(userId, status.getId(), status.getEntityType())) {
            favoriteRepository.removeForUser(userId, status.getId(), status.getEntityType());
            favoriteRepository.flush();
        } else {
            favoriteService.create(
                Favorite.builder()
                        .referenceId(status.getId())
                        .referenceType(status.getEntityType())
                        .user(User.builder().id(userId).build())
                        .build()
            );
        }
        status.setIsFavorite(favoriteRepository.exists(userId, status.getId(), status.getEntityType()));
        return status;
    }
}
