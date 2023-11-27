package com.siryus.swisscon.api.location.location;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.tasks.TaskTeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("locationController")
@Api(
        tags = "Locations",
        produces = "application/json, application/hal+json, application/vnd.api+json"
)
@RequestMapping("/api/rest/location")
public class LocationController {

    private final LocationService locationService;
    private final TaskTeamService taskTeamService;
    
    @Autowired
    public LocationController(LocationService locationService, TaskTeamService taskTeamService) {
        this.locationService = locationService;
        this.taskTeamService = taskTeamService;
    }

    @GetMapping(value = "/tree", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Gets all child locations (first level only) based on the project id or the parent id",
            notes = "Parameters are optional but at least one needs to be present")
    @PreAuthorize("hasPermission(#project, 'PROJECT', 'LOCATION_READ')")
    public List<LocationDetailsDTO> getLocations(@RequestParam Integer project,
                                                 @RequestParam(required = false) Integer parent) {
        return locationService.getChildLocationsBy(project, parent);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Gets information pertaining to a location")
    @PreAuthorize("hasPermission(#id, 'LOCATION', 'LOCATION_READ')")
    public LocationDetailsDTO getLocation(@PathVariable Integer id) {
        return locationService.getLocation(id);
    }    

    @GetMapping(value="/{id}/expanded", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Gets expanded tree information for a location", 
                  notes="Details are only given for nodes where it is absolutely needed for expanding the tree")
    @PreAuthorize("hasPermission(#id, 'LOCATION', 'LOCATION_READ')")
    public List<LocationTreeDTO> getExpandedLocationTree(@PathVariable Integer id) {
        return locationService.getExpandedTree(id);
    }    
    
    @PostMapping(value="/update", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Updates an existing location", 
                  notes="Linked file resources are also updated;")
    @PreAuthorize("hasPermission(#locationInfo.id, 'LOCATION', 'LOCATION_UPDATE')")
    public LocationDetailsDTO updateLocation(@RequestBody LocationUpdateDTO locationInfo) {
        return locationService.update(locationInfo);
    }

    @PostMapping(value="/{id}/delete", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Deletes a location", 
                  notes="Only possible if it is a leaf node and it has no resources;")
    @PreAuthorize("hasPermission(#id, 'LOCATION', 'LOCATION_ARCHIVE')")
    public void deleteLocation(@PathVariable Integer id) {
        locationService.delete(id);
    }
    
    @PostMapping(value="/create", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Creates a location", 
                  notes="Linked file resources are also updated;")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(#locationInfo.projectId, 'PROJECT', 'LOCATION_CREATE')")
    public LocationDetailsDTO createLocation(@RequestBody LocationCreateDTO locationInfo) {
        return locationService.create(locationInfo);
    }
    
    @PostMapping(value="/copy", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Duplicates/Copies an existing location within the same project",
    notes = "It's not possible to that the target location has the same id than the source location")
    @PreAuthorize("hasPermission(#id, 'LOCATION', 'LOCATION_UPDATE')")
    public LocationDetailsDTO copyLocation(@RequestParam Integer id, @RequestParam Integer targetId) {
        return locationService.copy(id, targetId);
    }

    @PostMapping(value="/move")
    @ApiOperation(value = "Moves a location structure")
    @PreAuthorize("hasPermission(#id, 'LOCATION', 'LOCATION_UPDATE')")
    public LocationDetailsDTO moveLocation(@RequestParam Integer id, @RequestParam Integer targetId) {
        return locationService.move(id, targetId);
    }

    @PostMapping(value="{locationId}/move-order")
    @ApiOperation(value = "moves the location node within it's siblings")
    @PreAuthorize("hasPermission(#locationId, 'LOCATION', 'LOCATION_UPDATE')")
    public List<LocationDetailsDTO> moveOrder(@PathVariable Integer locationId, @RequestParam Integer order) {
        return locationService.moveOrder(locationId, order);
    }


    @GetMapping("/{locationId}/team")
    @PreAuthorize("hasPermission(#locationId, 'LOCATION', 'LOCATION_READ')")
    public List<TeamUserDTO> locationTeam(@PathVariable Integer locationId) {
        return taskTeamService.getLocationTeam(locationId);
    }
}
