package com.fintex.ce.util;

import com.fintex.ce.dto.CommonDates;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;

public class MapUtils {

    private MapUtils() {
    }

    public static <K, V> Map<K, Map<LocalDate, V>> filterWithinRange(final CommonDates dates, final Map<K, Map<LocalDate, V>> map) {
        if (dates.hasNoDates()) {
            return map;
        }
        return map.entrySet().stream().collect(CollectorUtils.toMap(Map.Entry::getKey, entry -> filterDatesWithinRange(dates, entry.getValue())));
    }

    public static <V> Map<LocalDate, V> filterDatesWithinRange(final CommonDates dates, final Map<LocalDate, V> map) {
        return map.entrySet().stream().filter(entry -> isWithinTheRange(dates, entry.getKey())).collect(CollectorUtils.toTreeMap());
    }

    static boolean isWithinTheRange(final CommonDates dates, final LocalDate date) {
        return Optional.ofNullable(dates.getStart()).map(date::compareTo).orElse(1) >= 0
                && Optional.ofNullable(dates.getEnd()).map(date::compareTo).orElse(-1) <= 0;
    }

    /**
     * Replaces {@param defaultMap} values with {@param userMap} values.
     * New map will have the same size and the same keys as {@param defaultMap} but will contain new values from {@param userMap} if exist
     *
     * @param defaultMap default values
     * @param userMap    user values
     * @param <T>        type
     * @return new map
     */
    public static <T> Map<T, BigDecimal> overrideDefaultValues(final Map<T, BigDecimal> defaultMap,
                                                               final Map<T, BigDecimal> userMap) {
        if (userMap.isEmpty()) {
            return userMap;
        }
        final Map<T, BigDecimal> newMap = new HashMap<>(defaultMap);
        newMap.putAll(userMap);
        return newMap;
    }

    public static <K, V> Map<K, V> copy(final Map<K, V> originalMap, final Supplier<Map<K, V>> mapCreator) {
        final Map<K, V> newMap = mapCreator.get();
        if (!CollectionUtils.isEmpty(originalMap)) {
            newMap.putAll(originalMap);
        }
        return newMap;
    }

    public static <K, V> TreeMap<K, V> copyTreeMap(final Map<K, V> originalMap, final Supplier<TreeMap<K, V>> mapCreator) {
        final TreeMap<K, V> newMap = mapCreator.get();
        if (!CollectionUtils.isEmpty(originalMap)) {
            newMap.putAll(originalMap);
        }
        return newMap;
    }
}
