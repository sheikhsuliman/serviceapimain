package com.siryus.swisscon.api.general.reference;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.location.location.Location;
import com.siryus.swisscon.api.location.location.LocationCreateDTO;
import com.siryus.swisscon.api.location.location.LocationDetailsDTO;
import com.siryus.swisscon.api.location.location.LocationService;
import com.siryus.swisscon.api.project.project.ProjectRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Disabled;

public class ReferenceServiceTest extends AbstractMvcTestBase {

    private final ReferenceService referenceService;
    private final LocationService locationService;

    @Autowired
    public ReferenceServiceTest(ReferenceService referenceService, LocationService locationService, ProjectRepository projectRepository) {
        this.referenceService = referenceService;
        this.locationService = locationService;
    }

    @Override
    protected boolean doMockLogin() {
        return false;
    }

    @Override
    protected Integer mockLocalLoginUserId() {
        return 1;
    }

    @Test
    @Disabled
    public void testExistingForeignKey() {
        LocationCreateDTO createDTO = TestBuilder.testLocationCreateDTO(1, "Top");

        LocationDetailsDTO savedLocation = this.locationService.create(createDTO);

        // This should not throw an exception
        this.referenceService.validateForeignKey(ReferenceType.LOCATION.toString(), savedLocation.getId());
    }

    @Test
    public void testNonExistingForeignKey() {
        assertThrows(RuntimeException.class, () -> this.referenceService.validateForeignKey(ReferenceType.PROJECT.toString(), 789764564));
    }

    @Test
    public void testTemporaryReference() {
        // This should not throw an exception
        this.referenceService.validateForeignKey(ReferenceType.TEMPORARY.toString(), 1);
    }
}

