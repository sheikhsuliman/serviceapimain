package com.siryus.swisscon.api.location.location;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

@ExtendWith(FailFastExtension.class)
public class LocationUnhappyPathIT extends AbstractMvcTestBase {
    private TestHelper.ExtendedTestProject testProject;
    private LocationDetailsDTO location1A;
    private LocationDetailsDTO location2A;
    private LocationDetailsDTO location3A;
    private LocationDetailsDTO location3B;
    private LocationDetailsDTO location2B;
    private LocationDetailsDTO location2C;
    
    private final String UPDATED_LOCATION_NAME = "3B_UPDATED";
    private final String UPDATED_LOCATION_DESCRIPTION = "New Location description";
    private final String UNIT_METRE = "m";    

    @BeforeEach
    void doBeforeEach() {
        cleanDatabase();
        testProject = testHelper.createExtendedProject();

        location1A = testHelper.createLocation(
                testProject.ownerCompany.asAdmin,
                TestBuilder.testLocationCreateDTO(testProject.ownerCompany.projectId, "Location 1 - A")
        );
        {
            location2A = testHelper.createLocation(
                    testProject.ownerCompany.asAdmin,
                    TestBuilder.testLocationCreateDTO(
                            testProject.ownerCompany.projectId,
                            location1A.id,
                            "Location 2 - A"
                    )
            );

            {
                location3A = testHelper.createLocation(
                        testProject.ownerCompany.asAdmin,
                        TestBuilder.testLocationCreateDTO(
                                testProject.ownerCompany.projectId,
                                location2A.id,
                                "Location 3 - A"
                        )
                );

                location3B = testHelper.createLocation(
                        testProject.ownerCompany.asAdmin,
                        TestBuilder.testLocationCreateDTO(
                                testProject.ownerCompany.projectId,
                                location2A.id,
                                "Location 3 - B"
                        )
                );
            }
            location2B = testHelper.createLocation(
                    testProject.ownerCompany.asAdmin,
                    TestBuilder.testLocationCreateDTO(
                            testProject.ownerCompany.projectId,
                            location1A.id,
                            "Location 2 - B"
                    )
            );
            location2C = testHelper.createLocation(
                    testProject.ownerCompany.asAdmin,
                    TestBuilder.testLocationCreateDTO(
                            testProject.ownerCompany.projectId,
                            location1A.id,
                            "Location 2 - C"
                    )
            );
        }
    }
    
    // ======= LIST Locations =======
    @Test
    void Given_invalidProjectId_When_getRootLocations_Then_fail() {        
        testHelper.getChildren(testProject.ownerCompany.asAdmin, -1, null, r -> { r.statusCode(HttpStatus.FORBIDDEN.value()); return null;} );
    } 
    
