package com.dank.hook;

import java.security.AccessController;

public class Test implements Runnable {
    @Override
    public void run() {
        System.out.println("HI");
        System.out.println(getClass().getClassLoader());
        System.out.println(getClass().getClassLoader().getResource("com/dank/hook/Test.class"));
        
        System.out.println(AccessController.getContext());
    }

    public static class Ok {

        public Ok() {
            System.out.println("COOL");
        }
    }

}
