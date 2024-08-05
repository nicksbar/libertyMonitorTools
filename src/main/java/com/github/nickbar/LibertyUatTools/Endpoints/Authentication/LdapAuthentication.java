package com.github.nickbar.LibertyUatTools.Endpoints.Authentication;

import com.github.nickbar.LibertyUatTools.Util.ConfigUtil;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.Properties;

public class LdapAuthentication {
    public boolean UserAuthentication(String username, String password) {
        try {
            // Configure LDAP server details
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ConfigUtil.LDAP_SERVER); // Replace with your server URL
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, username); // Replace with user's DN
            env.put(Context.SECURITY_CREDENTIALS, password); // Replace with user's password

            Properties props = System.getProperties();
            props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());
            // Create an InitialDirContext
            DirContext context = new InitialDirContext(env);

            // Authentication successful
            System.out.println("User authenticated successfully!");
        } catch (Exception e) {
            // Authentication failed

            System.err.println("Authentication failed: " + e.getMessage());
            return false;
        }
        return true;
    }

}
