package com.github.nickbar.LibertyUatTools.Endpoints.Performance;

import jakarta.websocket.Session;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class SystemPerformanceInfo implements Runnable {
    private final SystemInfo systemInfo;
    private final Set<Session> sessions;
    private final CentralProcessor processor;
    private final GlobalMemory memory;
    private final Map<Integer, Long> previousProcessCpuTimes;
    private long[] prevTicks;
    private long[][] prevProcessorTicks;
    private Timer timer;

    public SystemPerformanceInfo() {
        this.systemInfo = new SystemInfo();
        this.sessions = new CopyOnWriteArraySet<>();
        this.processor = systemInfo.getHardware().getProcessor();
        this.memory = systemInfo.getHardware().getMemory();
        this.prevTicks = processor.getSystemCpuLoadTicks();
        this.prevProcessorTicks = processor.getProcessorCpuLoadTicks();
        this.previousProcessCpuTimes = new HashMap<>();
    }

    public double getCpuLoad() {
        long[] ticks = processor.getSystemCpuLoadTicks();
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = ticks;
        return cpuLoad;
    }

    public double getAverageCpuLoad() {
        long[][] processorTicks = processor.getProcessorCpuLoadTicks();
        double[] load = processor.getProcessorCpuLoadBetweenTicks(prevProcessorTicks);
        double avgLoad = Arrays.stream(load).average().orElse(0.0) * 100;
        prevProcessorTicks = processorTicks;
        return avgLoad;
    }

    public long getTotalMemory() {
        return memory.getTotal();
    }

    public long getUsedMemory() {
        return memory.getTotal() - memory.getAvailable();
    }

    public List<String> getUserProcesses() {
        OperatingSystem os = systemInfo.getOperatingSystem();
        long systemUptime = os.getSystemUptime();
        List<OSProcess> processes = os.getProcesses(null, OperatingSystem.ProcessSorting.CPU_DESC, 0);
        return processes.stream()
                .filter(p -> p.getUser().equals(System.getProperty("user.name")))
                .map(p -> String.format("%s (PID %d) %s CPU: %.2f%% MEM: %s",
                        p.getName(), p.getProcessID(), p.getUser(),
                        100d * (p.getKernelTime() + p.getUserTime()) / systemUptime,
                        FormatUtil.formatBytes(p.getResidentSetSize())))
                .collect(Collectors.toList());
    }

    public String getSystemPerformanceInfo() {
        String sb = "<div id=\"systemperformance\">CPU Load: " + getCpuLoad() + "%\n" +
                "Average CPU Load: " + getAverageCpuLoad() + "%\n" +
                "Total Memory: " + FormatUtil.formatBytes(getTotalMemory()) + "\n" +
                "Used Memory: " + FormatUtil.formatBytes(getUsedMemory()) + "\n</div>";
        // sb.append("User Processes:\n");
        // getUserProcesses().forEach(process -> sb.append(process).append("\n"));
        return sb;
    }

    public void addSession(Session session) {
        sessions.add(session);
        if (sessions.size() == 1) {
            startTelemetry();
        }
    }

    public void removeSession(Session session) {
        sessions.remove(session);
        if (sessions.isEmpty()) {
            stopTelemetry();
        }
    }

    public void startTelemetry() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                broadcastTelemetry();
            }
        }, 0, 5000); // Send telemetry every 5 seconds
    }

    public void stopTelemetry() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void broadcastTelemetry() {
        String telemetry = getSystemPerformanceInfo();
        for (Session session : sessions) {
            session.getAsyncRemote().sendText(telemetry);
        }
    }

    @Override
    public void run() {
        startTelemetry();
    }
}