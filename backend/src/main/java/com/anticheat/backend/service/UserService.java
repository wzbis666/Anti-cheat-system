package com.anticheat.backend.service;

import com.anticheat.backend.model.User;
import com.anticheat.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User register(String username, String password, String email, String nickname) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setNickname(nickname != null && !nickname.isEmpty() ? nickname : username);
        user.setCreatedTime(System.currentTimeMillis());
        user.setActive(true);
        userRepository.save(user);
        
        logger.info("新用户注册: {}", username);
        return user;
    }

    @Transactional
    public void updateLastLoginTime(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setLastLoginTime(System.currentTimeMillis());
            userRepository.save(user);
        });
    }

    @Transactional
    public User updateProfile(Long id, String nickname, String email, String avatar, String mcUsername, String mcUuid) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (nickname != null) user.setNickname(nickname);
        if (email != null) user.setEmail(email);
        if (avatar != null) user.setAvatar(avatar);
        if (mcUsername != null) user.setMcUsername(mcUsername);
        if (mcUuid != null) user.setMcUuid(mcUuid);
        
        userRepository.save(user);
        logger.info("更新用户资料: {}", user.getUsername());
        return user;
    }

    @Transactional
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("用户修改密码: {}", user.getUsername());
        return true;
    }

    @Transactional
    public String resetPassword(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("用户 {} 密码已重置", username);
        return newPassword;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public boolean validateLogin(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isActive() && passwordEncoder.matches(password, user.getPassword())) {
                updateLastLoginTime(user.getId());
                return true;
            }
        }
        return false;
    }

    public Optional<User> bindMinecraftAccount(Long userId, String mcUsername, String mcUuid) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setMcUsername(mcUsername);
            user.setMcUuid(mcUuid);
            userRepository.save(user);
            logger.info("用户 {} 绑定Minecraft账号: {}", user.getUsername(), mcUsername);
            return Optional.of(user);
        }
        return Optional.empty();
    }
}
