import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StreamsAppTest {

    @Test
    public void createTopology() {
        Topology topology = StreamsApp.createTopology();
        Properties props = StreamsApp.createProperties(false);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");

        try (TopologyTestDriver driver = new TopologyTestDriver(topology, props)) {
            TestInputTopic<String, String> events = driver.createInputTopic("events", new StringSerializer(), new StringSerializer());
            TestInputTopic<String, String> users = driver.createInputTopic("users", new StringSerializer(), new StringSerializer());
            TestOutputTopic<String, String> output = driver.createOutputTopic("output", new StringDeserializer(), new StringDeserializer());

            users.pipeInput("key_0", "user");
            events.pipeInput("key_0", "event");

            assertEquals("User user sent event", output.readValue());
        }
    }
}
