import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.ClassSecurityValidator;
import org.junit.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class AvroSerdeCompatibilityTest {
    private static final String TOPIC = "mysql-users";
    private static final Map<String, String> CONFIG =
            Map.of("schema.registry.url", "mock://avro-compatibility");

    @Test
    public void roundTripsGenericUsersAndTombstones() {
        try (GenericAvroSerde serde = new GenericAvroSerde(new MockSchemaRegistryClient())) {
            serde.configure(CONFIG, false);
            GenericRecord user = new GenericData.Record(users.getClassSchema());
            user.put("id", 42);
            user.put("name", "Zoë");
            user.put("updated_at", null);

            byte[] encoded = serde.serializer().serialize(TOPIC, user);
            GenericRecord decoded = serde.deserializer().deserialize(TOPIC, encoded);

            assertEquals(42, decoded.get("id"));
            assertEquals("Zoë", decoded.get("name").toString());
            assertNull(decoded.get("updated_at"));
            assertNull(serde.serializer().serialize(TOPIC, null));
            assertNull(serde.deserializer().deserialize(TOPIC, null));
        }
    }

    @Test
    public void roundTripsGeneratedUsersWithLogicalTimestamp() {
        String scope = "specific-avro-compatibility";
        try (SpecificAvroSerde<users> serde = StreamsApp.createSpecificAvroSerde("mock://" + scope)) {
            users user = users.newBuilder()
                    .setId(42)
                    .setName("Zoë")
                    .setUpdatedAt(Instant.parse("2026-10-14T10:20:30.123Z"))
                    .build();

            byte[] encoded = serde.serializer().serialize(TOPIC, user);
            users decoded = serde.deserializer().deserialize(TOPIC, encoded);

            assertEquals(user, decoded);
            assertThrows(SecurityException.class,
                    () -> ClassSecurityValidator.validate(AvroSerdeCompatibilityTest.class));
        } finally {
            MockSchemaRegistry.dropScope(scope);
        }
    }
}
