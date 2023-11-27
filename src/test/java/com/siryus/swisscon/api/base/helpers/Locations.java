package com.siryus.swisscon.api.base.helpers;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.location.location.LocationCreateDTO;
import com.siryus.swisscon.api.location.location.LocationDetailsDTO;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.siryus.swisscon.api.base.AbstractMvcTestBase.endPoint;
import com.siryus.swisscon.api.location.location.LocationTreeDTO;
import com.siryus.swisscon.api.location.location.LocationUpdateDTO;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import java.util.function.Consumer;

public class Locations {
    public LocationDetailsDTO createLocation(RequestSpecification spec, LocationCreateDTO request) {
        return createLocation(spec, request,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.CREATED.value()))
                        .extract()
                        .as(LocationDetailsDTO.class)
        );
    }
    public LocationDetailsDTO createLocation(RequestSpecification spec, LocationCreateDTO request, Function<ValidatableResponse, LocationDetailsDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .post(endPoint("/location/create"))
                        .then()
        );
    }

    public List<LocationDetailsDTO> getChildren(RequestSpecification spec, Integer projectId, Integer parentLocationId) {
        return getChildren(spec, projectId, parentLocationId, r -> r.statusCode(HttpStatus.OK.value()).extract()
                .jsonPath().getList(".", LocationDetailsDTO.class));
    }

    public List<LocationDetailsDTO> getChildren(RequestSpecification spec, Integer projectId, Integer parentLocationId, Function<ValidatableResponse, List<LocationDetailsDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                    .spec(spec)
                    .queryParam("project", projectId)
                    .queryParam("parent", parentLocationId)
                    .get(endPoint("/location/tree"))
                    .then()
        );
    }

    public TeamUserDTO[] getLocationTeam(RequestSpecification spec, Integer locationId) {
        return getLocationTeam(
                spec, locationId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(TeamUserDTO[].class)
        );
    }
    public TeamUserDTO[] getLocationTeam(RequestSpecification spec, Integer locationId, Function<ValidatableResponse, TeamUserDTO[]> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("locationId", locationId)
                        .get(endPoint("/location/{locationId}/team"))
                        .then()
        );
    }

    public LocationDetailsDTO copyLocation(RequestSpecification spec, Integer srcLocationId, Integer desLocationId) {
        return copyLocation(spec, srcLocationId, desLocationId, r -> r.statusCode(HttpStatus.OK.value()).extract().as(LocationDetailsDTO.class));
    }
    
    public LocationDetailsDTO copyLocation(RequestSpecification spec, Integer srcLocationId, Integer desLocationId, Function<ValidatableResponse, LocationDetailsDTO> responseValidator) {
        return responseValidator.apply(
                given()
                    .spec(spec)
                    .queryParam("id", srcLocationId)
                    .queryParam("targetId", desLocationId)
                    .post(endPoint("/location/copy"))
                    .then()
        );
    }

    public LocationDetailsDTO moveLocation(RequestSpecification spec, Integer srcLocationId, Integer desLocationId) {
        return moveLocation(spec, srcLocationId, desLocationId, r -> r.statusCode(HttpStatus.OK.value()).extract().as(LocationDetailsDTO.class));
    }

    public LocationDetailsDTO moveLocation(RequestSpecification spec, Integer srcLocationId, Integer desLocationId, Function<ValidatableResponse, LocationDetailsDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .queryParam("id", srcLocationId)
                        .queryParam("targetId", desLocationId)
                        .post(endPoint("/location/move"))
                        .then()
        );
    }

    public LocationDetailsDTO updateLocation(RequestSpecification spec, LocationUpdateDTO request) {
        return updateLocation(spec, request, r -> r.statusCode(HttpStatus.OK.value()).extract().as(LocationDetailsDTO.class));
    }
    
    public LocationDetailsDTO updateLocation(RequestSpecification spec, LocationUpdateDTO request, Function<ValidatableResponse, LocationDetailsDTO> responseValidator) {
        return responseValidator.apply(
                given()
                    .spec(spec)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .post(endPoint("/location/update"))
                    .then()     
        );
    }

    public void deleteLocation(RequestSpecification spec, Integer locationId) {
        deleteLocation(spec, locationId, r -> r.statusCode(HttpStatus.OK.value()));
    }

    public void deleteLocation(RequestSpecification spec, Integer locationId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
            given()
                .spec(spec)
                .pathParam("id", locationId)
                .post(endPoint("/location/{id}/delete"))
                .then()     
        );
    }

    public List<LocationTreeDTO> getExpandedTree(RequestSpecification spec, Integer locationId) {
        return getExpandedTree(spec, locationId, r -> r.statusCode(HttpStatus.OK.value()).extract().body().jsonPath().getList("$", LocationTreeDTO.class));
    }
    
    public List<LocationTreeDTO> getExpandedTree(RequestSpecification spec, Integer locationId, Function<ValidatableResponse, List<LocationTreeDTO>> responseValidator) {
        return responseValidator.apply(
            given()
                .spec(spec)
                .pathParam("id", locationId)
                .get(endPoint("/location/{id}/expanded"))
                .then()                    
        );
    }    
    public List<LocationDetailsDTO> moveLocationOrder(RequestSpecification spec, Integer locationId, Integer order) {
        return moveLocationOrder(spec, locationId, order, r -> Arrays.asList(r.statusCode(HttpStatus.OK.value()).extract().as(LocationDetailsDTO[].class)));
    }

    public List<LocationDetailsDTO> moveLocationOrder(RequestSpecification spec, Integer locationId, Integer order, Function<ValidatableResponse, List<LocationDetailsDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("locationId", locationId)
                        .queryParam("order", order)
                        .post(endPoint("/location/{locationId}/move-order"))
                        .then()
        );
    }
}
