package com.thalesgroup.datastorage.dojo;

public class NameGenerator {

    public static String getName() {
        return System.nanoTime() + "";
    }
}
