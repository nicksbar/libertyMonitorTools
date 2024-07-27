package com.github.nicksbar.libertyMonitorTools.EndPoints;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * TO-DO: This has a bug where if you redeploy the app on a running server, i.e. just replace the war
 * A secondary process pushing stats to the clients is started until the server is restarted. Needs more cleanup.
 */
@ServerEndpoint(value = "/systemLoad")
public class SystemLoad {


    private static final Set<Session> SESSIONS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor();
    int threadCount = 0;
    OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    public SystemLoad() {

        ThreadMXBean newBean = ManagementFactory.getThreadMXBean();
        if (newBean.isThreadCpuTimeSupported())
            newBean.setThreadCpuTimeEnabled(true);

    }

    public static Double getProcessCpuLoad() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

            return Optional.ofNullable(list)
                    .map(l -> l.isEmpty() ? null : l)
                    .map(List::iterator)
                    .map(Iterator::next)
                    .map(Attribute.class::cast)
                    .map(Attribute::getValue)
                    .map(Double.class::cast)
                    .orElse(null);

        } catch (Exception ex) {
            return null;
        }
    }

    @OnOpen
    public void onOpen(final Session session) {
        SESSIONS.add(session);

        // schedule timer first time
        if (SESSIONS.size() == 1) {
            TIMER.scheduleAtFixedRate(() -> runPerformace(session), 0, 1,
                    TimeUnit.SECONDS);
        }
    }

    private void runPerformace(Session sessions) {

        SESSIONS.clear();
        SESSIONS.addAll(sessions.getOpenSessions());


        String loadString = Double.toString(getProcessCpuLoad());
        //loadString = loadString.substring(0, loadString.indexOf(".") + 2);


        sendAll(sessions, "{\"cpu\":\"" + loadString
                + "\", \"cpuAvg\":\"" + osBean.getSystemLoadAverage()
                + "\",\"cpuCount\":\"" + osBean.getAvailableProcessors()
                + "\""
                + "}");

    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        SESSIONS.clear();
        SESSIONS.addAll(session.getOpenSessions());
    }

    public void sendAll(Session session, String json) {
        SESSIONS.clear();
        SESSIONS.addAll(session.getOpenSessions());

        for (Session openSession : SESSIONS) {
            try {
                if (openSession.isOpen()) {
                    //openSession.getBasicRemote().sendText(new Date().toString());
                    //System.out.println(json);
                    openSession.getAsyncRemote().sendText("<div id=\"systemperformance\">" + json + "</div>");
                }
            } catch (Exception ex) {
                // client session is broken or closed
                // System.out.println(ex);
                SESSIONS.remove(openSession);
            }
        }

    }


}