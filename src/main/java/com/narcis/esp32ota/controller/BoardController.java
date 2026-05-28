package com.narcis.esp32ota.controller;

import com.narcis.esp32ota.JwtService;
import com.narcis.esp32ota.model.Board;
import com.narcis.esp32ota.service.BoardService;
import com.narcis.esp32ota.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService boardService;
    private final UserService userService;
    private final JwtService jwtService;
    public record RegisterBoard(String name, String chipModel, Integer chipRevision, Integer cpuFreqMhz, Integer flashSize, Integer heapSize, String macAddress) {}

    public BoardController(BoardService boardService, UserService userService, JwtService jwtService) {
        this.boardService = boardService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerBoard(@RequestHeader("X-API-TOKEN") String apiToken, @RequestBody RegisterBoard record){
        var user = userService.getUserByApiToken(apiToken);
        if (user.isEmpty()){
            return ResponseEntity.status(401).body("Invalid API Token!");
        }
        //if (!user.get().getVerified()){
        //    return ResponseEntity.status(403).body("Email not verified!");
        //}

        boolean registered = boardService.registerBoard(user.get().getId(), record.name(), record.chipModel(), record.chipRevision(), record.cpuFreqMhz(), record.flashSize(), record.heapSize(), record.macAddress());
        if (registered){return ResponseEntity.ok("Board registered successfully!");}
        return ResponseEntity.badRequest().body("Board limit reached!");
    }

    @GetMapping
    public ResponseEntity<List<Board>> getBoardByUser(@RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(boardService.getBoardsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardById(@PathVariable Long id, @RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserId(authHeader.substring(7));

        Optional<Board> board = boardService.getBoardById(id);
        if (board.isEmpty()) return ResponseEntity.notFound().build();
        if (!board.get().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(board.get());
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<String> heartbeat(@PathVariable Long id, @RequestHeader("X-API-TOKEN") String apiToken){
        var user = userService.getUserByApiToken(apiToken);
        if (user.isEmpty()) return ResponseEntity.status(401).body("Invalid API Token!");

        Optional<Board> board = boardService.getBoardById(id);
        if (board.isEmpty()) return ResponseEntity.notFound().build();
        if (!board.get().getUserId().equals(user.get().getId())) {
            return ResponseEntity.status(403).body("Forbidden!");
        }

        boardService.updateLastSeen(id);
        boardService.updateStatus(id, "ONLINE");
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/my")
    public ResponseEntity<List<Board>> getBoardsByApiToken(
            @RequestHeader("X-API-TOKEN") String apiToken) {
        var user = userService.getUserByApiToken(apiToken);
        if (user.isEmpty()) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(boardService.getBoardsByUserId(user.get().getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBoard(@PathVariable Long id, @RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        Optional<Board> board = boardService.getBoardById(id);
        if (board.isEmpty()) return ResponseEntity.notFound().build();
        if (!board.get().getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("You can only delete your own boards!");
        }

        boardService.deleteBoard(id);
        return ResponseEntity.ok("Board deleted!");
    }
}
