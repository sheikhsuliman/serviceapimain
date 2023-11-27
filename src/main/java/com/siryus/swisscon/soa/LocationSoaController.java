package com.siryus.swisscon.soa;

import com.siryus.swisscon.api.location.location.Location;
import com.siryus.swisscon.api.location.location.LocationDetailsDTO;
import com.siryus.swisscon.api.location.location.LocationService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/soa/")
public class LocationSoaController {

    private final LocationService locationService;

    @Autowired
    public LocationSoaController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("location/{locationId}/sub-location-ids")
    public List<Integer> subLocationIds(@PathVariable Integer locationId) {
        return locationService.getAllLocationsByParent(locationId)
                .stream()
                .map(Location::getId)
                .collect(Collectors.toList());
    }
    @GetMapping(path="location/{locationId}", produces="application/json")
    @ApiOperation(value = "Retrieve location details", notes = "This is only for non-sensitive information")
    public LocationDetailsDTO getLocation(@PathVariable Integer locationId) {
        return locationService.getLocation(locationId);
    }
}
