package com.siryus.swisscon.api.location.location;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public class LocationException {

    private static int e(int n) {
        return LocalizedResponseStatusException.LOCATION_ERROR_CODE + n;
    }

    public static final LocalizedReason LOCATION_ID_CAN_NOT_BE_NULL = LocalizedReason.like(e(1), "Location id can not be null");
    public static final LocalizedReason PROJECT_WITH_ID_NOT_FOUND = LocalizedReason.like(e(2), "Project with id = {{projectId}} was not found");
    public static final LocalizedReason LOCATION_WITH_ID_NOT_FOUND = LocalizedReason.like(e(3), "Location with id = {{locationId}} was not found");
    public static final LocalizedReason FILE_WITH_ID_NOT_FOUND = LocalizedReason.like(e(4), "File with id = {{fileId}} was not found");
    public static final LocalizedReason CAN_NOT_UPDATE_DISABLED_LOCATION = LocalizedReason.like(e(5), "Can not update disabled location with id = {{locationId}}");
    public static final LocalizedReason CAN_NOT_COPY_DISABLED_LOCATION = LocalizedReason.like(e(6), "Can not copy disabled location with id = {{locationId}}");
    public static final LocalizedReason CAN_NOT_COPY_TO_CHILD = LocalizedReason.like(e(7), "Can not copy to parent location with id {{srcLocationId}} to one of its children locations {{destLocationId}}");
    public static final LocalizedReason CAN_NOT_COPY_TO_ITSELF = LocalizedReason.like(e(8), "Can not copy location of id {{locationId}} to itself. Source and target IDs must be different.");

    public static final LocalizedReason INVALID_MEASUREMENTS = LocalizedReason.like(e(9), "Invalid measurements information for location");
    public static final LocalizedReason LOCATIONS_SHOULD_BE_FROM_SAME_PROJECT = LocalizedReason.like(e(10), "Source and destination ids must be within the same project");
    public static final LocalizedReason INVALID_UNIT = LocalizedReason.like(e(11), "Unit {{unitSymbol}} not found");
    public static final LocalizedReason MISSING_PROJECT_ID_OR_LOCATION_ID = LocalizedReason.like(e(12), "Missing project id or location id data");

    public static final LocalizedReason LOCATION_TREE_CORRUPTED = LocalizedReason.like(e(13), "Location tree is corrupted : {{message}}");

    public static final LocalizedReason CAN_NOT_DELETE_NULL_LOCATION = LocalizedReason.like(e(14), "Can not delete null location");
    public static final LocalizedReason CAN_NOT_DELETE_DISABLED_LOCATION = LocalizedReason.like(e(15), "Can not delete disabled location");
    public static final LocalizedReason CAN_NOT_DELETE_NON_LEAF_LOCATION = LocalizedReason.like(e(16), "Can not delete non leaf location");
    public static final LocalizedReason CAN_NOT_DELETE_TOP_LOCATION = LocalizedReason.like(e(17), "Can not delete top location");

    public static final LocalizedReason CAN_NOT_FIND_ROOT_LOCATION = LocalizedReason.like(e(18), "Can find root location for project: {{projectId}}");
    public static final LocalizedReason CAN_NOT_MOVE_ROOT_LOCATION = LocalizedReason.like(e(19), "Can not move root location");
    public static final LocalizedReason ORDER_INDEX_OUT_OF_RANGE = LocalizedReason.like(e(20), "Order Index out of Range: {{order}}");
    public static final LocalizedReason PARENT_LOCATION_HAS_TO_BELONG_TO_PROJECT = LocalizedReason.like(e(21), "parent location id: {{locationId}} has to belong to project: {{projectId}}");

    public static final LocalizedReason LOCATION_CANNOT_BE_MOVED_TO_THE_SAME_PARENT = LocalizedReason.like(e(22), "location: {{locationId}} cannot be moved to the same parent: {{parentId}}");

    public static LocalizedResponseStatusException projectNotFound(Integer projectId) {
        return LocalizedResponseStatusException.badRequest(PROJECT_WITH_ID_NOT_FOUND.with(pv("projectId", projectId)));
    }
    public static LocalizedResponseStatusException locationNotFound(Integer locationId) {
        return LocalizedResponseStatusException.badRequest(LOCATION_WITH_ID_NOT_FOUND.with(pv("locationId", locationId)));
    }
    public static LocalizedResponseStatusException fileNotFound(Integer fileId) {
        return LocalizedResponseStatusException.badRequest(FILE_WITH_ID_NOT_FOUND.with(pv("fileId", fileId)));
    }
    public static LocalizedResponseStatusException canNotUpdateDisabledLocation(Integer locationId) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_UPDATE_DISABLED_LOCATION.with(pv("locationId", locationId)));
    }
    public static LocalizedResponseStatusException canNotCopyDisabledLocation(Integer locationId) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_COPY_DISABLED_LOCATION.with(pv("locationId", locationId)));
    }
    public static LocalizedResponseStatusException canNotCopyToChildLocation(Integer srcLocationId, Integer destLocationId) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_COPY_TO_CHILD.with(pv("srcLocationId", srcLocationId), pv("destLocationId", destLocationId)));
    }
    public static LocalizedResponseStatusException canNotCopyToSameLocation(Integer locationId) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_COPY_TO_ITSELF.with(pv("locationId", locationId)));
    }
    public static LocalizedResponseStatusException locationIdCanNotBeNull() {
        return LocalizedResponseStatusException.badRequest(LOCATION_ID_CAN_NOT_BE_NULL.with());
    }

    public static LocalizedResponseStatusException invalidMeasurements() {
        return LocalizedResponseStatusException.badRequest(INVALID_MEASUREMENTS.with());
    }

    public static LocalizedResponseStatusException locationsShouldBeFromSameProject() {
        return LocalizedResponseStatusException.badRequest(LOCATIONS_SHOULD_BE_FROM_SAME_PROJECT.with());
    }

    public static LocalizedResponseStatusException invalidUnit(String unitSymbol) {
        return LocalizedResponseStatusException.badRequest(INVALID_UNIT.with(pv("unitSymbol", unitSymbol)));
    }

    public static LocalizedResponseStatusException missingProjectOrLocationId() {
        return LocalizedResponseStatusException.badRequest(MISSING_PROJECT_ID_OR_LOCATION_ID.with());
    }

    public static LocalizedResponseStatusException locationTreeIsCorrupted(String message) {
        return LocalizedResponseStatusException.internalError(LOCATION_TREE_CORRUPTED.with(pv("message", message)));
    }

    public static LocalizedResponseStatusException canNotDeleteNullLocation() {
        return LocalizedResponseStatusException.internalError(CAN_NOT_DELETE_NULL_LOCATION.with());
    }

    public static LocalizedResponseStatusException canNotDeleteDisabledLocation() {
        return LocalizedResponseStatusException.businessLogicError(CAN_NOT_DELETE_DISABLED_LOCATION.with());
    }

    public static LocalizedResponseStatusException canNotDeleteNonLeafLocation() {
        return LocalizedResponseStatusException.businessLogicError(CAN_NOT_DELETE_NON_LEAF_LOCATION.with());
    }

    public static LocalizedResponseStatusException canNotDeleteTopLocation() {
        return LocalizedResponseStatusException.businessLogicError(CAN_NOT_DELETE_TOP_LOCATION.with());
    }

    public static LocalizedResponseStatusException canNotFindRootLocation(Integer projectId) {
        return LocalizedResponseStatusException.internalError(CAN_NOT_FIND_ROOT_LOCATION.with(pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException canNotMoveRootLocation() {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_MOVE_ROOT_LOCATION.with());
    }

    public static LocalizedResponseStatusException orderIndexOutOfRange(Integer order) {
        return LocalizedResponseStatusException.badRequest(ORDER_INDEX_OUT_OF_RANGE.with(pv("order", order)));
    }

    public static LocalizedResponseStatusException parentLocationIdHasToBelongToProject(Integer locationId, Integer projectId) {
        return LocalizedResponseStatusException.badRequest(PARENT_LOCATION_HAS_TO_BELONG_TO_PROJECT.with(pv("locationId", locationId), pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException locationCannotBeMovedToTheSameParent(Integer locationId, Integer parentId) {
        return LocalizedResponseStatusException.badRequest(LOCATION_CANNOT_BE_MOVED_TO_THE_SAME_PARENT.with(pv("locationId", locationId), pv("parentId", parentId)));
    }
}
