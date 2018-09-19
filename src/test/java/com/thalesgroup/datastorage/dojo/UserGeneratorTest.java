package com.thalesgroup.datastorage.dojo;

import org.apache.kafka.clients.producer.MockProducer;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserGeneratorTest {

    @Test
    public void generateTest() {
        MockProducer<String, String> mockProducer = new MockProducer<String, String>();

        UserGenerator ug = new UserGenerator(mockProducer);
        ug.generate(5);
        assertEquals(5, mockProducer.history().size());
    }
}