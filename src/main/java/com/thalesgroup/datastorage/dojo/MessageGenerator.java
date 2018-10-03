package com.thalesgroup.datastorage.dojo;

import com.thalesgroup.datastorage.dojo.config.KafkaConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
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

    public static void main(String[] args) {
        KafkaProducer<String, String> kp = new KafkaProducer<>(KafkaConfig.getProducerConfig("messageGenerator"));
        MessageGenerator ug = new MessageGenerator(kp);
        long timeBefore = System.nanoTime();
        ug.generate(100000);
        long timeAfter = System.nanoTime();

        long timePassed = timeAfter - timeBefore;
        long timePassedMs = timePassed / 1000000;
        System.out.println("Generation took " + timePassedMs);
    }
}
