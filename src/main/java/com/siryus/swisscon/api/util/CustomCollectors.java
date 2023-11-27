package com.siryus.swisscon.api.util;

import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CustomCollectors {

    public static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException("List has less or more items than one");
                    }
                    return list.get(0);
                }
        );
    }

}
