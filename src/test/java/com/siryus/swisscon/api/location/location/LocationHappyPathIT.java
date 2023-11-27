package com.siryus.swisscon.api.location.location;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.base.TestHelper.ExtendedTestProject;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FailFastExtension.class)
public class LocationHappyPathIT extends AbstractMvcTestBase {

    private TestHelper.ExtendedTestProject testProject;
    private LocationDetailsDTO location1A;
    private LocationDetailsDTO location2A;
    private LocationDetailsDTO location3A;
    private LocationDetailsDTO location3B;
    private LocationDetailsDTO location2B;
    private LocationDetailsDTO location2C;
    
    private final String ANOTHER_PROJECT_COMPANY_NAME = "TestOwnerCompany";
    private final String ANOTHER_CONTRACTOR_COMPANY_NAME = "TestContractorCompany";
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
    void Given_validProjectId_When_getRootLocations_Then_onlyRootLocationIsReturned() {
        List<LocationDetailsDTO> rootNodes = testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.projectId, null);
        
        assertNotNull(rootNodes);
        assertEquals(rootNodes.size(), 1);
    }
    
    @Test
    void Given_validParentIdWithChildren_When_getChildren_Then_onlyDirectChildrenAreReturned() {                
        assertHasElements(testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.projectId, location1A.getId()), location2A, location2B, location2C);
    }    
    
    @Test
    void Given_validParentIdWithoutChildren_When_getChildren_Then_receiveEmptyResult() {
        List<LocationDetailsDTO> children = testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.projectId, location2B.getId());
        
        assertEquals(0, children.size());
    }
    
    @Test
    void Given_validProjectIdWithoutChildren_When_getChildren_Then_receiveOnlyDefaultTopLocation() {
        ExtendedTestProject project = testHelper.createExtendedProject(ANOTHER_PROJECT_COMPANY_NAME, ANOTHER_CONTRACTOR_COMPANY_NAME);

        // you cannot access another's project locations
        testHelper.getChildren(testProject.ownerCompany.asAdmin, project.projectId, null, r-> {
            r.assertThat().statusCode(HttpStatus.FORBIDDEN.value());
            return null;
        });

        List<LocationDetailsDTO> children = testHelper.getChildren(project.ownerCompany.asOwner, project.projectId, null);
        
        assertEquals(1, children.size());
    }

    // ======= UPDATE Locations =======
    
    @Test
    void Given_validLocationId_When_validUpdateRequest_Then_updateIsSuccesful() {
        File image = testHelper.fileUpload(testProject.ownerCompany.asAdmin, ReferenceType.LOCATION, location3B.getId());
        File layout = testHelper.fileUpload(testProject.ownerCompany.asAdmin, ReferenceType.LOCATION, location3B.getId());
        
        LocationUpdateDTO update = LocationUpdateDTO.builder()
                                                       .id(location3B.getId())
                                                       .name(UPDATED_LOCATION_NAME)
                                                       .description(UPDATED_LOCATION_DESCRIPTION)
                                                       .height(BigDecimal.TEN)
                                                       .unit(UNIT_METRE)
                                                       .imageFileId(image.getId())
                                                       .layoutFileId(layout.getId())
                                                       .build();
        
        LocationDetailsDTO result = testHelper.updateLocation(testProject.ownerCompany.asAdmin, update);        
        
        testAssert.assertLocationUpdateIsCorrect(result, update);
    }

    // ======= DELETE Locations =======
    
    @Test
    void Given_validLocationWithoutChildren_When_deleteRequest_Then_success() {
        testHelper.deleteLocation(testProject.ownerCompany.asAdmin, location2C.getId());
        
        assertHasElements(testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.projectId, location1A.getId()), location2A, location2B);
    }

    // ======= EXPAND Locations =======    
    @Test
    void Given_validLocationId_When_expandedRequest_Then_minimalTreeWithExpandedNodesIsReturned() {              
        LocationDetailsDTO rootNode = testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.projectId, null).get(0);
        
        assertHasElements(
            flattenTree(testHelper.getExpandedTree(testProject.ownerCompany.asAdmin, location2C.getId()).get(0)), 
            rootNode, location1A, location2A, location2B, location2C
        );
        
        assertHasElements(
            flattenTree(testHelper.getExpandedTree(testProject.ownerCompany.asAdmin, location3A.getId()).get(0)), 
            rootNode, location1A, location2A, location2B, location2C, location3A, location3B
        );
    }
    
    // ======= COPY Locations ======= 
    @Test
    void Given_validLocationId_When_copyLocationWithoutChildren_Then_locationIsCopied() {
        LocationDetailsDTO copyLocation2B = testHelper.copyLocation(testProject.ownerCompany.asAdmin, location2B.getId(), location2A.getId());

        assertNotNull(copyLocation2B);
        
        assertHasElements(testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.projectId, location2A.getId()),
            location3A, location3B, copyLocation2B);
        
        assertEquals(0, testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.projectId, copyLocation2B.getId()).size());
    }
    
    @Test
    void Given_validLocationId_When_copyLocationWithChildren_Then_locationAndchildrenAreCopied() {
        LocationDetailsDTO copyLocation2A = testHelper.copyLocation(testProject.ownerCompany.asAdmin, location2A.getId(), location1A.getId());

        assertNotNull(copyLocation2A);
        
        assertHasElements(testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.projectId, location1A.getId()),
            location2A, location2B, location2C, copyLocation2A);

        List<LocationDetailsDTO> childrenCopies = testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.projectId, copyLocation2A.getId());
        
        assertEquals(2, childrenCopies.size());
    }

    // ======= MOVE Locations =======      
    
    @Test
    void Given_validLocationId_When_moveLocationWithoutChildren_Then_locationIsMovedWithOrder() {
        testHelper.moveLocation(testProject.ownerCompany.asAdmin, location3A.getId(), location2B.getId());
        testHelper.moveLocation(testProject.ownerCompany.asAdmin, location3B.getId(), location2B.getId());

        Map<Integer, LocationDetailsDTO> locations = toMap(testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.ownerCompany.projectId, location2B.getId()));
        
        assertHasElements(locations.values(), location3A, location3B);
        
        assertEquals(0, locations.get(location3A.getId()).getOrder());
        assertFalse(locations.get(location3A.getId()).isHasChildren());
        
        assertEquals(1, locations.get(location3B.getId()).getOrder());
        assertFalse(locations.get(location3B.getId()).isHasChildren());
        
        locations = toMap(testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.ownerCompany.projectId, location1A.getId()));
        
        assertTrue(!locations.get(location2A.getId()).isHasChildren());
        assertTrue(locations.get(location2B.getId()).isHasChildren());
    }

    @Test
    void Given_validLocationId_When_moveLocationWithChildren_Then_locationAndChildrenAreMoved() {
        testHelper.moveLocation(testProject.ownerCompany.asAdmin, location2A.getId(), location2B.getId());

        assertHasElements(testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.ownerCompany.projectId, location2B.getId()), location2A);
        
        assertHasElements(testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.ownerCompany.projectId, location2A.getId()), 
            location3A, location3B
        );
    }

    @Test
    void Given_validLocationId_When_moveLocationWithChildrenToIndex_Then_locationAndChildrenAreMovedWithOrder() {
        testHelper.moveLocationOrder(testProject.ownerCompany.asAdmin, location2A.getId(), 2);

        Map<Integer, LocationDetailsDTO> children1A = toMap(testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.ownerCompany.projectId, location1A.getId()));        
        assertHasElements(children1A.values(), location2B, location2A, location2C);
        assertEquals(0, children1A.get(location2B.getId()).getOrder());
        assertEquals(1, children1A.get(location2A.getId()).getOrder());
        assertEquals(2, children1A.get(location2C.getId()).getOrder());
    }
    
    @Test
    void Given_validLocationId_When_moveLocationWithChildrenToLastIndex_Then_locationAndChildrenAreMovedWithOrder() {
        testHelper.moveLocationOrder(testProject.ownerCompany.asAdmin, location2A.getId(), 3);

        Map<Integer, LocationDetailsDTO> children1A = toMap(testHelper.getChildren(testProject.ownerCompany.asAdmin, testProject.ownerCompany.projectId, location1A.getId()));        
        assertHasElements(children1A.values(), location2B, location2A, location2C);
        assertEquals(0, children1A.get(location2B.getId()).getOrder());
        assertEquals(1, children1A.get(location2C.getId()).getOrder());
        assertEquals(2, children1A.get(location2A.getId()).getOrder());
    }
    
    private void assertHasElements(LocationDetailsDTO[] container, LocationDetailsDTO ...elements) {
        Set<Integer> containerIds = new HashSet<>(Arrays.asList(container).stream().map(e -> e.getId()).collect(Collectors.toList()));
        Set<Integer> elementIds = new HashSet<>(Arrays.asList(elements).stream().map(e -> e.getId()).collect(Collectors.toList()));
        
        assertTrue(containerIds.equals(elementIds));
    }
    
    private void assertHasElements(Collection<? extends LocationDetailsDTO> container, LocationDetailsDTO ...elements) {
        Set<Integer> containerIds = new HashSet<>(container.stream().map(e -> e.getId()).collect(Collectors.toList()));
        Set<Integer> elementIds = new HashSet<>(Arrays.asList(elements).stream().map(e -> e.getId()).collect(Collectors.toList()));
        
        assertTrue(containerIds.equals(elementIds));    
    }
    
    private LocationDetailsDTO findChild(LocationDetailsDTO copyLocation2A, LocationDetailsDTO[] location1AChildren) {
        return Arrays.stream(location1AChildren)
                .filter(c -> c.getId().equals(copyLocation2A.getId()))
                .findFirst().orElseThrow();
    }    
    
    private List<LocationTreeDTO> flattenTree(LocationTreeDTO tree) {
        if (tree.children.isEmpty()) {
            return Collections.singletonList(tree);
        }
        
        List<LocationTreeDTO> r = tree.children.stream()
            .map(child -> flattenTree(child))
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        r.add(tree);
        return r;
    }

    private Map<Integer, LocationDetailsDTO> toMap(List<LocationDetailsDTO> locations) {
        return locations
                .stream()
                .collect(Collectors.toMap(LocationDetailsDTO::getId, location -> location));
    }
}
