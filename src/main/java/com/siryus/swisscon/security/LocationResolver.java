package com.siryus.swisscon.security;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.location.location.Location;
import com.siryus.swisscon.api.location.location.LocationRepository;

import java.io.Serializable;

public class LocationResolver implements SiryusPermissionChecker.TargetResolver {
    private final LocationRepository locationRepository;

    public LocationResolver(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (!Location.class.isAssignableFrom(target.getClass())) {
            throw SecurityException.unsupportedTargetType(target.getClass().toString());
        }

        return AuthorizationTarget.builder().projectId(((Location)target).getProject().getId()).build();
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        final Location location = this.locationRepository.findById(Integer.valueOf(target.toString()))
                .orElseThrow(() -> SecurityException.requiredResolutionFailed(ReferenceType.LOCATION.name(), target));
        return this.resolveTarget(location);
    }
}
