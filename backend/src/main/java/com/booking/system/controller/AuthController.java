package com.booking.system.controller;

import com.booking.system.entity.Merchant;
import com.booking.system.entity.User;
import com.booking.system.mapper.MerchantRepository;
import com.booking.system.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return userRepository.findByUsername(req.getUsername())
                .map(user -> {
                    if (req.getRole() != null && !req.getRole().isBlank()) {
                        User.UserRole expected = parseRole(req.getRole());
                        if (expected == null) {
                            return ResponseEntity.status(400).body(Map.of("error", "角色无效"));
                        }
                        if (user.getRole() != expected) {
                            return ResponseEntity.status(401).body(Map.of("error", "角色不匹配"));
                        }
                    }
                    if (matchesPassword(req.getPassword(), user.getPassword())) {
                        user.setLastLoginTime(LocalDateTime.now());
                        userRepository.save(user);

                        Map<String, Object> body = new HashMap<>();
                        body.put("id", user.getId());
                        body.put("username", user.getUsername());
                        body.put("role", user.getRole().name());
                        body.put("email", user.getEmail());
                        body.put("phone", user.getPhone());
                        body.put("avatar", user.getAvatar());
                        body.put("status", user.getStatus().name());
                        body.put("token", "dev-token-" + user.getId());
                        merchantRepository.findByUser_Id(user.getId())
                                .ifPresent(m -> body.put("merchantId", m.getId()));
                        return ResponseEntity.ok(body);
                    } else {
                        return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
                    }
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "用户不存在")));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("error", "用户名不能为空"));
        }
        if (req.getUsername().length() < 3 || req.getUsername().length() > 20) {
            return ResponseEntity.status(400).body(Map.of("error", "用户名长度为3-20个字符"));
        }
        if (!req.getUsername().matches("^[a-zA-Z0-9_]+$")) {
            return ResponseEntity.status(400).body(Map.of("error", "用户名只能包含字母、数字和下划线"));
        }
        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("error", "密码不能为空"));
        }
        if (req.getPassword().length() < 6 || req.getPassword().length() > 50) {
            return ResponseEntity.status(400).body(Map.of("error", "密码长度应为6-50个字符"));
        }
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.status(400).body(Map.of("error", "用户名已存在"));
        }
        if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
            if (!req.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return ResponseEntity.status(400).body(Map.of("error", "邮箱格式不正确"));
            }
            if (userRepository.findByEmail(req.getEmail()).isPresent()) {
                return ResponseEntity.status(400).body(Map.of("error", "邮箱已被注册"));
            }
        }
        if (req.getPhone() != null && !req.getPhone().trim().isEmpty()) {
            if (!req.getPhone().matches("^1[3-9]\\d{9}$")) {
                return ResponseEntity.status(400).body(Map.of("error", "手机号格式不正确"));
            }
            if (userRepository.findByPhone(req.getPhone()).isPresent()) {
                return ResponseEntity.status(400).body(Map.of("error", "手机号已被注册"));
            }
        }

        User user = new User();
        user.setUsername(req.getUsername().trim());
        // 实际应使用加密存储
        user.setPassword(PASSWORD_ENCODER.encode(req.getPassword()));
        user.setEmail(req.getEmail() != null ? req.getEmail().trim() : null);
        user.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);
        User.UserRole role = parseRole(req.getRole());
        if (role == null) {
            return ResponseEntity.status(400).body(Map.of("error", "角色无效"));
        }
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        Long merchantId = null;
        if (savedUser.getRole() == User.UserRole.MERCHANT) {
            if (req.getMerchantName() == null || req.getMerchantName().trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "商户名称不能为空"));
            }
            if (req.getMerchantContactName() == null || req.getMerchantContactName().trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "联系人姓名不能为空"));
            }
            if (req.getMerchantContactPhone() == null || req.getMerchantContactPhone().trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "联系人电话不能为空"));
            }
            Merchant merchant = new Merchant();
            // 仅设置关联用户的 id，避免再次查询
            User merchantUser = new User();
            merchantUser.setId(savedUser.getId());
            merchant.setUser(merchantUser);
            merchant.setMerchantName(req.getMerchantName().trim());
            merchant.setContactName(req.getMerchantContactName().trim());
            merchant.setContactPhone(req.getMerchantContactPhone().trim());
            merchant.setContactEmail(req.getMerchantContactEmail() != null ? req.getMerchantContactEmail().trim() : null);
            merchant.setAddress(req.getMerchantAddress());
            merchant.setProvince(req.getMerchantProvince());
            merchant.setCity(req.getMerchantCity());
            merchant.setDistrict(req.getMerchantDistrict());
            merchant.setBusinessLicense(req.getBusinessLicense());
            merchant.setStatus(Merchant.MerchantStatus.PENDING);
            merchantId = merchantRepository.save(merchant).getId();
        }

        Map<String, Object> body = new HashMap<>();
        body.put("id", savedUser.getId());
        body.put("username", savedUser.getUsername());
        body.put("role", savedUser.getRole().name());
        if (merchantId != null) {
            body.put("merchantId", merchantId);
        }
        body.put("message", "注册成功");
        return ResponseEntity.ok(body);
    }

    private User.UserRole parseRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            return User.UserRole.USER;
        }
        try {
            return User.UserRole.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean matchesPassword(String raw, String stored) {
        if (raw == null || stored == null) {
            return false;
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return PASSWORD_ENCODER.matches(raw, stored);
        }
        return stored.equals(raw);
    }

    public static class LoginRequest {
        private String username;
        private String password;
        private String role;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
        private String role;
        private String merchantName;
        private String merchantContactName;
        private String merchantContactPhone;
        private String merchantContactEmail;
        private String merchantAddress;
        private String merchantProvince;
        private String merchantCity;
        private String merchantDistrict;
        private String businessLicense;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getMerchantName() {
            return merchantName;
        }

        public void setMerchantName(String merchantName) {
            this.merchantName = merchantName;
        }

        public String getMerchantContactName() {
            return merchantContactName;
        }

        public void setMerchantContactName(String merchantContactName) {
            this.merchantContactName = merchantContactName;
        }

        public String getMerchantContactPhone() {
            return merchantContactPhone;
        }

        public void setMerchantContactPhone(String merchantContactPhone) {
            this.merchantContactPhone = merchantContactPhone;
        }

        public String getMerchantContactEmail() {
            return merchantContactEmail;
        }

        public void setMerchantContactEmail(String merchantContactEmail) {
            this.merchantContactEmail = merchantContactEmail;
        }

        public String getMerchantAddress() {
            return merchantAddress;
        }

        public void setMerchantAddress(String merchantAddress) {
            this.merchantAddress = merchantAddress;
        }

        public String getMerchantProvince() {
            return merchantProvince;
        }

        public void setMerchantProvince(String merchantProvince) {
            this.merchantProvince = merchantProvince;
        }

        public String getMerchantCity() {
            return merchantCity;
        }

        public void setMerchantCity(String merchantCity) {
            this.merchantCity = merchantCity;
        }

        public String getMerchantDistrict() {
            return merchantDistrict;
        }

        public void setMerchantDistrict(String merchantDistrict) {
            this.merchantDistrict = merchantDistrict;
        }

        public String getBusinessLicense() {
            return businessLicense;
        }

        public void setBusinessLicense(String businessLicense) {
            this.businessLicense = businessLicense;
        }
    }
}
