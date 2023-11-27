package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class ListTaskIdsRequest {

    private final List<Integer> tradeIds;

    @JsonCreator
    public ListTaskIdsRequest(@JsonProperty("tradeIds") List<Integer> tradeIds) {
        this.tradeIds = orEmptyList(tradeIds);
    }

    private static <T> List<T> orEmptyList(List<T> listOrNull) {
        return listOrNull == null ? Collections.emptyList() : Collections.unmodifiableList(listOrNull);
    }

}
