package com.github.nicksbar.libertyMonitorTools.EndPoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

@Path("/remoteendpoint")
public class RemoteEndpointCheck {

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
        } else if (!endpointUrl.isEmpty()) {

            Properties props = System.getProperties();
            props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());


            try {
                HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpointUrl))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return Response.ok(response.statusCode()).build();
            } catch (IOException | InterruptedException e) {
                return Response.ok().entity(e).build();
            }

        }
        return Response.noContent().build();
    }
}