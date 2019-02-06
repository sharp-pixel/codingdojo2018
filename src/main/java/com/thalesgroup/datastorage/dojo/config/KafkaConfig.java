package com.thalesgroup.datastorage.dojo.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaConfig {

    public static Properties getProducerConfig(String transactionalId) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "PLAINTEXT://localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
//        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG,
//                transactionalId);

        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 305000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 305000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Integer.MAX_VALUE);

        return props;
    }

    public static Properties getConsumerConfig() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "PLAINTEXT://localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "dojo");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG,
                "read_committed");
        return props;
    }
}
