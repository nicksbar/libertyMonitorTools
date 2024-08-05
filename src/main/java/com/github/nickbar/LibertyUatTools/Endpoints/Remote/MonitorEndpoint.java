package com.github.nickbar.LibertyUatTools.Endpoints.Remote;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Properties;

@Path("/remoteendpoint")
public class MonitorEndpoint {

    @Inject
    private MonitorResource propertiesService;

    @GET
    @Path("/getlist")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheckboxes() throws IOException {
        Map<String, Map<String, String>> properties = propertiesService.loadProperties();
        return Response.ok(properties).build();
    }

    @GET
    @Path("/check")
    @Produces(MediaType.TEXT_PLAIN)
    public Response checkRemoteEndpoint(@QueryParam("url") String endpointUrl, @QueryParam("host") String host, @QueryParam("port") String port) {
        if (host != null && port != null && !host.isEmpty() && !port.isEmpty()) {
            try {
                new Socket(host, Integer.parseInt(port));
                return Response.ok("200").build();
            } catch (IOException e) {
                return Response.ok("error").build();
            }
        } else if (endpointUrl != null && !endpointUrl.isEmpty()) {
            int portNumber = (port != null && !port.isEmpty()) ? Integer.parseInt(port) : -1;
            if (endpointUrl.startsWith("sftp://")) {
                return checkSftpConnection(endpointUrl, portNumber);
            } else if (endpointUrl.startsWith("ftp://")) {
                return checkFtpConnection(endpointUrl, portNumber);
            } else if (endpointUrl.startsWith("http://") || endpointUrl.startsWith("https://")) {
                return checkHttpConnection(endpointUrl, portNumber);
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Unsupported protocol").build();
            }
        }
        return Response.noContent().build();
    }

    private Response checkHttpConnection(String endpointUrl, int port) {
        Properties props = System.getProperties();
        props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());

        try {
            HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

            URI uri = (port > 0) ? new URI(endpointUrl.replaceFirst("(:\\d+)?(?=/|$)", ":" + port)) : URI.create(endpointUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return Response.ok(response.statusCode()).build();
        } catch (Exception e) {
            return Response.ok().entity(e).build();
        }
    }

    private Response checkFtpConnection(String endpointUrl, int port) {
        try {
            URL url = new URL((port > 0) ? endpointUrl.replaceFirst("(:\\d+)?(?=/|$)", ":" + port) : endpointUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("HEAD");
            int responseCode = urlConnection.getResponseCode();
            return Response.ok(responseCode).build();
        } catch (IOException e) {
            return Response.ok().entity(e).build();
        }
    }

    private Response checkSftpConnection(String endpointUrl, int port) {
        String[] parts = endpointUrl.split("://")[1].split("/");
        String host = parts[0];
        int sftpPort = (port > 0) ? port : 22; // Default SFTP port if not provided

        JSch jsch = new JSch();
        Session session = null;

        try {
            // Initialize a session with a dummy username
            session = jsch.getSession("dummy", host, sftpPort);
            session.setConfig("StrictHostKeyChecking", "no");

            // Set a dummy password to bypass login for connection test
            session.setPassword("dummy");

            // Set timeout to a reasonable value to avoid long wait times
            session.connect(10000);

            if (session.isConnected()) {
                return Response.ok("Connection successful").build();
            } else {
                return Response.ok()
                        .entity("Failed to connect")
                        .build();
            }
        } catch (JSchException e) {
            // we don't care about this failure, getting a prompt is enough
            if (e.getMessage().contains("Auth fail"))
                return Response.ok("200").build();

            return Response.ok()
                    .entity("Failed to connect: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.ok()
                    .entity("Failed to connect: " + e.getMessage())
                    .build();
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
