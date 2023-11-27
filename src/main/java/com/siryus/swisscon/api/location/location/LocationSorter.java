package com.siryus.swisscon.api.location.location;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class LocationSorter {

    private LocationRepository locationRepository;

    LocationSorter(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    List<Location> updateSiblingsOrder(List<Location> siblings) {
        IntStream.range(0, siblings.size()).forEach(i -> siblings.get(i).setOrder(i));
        return locationRepository.saveAll(siblings);
    }

    void updateSiblingsOnRemove(Location locationToRemove) {
        List<Location> siblings = findSiblings(locationToRemove);
        updateSiblingsOrder(siblings);
    }

    void updateSiblingsOnAdd(Location locationToAdd) {
        List<Location> siblings = findSiblings(locationToAdd);
        siblings.add(locationToAdd);
        updateSiblingsOrder(siblings);
    }

    private List<Location> findSiblings(Location modifiedLocation) {
        List<Integer> filteredSiblings = locationRepository
                .findChildrenIdsByParent(modifiedLocation.getParentId())
                .stream()
                .filter(id -> !modifiedLocation.getId().equals(id))
                .collect(Collectors.toList());
        return locationRepository.findAllByIdOrderByOrder(filteredSiblings);
    }


}
