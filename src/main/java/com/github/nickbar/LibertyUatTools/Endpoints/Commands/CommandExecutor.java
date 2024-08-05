package com.github.nickbar.LibertyUatTools.Endpoints.Commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class CommandExecutor {

    public String executeCommand(String commandTemplate, Map<String, String> params) throws IOException {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            commandTemplate = commandTemplate.replace("${" + entry.getKey() + "}", entry.getValue());
        }

        Process process = Runtime.getRuntime().exec(commandTemplate);
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        return output.toString();
    }
}
