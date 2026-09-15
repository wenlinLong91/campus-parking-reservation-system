package com.segi.parking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5175")
public class LoginController {

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public LoginController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> loginRequest) {

        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "code", 400,
                            "message", "Username and password are required."
                    )
            );
        }

        String sql =
                "SELECT username, password, role FROM sys_user WHERE username = ?";

        List<Map<String, Object>> users =
                jdbcTemplate.queryForList(sql, username);

        if (users.isEmpty()) {
            return invalidCredentialsResponse();
        }

        Map<String, Object> databaseUser = users.get(0);
        String storedPassword =
                String.valueOf(databaseUser.get("password"));
        String role =
                String.valueOf(databaseUser.get("role"));

        if (!passwordEncoder.matches(password, storedPassword)) {
            return invalidCredentialsResponse();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("username", username);
        response.put("role", role.toUpperCase());

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>>
            invalidCredentialsResponse() {

        Map<String, Object> response = new HashMap<>();
        response.put("code", 401);
        response.put(
                "message",
                "Invalid username or password."
        );

        return ResponseEntity.status(401).body(response);
    }
}