package com.github.nicksbar.libertyMonitorTools.EndPoints.tailer;


import com.github.nicksbar.libertyMonitorTools.Util.CDIRequestScopeActivator;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/logtailer/{logName}")
public class LogTailerEndpoint {

    @OnOpen
    public void onOpen(Session session, @PathParam("logName") String logName) {
        CDIRequestScopeActivator.activate(() -> {
            LogTailerManager logTailerManager = CDIRequestScopeActivator.getBean(LogTailerManager.class);
            LogFileTailer tailer = logTailerManager.getTailer(logName);
            if (tailer != null) {
                tailer.addSession(session);
            } else {
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid log name"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @OnClose
    public void onClose(Session session, @PathParam("logName") String logName) {
        CDIRequestScopeActivator.activate(() -> {
            LogTailerManager logTailerManager = CDIRequestScopeActivator.getBean(LogTailerManager.class);
            LogFileTailer tailer = logTailerManager.getTailer(logName);
            if (tailer != null) {
                tailer.removeSession(session);
            }
        });
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("logName") String logName) {
        CDIRequestScopeActivator.activate(() -> {
            LogTailerManager logTailerManager = CDIRequestScopeActivator.getBean(LogTailerManager.class);
            LogFileTailer tailer = logTailerManager.getTailer(logName);
            if (tailer != null) {
                tailer.removeSession(session);
            }
            throwable.printStackTrace();
        });
    }
}
