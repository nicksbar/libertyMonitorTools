package com.github.nickbar.LibertyUatTools.Endpoints.FileDownload;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@ApplicationScoped
public class LogFileDownloaderResource {

    private Properties properties;

    @PostConstruct
    public void init() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("tailFiles.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find tailFiles.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading tailFiles.properties", ex);
        }
    }

    public String getFilePath(String key) {
        return properties.getProperty(key);
    }
}