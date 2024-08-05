package com.github.nickbar.LibertyUatTools.Endpoints.FileDownload;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("/log")
@ApplicationScoped
public class LogFileDownloader {

    @Inject
    private LogFileDownloaderResource textFileConfig;

    @GET
    @Path("/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getFileContent(@PathParam("key") String key) {
        String filePath = textFileConfig.getFilePath(key);
        if (filePath == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("File not found for key: " + key).build();
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            return Response.ok(content).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error reading file: " + e.getMessage()).build();
        }
    }
}