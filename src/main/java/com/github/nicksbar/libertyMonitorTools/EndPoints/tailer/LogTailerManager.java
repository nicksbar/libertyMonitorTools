package com.github.nicksbar.libertyMonitorTools.EndPoints.tailer;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@ApplicationScoped
public class LogTailerManager {

    private final Map<String, LogFileTailer> tailers = new HashMap<>();

    @PostConstruct
    public void init() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("tailFiles.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            prop.forEach((key, value) -> {
                String logName = (String) key;
                String filePath = (String) value;
                tailers.put(logName, new LogFileTailer(logName, filePath));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LogFileTailer getTailer(String logName) {
        return tailers.get(logName);
    }

    public void stopAllTailers() {
        tailers.values().forEach(LogFileTailer::stopTailing);
    }
}
