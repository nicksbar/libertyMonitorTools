package com.github.nickbar.LibertyUatTools.Endpoints.Emulator;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/emulator")
@ApplicationScoped
public class EmulatorEndpoint {

    private static final Map<String, String> dataStore = new HashMap<>();

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getForm() {
        return "<html>" +
                "<body>" +
                "<form action=\"emulator/submit\" method=\"post\">" +
                "Key: <input type=\"text\" name=\"key\"><br>" +
                "Data: <textarea name=\"data\"></textarea><br>" +
                "<input type=\"submit\" value=\"Submit\">" +
                "</form>" +
                "</body>" +
                "</html>";
    }

  
    @POST
    @Path("/submit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response submitForm(@FormParam("key") String key, @FormParam("data") String data) {
        if (key == null || key.isEmpty() || data == null || data.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Key and data must not be empty")
                    .build();
        }
        dataStore.put(key, data);
        return Response.ok("Data stored successfully!").build();
    }

    @GET
    @Path("/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getData(@PathParam("key") String key) {
        String data = dataStore.get(key);
        if (data == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No data found for key: " + key)
                    .build();
        }
        return Response.ok(data).build();
    }
}