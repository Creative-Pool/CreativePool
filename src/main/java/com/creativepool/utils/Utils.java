package com.creativepool.utils;

public class Utils {


    public static <T> T getOrDefault(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }
}
