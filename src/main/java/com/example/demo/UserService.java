package com.example.demo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Map<Long, User> idToUser = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    public UserService() {
        createUser("Alice", "alice@example.com", "https://unavatar.io/alice");
        createUser("Bob", "bob@example.com", "https://unavatar.io/bob");
        createUser("Charlie", "charlie@example.com", "https://unavatar.io/charlie");
    }

    public List<User> listUsers() {
        return new ArrayList<>(idToUser.values());
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return listUsers();
        }
        String q = query.toLowerCase();
        Collection<User> all = idToUser.values();
        List<User> matched = new ArrayList<>();
        for (User user : all) {
            boolean nameMatch = user.getName() != null && user.getName().toLowerCase().contains(q);
            boolean emailMatch = user.getEmail() != null && user.getEmail().toLowerCase().contains(q);
            if (nameMatch || emailMatch) {
                matched.add(user);
            }
        }
        return matched;
    }

    public Optional<User> getUser(long id) {
        return Optional.ofNullable(idToUser.get(id));
    }

    public User createUser(String name, String email, String avatarUrl) {
        long id = idSequence.incrementAndGet();
        User user = new User(id, name, email, avatarUrl, Instant.now());
        idToUser.put(id, user);
        return user;
    }

    public Optional<User> updateUser(long id, String name, String email, String avatarUrl) {
        User existing = idToUser.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        if (name != null && !name.isBlank()) {
            existing.setName(name);
        }
        if (email != null && !email.isBlank()) {
            existing.setEmail(email);
        }
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            existing.setAvatarUrl(avatarUrl);
        }
        return Optional.of(existing);
    }

    public boolean deleteUser(long id) {
        return idToUser.remove(id) != null;
    }
}

