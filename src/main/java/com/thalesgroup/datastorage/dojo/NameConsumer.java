package com.thalesgroup.datastorage.dojo;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class NameConsumer implements Closeable, Runnable {
    private Consumer<String, String> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private Map<String, String> users;

    public NameConsumer(Consumer<String, String> consumer) {
        this.consumer = consumer;
        users = new HashMap<>();
    }

    public void consumeUsers() {
        running.set(true);
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    users.put(record.key(), record.value());
                }

                consumer.commitAsync();
            }
        } catch (Exception e) {
            System.out.println("Erreur " + e.getMessage());
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }

    @Override
    public void close() throws IOException {
        consumer.close();
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public Map<String, String> getUsers() {
        return this.users;
    }

    public void start() {
        Thread worker = new Thread(this);
        worker.start();
    }

    public void stop() {
        running.set(false);
    }

    @Override
    public void run() {
        consumeUsers();
    }
}
