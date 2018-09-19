import com.thalesgroup.datastorage.dojo.listeners.ConsoleGlobalRestoreListener;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.LogAndSkipOnInvalidTimestamp;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.HashMap;
import java.util.Properties;

@Slf4j
public class StreamsApp {
    public static void main(String[] args) {
        Properties props = createProperties(true);
        Topology topology = createTopologyAvroSpecific();
        final KafkaStreams streams = new KafkaStreams(topology, props);

        System.out.println(topology.describe().toString());

        streams.setGlobalStateRestoreListener(new ConsoleGlobalRestoreListener());

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                log.trace("Closing Kafka Streams app");
                streams.close();
            }
        });

        streams.start();
    }

    static Properties createProperties(boolean specificAvro) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "PLAINTEXT://localhost:9092");
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once");

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        if (specificAvro) {
            props.put("specific.avro.reader", "true");
        }

        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest");
        props.put(StreamsConfig.producerPrefix(ProducerConfig.RETRIES_CONFIG), Integer.MAX_VALUE);
        props.put(StreamsConfig.producerPrefix(ProducerConfig.MAX_BLOCK_MS_CONFIG), Integer.MAX_VALUE);
        props.put(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG, 305000);
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG), Integer.MAX_VALUE);

//        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
//        props.put(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, DefaultProductionExceptionHandler.class);

        return props;
    }

    static Topology createTopology() {
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> eventsStream = builder.stream("events");
        KTable<String, String> usersKTable = builder.table("users", Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("USERS").withLoggingDisabled());

        eventsStream.leftJoin(usersKTable, (ValueJoiner<String, String, Object>) (event, user) -> {
            if (user == null) {
                System.out.println("User not found : " + event);
                return null;
            }
            return "User " + user + " sent " + event;
        }).filter((key, value) -> value != null).to("output");

        return builder.build();
    }

    static Topology createTopologyAvroGeneric() {
        final StreamsBuilder builder = new StreamsBuilder();

        GenericAvroSerde genericAvroSerde = new GenericAvroSerde();
        HashMap<String, String> properties = new HashMap<>();
        properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        genericAvroSerde.configure(properties, false);

        Consumed<String, GenericRecord> consumed = Consumed.with(new Serdes.StringSerde(), genericAvroSerde, new LogAndSkipOnInvalidTimestamp(), Topology.AutoOffsetReset.EARLIEST);
        KStream<String, GenericRecord> usersKStream = builder.stream("mysql-users", consumed);
        KStream<String, GenericRecord> usersKStreamKey = usersKStream.selectKey((s, users) -> users.get("id").toString());
        KTable<String, GenericRecord> usersKTable = usersKStreamKey.groupByKey(Serialized.with(new Serdes.StringSerde(), genericAvroSerde)).reduce((users, v1) -> users, Materialized.<String, GenericRecord, KeyValueStore<Bytes, byte[]>>as("USERS").withValueSerde(genericAvroSerde));

        KStream<String, String> eventsStream = builder.stream("events", Consumed.with(new Serdes.StringSerde(), new Serdes.StringSerde(), new LogAndSkipOnInvalidTimestamp(), Topology.AutoOffsetReset.EARLIEST));

        eventsStream.leftJoin(usersKTable, (ValueJoiner<String, GenericRecord, Object>) (event, users) -> {
            if (users == null) {
                System.out.println("User not found : " + event);
                return null;
            }
            return "User " + users.get("name").toString() + " sent " + event;
        }).filter((key, value) -> value != null).to("output");

        return builder.build();
    }

    static Topology createTopologyAvroSpecific() {
        final StreamsBuilder builder = new StreamsBuilder();

        SpecificAvroSerde<users> specificAvroSerde = new SpecificAvroSerde<>();
        HashMap<String, String> properties = new HashMap<>();
        properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        specificAvroSerde.configure(properties, false);

        Consumed<String, users> consumed = Consumed.with(new Serdes.StringSerde(), specificAvroSerde, new LogAndSkipOnInvalidTimestamp(), Topology.AutoOffsetReset.EARLIEST);
        KStream<String, users> usersKStream = builder.stream("mysql-users", consumed);
        KStream<String, users> usersKStreamKey = usersKStream.selectKey((s, users) -> users.get("id").toString());
        KTable<String, users> usersKTable = usersKStreamKey.groupByKey(Serialized.with(new Serdes.StringSerde(), specificAvroSerde)).reduce((users, v1) -> users, Materialized.<String, users, KeyValueStore<Bytes, byte[]>>as("USERS").withValueSerde(specificAvroSerde));

        KStream<String, String> eventsStream = builder.stream("events");
        eventsStream.leftJoin(usersKTable, (ValueJoiner<String, users, Object>) (event, user) -> {
            if (user == null) {
                System.out.println("User not found : " + event);
                return null;
            }
            return "User " + user.get("name").toString() + " sent " + event;
        }).filter((key, value) -> value != null).to("output");

        return builder.build();
    }
}
