package com.narcis.esp32ota.controller;

import com.narcis.esp32ota.JwtService;
import com.narcis.esp32ota.model.User;
import com.narcis.esp32ota.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    public record RegisterRecord(String username, String email, String password) {}
    public record LoginRecord(String email, String password) {}
    public record LoginResponse(User user, String token) {}

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRecord record){
        if (userService.register(record.username(), record.email(), record.password())){
            return ResponseEntity.ok("Registered successfully!");
        }
        return ResponseEntity.badRequest().body("Couldn't register new user!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRecord record){
        Optional<User> user = userService.login(record.email(), record.password());
        if (user.isPresent()){
            String token = jwtService.generateToken(user.get().getId(), user.get().getUsername());
            return ResponseEntity.ok(new LoginResponse(user.get(), token));
        }
        return ResponseEntity.badRequest().body("Invalid credentials!");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        if (userService.verifyEmail(token)) {
            return ResponseEntity.ok("Email verified successfully!");
        }
        return ResponseEntity.badRequest().body("Invalid or expired token!");
    }

    @PostMapping("/token/regenerate")
    public ResponseEntity<?> regenerateToken(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        if (userService.regenerateApiToken(userId)) {
            Optional<User> user = userService.getUserById(userId);
            return ResponseEntity.ok(user.get().getApiToken());
        }
        return ResponseEntity.badRequest().body("Failed to regenerate token!");
    }

}