    @Test
    void Given_invalidLocationId_When_getLocationsByParentId_Then_fail() {        
        testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.projectId, -1, r -> { r.statusCode(HttpStatus.BAD_REQUEST.value()); return null;} );
    }
    
    // ======= UPDATE Locations =======
    @Test
    void Given_invalidFileId_When_updatingLocation_Then_fail() {   
        File file = testHelper.fileUpload(testProject.ownerCompany.asAdmin, ReferenceType.LOCATION, location3B.getId());
        
        LocationUpdateDTO dto = LocationUpdateDTO.builder()
                                                       .id(location1A.getId())
                                                       .name(UPDATED_LOCATION_NAME)
                                                       .description(UPDATED_LOCATION_DESCRIPTION)
                                                       .height(BigDecimal.TEN)
                                                       .unit(UNIT_METRE)
                                                       .imageFileId(-1)
                                                       .layoutFileId(file.getId())
                                                       .build();
        
        testHelper.updateLocation(testProject.ownerCompany.asAdmin, dto, r -> { r.statusCode(HttpStatus.BAD_REQUEST.value()); return null;} );                
    }

    @Test
    void Given_invalidLayoutFileId_When_updatingLocation_Then_fail() {       
        File file = testHelper.fileUpload(testProject.ownerCompany.asAdmin, ReferenceType.LOCATION, location3B.getId());
        
        LocationUpdateDTO dto = LocationUpdateDTO.builder()
                                                       .id(location3B.getId())
                                                       .name(UPDATED_LOCATION_NAME)
                                                       .description(UPDATED_LOCATION_DESCRIPTION)
                                                       .height(BigDecimal.TEN)
                                                       .unit(UNIT_METRE)
                                                       .imageFileId(file.getId())
                                                       .layoutFileId(-1)
                                                       .build();
        
        testHelper.updateLocation(testProject.ownerCompany.asAdmin, dto, r -> { r.statusCode(HttpStatus.BAD_REQUEST.value()); return null;} );                
    }
    
    @Test
    void Given_invalidUnit_When_updatingLocation_Then_fail() {       
        File image = testHelper.fileUpload(testProject.ownerCompany.asAdmin, ReferenceType.LOCATION, location3B.getId());
        File layout = testHelper.fileUpload(testProject.ownerCompany.asAdmin, ReferenceType.LOCATION, location3B.getId());
        
        LocationUpdateDTO dto = LocationUpdateDTO.builder()
                                                       .id(location1A.getId())
                                                       .name(UPDATED_LOCATION_NAME)
                                                       .description(UPDATED_LOCATION_DESCRIPTION)
                                                       .height(BigDecimal.TEN)
                                                       .unit(UNIT_METRE + "1")
                                                       .imageFileId(image.getId())
                                                       .layoutFileId(layout.getId())
                                                       .build();
        
        testHelper.updateLocation(testProject.ownerCompany.asAdmin, dto, r -> { r.statusCode(HttpStatus.BAD_REQUEST.value()); return null;} );                
    }    
    
    @Test
    void Given_missingLocationName_When_updatingLocation_Then_fail() {       
        File image = testHelper.fileUpload(testProject.ownerCompany.asAdmin, ReferenceType.LOCATION, location3B.getId());
        File layout = testHelper.fileUpload(testProject.ownerCompany.asAdmin, ReferenceType.LOCATION, location3B.getId());
        
        LocationUpdateDTO dto = LocationUpdateDTO.builder()
                                                       .id(location1A.getId())
                                                       .name(null)
                                                       .description(UPDATED_LOCATION_DESCRIPTION)
                                                       .height(BigDecimal.TEN)
                                                       .unit(UNIT_METRE + "1")
                                                       .imageFileId(image.getId())
                                                       .layoutFileId(layout.getId())
                                                       .build();
        
        testHelper.updateLocation(testProject.ownerCompany.asAdmin, dto, r -> { r.statusCode(HttpStatus.BAD_REQUEST.value()); return null;} );                
    }
           
    // ======= DELETE Locations =======
    @Test
    void Given_invalidLocationId_When_deleteLocation_Then_fail() {              
        testHelper.deleteLocation(testProject.ownerCompany.asAdmin, -1, r -> r.statusCode(HttpStatus.FORBIDDEN.value()));
    }
    
    // ======= EXPAND Locations =======  
    @Test
    void Given_invalidLocationId_When_expandLocation_Then_fail() {              
        testHelper.deleteLocation(testProject.ownerCompany.asAdmin, -1, r -> r.statusCode(HttpStatus.FORBIDDEN.value()));
    }
    
    // ======= COPY Locations ======= 
    @Test
    void Given_validLocationId_When_copyLocationToItself_Then_fail() {              
        testHelper.copyLocation(testProject.ownerCompany.asAdmin, location2A.getId(), location2A.getId(), r -> { r.statusCode(HttpStatus.BAD_REQUEST.value()); return null; }); 
    }

    @Test
    void Given_validLocationId_When_copyLocationToChildLocation_Then_fail() {              
        testHelper.copyLocation(testProject.ownerCompany.asAdmin, location2A.getId(), location3A.getId(), r -> { r.statusCode(HttpStatus.BAD_REQUEST.value()); return null; }); 
    }

    @Test
    void Given_invalidSourceLocationId_When_copyLocation_Then_fail() {              
        testHelper.copyLocation(testProject.ownerCompany.asAdmin, -1, location2A.getId(), r -> { r.statusCode(HttpStatus.FORBIDDEN.value()); return null; });
    }        
    
    @Test
    void Given_invalidDestinationLocationId_When_copyLocation_Then_fail() {              
        testHelper.copyLocation(testProject.ownerCompany.asAdmin, location2A.getId(), -1, r -> { r.statusCode(HttpStatus.BAD_REQUEST.value()); return null; }); 
    }
    
    // ======= MOVE Locations =======     
    @Test
    void Given_validLocationId_When_moveLocationToSameParent_Then_fail() {              
        testHelper.moveLocation(testProject.ownerCompany.asAdmin, location2A.getId(), location1A.getId(), r -> { r.statusCode(HttpStatus.BAD_REQUEST.value()); return null; }); 
    }    
    
    @Test
    void Given_validLocationId_When_moveLocationToChildLocation_Then_fail() {              
        testHelper.moveLocation(testProject.ownerCompany.asAdmin, location1A.getId(), location2A.getId(), r -> { r.statusCode(HttpStatus.BAD_REQUEST.value()); return null; }); 
    }    
    
    @Test
    void Given_rootLocation_When_moveLocation_Then_fail() {              
        testHelper.moveLocationOrder(testProject.ownerCompany.asAdmin, testProject.ownerCompany.topLocationId, 0, r-> {
            TestAssert.assertError(HttpStatus.BAD_REQUEST, LocationException.CAN_NOT_MOVE_ROOT_LOCATION.getErrorCode(), r);
            return null;
        });
    }
}
