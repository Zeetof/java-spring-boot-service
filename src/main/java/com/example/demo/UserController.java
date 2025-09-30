package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Backward-compatible simple list
    @GetMapping("/users")
    public List<Map<String, String>> getUsers() {
        return userService.listUsers().stream()
                .map(u -> Map.of(
                        "id", String.valueOf(u.getId()),
                        "name", u.getName() == null ? "" : u.getName()
                ))
                .toList();
    }

    // New creative API
    @GetMapping("/api/users")
    public List<User> list(@RequestParam(value = "q", required = false) String query) {
        return userService.searchUsers(query);
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<User> get(@PathVariable("id") long id) {
        Optional<User> user = userService.getUser(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/api/users")
    public ResponseEntity<User> create(@RequestBody UserCreateRequest request) {
        if (request == null || request.name == null || request.name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User created = userService.createUser(request.name, request.email, request.avatarUrl);
        return ResponseEntity.created(URI.create("/api/users/" + created.getId())).body(created);
    }

    @PutMapping("/api/users/{id}")
    public ResponseEntity<User> update(@PathVariable("id") long id, @RequestBody UserUpdateRequest request) {
        Optional<User> updated = userService.updateUser(id, request == null ? null : request.name,
                request == null ? null : request.email,
                request == null ? null : request.avatarUrl);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        boolean removed = userService.deleteUser(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    public static class UserCreateRequest {
        public String name;
        public String email;
        public String avatarUrl;
    }

    public static class UserUpdateRequest {
        public String name;
        public String email;
        public String avatarUrl;
    }
}
