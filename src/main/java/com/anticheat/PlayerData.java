package com.anticheat;

import org.bukkit.Location;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class PlayerData {

    volatile long lastMoveTime;
    volatile Location lastLocation;
    volatile float lastYaw;
    volatile float lastPitch;
    volatile double lastYCoord;

    volatile int clickCount;
    volatile long lastClickTime;
    volatile int leftClickCount;
    volatile long lastLeftClickTime;

    volatile long lastAimTime;
    volatile int aimViolationCount;

    final List<Long> attackTimestamps = new CopyOnWriteArrayList<>();
    final Set<UUID> attackedEntities = new CopyOnWriteArraySet<>();

    volatile int totalBlocksBroken;
    volatile int rareOresBroken;
    volatile long miningStartTime;

    volatile int airTime;
    volatile long lastGroundTime;
    volatile int hoverCount;
    volatile long jumpTime;

    volatile int warningCount;
    volatile String lastCheatType;
    volatile long lastCheatTime;
    volatile boolean hasReceivedWarning;
}
