package com.thalesgroup.datastorage.dojo;

import java.rmi.server.UID;

public class NameGenerator {

    public static String getName() {
        return System.nanoTime() + "";
    }
}
