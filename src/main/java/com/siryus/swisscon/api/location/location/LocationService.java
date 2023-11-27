package com.siryus.swisscon.api.location.location;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.general.favorite.FavoriteRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.general.unit.UnitService;
import com.siryus.swisscon.api.mediawidget.MediaWidgetService;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.project.project.ProjectRepository;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.DTOValidator;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.siryus.swisscon.api.general.reference.ReferenceType.TEMPORARY;

@Service("locationService")
@Validated
public class LocationService {
    public static final String LOCATION = ReferenceType.LOCATION.name();

    private final FavoriteRepository favoriteRepository;
    private final ProjectRepository projectRepository;
    private final LocationRepository locationRepository;
    private final FileService fileService;
    private final UnitService unitService;
    private final MediaWidgetService mediaWidgetService;
    private final LocationSorter locationSorter;
    private final EventsEmitter eventsEmitter;
    private final SecurityHelper securityHelper;

    @Autowired
    public LocationService(
            FavoriteRepository favoriteRepository,
            ProjectRepository projectRepository,
            LocationRepository locationRepository,
            FileService fileService,
            UnitService unitService,
            MediaWidgetService mediaWidgetService,
            EventsEmitter eventsEmitter,
            SecurityHelper securityHelper) {
        this.favoriteRepository = favoriteRepository;
        this.projectRepository = projectRepository;
        this.locationRepository = locationRepository;
        this.fileService = fileService;
        this.unitService = unitService;
        this.mediaWidgetService = mediaWidgetService;
        this.locationSorter = new LocationSorter(locationRepository);
        this.eventsEmitter = eventsEmitter;
        this.securityHelper = securityHelper;
    }

    /**
     * Adds a location as a child based on id of parent location (not location node!) OR project id
    *
     * If the id of the parent is present then it will be used, otherwise it will use project id.
     * If no root node exists, then one is created and data is added as a first child of that node.
     *
     * @param request actual data which can include a project id
     * @return newly created location
     */
    @Transactional(rollbackFor=Exception.class)
    public LocationDetailsDTO create(@Valid LocationCreateDTO request) {
        LocationCreateDTO validRequest = validateRequest(request);

        Location parent = null;

        if (null != validRequest.getParent()) {
            parent = getValidLocation(validRequest.getParent());
            validRequest.setProjectId(parent.getProject().getId());
        } else if (null == validRequest.getProjectId()) {
            throw LocationException.missingProjectOrLocationId();
        }

        Location location =  createLocationEntity(validRequest, parent);
        locationSorter.updateSiblingsOnAdd(location);
        final LocationDetailsDTO dto = LocationDetailsDTO.from(location, this::hasChildren);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .projectId(location.getProject().getId())
                .notificationType(NotificationType.LOCATION_CREATED)
                .referenceId(location.getProject().getId())
                .subjectId(dto.getId())
                .senderId(securityHelper.currentUserId())
                .build());

