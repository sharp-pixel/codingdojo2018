package com.thalesgroup.datastorage.dojo;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.status.Status;
import org.junit.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.Assert.*;

public class LoggingConfigurationTest {
    @Test
    public void consoleAppenderWritesMessages() throws Exception {
        assertTrue(logMessage("STDOUT").contains("dependency-upgrade-smoke"));
    }

    @Test
    public void jsonAppenderWritesStructuredMessages() throws Exception {
        var json = JsonMapper.builder().build().readTree(logMessage("STDOUT-JSON"));
        assertEquals("dependency-upgrade-smoke", json.get("message").stringValue());
        assertEquals("ERROR", json.get("level").stringValue());
        assertEquals("ROOT", json.get("logger_name").stringValue());
        Instant.parse(json.get("timestamp").stringValue());
    }

    private String logMessage(String appenderName) throws Exception {
        String xml;
        try (InputStream resource = getClass().getResourceAsStream("/logback.xml")) {
            assertNotNull("Application logging configuration must be present", resource);
            xml = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        }
        // Exercise the optional JSON appender using the application's real configuration.
        xml = xml.replace("ref=\"STDOUT\"", "ref=\"" + appenderName + "\"");
        LoggerContext context = new LoggerContext();
        context.setMDCAdapter(new LogbackMDCAdapter());
        context.start();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            configurator.doConfigure(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
            var appender = (OutputStreamAppender<ILoggingEvent>) logger.getAppender(appenderName);
            assertNotNull(appender);
            assertTrue(appender.isStarted());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            appender.setOutputStream(output);
            logger.error("dependency-upgrade-smoke");

            for (Status status : context.getStatusManager().getCopyOfStatusList()) {
                assertNotEquals(status.toString(), Status.ERROR, status.getLevel());
            }
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            context.stop();
        }
    }
}
