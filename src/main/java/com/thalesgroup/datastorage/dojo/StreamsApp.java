package com.thalesgroup.datastorage.dojo;

import com.thalesgroup.datastorage.dojo.listeners.ConsoleGlobalRestoreListener;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Properties;

public class StreamsApp {
    public static void main(String[] args) {
        Properties props = createProperties();
        Topology topology = createTopology();
        final KafkaStreams streams = new KafkaStreams(topology, props);

        System.out.println(topology.describe().toString());

        streams.setGlobalStateRestoreListener(new ConsoleGlobalRestoreListener());
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                System.out.println("Closing Kafka Streams app");
                streams.close();
            }
        });
        streams.start();
    }

    static Properties createProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "PLAINTEXT://localhost:9092");
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once");

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest");
        props.put(StreamsConfig.producerPrefix(ProducerConfig.RETRIES_CONFIG), Integer.MAX_VALUE);
        props.put(StreamsConfig.producerPrefix(ProducerConfig.MAX_BLOCK_MS_CONFIG), Integer.MAX_VALUE);
        props.put(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG, 305000);
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG), Integer.MAX_VALUE);
        return props;
    }
    static Topology createTopology() {
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> eventsStream = builder.stream("events");
        KTable<String, String> usersKTable = builder.table("users", Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("USERS").withLoggingDisabled());

        eventsStream.leftJoin(usersKTable, new ValueJoiner<String, String, Object>() {
            @Override
            public Object apply(String event, String user) {
                if (user == null) {
                    System.out.println("User not found : " + event);
                    return null;
                }
                return "User " + user + " sent " + event;
            }
        }).filter((key, value) -> value != null).to("output");
        return builder.build();
    }
}
