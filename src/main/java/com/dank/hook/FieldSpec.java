package com.dank.hook;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.List;

public class FieldSpec implements MemberVerify {

    Hook owner;
    String desc;
    boolean stat;

    public FieldSpec(Hook owner, String desc) { //For non-static fields
        this.owner = owner;
        this.desc = desc;
        this.stat = false;
    }

    public FieldSpec(String desc) { //For static fields
        this.owner = null;
        this.desc = desc;
        this.stat = true;
    }

    public boolean isStatic() {
        return stat;
    }

    public Hook getOwner() {
        return owner;
    }



    @Override
    public List<String> verify(RSMember member) {
        return null;
    }


    public static void main(String... args) throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        System.out.println(new File("C:\\Users\\Jamie\\HDUpdater\\Updater\\out\\production\\Updater\\com\\dank").toURL());
        ClassLoader loader = new Loader(new URL[]{new File("C:\\Users\\Jamie\\HDUpdater\\Updater\\out\\production\\Updater\\").toURL()});
        Class clazz = loader.loadClass("com.dank.hook.Test");
        Runnable r = (Runnable) clazz.newInstance();
        Thread thread = new Thread(r);
        thread.setContextClassLoader(loader);
        thread.start();

    }



    static class Loader extends URLClassLoader {

        private Hashtable classes = new Hashtable();

        public Loader(URL[] urls) {
            super(urls, null);

        }

        protected Class loadClass(String class_name, boolean resolve)
                throws ClassNotFoundException
        {
            System.out.println("LOAD CLASS:" + class_name + "," + resolve);
         //   if(class_name.equals("com.dank.hook.Hook")) return null;
            return super.loadClass(class_name,resolve);
        }
    }
}
