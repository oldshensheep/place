package com.oldshensheep.place.web;

import java.util.Collection;

public class ValidateUtils {

    public static void Between(Integer data, Integer a, Integer b) {
        if (data < a) throw new IllegalArgumentException("data must be between %s and %s".formatted(a, b));
        if (data > b) throw new IllegalArgumentException("data must be between %s and %s".formatted(a, b));
    }

    public static void Length(Collection collection, Integer a, Integer b) {
        if (collection.size() < a)
            throw new IllegalArgumentException("data size must be between %s and %s".formatted(a, b));
        if (collection.size() > b)
            throw new IllegalArgumentException("data size must be between %s and %s".formatted(a, b));
    }
}
