package com.github.nickbar.LibertyUatTools.Endpoints.Commands;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@ApplicationScoped
public class CommandConfig {

    private final Map<String, String> commands = new HashMap<>();
    private final Map<String, String[]> commandParams = new HashMap<>();

    @PostConstruct
    public void init() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("commands.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find commands.properties");
            }

            Properties prop = new Properties();
            prop.load(input);

            Pattern pattern = Pattern.compile("\\$\\{(\\w+)}");

            for (String name : prop.stringPropertyNames()) {
                String command = prop.getProperty(name);
                commands.put(name, command);

                Matcher matcher = pattern.matcher(command);
                StringBuilder params = new StringBuilder();
                while (matcher.find()) {
                    if (params.length() > 0) {
                        params.append(",");
                    }
                    params.append(matcher.group(1));
                }

                commandParams.put(name, params.toString().split(","));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error loading commands.properties", ex);
        }
    }

    public Map<String, String> getCommands() {
        return commands;
    }

    public String getCommandTemplate(String commandName) {

        return commands.get(commandName);

    }


    public String[] getCommandParams(String commandName) {
        return commandParams.get(commandName);
    }
}