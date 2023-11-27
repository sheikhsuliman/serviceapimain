package com.siryus.swisscon.api.file;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public class FileExceptions {
    private static int e(int n) {
        return LocalizedResponseStatusException.FILE_ERROR_CODE + n;
    }

    private static final LocalizedReason NO_FILE_IN_FORM = LocalizedReason.like(e(1), "No File in the form");
    private static final LocalizedReason REFERENCE_TYPE_INCORRECT = LocalizedReason.like(e(2), "Reference Type {{referenceType}} is incorrect");
    private static final LocalizedReason REFERENCE_ID_IS_NOT_NUMERIC = LocalizedReason.like(e(3), "Reference Id {{referenceId}} is not numeric");
    private static final LocalizedReason NON_TEMPORARY_FILE_NEEDS_REFERENCE_ID = LocalizedReason.like(e(4), "Non temporary file needs reference id");

    private static final LocalizedReason FAILED_PERSISTING_FILE = LocalizedReason.like(e(5), "Failed to persist file {{fileName}}");
    private static final LocalizedReason FAILED_TO_CONVERT_FILE = LocalizedReason.like(e(6), "Failed to convert file {{fileName}}");
    private static final LocalizedReason CAN_NOT_UPDATE_SYSTEM_FILE = LocalizedReason.like(e(7), "Can not update system file {{fileName}}");
    private static final LocalizedReason CAN_NOT_DELETE_SYSTEM_FILE = LocalizedReason.like(e(8), "Can not delete system file {{fileName}}");
    private static final LocalizedReason INTERNAL_ERROR = LocalizedReason.like(e(9), "Internal error {{errorMessage}}");

    private static final LocalizedReason FILE_NOT_FOUND = LocalizedReason.like(e(10), "File Not Found {{fileId}}");
    private static final LocalizedReason FILE_IS_NOT_OWNED_BY_USER = LocalizedReason.like(e(11), "File {{fileId}} is not owned by user {{userId}}");
    private static final LocalizedReason FILE_IS_NOT_ASSIGNABLE = LocalizedReason.like(e(12), "File {{fileId}} can not be assigned / attached");
    private static final LocalizedReason CAN_NOT_RENAME_NON_TEMPORARY_FILE = LocalizedReason.like(e(13), "Can not rename non-temporary file: {{fileId}}");

    public static LocalizedResponseStatusException noFileInForm() {
        return LocalizedResponseStatusException.badRequest(NO_FILE_IN_FORM.with());
    }

    public static LocalizedResponseStatusException referenceTypeIncorrect(String referenceType) {
        return LocalizedResponseStatusException.badRequest(REFERENCE_TYPE_INCORRECT.with(pv("referenceType", referenceType)));
    }

    public static LocalizedResponseStatusException referenceIdNotNumeric(String referenceId) {
        return LocalizedResponseStatusException.badRequest(REFERENCE_ID_IS_NOT_NUMERIC.with(pv("referenceId", referenceId)));
    }

    public static LocalizedResponseStatusException nonTemporaryFileNeedsReferenceId() {
        return LocalizedResponseStatusException.badRequest(NON_TEMPORARY_FILE_NEEDS_REFERENCE_ID.with());
    }

    public static LocalizedResponseStatusException failedToPersistFile(String fileName) {
        return LocalizedResponseStatusException.internalError(FAILED_PERSISTING_FILE.with(pv("fileName", fileName)));
    }

    public static LocalizedResponseStatusException failedToConvertFile(String fileName) {
        return LocalizedResponseStatusException.internalError(FAILED_TO_CONVERT_FILE.with(pv("fileName", fileName)));
    }

    public static LocalizedResponseStatusException canNotUpdateSystemFile(String fileName) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_UPDATE_SYSTEM_FILE.with(pv("fileName", fileName)));
    }
    public static LocalizedResponseStatusException canNotDeleteSystemFile(String fileName) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_DELETE_SYSTEM_FILE.with(pv("fileName", fileName)));
    }

    public static LocalizedResponseStatusException internalError(String errorMessage) {
        return LocalizedResponseStatusException.internalError(INTERNAL_ERROR.with(pv("errorMessage", errorMessage)));
    }

    public static LocalizedResponseStatusException fileNotFound(Integer fileId) {
        return LocalizedResponseStatusException.badRequest(FILE_NOT_FOUND.with(pv("fileId", fileId)));
    }

    public static LocalizedResponseStatusException fileIsNotOwnBy(Integer fileId, Integer userId) {
        return LocalizedResponseStatusException.badRequest(FILE_IS_NOT_OWNED_BY_USER.with(pv("fileId", fileId), pv("userId", userId)));
    }
    
    public static LocalizedResponseStatusException fileIsNotAssignable(Integer fileId) {
        return LocalizedResponseStatusException.badRequest(FILE_IS_NOT_ASSIGNABLE.with(pv("fileId", fileId)));
    }

    public static LocalizedResponseStatusException canNotRenameNonTemporaryFile(Integer fileId) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_RENAME_NON_TEMPORARY_FILE.with(pv("fileId", fileId)));
    }
}

