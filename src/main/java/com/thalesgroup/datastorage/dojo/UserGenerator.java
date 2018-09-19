package com.thalesgroup.datastorage.dojo;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;

import java.io.Closeable;
import java.io.IOException;

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
                ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(topicName, i + "", NameGenerator.getName());
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

    public void close() throws IOException {
        producer.close();
    }
}
