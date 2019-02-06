package com.thalesgroup.datastorage.dojo;

import com.thalesgroup.datastorage.dojo.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class MessageGenerator implements Closeable {
    Producer<String, String> producer;
    final String topicName = "events";

    public MessageGenerator(Producer<String, String> producer) {
        this.producer = producer;
    }

    public void generateWithTransaction(int n) {
        producer.initTransactions();
        try {
            producer.beginTransaction();
            // for (int i = n - 1; i >= 0; i--) {
            for (int i = n; i-- > 0; ) {
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, i + "", UUID.randomUUID().toString());
                producer.send(producerRecord, (recordMetadata, e) -> {
                    if (e != null) {
                        String topic = recordMetadata == null ? "<unknown>" : recordMetadata.topic();
                        log.error("Error when sending to topic {}", topic, e);
                    }
                });
            }
            producer.commitTransaction();
        } catch (KafkaException e) {
            log.error("Could not send to topic {}", topicName, e);
            producer.abortTransaction();
        }
    }

    public void generateSimple(int n) {
        try {
            for (int i = n; i-- > 0; ) {
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, i + "", UUID.randomUUID().toString());
                producer.send(producerRecord, (recordMetadata, e) -> {
                    if (e != null) {
                        String topic = recordMetadata == null ? "<unknown>" : recordMetadata.topic();
                        log.error("Error when sending to topic {}", topic, e);
                    }
                });
            }
        } catch (KafkaException e) {
            log.error("Could not send to topic {}", topicName, e);
        }
    }

    public void close() throws IOException {
        producer.close();
    }

    public static void main(String[] args) {
        KafkaProducer<String, String> kp = new KafkaProducer<>(KafkaConfig.getProducerConfig("messageGenerator"));

        long timeBefore;
        try (MessageGenerator ug = new MessageGenerator(kp)) {
            log.trace("Starting message generation");
            timeBefore = System.nanoTime();
            ug.generateSimple(100000);

            long timeAfter = System.nanoTime();

            long timePassed = timeAfter - timeBefore;
            long timePassedMs = timePassed / 1000000;
            log.info("Generation took {}", timePassedMs);
        } catch (IOException e) {
            log.error("Error while generating messages", e);
        }
    }
}
