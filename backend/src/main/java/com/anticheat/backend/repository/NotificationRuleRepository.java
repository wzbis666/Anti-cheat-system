package com.anticheat.backend.repository;

import com.anticheat.backend.model.NotificationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRuleRepository extends JpaRepository<NotificationRule, Long> {

    List<NotificationRule> findByEnabledTrue();

    List<NotificationRule> findByRuleType(String ruleType);
}
