package com.fintex.ce.framework.utils;

public class Utils {

    /**
     * Generic Enum search to
     * Adopeted from
     * https://stackoverflow.com/questions/28332924/case-insensitive-matching-of-a-string-to-a-java-enum
     *
     * @param enumeration
     * @param enumertationConstant
     * @param <T>
     * @return
     */
    public static <T extends Enum<?>> T lookUpEnum(Class<T> enumeration, String enumertationConstant) {
        for (T enumConstant : enumeration.getEnumConstants()) {
            if (enumConstant.name().compareToIgnoreCase(enumertationConstant) == 0) {
                return enumConstant;
            }
        }
        return null;
    }

}

