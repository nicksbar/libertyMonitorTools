package com.github.nickbar.LibertyUatTools.Endpoints.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Path("/commands")
@ApplicationScoped
public class CommandResource {


    @Inject
    private com.github.nickbar.LibertyUatTools.Endpoints.Commands.CommandConfig commandConfig;

    @Inject
    private com.github.nickbar.LibertyUatTools.Endpoints.Commands.CommandExecutor commandExecutor;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getCommandForm() {
        StringBuilder formHtml = new StringBuilder();
        formHtml.append("<form id='commandForm'>")
                .append("<label for='command'>Command:</label>")
                .append("<select id='command' name='command' hx-get='/api/commands/form' hx-target='#params' hx-trigger='change'><option></option>");

        for (String commandName : commandConfig.getCommands().keySet()) {
            /*if (commandName.endsWith(".unix")) {
                commandName = commandName.substring(0, commandName.lastIndexOf("."));
            }*/
            formHtml.append("<option value='").append(commandName).append("'>")
                    .append(commandName).append("</option>");
        }

        formHtml.append("</select><br>")
                .append("<div id='params'></div>")
                .append("<input type='submit' value='Execute' hx-post='/api/commands/execute' hx-target='#result' hx-swap='innerHTML'>")
                .append("<div><pre id='result'></pre></div>")
                .append("</form>");

        return formHtml.toString();
    }

    @GET
    @Path("/form")
    @Produces(MediaType.TEXT_HTML)
    public String getCommandParams(@QueryParam("command") String command) {
        String[] params = commandConfig.getCommandParams(command);
        if (params == null) {
            return "";
        }

        StringBuilder paramsHtml = new StringBuilder();
        for (String param : params) {
            if (!param.isEmpty()) {
                paramsHtml.append("<label for='").append(param).append("'>").append(param).append(":</label>")
                        .append("<input type='text' id='").append(param).append("' name='").append(param).append("'><br>");
            }
        }

        return paramsHtml.toString();
    }

    @POST
    @Path("/execute")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response executeCommand(@FormParam("command") String command,
                                   @FormParam("dir") String dir,
                                   @FormParam("message") String message) {

        String commandTemplate = commandConfig.getCommandTemplate(command);
        if (commandTemplate == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Command not found").build();
        }

        Map<String, String> paramMap = new HashMap<>();
        if (dir != null) {
            paramMap.put("dir", dir);
        }
        if (message != null) {
            paramMap.put("message", message);
        }

        try {
            String output = commandExecutor.executeCommand(commandTemplate, paramMap);
            return Response.ok(output).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Command execution failed: " + e.getMessage()).build();
        }
    }
}