package com.siryus.swisscon.api.general;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import com.siryus.swisscon.api.general.reference.ReferenceType;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public class GeneralException {
    private static int e(int n) {
        return LocalizedResponseStatusException.GENERAL_ERROR_CODE + n;
    }

    public static final LocalizedReason COUNTRY_ID_CAN_NOT_BE_NULL = LocalizedReason.like(e(1), "Country Id can not be null");
    public static final LocalizedReason COUNTRY_NOT_FOUND = LocalizedReason.like(e(2), "Country {{countryId}} does not exist");

    public static final LocalizedReason CAN_NOT_MAP_REFERENCE_TYPE_TO_CLASS = LocalizedReason.like(e(4), "Unable to map ReferenceType {{referenceType}} to a class.");
    public static final LocalizedReason UNIT_NOT_FOUND = LocalizedReason.like(e(5), "Unit {{unitId}} does not exists");

    public static LocalizedResponseStatusException countryIdCanNotBeNull() {
        return LocalizedResponseStatusException.notFound(COUNTRY_ID_CAN_NOT_BE_NULL.with());
    }

    public static LocalizedResponseStatusException countryNotFound(Integer countryId) {
        return LocalizedResponseStatusException.notFound(COUNTRY_NOT_FOUND.with(pv("countryId", countryId)));
    }

    public static LocalizedResponseStatusException canNotMapReferenceTypeToClass(ReferenceType referenceType) {
        return LocalizedResponseStatusException.notFound(CAN_NOT_MAP_REFERENCE_TYPE_TO_CLASS.with(pv("referenceType", referenceType)));
    }

    public static LocalizedResponseStatusException unitNotFound(Integer unitId) {
        return LocalizedResponseStatusException.notFound(UNIT_NOT_FOUND.with(pv("unitId", unitId)));
    }
}
