package com.thalesgroup.datastorage.dojo;

import com.thalesgroup.datastorage.dojo.config.KafkaConfig;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

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
                producer.send(producerRecord, new Callback() {
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (e != null) {
                            System.out.println("Error " + recordMetadata.topic());
                        }
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
