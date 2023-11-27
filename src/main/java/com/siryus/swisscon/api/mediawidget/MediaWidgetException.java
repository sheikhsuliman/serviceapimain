package com.siryus.swisscon.api.mediawidget;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public class MediaWidgetException {
    private static int e(int n) {
        return LocalizedResponseStatusException.MEDIA_WIDGET_ERROR_CODE + n;
    }

    private static final LocalizedReason CAN_NOT_DELETE_NON_EMPTY_FOLDER = LocalizedReason.like(e(1), "Can not delete non empty folder");
    private static final LocalizedReason FILE_NOT_FOUND = LocalizedReason.like(e(2), "File {{fileId}} not found");
    private static final LocalizedReason USER_NOT_FOUND = LocalizedReason.like(e(3), "User {{userId}} not found");
    private static final LocalizedReason NOT_IMPLEMENTED = LocalizedReason.like(e(4), "Not Implemented");
    private static final LocalizedReason CAN_NOT_CREATE_FOLDER_AT_SPECIFIED_LOCATION = LocalizedReason.like(e(5), "Can not create folder in specified location");
    private static final LocalizedReason CAN_NOT_CREATE_FILE_AT_SPECIFIED_LOCATION = LocalizedReason.like(e(6), "Can not create file in specified location");
    private static final LocalizedReason FAIL_TO_GET_FILE_FROM_REQUEST = LocalizedReason.like(e(7), "Fail to get file from request");
    private static final LocalizedReason SYSTEM_FOLDER_NOT_FOUND = LocalizedReason.like(e(8), "System folder {{folderName}} not found");

    private static final LocalizedReason FILE_NAME_IS_MISSING = LocalizedReason.like(e(9), "File Name is Missing");
    private static final LocalizedReason PARENT_NODE_ID_IS_MISSING = LocalizedReason.like(e(10), "Parent node ID is missing");
    private static final LocalizedReason MISSING_REFERENCE_TYPE_OR_ID = LocalizedReason.like(e(11), "Missing referenceType and/or referenceId");


    public static LocalizedResponseStatusException canNotDeleteNonEmptyFolder() {
        return LocalizedResponseStatusException.businessLogicError(CAN_NOT_DELETE_NON_EMPTY_FOLDER.with());
    }

    public static LocalizedResponseStatusException fileNotFound(Integer fileId) {
        return LocalizedResponseStatusException.notFound(FILE_NOT_FOUND.with(pv("fileId", fileId)));
    }

    public static LocalizedResponseStatusException userNotFound(Integer userId) {
        return LocalizedResponseStatusException.notFound(USER_NOT_FOUND.with(pv("userId", userId)));
    }

    public static LocalizedResponseStatusException notImplemented() {
        return LocalizedResponseStatusException.internalError(NOT_IMPLEMENTED.with());
    }

    public static LocalizedResponseStatusException canNotCreateFolderAtSpecifiedLocation() {
        return LocalizedResponseStatusException.notPermitted(CAN_NOT_CREATE_FOLDER_AT_SPECIFIED_LOCATION.with());
    }

    public static LocalizedResponseStatusException canNotCreateFileAtSpecifiedLocation() {
        return LocalizedResponseStatusException.notPermitted(CAN_NOT_CREATE_FILE_AT_SPECIFIED_LOCATION.with());
    }

    public static LocalizedResponseStatusException failToGetFileFromRequest() {
        return LocalizedResponseStatusException.badRequest(FAIL_TO_GET_FILE_FROM_REQUEST.with());
    }

    public static LocalizedResponseStatusException systemFolderNotFound(String folderName) {
        return LocalizedResponseStatusException.internalError(SYSTEM_FOLDER_NOT_FOUND.with(pv("folderName", folderName)));
    }

    public static LocalizedResponseStatusException fileNameIsMissing() {
        return LocalizedResponseStatusException.badRequest(FILE_NAME_IS_MISSING.with());
    }

    public static LocalizedResponseStatusException parentNodeIdIsMissing() {
        return LocalizedResponseStatusException.badRequest(PARENT_NODE_ID_IS_MISSING.with());
    }

    public static LocalizedResponseStatusException missingReferenceTypeOrId() {
        return LocalizedResponseStatusException.badRequest(MISSING_REFERENCE_TYPE_OR_ID.with());
    }
}
