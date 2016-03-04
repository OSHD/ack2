/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the license, or (at your option) any later version.
 */
package com.runetek;

import com.runetek.services.RSService;

public abstract class EngineToolkit {

    private static volatile EngineToolkit toolkit;

    /**
     * @return The default EngineToolkit instance
     */
    public static synchronized EngineToolkit getDefault() {
        return null;
    }

    /**
     * Given an anonymous object, generate an {@link com.runetek.services.RSService} instance
     * to define the given raw type. The service type can be defined by any means so long as it refers to
     * the provided object for the only source of possible return.
     *
     * @param raw  The raw object to delegate the interface from
     * @param type The interface type to define
     * @param <S>  The interface to define
     * @return An {@link com.runetek.services.RSService} instance in which will delegate
     * the anonymous object type into the defined service.
     */
    public abstract <S extends RSService> S define(Object raw, Class<? extends S> type);
}
