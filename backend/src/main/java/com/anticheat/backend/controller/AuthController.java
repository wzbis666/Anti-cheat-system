package com.anticheat.backend.controller;

import com.anticheat.backend.dto.*;
import com.anticheat.backend.model.Admin;
import com.anticheat.backend.model.User;
import com.anticheat.backend.security.JwtUtils;
import com.anticheat.backend.service.AdminService;
import com.anticheat.backend.service.UserService;
import jakarta.validation.Valid;
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
        return isRegisterRateLimited(key, registerAttempts, 5, 60);
    }

    private boolean isRegisterRateLimited(String key, Map<String, LoginAttempt> attemptMap, int maxAttempts, int windowMinutes) {
        LoginAttempt attempt = attemptMap.computeIfAbsent(key, k -> new LoginAttempt());
        long now = System.currentTimeMillis();
        if (now - attempt.lastAttemptTime > windowMinutes * 60 * 1000L) {
            attempt.count.set(0);
        }
        attempt.lastAttemptTime = now;
        return attempt.count.incrementAndGet() > maxAttempts;
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
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (isRateLimited("admin:" + username)) {
            return ResponseEntity.status(429)
                    .body(ApiResponse.fail("登录尝试过于频繁，请15分钟后再试"));
        }

        if (adminService.validateLogin(username, password)) {
            loginAttempts.remove("admin:" + username);
            Optional<Admin> adminOpt = adminService.findByUsername(username);
            if (!adminOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.fail("管理员账户异常"));
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

            Map<String, Object> result = new HashMap<>();
            result.put("userType", "admin");
            result.put("user", adminInfo);
            result.put("token", token);

            return ResponseEntity.ok(ApiResponse.ok(result, "登录成功"));
        }

        return ResponseEntity.ok(ApiResponse.fail("用户名或密码错误"));
    }

    @PostMapping("/user/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> userLogin(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (isRateLimited("user:" + username)) {
            return ResponseEntity.status(429)
                    .body(ApiResponse.fail("登录尝试过于频繁，请15分钟后再试"));
        }

        if (userService.validateLogin(username, password)) {
            loginAttempts.remove("user:" + username);
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.fail("用户账户异常"));
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

            Map<String, Object> result = new HashMap<>();
            result.put("userType", "user");
            result.put("user", userInfo);
            result.put("token", token);

            return ResponseEntity.ok(ApiResponse.ok(result, "登录成功"));
        }

        return ResponseEntity.ok(ApiResponse.fail("用户名或密码错误"));
    }

    @PostMapping("/user/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();
        String nickname = request.getNickname();

        if (isRegisterRateLimited("register:" + username)) {
            return ResponseEntity.status(429)
                    .body(ApiResponse.fail("注册尝试过于频繁，请60分钟后再试"));
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

            Map<String, Object> result = new HashMap<>();
            result.put("user", userInfo);
            result.put("token", token);

            return ResponseEntity.ok(ApiResponse.ok(result, "注册成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.fail(e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(ApiResponse.fail("No token provided"));
        }

        String token = authHeader.substring(7);

        if (jwtUtils.validateToken(token)) {
            String username = jwtUtils.getUsernameFromToken(token);
            String role = jwtUtils.getRoleFromToken(token);
            Long userId = jwtUtils.getUserIdFromToken(token);

            Map<String, Object> data = new HashMap<>();
            data.put("valid", true);
            data.put("username", username);
            data.put("role", role);
            data.put("userId", userId);

            return ResponseEntity.ok(ApiResponse.ok(data));
        }

        return ResponseEntity.ok(ApiResponse.of(false, "Invalid token", Map.of("valid", false)));
    }

    @GetMapping("/user/profile/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserProfile(@PathVariable Long id) {
        if (!isOwnerOrAdmin(id, "USER")) {
            return ResponseEntity.status(403).body(ApiResponse.fail("无权访问"));
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

            return ResponseEntity.ok(ApiResponse.ok(userInfo));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/user/profile/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (!isOwnerOrAdmin(id, "USER")) {
            return ResponseEntity.status(403).body(ApiResponse.fail("无权修改他人资料"));
        }
        try {
            User user = userService.updateProfile(id, request.getNickname(), request.getEmail(),
                    request.getAvatar(), request.getMcUsername(), request.getMcUuid());

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("nickname", user.getNickname());
            userData.put("email", user.getEmail());
            userData.put("avatar", user.getAvatar());
            userData.put("mcUsername", user.getMcUsername());
            userData.put("mcUuid", user.getMcUuid());

            return ResponseEntity.ok(ApiResponse.ok(userData, "资料更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    @PostMapping("/user/password/{id}")
    public ResponseEntity<ApiResponse<Void>> changeUserPassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (!isOwnerOrAdmin(id, "USER")) {
            return ResponseEntity.status(403).body(ApiResponse.fail("无权修改他人密码"));
        }

        if (userService.changePassword(id, request.getOldPassword(), request.getNewPassword())) {
            return ResponseEntity.ok(ApiResponse.ok(null, "密码修改成功"));
        }

        return ResponseEntity.ok(ApiResponse.fail("原密码错误"));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(@PathVariable Long id) {
        if (!isOwnerOrAdmin(id, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.fail("无权访问他人资料"));
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

            return ResponseEntity.ok(ApiResponse.ok(adminInfo));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (!isOwnerOrAdmin(id, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.fail("无权修改他人资料"));
        }
        try {
            Admin admin = adminService.updateProfile(id, request.getNickname(), request.getEmail(), request.getAvatar());

            Map<String, Object> adminData = new HashMap<>();
            adminData.put("id", admin.getId());
            adminData.put("username", admin.getUsername());
            adminData.put("nickname", admin.getNickname());
            adminData.put("email", admin.getEmail());
            adminData.put("avatar", admin.getAvatar());

            return ResponseEntity.ok(ApiResponse.ok(adminData, "资料更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    @PostMapping("/password/{id}")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (!isOwnerOrAdmin(id, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.fail("无权修改他人密码"));
        }

        if (adminService.changePassword(id, request.getOldPassword(), request.getNewPassword())) {
            return ResponseEntity.ok(ApiResponse.ok(null, "密码修改成功"));
        }

        return ResponseEntity.ok(ApiResponse.fail("原密码错误"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String username = request.getUsername();
        String userType = request.getUserType();

        try {
            if ("user".equals(userType)) {
                userService.resetPassword(username);
            } else {
                adminService.resetPassword(username);
            }
            return ResponseEntity.ok(ApiResponse.ok(null, "密码重置成功，新密码已发送至注册邮箱"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.fail(e.getMessage()));
        }
    }

    private boolean isOwnerOrAdmin(Long targetId, String targetType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isAdmin) return true;

        Object principal = auth.getPrincipal();
        if (principal instanceof String) {
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
}
