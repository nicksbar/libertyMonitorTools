package com.github.nicksbar.libertyMonitorTools.EndPoints;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ServerEndpoint(value = "/tail/{fileId}")
public class FileTailerEndPoint {

    private static final Map<String, String> tailedFiles = Map.of(
            "wlp-u1-console", "/opt/IBM/WebSphere/svc_exps/exprs-servers/wlp-u1/usr/servers/exprsApplication-u1/logs/console.log",
            "wlp-u1-exprslog", "/opt/IBM/WebSphere/svc_exps/exprs-files/u1/logs/eXPRS.log",
            "wlp-uat-console", "/opt/IBM/WebSphere/svc_exps/exprs-servers/wlp-uat/usr/servers/exprsApplication-uat/logs/console.log",
            "wlp-uat-exprslog", "/opt/IBM/WebSphere/svc_exps/exprs-files/uat/logs/eXPRS.log",
            "wlp-tr-console", "/opt/IBM/WebSphere/svc_exps/exprs-servers/wlp-tr/usr/servers/exprsApplication-tr/logs/console.log",
            "wlp-tr-exprslog", "/opt/IBM/WebSphere/svc_exps/exprs-files/tr/logs/eXPRS.log",
            "local", "logs/messages.log"
    );
    private static final Map<String, Set<Session>> sessionMap = Map.of("wlp-u1-console", new HashSet<Session>(),
            "wlp-u1-exprslog", new HashSet<Session>(),
            "wlp-uat-console", new HashSet<Session>(),
            "wlp-uat-exprslog", new HashSet<Session>(),
            "wlp-tr-console", new HashSet<Session>(),
            "wlp-tr-exprslog", new HashSet<Session>(),
            "local", new HashSet<Session>());
    private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(10);
    static long sleepTime = 1000;
    String fileKey;

    public FileTailerEndPoint() {
    }

    private static void runPerformace(Session sessions, String fileKey) {

      /*  SESSIONS.clear();
        SESSIONS.addAll(sessions.getOpenSessions());
*/
        Set<Session> scopedSessions = sessionMap.get(fileKey);

        try {

            BufferedReader input = new BufferedReader(new FileReader(tailedFiles.get(fileKey)));
            String currentLine = null;
            while (true) {
                if ((currentLine = input.readLine()) != null) {
                    String htmlContent = "<div hx-swap-oob=\"beforeend:#logContent-" + fileKey + "\">"
                            + "<p>" + currentLine + "</p>"
                            + "</div>";
                    currentLine = null;
                    sendAll(scopedSessions, htmlContent);
                    continue;
                }

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

            }
            input.close();

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static void sendAll(Set<Session> session, String htmlContent) {


        for (Session openSession : session) {

            try {
                if (openSession.isOpen()) {
                    //openSession.getBasicRemote().sendText(new Date().toString());
                    openSession.getAsyncRemote().sendText(htmlContent);
                }
            } catch (Exception ex) {
                // client session is broken or closed
                session.remove(openSession);
                //sessionMap.put(fileKey, session);
            }
        }

    }

    @OnOpen
    public void onOpen(Session session, @PathParam("fileId") String fileKey) {

        Set<Session> thisSessionFileKey = sessionMap.get(fileKey);
        thisSessionFileKey.add(session);
        //sessionMap.put(fileKey,thisSessionFileKey);

        this.fileKey = fileKey;


        // schedule timer first time
        File f = new File(tailedFiles.get(fileKey));
        if (f.exists() && !f.isDirectory() && thisSessionFileKey.size() == 1) {
            TIMER.scheduleAtFixedRate(() -> runPerformace(session, fileKey), 0, 1,
                    TimeUnit.SECONDS);
            System.out.println("Timer starting for: " + fileKey);

        } else if (!f.exists()) {

            String htmlContent = "<div hx-swap-oob=\"beforeend:#logContent-" + fileKey + "\">"
                    + "<p>File not found: " + tailedFiles.get(fileKey) + "</p>"
                    + "</div";
            Set<Session> thisSession = new HashSet<>(Set.of());
            thisSession.add(session);
            sendAll(thisSession, htmlContent);
        }

    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
       /* SESSIONS.clear();
        SESSIONS.addAll(session.getOpenSessions());*/
    }


}
