package com.narcis.esp32ota.controller;

import com.narcis.esp32ota.JwtService;
import com.narcis.esp32ota.model.Board;
import com.narcis.esp32ota.model.Program;
import com.narcis.esp32ota.service.BoardService;
import com.narcis.esp32ota.service.ProgramService;
import com.narcis.esp32ota.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/programs")
public class ProgramController {
    private final ProgramService programService;
    private final BoardService boardService;
    private final UserService userService;
    private final JwtService jwtService;
    public record UploadProgramRecord(Long boardId, String name, String code) {}
    public record UpdateStatusRecord(String status) {}

    public ProgramController(ProgramService programService, BoardService boardService, UserService userService, JwtService jwtService) {
        this.programService = programService;
        this.boardService = boardService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProgram(@RequestHeader("Authorization") String authHeader, @RequestBody UploadProgramRecord record){
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        Optional<Board> board = boardService.getBoardById(record.boardId());
        if (board.isEmpty()){return ResponseEntity.notFound().build();}
        if (!board.get().getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("You can only upload to your own boards!");
        }

        programService.uploadProgram(record.boardId(), userId, record.name(), record.code());
        return ResponseEntity.ok("Program uploaded successfully!");
    }

    @GetMapping("/pending/{boardId}")
    public ResponseEntity<?> getPendingProgram(@PathVariable Long boardId, @RequestHeader("X-API-TOKEN") String apiToken){
        var user = userService.getUserByApiToken(apiToken);
        if (user.isEmpty()) return ResponseEntity.status(401).body("Invalid API Token!");

        Optional<Board> board = boardService.getBoardById(boardId);
        if (board.isEmpty()) return ResponseEntity.notFound().build();
        if (!board.get().getUserId().equals(user.get().getId())) {
            return ResponseEntity.status(403).body("Forbidden!");
        }

        Optional<Program> program = programService.getPendingProgram(boardId);
        if (program.isPresent()) {
            return ResponseEntity.ok(program.get());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user")
    public ResponseEntity<List<Program>> getProgramsByUser(@RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(programService.getProgramsByUserId(userId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestHeader("X-API-TOKEN") String apiToken, @RequestBody UpdateStatusRecord record){
        var user = userService.getUserByApiToken(apiToken);
        if (user.isEmpty()) return ResponseEntity.status(401).body("Invalid API Token!");

        Optional<Program> program = programService.getProgramsByBoardId(id)
                .stream().filter(p -> p.getId().equals(id)).findFirst();

        if (program.isEmpty()) return ResponseEntity.notFound().build();

        programService.updateProgramStatus(id, record.status());
        return ResponseEntity.ok("Status updated!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProgram(@PathVariable Long id, @RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        Optional<Program> program = programService.getProgramsByUserId(userId)
                .stream().filter(p -> p.getId().equals(id)).findFirst();

        if (program.isEmpty()) return ResponseEntity.notFound().build();
        if (!program.get().getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("You can only delete your own programs!");
        }

        programService.deleteProgram(id);
        return ResponseEntity.ok("Program deleted!");
    }
}
