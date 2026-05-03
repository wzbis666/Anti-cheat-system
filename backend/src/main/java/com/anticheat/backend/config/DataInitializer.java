package com.anticheat.backend.config;

import com.anticheat.backend.service.AdminService;
import com.anticheat.backend.service.SystemSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final SystemSettingsService settingsService;
    private final AdminService adminService;

    @Autowired
    public DataInitializer(SystemSettingsService settingsService, AdminService adminService) {
        this.settingsService = settingsService;
        this.adminService = adminService;
    }

    @Override
    public void run(String... args) {
        logger.info("初始化系统数据...");
        settingsService.initDefaultSettings();
        adminService.initDefaultAdmin();
        logger.info("系统数据初始化完成");
    }
}
