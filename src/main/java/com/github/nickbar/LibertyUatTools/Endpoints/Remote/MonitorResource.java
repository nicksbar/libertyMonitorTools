package com.github.nickbar.LibertyUatTools.Endpoints.Remote;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@ApplicationScoped
public class MonitorResource {

    public Map<String, Map<String, String>> loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("monitored.properties")) {
            if (input == null) {
                throw new IOException("Unable to find monitored.properties");
            }
            properties.load(input);
        }

        Map<String, Map<String, String>> categorizedProperties = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            String[] parts = key.split("\\.");
            if (parts.length < 2) continue;

            String name = parts[0];
            String propertyKey = parts[1];
            String value = properties.getProperty(key);

            categorizedProperties.computeIfAbsent(name, k -> new HashMap<>()).put(propertyKey, value);
        }

        return categorizedProperties;
    }
}
