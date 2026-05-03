package com.anticheat.backend.controller;

import com.anticheat.backend.model.Admin;
import com.anticheat.backend.model.User;
import com.anticheat.backend.security.JwtUtils;
import com.anticheat.backend.service.AdminService;
import com.anticheat.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, LoginAttempt> registerAttempts = new ConcurrentHashMap<>();

    private static class LoginAttempt {
        AtomicInteger count = new AtomicInteger(0);
        volatile long lastAttemptTime = 0;
    }

    private boolean isRateLimited(String key) {
        return isRateLimited(key, loginAttempts, 10, 15);
    }

    private boolean isRegisterRateLimited(String key) {
        return isRateLimited(key, registerAttempts, 5, 60);
    }

    private boolean isRateLimited(String key, Map<String, LoginAttempt> attemptMap, int maxAttempts, int windowMinutes) {
        LoginAttempt attempt = attemptMap.computeIfAbsent(key, k -> new LoginAttempt());
        long now = System.currentTimeMillis();
        if (now - attempt.lastAttemptTime > windowMinutes * 60 * 1000L) {
            attempt.count.set(0);
        }
        attempt.lastAttemptTime = now;
        return attempt.count.incrementAndGet() > maxAttempts;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户名和密码不能为空"));
        }

        if (isRateLimited("admin:" + username)) {
            return ResponseEntity.status(429).body(Map.of("success", false, "message", "登录尝试过于频繁，请15分钟后再试"));
        }

        if (adminService.validateLogin(username, password)) {
            loginAttempts.remove("admin:" + username);
            Optional<Admin> adminOpt = adminService.findByUsername(username);
            if (!adminOpt.isPresent()) {
                return ResponseEntity.ok(Map.of("success", false, "message", "管理员账户异常"));
            }
            Admin admin = adminOpt.get();

            String token = jwtUtils.generateToken(username, admin.getRole(), admin.getId());

            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("id", admin.getId());
            adminInfo.put("username", admin.getUsername());
            adminInfo.put("nickname", admin.getNickname());
            adminInfo.put("email", admin.getEmail());
            adminInfo.put("avatar", admin.getAvatar());
            adminInfo.put("role", admin.getRole());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "登录成功",
                "userType", "admin",
                "user", adminInfo,
                "token", token
            ));
        }

        return ResponseEntity.ok(Map.of("success", false, "message", "用户名或密码错误"));
    }

    @PostMapping("/user/login")
    public ResponseEntity<Map<String, Object>> userLogin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户名和密码不能为空"));
        }

        if (isRateLimited("user:" + username)) {
            return ResponseEntity.status(429).body(Map.of("success", false, "message", "登录尝试过于频繁，请15分钟后再试"));
        }

        if (userService.validateLogin(username, password)) {
            loginAttempts.remove("user:" + username);
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.ok(Map.of("success", false, "message", "用户账户异常"));
            }
            User user = userOpt.get();

            String token = jwtUtils.generateToken(username, "USER", user.getId());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("email", user.getEmail());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("mcUsername", user.getMcUsername());
            userInfo.put("mcUuid", user.getMcUuid());
            userInfo.put("userType", "user");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "登录成功",
                "userType", "user",
                "user", userInfo,
                "token", token
            ));
        }

        return ResponseEntity.ok(Map.of("success", false, "message", "用户名或密码错误"));
    }

    @PostMapping("/user/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String email = request.get("email");
        String nickname = request.get("nickname");

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户名不能为空"));
        }

        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "密码长度不能少于6位"));
        }

        if (isRegisterRateLimited("register:" + username)) {
            return ResponseEntity.status(429).body(Map.of("success", false, "message", "注册尝试过于频繁，请60分钟后再试"));
        }

        try {
            User user = userService.register(username, password, email, nickname);

            String token = jwtUtils.generateToken(username, "USER", user.getId());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("email", user.getEmail());
            userInfo.put("userType", "user");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "注册成功",
                "user", userInfo,
                "token", token
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "No token provided"));
        }

        String token = authHeader.substring(7);

        if (jwtUtils.validateToken(token)) {
            String username = jwtUtils.getUsernameFromToken(token);
            String role = jwtUtils.getRoleFromToken(token);
            Long userId = jwtUtils.getUserIdFromToken(token);

            return ResponseEntity.ok(Map.of(
                "valid", true,
                "username", username,
                "role", role,
                "userId", userId
            ));
        }

        return ResponseEntity.ok(Map.of("valid", false, "message", "Invalid token"));
    }

    @GetMapping("/user/profile/{id}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable Long id) {
        if (!isOwnerOrAdmin(id, "USER")) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权访问"));
        }
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("email", user.getEmail());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("mcUsername", user.getMcUsername());
            userInfo.put("mcUuid", user.getMcUuid());
            userInfo.put("createdTime", user.getCreatedTime());
            userInfo.put("lastLoginTime", user.getLastLoginTime());
            userInfo.put("userType", "user");

            return ResponseEntity.ok(userInfo);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/user/profile/{id}")
    public ResponseEntity<Map<String, Object>> updateUserProfile(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        if (!isOwnerOrAdmin(id, "USER")) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权修改他人资料"));
        }
        try {
            String nickname = request.get("nickname");
            String email = request.get("email");
            String avatar = request.get("avatar");
            String mcUsername = request.get("mcUsername");
            String mcUuid = request.get("mcUuid");

            User user = userService.updateProfile(id, nickname, email, avatar, mcUsername, mcUuid);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "资料更新成功",
                "user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "nickname", user.getNickname(),
                    "email", user.getEmail(),
                    "avatar", user.getAvatar(),
                    "mcUsername", user.getMcUsername(),
                    "mcUuid", user.getMcUuid()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/user/password/{id}")
    public ResponseEntity<Map<String, Object>> changeUserPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        if (!isOwnerOrAdmin(id, "USER")) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权修改他人密码"));
        }
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "密码不能为空"));
        }

        if (userService.changePassword(id, oldPassword, newPassword)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "密码修改成功"));
        }

        return ResponseEntity.ok(Map.of("success", false, "message", "原密码错误"));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long id) {
        if (!isOwnerOrAdmin(id, "ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权访问他人资料"));
        }
        Optional<Admin> adminOpt = adminService.findById(id);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("id", admin.getId());
            adminInfo.put("username", admin.getUsername());
            adminInfo.put("nickname", admin.getNickname());
            adminInfo.put("email", admin.getEmail());
            adminInfo.put("avatar", admin.getAvatar());
            adminInfo.put("role", admin.getRole());
            adminInfo.put("createdTime", admin.getCreatedTime());
            adminInfo.put("lastLoginTime", admin.getLastLoginTime());

            return ResponseEntity.ok(adminInfo);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/profile/{id}")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        if (!isOwnerOrAdmin(id, "ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权修改他人资料"));
        }
        try {
            String nickname = request.get("nickname");
            String email = request.get("email");
            String avatar = request.get("avatar");

            Admin admin = adminService.updateProfile(id, nickname, email, avatar);

            Map<String, Object> adminData = new HashMap<>();
            adminData.put("id", admin.getId());
            adminData.put("username", admin.getUsername());
            adminData.put("nickname", admin.getNickname());
            adminData.put("email", admin.getEmail());
            adminData.put("avatar", admin.getAvatar());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "资料更新成功");
            result.put("admin", adminData);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/password/{id}")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        if (!isOwnerOrAdmin(id, "ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权修改他人密码"));
        }
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "密码不能为空"));
        }

        if (adminService.changePassword(id, oldPassword, newPassword)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "密码修改成功"));
        }

        return ResponseEntity.ok(Map.of("success", false, "message", "原密码错误"));
    }

    private boolean isOwnerOrAdmin(Long targetId, String targetType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isAdmin) return true;

        Object principal = auth.getPrincipal();
        if (principal instanceof String) {
            String username = (String) principal;
            try {
                String token = getTokenFromContext();
                if (token != null) {
                    Long currentUserId = jwtUtils.getUserIdFromToken(token);
                    String currentRole = jwtUtils.getRoleFromToken(token);
                    if (targetType.equals("USER") && "USER".equals(currentRole)) {
                        return currentUserId.equals(targetId);
                    }
                    if (targetType.equals("ADMIN") && ("ADMIN".equals(currentRole) || "SUPER_ADMIN".equals(currentRole))) {
                        return currentUserId.equals(targetId);
                    }
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    private String getTokenFromContext() {
        jakarta.servlet.http.HttpServletRequest request =
                ((org.springframework.web.context.request.ServletRequestAttributes)
                        org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String userType = request.getOrDefault("userType", "admin");
        
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "请输入用户名"));
        }
        
        try {
            String newPassword;
            if ("user".equals(userType)) {
                newPassword = userService.resetPassword(username);
            } else {
                newPassword = adminService.resetPassword(username);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "密码重置成功");
            result.put("newPassword", newPassword);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
