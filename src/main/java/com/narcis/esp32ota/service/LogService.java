package com.narcis.esp32ota.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LogService {
    private final Map<Long, List<String>> logs = new ConcurrentHashMap<>();

    public void addLog(Long boardId, String message) {
        logs.computeIfAbsent(boardId, k -> new ArrayList<>())
                .add("[" + LocalDateTime.now() + "] " + message);
    }

    public List<String> getLogs(Long boardId) {
        return logs.getOrDefault(boardId, new ArrayList<>());
    }

    public void clearLogs(Long boardId) {
        logs.remove(boardId);
    }
}