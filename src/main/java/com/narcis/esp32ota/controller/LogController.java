package com.narcis.esp32ota.controller;

import com.narcis.esp32ota.JwtService;
import com.narcis.esp32ota.model.Board;
import com.narcis.esp32ota.service.BoardService;
import com.narcis.esp32ota.service.LogService;
import com.narcis.esp32ota.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/log")
public class LogController {
    private final LogService logService;
    private final BoardService boardService;
    private final UserService userService;
    private final JwtService jwtService;
    public record LogRecord(String message) {}

    public LogController(LogService logService, BoardService boardService, UserService userService, JwtService jwtService) {
        this.logService = logService;
        this.boardService = boardService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/{boardId}")
    public ResponseEntity<String> addLog(@PathVariable Long boardId, @RequestHeader("X-API-TOKEN") String apiToken, @RequestBody LogRecord record){
        var user = userService.getUserByApiToken(apiToken);
        if (user.isEmpty()) return ResponseEntity.status(401).body("Invalid API Token!");

        Optional<Board> board = boardService.getBoardById(boardId);
        if (board.isEmpty()) return ResponseEntity.notFound().build();
        if (!board.get().getUserId().equals(user.get().getId())) {
            return ResponseEntity.status(403).body("Forbidden!");
        }

        logService.addLog(boardId, record.message());
        return ResponseEntity.ok("Log added!");
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<List<String>> getLogs(@PathVariable Long boardId, @RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        Optional<Board> board = boardService.getBoardById(boardId);
        if (board.isEmpty()) return ResponseEntity.notFound().build();
        if (!board.get().getUserId().equals(userId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(logService.getLogs(boardId));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> clearLogs(@PathVariable Long boardId, @RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        Optional<Board> board = boardService.getBoardById(boardId);
        if (board.isEmpty()) return ResponseEntity.notFound().build();
        if (!board.get().getUserId().equals(userId)) return ResponseEntity.status(403).body("Forbidden!");
        logService.clearLogs(boardId);
        return ResponseEntity.ok("Logs cleared!");
    }
}
