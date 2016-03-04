package com.dank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Septron
 * @since February 12, 2015
 */
public class DankBuild {

    public static final DankBuild instance;

    static {
        instance = new DankBuild();
    }

    final Properties prop = new Properties();
    final File src = new File("./build.xml");

    public DankBuild() {
        init();
    }

    public void init() {
        if (src.exists()) {
            try {
                prop.loadFromXML(new FileInputStream(new File("./build.xml")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            prop.setProperty("curRevision", String.valueOf(1));
            prop.setProperty("build", String.valueOf(0));
        }
    }

    public Object get(String key) {
        return prop.get(key);
    }

    public void set(String key, String value) {
        prop.setProperty(key, value);
    }

    public void save() throws Exception {
        prop.storeToXML(new FileOutputStream(src), null);
    }
}
