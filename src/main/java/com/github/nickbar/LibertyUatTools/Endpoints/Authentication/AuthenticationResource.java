package com.github.nickbar.LibertyUatTools.Endpoints.Authentication;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/authenticate")
public class AuthenticationResource {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticate(@FormParam("username") String username,
                                 @FormParam("password") String password) {
        LdapAuthentication ldapAuth = new LdapAuthentication();
        boolean isAuthenticated = ldapAuth.UserAuthentication(username, password);

        if (isAuthenticated) {
            return Response.ok("{\"message\": \"User authenticated successfully\"}").build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Authentication failed\"}")
                    .build();
        }
    }
}

