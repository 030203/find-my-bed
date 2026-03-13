package com.booking.system.controller;

import com.booking.system.entity.User;
import com.booking.system.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @GetMapping
    public ResponseEntity<List<User>> list() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody User user) {
        String err = validateUser(user, null);
        if (err != null) {
            return ResponseEntity.status(400).body(java.util.Map.of("error", err));
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.status(400).body(java.util.Map.of("error", "密码不能为空"));
        }
        user.setPassword(encodeIfNeeded(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(User.UserRole.USER);
        }
        if (user.getStatus() == null) {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody User user) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        // 先补齐缺省字段，保持原值
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            user.setUsername(existing.getUsername());
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(existing.getPassword());
        }
        if (user.getRole() == null) {
            user.setRole(existing.getRole());
        }
        if (user.getStatus() == null) {
            user.setStatus(existing.getStatus());
        }
        if (user.getAvatar() == null) {
            user.setAvatar(existing.getAvatar());
        }
        if (user.getEmail() == null) {
            user.setEmail(existing.getEmail());
        }
        if (user.getPhone() == null) {
            user.setPhone(existing.getPhone());
        }
        user.setPassword(encodeIfNeeded(user.getPassword()));
        String err = validateUser(user, id);
        if (err != null) {
            return ResponseEntity.status(400).body(java.util.Map.of("error", err));
        }
        user.setCreatedAt(existing.getCreatedAt());
        user.setId(id);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private String validateUser(User user, Long selfId) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return "用户名不能为空";
        }
        if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
            return "用户名长度应为3-20";
        }
        if (!user.getUsername().matches("^[a-zA-Z0-9_]+$")) {
            return "用户名仅支持字母数字和下划线";
        }
        Optional<User> byName = userRepository.findByUsername(user.getUsername());
        if (byName.isPresent() && !byName.get().getId().equals(selfId)) {
            return "用户名已存在";
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            if (!user.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return "邮箱格式不正确";
            }
            Optional<User> byEmail = userRepository.findByEmail(user.getEmail());
            if (byEmail.isPresent() && !byEmail.get().getId().equals(selfId)) {
                return "邮箱已存在";
            }
        }
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            if (!user.getPhone().matches("^1[3-9]\\d{9}$")) {
                return "手机号格式不正确";
            }
            Optional<User> byPhone = userRepository.findByPhone(user.getPhone());
            if (byPhone.isPresent() && !byPhone.get().getId().equals(selfId)) {
                return "手机号已存在";
            }
        }
        if (user.getRole() == null) {
            user.setRole(User.UserRole.USER);
        }
        if (user.getStatus() == null) {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        return null;
    }

    private String encodeIfNeeded(String password) {
        if (password == null || password.isBlank()) {
            return password;
        }
        if (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) {
            return password;
        }
        return PASSWORD_ENCODER.encode(password);
    }
}