        return dto;
    }

    @Transactional
    public void createTopLocation(Project project, String name) {
        createLocationEntity(
                LocationCreateDTO.builder()
                        .projectId(project.getId())
                        .name(name)
                        .build(),
                null
        );
    }

    public LocationDetailsDTO getLocation(@Reference(ReferenceType.LOCATION) Integer locationId) {
        return LocationDetailsDTO.from(
                loadExistingLocation(locationId),
                this::hasChildren,
                this::calculatePathForLocation,
                null
        );
    }

    private Location loadExistingLocation(Integer locationId) {
        return locationRepository.findById(locationId).orElseThrow(() -> LocationException
                        .locationNotFound(locationId));
    }

    /**
     * Updates the basic fields of a location.
     * Fields which will not be updated are the parent, hasChildren and the project
     */
    @Transactional
    public LocationDetailsDTO update(@Valid LocationUpdateDTO locationInfo) {
        Location locationEntity = getValidLocation(locationInfo.getId());
        Integer oldDefaultImageId = Optional.ofNullable(locationEntity.getDefaultImage()).map(File::getId).orElse(null);
        Integer oldDefaultPlanId = Optional.ofNullable(locationEntity.getDefaultPlan()).map(File::getId).orElse(null);

        updateEntity(locationEntity, locationInfo);

        deleteOldImagesIfNeeded(locationInfo, oldDefaultImageId, oldDefaultPlanId);

        final LocationDetailsDTO dto = LocationDetailsDTO.from(locationRepository.save(locationEntity), this::hasChildren);
        eventsEmitter.emitCacheUpdate(ReferenceType.LOCATION, dto.getId());
        eventsEmitter.emitNotification(NotificationEvent.builder()
                .projectId(locationEntity.getProject().getId())
                .notificationType(NotificationType.LOCATION_UPDATED)
                .referenceId(locationEntity.getProject().getId())
                .subjectId(locationInfo.getId())
                .senderId(securityHelper.currentUserId())
                .build());

        return dto;
    }

    private void deleteOldImagesIfNeeded(LocationUpdateDTO locationInfo, Integer oldDefaultImageId, Integer oldDefaultPlanId) {

        if (! (oldDefaultImageId == null || Objects.equals(oldDefaultImageId, locationInfo.getImageFileId()))) {
            fileService.disable(oldDefaultImageId);
        }

        if (! (oldDefaultPlanId == null || Objects.equals(oldDefaultPlanId, locationInfo.getLayoutFileId()))) {
            fileService.disable(oldDefaultImageId);
        }
    }

    private void updateEntity(Location locationEntity, LocationUpdateDTO locationInfo) {
        List<Integer> ids = new ArrayList<>();
        File defaultImage = validateFileId(locationInfo.getImageFileId());
        Optional.ofNullable(defaultImage).ifPresent(i->ids.add(locationInfo.getImageFileId()));
        File defaultPlan = validateFileId(locationInfo.getLayoutFileId());
        Optional.ofNullable(defaultPlan).ifPresent(i->ids.add(locationInfo.getLayoutFileId()));

        locationEntity.setName(locationInfo.getName());
        locationEntity.setDescription(locationInfo.getDescription());
        locationEntity.setHeight(locationInfo.getHeight());
        locationEntity.setWidth(locationInfo.getWidth());
        locationEntity.setLength(locationInfo.getLength());
        locationEntity.setSurface(locationInfo.getSurface());
        locationEntity.setVolume(locationInfo.getVolume());
        locationEntity.setUnit(validateUnit(locationInfo.getUnit(), Unit::getLength));
        locationEntity.setSurfaceUnit(validateUnit(locationInfo.getSurfaceUnit(), Unit::getSurface));
        locationEntity.setVolumeUnit(validateUnit(locationInfo.getVolumeUnit(), Unit::getVolume));
        locationEntity.setDefaultImage(defaultImage);
        locationEntity.setDefaultPlan(defaultPlan);

        fileService.updateFileReferences(ids, locationEntity.getId(), ReferenceType.LOCATION);
    }

    private File validateFileId(Integer fileId)  {
        if (fileId == null) {
            return null;
        }

        File file = fileService.findById(fileId);

        if (null == file || !referenceIdIsValid(file) || !createdByIsValid(file)) {
            throw LocationException.fileNotFound(fileId);
        }
        return file;
    }

    private boolean referenceIdIsValid(File file) {
        return file.getReferenceType().equals(TEMPORARY.name()) || file.getReferenceId() != null;
    }

    private boolean createdByIsValid(File file) {
        return file.getCreatedBy().toString().equals(LecwUtils.currentUser().getId());
    }

    public List<LocationDetailsDTO> getChildLocationsBy(@Reference(ReferenceType.PROJECT) Integer projectId,
                                                        @Reference(ReferenceType.LOCATION)Integer parentLocationId) {
        List<Location> children;
        if (parentLocationId != null) {
            if(!projectId.equals(getValidLocation(parentLocationId).getProject().getId())) {
                throw LocationException.parentLocationIdHasToBelongToProject(parentLocationId, projectId);
            }
            children = getChildLocationsByParent(parentLocationId);
        } else {
            children = getChildLocationsByProject(projectId);
        }
        AtomicInteger naturalOrder = new AtomicInteger(0);
        return children.stream()
                .map(location -> LocationDetailsDTO.from(location, this::hasChildren, naturalOrder))
                .collect(Collectors.toList());
    }

    private List<Location> getChildLocationsByParent(Integer locationId) {
        Location parentLocation = getValidLocation(locationId);

        List<Integer> children = locationRepository.findChildrenIdsByParent(parentLocation.getId());

        return locationRepository.findAllByIdOrderByOrder(children);
    }

    protected List<Location> getChildLocationsByProject(Integer projectId) {
        Location root = locationRepository
                .findRootByProject(projectId)
                .orElseThrow(() -> LocationException.canNotFindRootLocation(projectId));
        return Collections.singletonList(root);
    }

    /**
     * Returns all descendent locations (non-disabled) of given parent location
     */
    public List<Location> getAllLocationsByParent(Integer parentLocationId) {
        Location parentLocation = getValidLocation(parentLocationId);

        // recursive stop
        if(!hasChildren(parentLocation)) {
            return Collections.singletonList(parentLocation);
        }

        final List<Location> children = new ArrayList<>();

        locationRepository
                .findChildrenIdsByParent(parentLocationId)
                .stream()
                .map(this::getAllLocationsByParent)
                .forEach(children::addAll);

        return children;
    }

    /**
     * Returns all descendent location id's (non-disabled) of given parent location
     */
    public List<Integer> getDescendentIDsByParent(Integer parentLocationId) {
        Location parentLocation = getValidLocation(parentLocationId);

        // recursive stop
        if(!hasChildren(parentLocation)) {
            return Collections.singletonList(parentLocationId);
        }

        final List<Integer> children = new ArrayList<>();

        locationRepository
                .findChildrenIdsByParent(parentLocationId)
                .stream()
                .map(this::getDescendentIDsByParent)
                .forEach(children::addAll);

        return children;
    }

    /**
     * Disables a location and removes associated tree node structures
     *
     * @param locationId Location Id
     */
    void delete(@Reference(ReferenceType.LOCATION) Integer locationId) {
        // Nick 11.11.2019 - why doesn't this have a mechanism to throw exceptions? Delete operations can fail
        Location location = getValidLocation(locationId);
        if (null == location) {
            throw LocationException.locationNotFound(locationId);
        }

        deleteLocation(location);
        eventsEmitter.emitCacheUpdate(ReferenceType.LOCATION, locationId);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .projectId(location.getProject().getId())
                .notificationType(NotificationType.LOCATION_ARCHIVED)
                .referenceId(location.getProject().getId())
                .subjectId(locationId)
                .senderId(securityHelper.currentUserId())
                .build());
    }

    /**
     * Removes (disables) a location and removes (deletes) the associated tree node information.
     *
     * Checks whether conditions for deleting a location are met:
     *  - location is a leaf node
     */
    @Transactional(rollbackFor=Exception.class)
    public void deleteLocation(Location location) {
        validateLocationCanBeDeleted(location);

        deleteAssociatedMedia(location);

        locationSorter.updateSiblingsOnRemove(location);

        // Delete favorites
        favoriteRepository.removeForAllUsers(location.getId(), ReferenceType.LOCATION.toString());

        // Delete location data
        locationRepository.disable(location.getId());
    }

    private void validateLocationCanBeDeleted(Location location) {
        if (null == location) {
            throw LocationException.canNotDeleteNullLocation();
        }

        if (null != location.getDisabled()) {
            throw LocationException.canNotDeleteDisabledLocation();
        }

        if (location.getParent() == null) {
            throw LocationException.canNotDeleteTopLocation();
        }

        if (hasChildren(location)) {
            throw LocationException.canNotDeleteNonLeafLocation();
        }
    }

    private void deleteAssociatedMedia(Location location) {
        Optional.ofNullable(location.getDefaultImage()).ifPresent(fileService::disable);
        Optional.ofNullable(location.getDefaultPlan()).ifPresent(fileService::disable);

        mediaWidgetService.deleteAllOwnedBy(ReferenceType.LOCATION, location.getId());
    }

    /**
     * Returns all Locations from minimal tree which includes location with given id.
     * <p>
     * Minimal tree is the tree which includes all nodes between target node and root and all
     * immediate children of these nodes.
     */
    List<LocationTreeDTO> getExpandedTree(@Reference(ReferenceType.LOCATION) Integer locationId) {
        Location location = getValidLocation(locationId);

        Map<Integer, List<LocationTreeDTO>> parentLocationIdToChildrenMap = new HashMap<>();
        List<Integer> parentLocationIDs = new ArrayList<>();
        if (location.getParentId() != null) {
            Optional<Location> parentLocation = locationRepository.findById(location.getParentId());


            while (parentLocation.isPresent()) {
                List<LocationTreeDTO> locationNodeChildren = getChildLocationsByParent(parentLocation.get().getId())
                        .stream()
                        .map(child->LocationTreeDTO.from(child, hasChildren(child)))
                        .collect(Collectors.toList());


                parentLocationIdToChildrenMap.put(parentLocation.get().getId(), locationNodeChildren);

                parentLocationIDs.add(0, parentLocation.get().getId());
                if (parentLocation.get().getParentId() == null) {
                    parentLocation = Optional.empty();
                } else {
                    parentLocation = locationRepository.findById(parentLocation.get().getParentId());
                }
            }

            for (Integer parent : parentLocationIDs) {
                List<LocationTreeDTO> parentChildren = parentLocationIdToChildrenMap.get(parent);

                for (LocationTreeDTO parentChild : parentChildren) {
                    parentChild.children = parentLocationIdToChildrenMap.computeIfAbsent(parentChild.id, ArrayList::new);
                }
                parentLocationIdToChildrenMap.put(parent, parentChildren);
            }
        }

        Location root = locationRepository.findRootByProject(location.getProject().getId())
                .orElseThrow(()-> LocationException.canNotFindRootLocation(location.getProject().getId()));
        LocationTreeDTO dto = LocationTreeDTO.from(root, hasChildren(root));
        dto.children = parentLocationIdToChildrenMap.computeIfAbsent(root.getId(), ArrayList::new);
        return Collections.singletonList(dto);
    }

    @Transactional
    public LocationDetailsDTO copy(@Reference(ReferenceType.LOCATION) Integer srcLocationId, @Reference(ReferenceType.LOCATION) Integer targetParentLocationId) {
        validateParameters(srcLocationId, targetParentLocationId);

        Location srcLocation = getValidLocation(srcLocationId);

        final LocationDetailsDTO dto = LocationDetailsDTO.from(
                copyNonRootLocation(srcLocation, targetParentLocationId),
                this::hasChildren,
                this::calculatePathForLocation,
                null
        );
        eventsEmitter.emitNotification(NotificationEvent.builder()
                .projectId(srcLocation.getProject().getId())
                .notificationType(NotificationType.LOCATION_COPIED)
                .referenceId(srcLocation.getProject().getId())
                .subjectId(dto.getId())
                .senderId(securityHelper.currentUserId())
                .build());
        return dto;
    }

    @Transactional
    public LocationDetailsDTO move(Integer srcLocationId, Integer targetParentLocationId) {
        validateParameters(srcLocationId, targetParentLocationId);

        Location srcLocation = getValidLocation(srcLocationId);

        validateMoveHasNotSameParent(targetParentLocationId, srcLocation);

        final LocationDetailsDTO dto = LocationDetailsDTO.from(
                moveNonRootLocation(srcLocation, targetParentLocationId),
                this::hasChildren,
                this::calculatePathForLocation,
                null
        );
        eventsEmitter.emitCacheUpdate(ReferenceType.LOCATION, srcLocationId);
                eventsEmitter.emitNotification(NotificationEvent.builder()
                .projectId(srcLocation.getProject().getId())
                .notificationType(NotificationType.LOCATION_MOVED)
                .referenceId(srcLocation.getProject().getId())
                .subjectId(dto.getId())
                .senderId(securityHelper.currentUserId())
                .build());
        return dto;
    }

    @Transactional
    public List<LocationDetailsDTO> moveOrder(Integer targetLocationId, Integer order) {
        Location targetLocation = loadExistingLocation(targetLocationId);

        if(targetLocation.getParentId() == null) {
            throw LocationException.canNotMoveRootLocation();
        }

        List<Location> reOrderedSiblingLocations = new ArrayList<>();
        AtomicInteger naturalOrder = new AtomicInteger(0);
        AtomicBoolean targetLocationNotAdded = new AtomicBoolean(true);

        locationRepository.findAllByIdOrderByOrder(locationRepository.findChildrenIdsByParent(targetLocation.getParentId()))
                .stream()
                .map( l -> ensureNaturalOrderIfNoOrder(l, naturalOrder))
                .filter(l -> !l.getId().equals(targetLocationId))
                .forEach( l -> {
                    if(l.getOrder() >= order && targetLocationNotAdded.get()) {
                        reOrderedSiblingLocations.add(targetLocation);
                        targetLocationNotAdded.set(false);
                    }
                    reOrderedSiblingLocations.add(l);
                });

        if (targetLocationNotAdded.get()) {
            reOrderedSiblingLocations.add(targetLocation);
        }

        return locationSorter
                .updateSiblingsOrder(reOrderedSiblingLocations)
                .stream()
                .map(l -> LocationDetailsDTO.from(l, this::hasChildren))
                .peek(dto->eventsEmitter.emitCacheUpdate(ReferenceType.LOCATION, dto.getId()))
                .collect(Collectors.toList());
    }

    private Location ensureNaturalOrderIfNoOrder(Location location, AtomicInteger naturalOrder) {
        if ((location.getOrder() == null) || location.getOrder().equals(0)) {
            location.setOrder(naturalOrder.getAndIncrement());
        }
        else {
            naturalOrder.set(location.getOrder()+1);
        }
        return location;
    }

    private Location copyNonRootLocation(Location srcLocation, Integer targetParentLocationId) {
        targetParentLocationId = Optional.ofNullable(targetParentLocationId)
                .orElse(locationRepository
                        .findRootByProject(srcLocation.getProject().getId())
                        .orElseThrow(() -> LocationException.canNotFindRootLocation(srcLocation.getProject().getId()))
                        .getId());

        Location parent = validateAndGetParentForCopyAndMove(srcLocation, targetParentLocationId);

        Location copyOfSrcLocation = locationRepository.save(
                srcLocation.toBuilder()
                        .id(null)
                        .parentId(targetParentLocationId)
                        .parent(parent)
                        .build()
        );

        recursivelyCopyLocation(srcLocation.getId(), copyOfSrcLocation.getId());

        locationSorter.updateSiblingsOnAdd(copyOfSrcLocation);
        return copyOfSrcLocation;
    }

    private Location moveNonRootLocation(Location srcLocation, Integer targetParentLocationId) {
        targetParentLocationId = Optional.ofNullable(targetParentLocationId)
                .orElse(locationRepository
                        .findRootByProject(srcLocation.getProject().getId())
                        .orElseThrow(() -> LocationException.canNotFindRootLocation(srcLocation.getProject().getId()))
                        .getId());

        locationSorter.updateSiblingsOnRemove(srcLocation);

        Location parent = validateAndGetParentForCopyAndMove(srcLocation, targetParentLocationId);

        srcLocation.setParentId(targetParentLocationId);
        srcLocation.setParent(parent);


        locationSorter.updateSiblingsOnAdd(srcLocation);

        return locationRepository.save(srcLocation);
    }

    private void validateMoveHasNotSameParent(Integer targetParentLocationId, Location srcLocation) {
        if (targetParentLocationId.equals(srcLocation.getParentId())) {
            throw LocationException.locationCannotBeMovedToTheSameParent(srcLocation.getId(), targetParentLocationId);
        }
    }

    private Location validateAndGetParentForCopyAndMove(Location srcLocation, Integer targetParentLocationId) {
        Location targetParentLocation = getValidLocation(targetParentLocationId);

        validateLocationsAreFromTheSameProject(srcLocation, targetParentLocation);

        validTargetIsNotChildOfSrc(srcLocation, targetParentLocation);

        return targetParentLocation;
    }

    private void validateParameters(Integer srcLocationId, Integer targetParentLocationId) {
        if (null == srcLocationId) {
            throw LocationException.locationIdCanNotBeNull();
        }

        if (srcLocationId.equals(targetParentLocationId)) {
            throw LocationException.canNotCopyToSameLocation(srcLocationId);
        }

    }

    public Location getValidLocation(Integer locationId) {
        return getValidLocation(locationId, false);
    }
    public Location getValidLocation(Integer locationId, boolean canBeDisabled) {
        Location result = loadExistingLocation(locationId);

        if ((!canBeDisabled) && (null != result.getDisabled())) {
            throw LocationException.locationNotFound(locationId);
        }

        return result;
    }

    private void validateLocationsAreFromTheSameProject(Location location1, Location location2) {
        if (!location1.getProject().getId().equals(location2.getProject().getId())) {
            throw LocationException.locationsShouldBeFromSameProject();
        }
    }

    private void validTargetIsNotChildOfSrc(Location src, Location parentTarget) {
        Location locationToCheck = parentTarget;

        // Make sure that dest is not a child of src
        while(locationToCheck.getParentId() != null) {
            if (locationToCheck.getParentId().equals(src.getId())) {
                throw LocationException.canNotCopyToChildLocation(src.getId(), parentTarget.getId());
            }

            locationToCheck = getValidLocation(locationToCheck.getParentId());
        }
    }

    /**
     * Actual create method.
     *
     * Performs a series of business logic checks on data:
     * - file ids must belong to resources created by the current user
     * - project id must exist.
     *
     * In addition File.referenceId is updated for all relevant resources
     *
     * @param locationData Location Create DTO
     * @param parentLocation Parent location
     * @return newly created location
     */
    private Location createLocationEntity(LocationCreateDTO locationData, Location parentLocation)  {
        // Neither of these should be null!
        Project project = projectRepository.findById(locationData.getProjectId())
                .orElseThrow(()-> LocationException.projectNotFound(locationData.getProjectId()));

        List<Integer> ids = new ArrayList<>();
        File defaultImage = validateFileId(locationData.getImageFileId());
        Optional.ofNullable(defaultImage).ifPresent(i->ids.add(locationData.getImageFileId()));
        File defaultPlan = validateFileId(locationData.getLayoutFileId());
        Optional.ofNullable(defaultPlan).ifPresent(i->ids.add(locationData.getLayoutFileId()));

        Location location = Location.builder()
                .parent(parentLocation)
                .defaultImage(defaultImage)
                .defaultPlan(defaultPlan)
                .project(project)
                .starred(false)
                .name(locationData.getName())
                .description(locationData.getDescription())
                .height(locationData.getHeight())
                .width(locationData.getWidth())
                .length(locationData.getLength())
                .surface(locationData.getSurface())
                .volume(locationData.getVolume())
                .unit(validateUnit(locationData.getUnit(), Unit::getLength))
                .surfaceUnit(validateUnit(locationData.getSurfaceUnit(), Unit::getSurface))
                .volumeUnit(validateUnit(locationData.getVolumeUnit(), Unit::getVolume))
            .build();

        location = locationRepository.save(location);

        fileService.updateFileReferences(ids, location.getId(), ReferenceType.LOCATION);

        mediaWidgetService.createDefaultFolders(ReferenceType.LOCATION, location.getId());

        return location;
    }

    private void recursivelyCopyLocation(Integer srcNodeId, Integer destNodeId) {
        List<Integer> childrenIds = locationRepository.findChildrenIdsByParent(srcNodeId);

        childrenIds.forEach(childId-> {
            Location childLocation = loadExistingLocation(childId);

            Location copyOfChildLocation = locationRepository.save(
                    childLocation.toBuilder()
                            .id(null)
                            .parentId(destNodeId)
                            .parent(Location.builder().id(destNodeId).build())
                            .build()
            );
            if (hasChildren(childLocation)) {
                recursivelyCopyLocation(childLocation.getId(), copyOfChildLocation.getId());
            }
        });
    }

    boolean hasChildren(Location location) {
        return ! locationRepository.findChildrenIdsByParent(location.getId()).isEmpty();
    }

    private LocationCreateDTO validateRequest(LocationCreateDTO request) {
        DTOValidator.validateAndThrow(request);

        if (!(
                unitsAndMeasuresAreNullOrNot(request.getUnit(), request.getHeight(), request.getWidth(), request.getLength()) &&
                unitsAndMeasuresAreNullOrNot(request.getSurfaceUnit(), request.getSurface()) &&
                unitsAndMeasuresAreNullOrNot(request.getVolumeUnit(), request.getVolume())
        )) {
            throw LocationException.invalidMeasurements();
        }

        if (request.getParent() == null) {
            request.setParent(findRootByProjectId(request.getProjectId()));
        }

        return request;
    }

    private Integer findRootByProjectId(Integer projectId) {
        return locationRepository.findRootByProject(projectId)
                .orElseThrow(()->LocationException.canNotFindRootLocation(projectId))
                .getId();
    }

    private boolean unitsAndMeasuresAreNullOrNot(String unit, BigDecimal... measures) {
        boolean allMeasuresAreNull = true;
        for( var measure : measures) {
            allMeasuresAreNull = allMeasuresAreNull && measure == null;
        }

        return allMeasuresAreNull == (unit == null);
    }

    private Unit validateUnit(String symbol, Predicate<Unit> checkUnitKind) {
        Unit result = null;
        if (symbol != null) {
            result = unitService.findBySymbolName(symbol);

            if (result == null) {
                throw LocationException.invalidUnit(symbol);
            }

            if (checkUnitKind != null && ! checkUnitKind.test(result)) {
                throw LocationException.invalidUnit(symbol);
            }
        }
        return  result;
    }

    public List<LocationReferenceDTO> calculatePath(Integer locationId) {
        return calculatePathForLocation(getValidLocation(locationId));
    }

    public List<LocationReferenceDTO> calculatePathForLocation(Location location) {
        LocationReferenceDTO thisLocationDTO = new LocationReferenceDTO(location.getId(), location.getParentId(), location.getName(), location.getType());
        List<LocationReferenceDTO> result = location.getParent() == null ? new ArrayList<>() : calculatePathForLocation(location.getParent());
        result.add(thisLocationDTO);
        return result;
    }

}
