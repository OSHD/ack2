package com.dank.util.deob;

/**
 * Created by Greg on 3/3/2016.
 */
public class ActiveModulas {

    private final Number getter;
    private final Number setter;

    /**
     * Will only be created when the correct modulas is found.
     * @param getter
     * @param setter
     */
    public ActiveModulas(Number getter, Number setter) {
        this.getter = getter;
        this.setter = setter;
    }
}
