package com.thalesgroup.datastorage.dojo;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

public class MessageGenerator implements Closeable {
    Producer<String, String> producer;
    final String topicName = "events";

    public MessageGenerator(Producer<String, String> producer) {
        this.producer = producer;
    }

    public void generate(int n) {
        producer.initTransactions();
        try {
            producer.beginTransaction();
            // for (int i = n - 1; i >= 0; i--) {
            for (int i = n; i-- > 0; ) {
                ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(topicName, i + "", UUID.randomUUID().toString());
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
