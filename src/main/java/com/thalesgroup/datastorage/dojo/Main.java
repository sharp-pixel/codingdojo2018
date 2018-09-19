package com.thalesgroup.datastorage.dojo;

import com.thalesgroup.datastorage.dojo.config.KafkaConfig;
import org.apache.kafka.clients.producer.KafkaProducer;

public class Main {
    public static void main(String[] args) {
        KafkaProducer<String, String> kp = new KafkaProducer<>(KafkaConfig.getProducerConfig("userGenerator"));
        UserGenerator ug = new UserGenerator(kp);
        Long timeBefore = System.nanoTime();
        ug.generate(10000000);
        Long timeAfter = System.nanoTime();

        Long timePassed = timeAfter - timeBefore;
        Long timePassedMs = timePassed / 1000000;
        System.out.println("Generation took " + timePassedMs);
    }
}
