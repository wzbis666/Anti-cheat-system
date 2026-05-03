package com.anticheat.backend.service;

import com.anticheat.backend.model.Admin;
import com.anticheat.backend.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public Optional<Admin> findById(Long id) {
        return adminRepository.findById(id);
    }

    @Transactional
    public Admin createAdmin(String username, String password, String nickname, String email) {
        if (adminRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setNickname(nickname != null ? nickname : username);
        admin.setEmail(email);
        admin.setRole("ADMIN");
        admin.setCreatedTime(System.currentTimeMillis());
        admin.setActive(true);
        adminRepository.save(admin);
        
        logger.info("创建管理员: {}", username);
        return admin;
    }

    @Transactional
    public void updateLastLoginTime(Long id) {
        adminRepository.findById(id).ifPresent(admin -> {
            admin.setLastLoginTime(System.currentTimeMillis());
            adminRepository.save(admin);
        });
    }

    @Transactional
    public Admin updateProfile(Long id, String nickname, String email, String avatar) {
        Admin admin = adminRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        if (nickname != null) admin.setNickname(nickname);
        if (email != null) admin.setEmail(email);
        if (avatar != null) admin.setAvatar(avatar);
        
        adminRepository.save(admin);
        logger.info("更新管理员资料: {}", admin.getUsername());
        return admin;
    }

    @Transactional
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        Admin admin = adminRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            return false;
        }
        
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
        logger.info("管理员修改密码: {}", admin.getUsername());
        return true;
    }

    @Transactional
    public String resetPassword(String username) {
        Admin admin = adminRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("管理员不存在"));
        String newPassword = generateRandomPassword();
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
        logger.info("管理员 {} 密码已重置", username);
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
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (admin.isActive() && passwordEncoder.matches(password, admin.getPassword())) {
                updateLastLoginTime(admin.getId());
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void initDefaultAdmin() {
        Optional<Admin> existingAdmin = adminRepository.findByUsername("admin");
        String defaultPassword = "admin123";

        if (existingAdmin.isEmpty()) {
            createAdmin("admin", defaultPassword, "系统管理员", "admin@localhost");
            logger.error("╔══════════════════════════════════════════════════════╗");
            logger.error("║              严重安全警告                            ║");
            logger.error("║  已创建默认管理员账户: admin / admin123              ║");
            logger.error("║  这是弱密码，任何人可凭此接管整个反作弊系统！        ║");
            logger.error("║  请立即登录后台 → 个人中心 → 修改为强密码！         ║");
            logger.error("║  生产环境建议：在首次部署后删除 initDefaultAdmin() ║");
            logger.error("╚══════════════════════════════════════════════════════╝");
        } else {
            Admin admin = existingAdmin.get();
            if (!admin.getPassword().startsWith("$2")) {
                admin.setPassword(passwordEncoder.encode(defaultPassword));
                adminRepository.save(admin);
                logger.warn("管理员密码已从明文升级为 BCrypt 编码");
            }
            // 检查是否仍为默认密码
            if (passwordEncoder.matches(defaultPassword, admin.getPassword())) {
                logger.warn("============================================");
                logger.warn("提醒: 管理员 admin 仍在使用默认密码 admin123");
                logger.warn("请立即修改为强密码以提高系统安全性！");
                logger.warn("============================================");
            }
        }
    }
}
