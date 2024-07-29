# Monitoring utilities - Liberty edition

**What it do...**

- Allows for the independent tailing of many logs on the host system (tail -f)
- Outputs tailed logs as a websocket to any connected clients
- Basic CPU load monitoring
- URL & Direct socket test from host server - Useful if you need to test connectivity to another host from the server
- Uses HTMX and Hyperscript to interact with the application services

**THIS IS NOT FOR PRODUCTION USE**

- I use this during our internal UAT cycle and when troubleshooting.
- This provides no security or authentication.

**How do I use it?**

1. Modify the _tailFiles.properties_ and add any key-name:/path/to/log/file.log
    - You can add as many as you want, messages will be distributed to the correct subscribers
2. Modify _server.xml_ and change the ports assigned to suit your needs.
3. Ensure a JRE is available, or a path specified in _wlp/java/java.env_
4. Run _liberty:dev_ or start your server using _wlp/bin/server[.bat] start_
