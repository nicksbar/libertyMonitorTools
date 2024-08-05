package com.github.nickbar.LibertyUatTools.Endpoints.FileTailer;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@ApplicationScoped
public class SshLogTailerConfig {

    private final Map<String, Map<String, String>> logTailerConfigs = new HashMap<>();

    @PostConstruct
    public void init() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("sshTailFiles.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find sshTailFiles.properties");
            }

            Properties prop = new Properties();
            prop.load(input);

            for (String name : prop.stringPropertyNames()) {
                String[] parts = name.split("\\.");
                String key = parts[0];
                String attribute = parts[1];

                logTailerConfigs.putIfAbsent(key, new HashMap<>());
                logTailerConfigs.get(key).put(attribute, prop.getProperty(name));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error loading sshTailFiles.properties", ex);
        }
    }

    public Map<String, String> getLogTailerConfig(String key) {
        return logTailerConfigs.get(key);
    }

    public Map<String, Map<String, String>> getAllLogTailerConfigs() {
        return logTailerConfigs;
    }
}
