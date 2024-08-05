package com.github.nickbar.LibertyUatTools.Endpoints.FileTailer;


import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
@ServerEndpoint("/sshlogtailer/{logType}")
public class SshLogTailerEndpoint {

    @Inject
    private SshLogTailerConfig logTailerConfig;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<Session, ChannelExec> sessionChannelMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("logType") String logType) {
        Map<String, String> config = logTailerConfig.getLogTailerConfig(logType);

        if (config == null) {
            session.getAsyncRemote().sendText("Invalid log type");
            return;
        }

        executorService.submit(() -> {
            JSch jsch = new JSch();
            com.jcraft.jsch.Session sshSession = null;
            ChannelExec channel = null;

            try {
                jsch.addIdentity(config.get("privateKeyPath"));
                sshSession = jsch.getSession(config.get("username"), config.get("host"), Integer.parseInt(config.get("port")));
                sshSession.setConfig("StrictHostKeyChecking", "no");
                sshSession.connect();

                String command = "tail -f " + config.get("logFilePath");
                channel = (ChannelExec) sshSession.openChannel("exec");
                channel.setCommand(command);

                InputStream inputStream = channel.getInputStream();
                channel.connect();
                sessionChannelMap.put(session, channel);

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null && channel.isConnected() && session.isOpen()) {
                        session.getAsyncRemote().sendText(line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (channel != null && channel.isConnected()) {
                    channel.disconnect();
                }
                if (sshSession != null && sshSession.isConnected()) {
                    sshSession.disconnect();
                }
                sessionChannelMap.remove(session);
            }
        });
    }

    @OnClose
    public void onClose(Session session) {
        ChannelExec channel = sessionChannelMap.get(session);
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        sessionChannelMap.remove(session);
    }
}
