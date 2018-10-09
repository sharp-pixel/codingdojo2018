package com.thalesgroup.datastorage.dojo;

import com.thalesgroup.datastorage.dojo.config.KafkaConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;

import java.io.Closeable;

public class UserGenerator implements Closeable {
    Producer<String, String> producer;
    final String topicName = "users";

    public UserGenerator(Producer<String, String> producer) {
        this.producer = producer;
    }

    public void generate(int n) {
        producer.initTransactions();
        try {
            producer.beginTransaction();
            for (int i = 0; i < n; i++) {
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, i + "", NameGenerator.getName());
                producer.send(producerRecord, (recordMetadata, e) -> {
                    if (e != null) {
                        System.out.println("Error " + recordMetadata.topic());
                    }
                });
            }
            producer.commitTransaction();
        } catch (KafkaException e) {
            producer.abortTransaction();
        }
    }

    public void close() {
        producer.close();
    }

    public static void main(String[] args) {
        KafkaProducer<String, String> kp = new KafkaProducer<>(KafkaConfig.getProducerConfig("userGenerator"));
        UserGenerator ug = new UserGenerator(kp);
        long timeBefore = System.nanoTime();
        ug.generate(100000);
        long timeAfter = System.nanoTime();

        long timePassed = timeAfter - timeBefore;
        long timePassedMs = timePassed / 1000000;
        System.out.println("Generation took " + timePassedMs);
    }
}
