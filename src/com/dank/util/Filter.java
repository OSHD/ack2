package com.dank.util;

public interface Filter<T> {
    boolean accepts(T value);
}
