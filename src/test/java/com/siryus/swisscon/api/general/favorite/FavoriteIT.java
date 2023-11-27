package com.siryus.swisscon.api.general.favorite;

import com.siryus.swisscon.api.auth.LemonService;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.general.country.Country;
import com.siryus.swisscon.api.general.gender.Gender;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.project.project.ProjectDTO;
import com.siryus.swisscon.api.project.project.ProjectRepository;
import static io.restassured.RestAssured.given;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

public class FavoriteIT extends AbstractMvcTestBase {

    private ProjectRepository projectRepository;
    private FavoriteService favoriteService;
    private FavoriteRepository favoriteRepository;
    private static LemonService lemonService;      
    private static UserService userService;      
    private static User createdUser;    

    private static final String password = RandomStringUtils.randomAlphabetic(15);

    private static List<Country> countries = null;
    private static List<Gender> genders = null;
    private static List<Company> companies = null;
    
    @Autowired
    public FavoriteIT(ProjectRepository projectRepository, FavoriteService fs, FavoriteRepository fr, LemonService ls, UserService us) {
        this.projectRepository = projectRepository;

        this.favoriteService = fs;
        
        this.favoriteRepository = fr;
        
        lemonService = ls;
        
        userService = us;
    }
    
    @BeforeAll
    public void setup() {
        List<Project> projects = projectRepository.findAll();
        
        User u = new User();
        u.setEmail("test-user-" + RandomStringUtils.randomAlphabetic(4) + "@test.com");
        u.setPassword(lemonService.encodePassword(password));
        
        createdUser = userService.create(u);
        for(int i = 0; i < Math.min(15, projects.size()); i++) {
            Favorite f = new Favorite();
            f.setUser(createdUser);
            f.setReferenceType(ReferenceType.PROJECT.name());
            f.setReferenceId(projects.get(i).getId());
            favoriteService.create(f);
        }
        
        List<Integer> ids = favoriteRepository.allFavouritesIds(createdUser.getId(), ReferenceType.PROJECT.name());
        
        assertTrue(ids != null, "Unable to create favorite test data");
        assertTrue(ids.size() == Math.min(15, projects.size()), "Favorite data was not saved correctly");
    }    
    
    @Disabled("Will be enabled once the create favorite endpoint is created")
    @Test
    public void testCreateFavorite() {
        RequestSpecification specification = loginSpec();

        User user = new User();
        user.setId(2);
        Favorite resource = Favorite.builder()
                .user(user)
                .referenceId(1)
                .referenceType(ReferenceType.PROJECT.toString())
                .build();

        // Save favorite
        resource = given().spec(specification).body(resource)
                .post("/api/rest/favorites").then()
                .assertThat().statusCode(201)
                .extract().as(Favorite.class);

        // Load and assert favorite
        resource = given().spec(specification).
                get("/api/rest/favorites/" + resource.getId()).then()
                .assertThat().statusCode(200)
                .extract().as(Favorite.class);

        assertNotNull(resource.getUser());
        assertNotNull(resource.getReferenceId());
        assertNotNull(resource.getReferenceType());
    }
    
    @Test
    public void testGetFavoriteProjectsAuthenticated() {
        RequestSpecification specification = loginSpec(createdUser.getEmail(), password);
        
        List<ProjectDTO> response = extractListInPage(
                                        getResponse(specification, BASE_PATH + "/favorites/projects")
                                        .body()
                                        .jsonPath(),
                                        ProjectDTO.class
                                    );
        
        assertTrue(response != null, "Response was null");
        assertTrue(response.size() > 0, "Response was empty!");
    }
    
    @Test
    public void testGetFavoriteProjectsNotAuthenticated() {        
            given()
            .spec(defaultSpec())
            .get(BASE_PATH + "/favorites/projects")
            .then()
            .assertThat()
            .statusCode(equalTo(HttpStatus.UNAUTHORIZED.value()));
    }    
}
