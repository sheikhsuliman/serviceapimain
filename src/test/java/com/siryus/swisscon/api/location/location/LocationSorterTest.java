package com.siryus.swisscon.api.location.location;

import com.siryus.swisscon.api.base.TestAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocationSorterTest {

    private static List<Location> siblings;
    private static Location addedLocation;

    private final LocationRepository locationRepository = Mockito.mock(LocationRepository.class);
    private final LocationSorter locationSorter = new LocationSorter(locationRepository);

    @BeforeEach
    public void initTestData() {
        Location root = Location.builder().id(1).name("root").build();
        Location a = Location.builder().id(2).parentId(1).parent(root).name("A").build();
        Location b = Location.builder().id(3).parentId(1).parent(root).name("B").build();
        Location c = Location.builder().id(4).parentId(1).parent(root).name("C").build();
        addedLocation = Location.builder().parentId(1).id(5).parent(root).name("D").build();
        siblings = new ArrayList<>(Arrays.asList(a, b, c));
        List<Integer> siblingIds = siblings.stream().map(Location::getId).collect(Collectors.toList());

        Mockito.when(locationRepository.findChildrenIdsByParent(1)).thenReturn(siblingIds);

        Mockito.when(locationRepository.findAllByIdOrderByOrder(Mockito.anyIterable())).then(
                (Answer<List<Location>>) invocation -> {
                    List<Integer> ids = invocation.getArgument(0);
                    return siblings.stream().filter(s->ids.contains(s.getId())).collect(Collectors.toList());
                }
        );
    }

    @Test
    public void Given_locationsWithoutOrder_WhenUpdateSiblingsOrder_Then_OrderAssigned() {
        List<Location> orderedLocations = locationSorter.updateSiblingsOrder(siblings);
        TestAssert.assertLocationOrder(orderedLocations);
    }

    @Test
    public void Given_locationToRemove_WhenUpdateSiblingsOrderForRemove_Then_OrderUpdated() {
        Mockito.when(locationRepository.findChildrenIdsByParent(1))
                .thenReturn(siblings.stream().map(Location::getId).collect(Collectors.toList()));
        locationSorter.updateSiblingsOnRemove(siblings.get(1));
        assertEquals(0, siblings.get(0).getOrder());
        assertEquals(1, siblings.get(2).getOrder());
    }

    @Test
    public void Given_locationToMoveWhichHasAnExistingOrder_WhenUpdateSiblingsOrderToAdd_Then_OrderUpdated() {
        siblings.get(0).setOrder(0);
        siblings.get(1).setOrder(1);
        siblings.get(2).setOrder(2);
        addedLocation.setOrder(1);
        Mockito.when(locationRepository.findChildrenIdsByParent(1))
                .thenReturn(siblings.stream().map(Location::getId).collect(Collectors.toList()));
        locationSorter.updateSiblingsOnAdd(addedLocation);
        assertEquals(0, siblings.get(0).getOrder());
        assertEquals(1, siblings.get(1).getOrder());
        assertEquals(2, siblings.get(2).getOrder());
        assertEquals(3, addedLocation.getOrder());
    }


}
