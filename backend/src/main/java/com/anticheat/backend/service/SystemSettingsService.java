package com.anticheat.backend.service;

import com.anticheat.backend.model.SystemSettings;
import com.anticheat.backend.repository.SystemSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class SystemSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(SystemSettingsService.class);

    private final SystemSettingsRepository settingsRepository;

    @Autowired
    public SystemSettingsService(SystemSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public String getSetting(String key, String defaultValue) {
        return settingsRepository.findBySettingKey(key)
                .map(SystemSettings::getSettingValue)
                .orElse(defaultValue);
    }

    public int getIntSetting(String key, int defaultValue) {
        try {
            return Integer.parseInt(getSetting(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBooleanSetting(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getSetting(key, String.valueOf(defaultValue)));
    }

    public void setSetting(String key, String value, String description) {
        Optional<SystemSettings> existing = settingsRepository.findBySettingKey(key);

        if (existing.isPresent()) {
            SystemSettings settings = existing.get();
            settings.setSettingValue(value);
            settingsRepository.save(settings);
        } else {
            SystemSettings settings = new SystemSettings(key, value, description);
            settingsRepository.save(settings);
        }

        logger.info("更新系统设置: {} = {}", key, value);
    }

    public void setIntSetting(String key, int value, String description) {
        setSetting(key, String.valueOf(value), description);
    }

    public void setBooleanSetting(String key, boolean value, String description) {
        setSetting(key, String.valueOf(value), description);
    }

    public Map<String, Object> getAllSettings() {
        Map<String, Object> settings = new HashMap<>();
        List<SystemSettings> allSettings = settingsRepository.findAll();

        for (SystemSettings s : allSettings) {
            settings.put(s.getSettingKey(), parseValue(s.getSettingValue()));
        }

        return settings;
    }

    public void updateSettings(Map<String, Object> settings) {
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            setSetting(entry.getKey(), String.valueOf(entry.getValue()), null);
        }
    }

    private Object parseValue(String value) {
        if (value == null) return null;

        // 尝试解析为布尔值
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }

        // 尝试解析为整数
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // 不是整数，返回字符串
        }

        return value;
    }

    public void initDefaultSettings() {
        if (settingsRepository.count() == 0) {
            setBooleanSetting("detect.fly", true, "启用飞行作弊检测");
            setBooleanSetting("detect.speed", true, "启用速度作弊检测");
            setBooleanSetting("detect.autoclick", true, "启用自动点击检测");
            setBooleanSetting("detect.fly_permission", true, "启用飞行权限检测");
            setIntSetting("threshold.autoclick", 20, "自动点击阈值（次/秒）");
            setIntSetting("threshold.speed", 10, "速度作弊阈值（m/s）");
            setSetting("punishment.strategy", "kick", "惩罚策略: warning/kick/ban");
            setIntSetting("threshold.violation", 3, "违规次数阈值");
            logger.info("初始化默认系统设置完成");
        }
    }
}
