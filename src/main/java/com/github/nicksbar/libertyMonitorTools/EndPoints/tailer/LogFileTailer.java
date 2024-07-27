package com.github.nicksbar.libertyMonitorTools.EndPoints.tailer;

import jakarta.websocket.Session;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class LogFileTailer extends TailerListenerAdapter {

    private final String logName;
    private final Set<Session> sessions = new CopyOnWriteArraySet<>();
    private final Tailer tailer;

    public LogFileTailer(String logName, String filePath) {
        this.logName = logName;
        File file = new File(filePath);
        if (!file.exists()) {
            handleError(new Exception("File not found: " + filePath));
            tailer = null;
        } else {
            this.tailer = Tailer.create(file, this, 1000);
        }
    }

    public void addSession(Session session) {
        sessions.add(session);
        if (tailer == null) {
            String htmlContent = "<div hx-swap-oob=\"beforeend:#logContent-" + logName + "\">"
                    + "<pre>Error: Log file " + logName + " not found.</pre>"
                    + "</div>";
            session.getAsyncRemote().sendText(htmlContent);

        }
    }

    public void removeSession(Session session) {
        sessions.remove(session);
    }

    @Override
    public void handle(String line) {
        for (Session session : sessions) {
            // a tab class is added so the UX can make the display better
            String htmlContent = "<div hx-swap-oob=\"beforeend:#logContent-" + logName + "\">"
                    + "<pre class=\"log " + (line.startsWith("\t") ? "tab" : "no-tab") + "\">" + line + "</pre>"
                    + "</div>";
            session.getAsyncRemote().sendText(htmlContent);
        }
    }

    @Override
    public void fileRotated() {
        for (Session session : sessions) {
            String htmlContent = "<div hx-swap-oob=\"beforeend:#logContent-" + logName + ">"
                    + "<pre>Log file " + logName + " rotated.</pre>"
                    + "</div>";
            session.getAsyncRemote().sendText(htmlContent);
        }
    }

    public void handleError(Exception ex) {
        for (Session session : sessions) {
            String htmlContent = "<div hx-swap-oob=\"beforeend:#logContent-" + logName + "\">"
                    + "<pre>" + ex.getMessage() + "</pre>"
                    + "</div>";
            session.getAsyncRemote().sendText(htmlContent);
        }
    }

    public void stopTailing() {
        if (tailer != null) {
            tailer.stop();
        }
    }
}
