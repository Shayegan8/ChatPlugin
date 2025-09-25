package shayegan8.github.commands;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CooldownManager {

    private static Map<String, Instant> waitings = new HashMap<>();

    public static void setCooldown(String playerName, Duration time) {
        waitings.put(playerName, Instant.now().plus(time));
    }

    public static boolean hasCooldown(String playerName) {
        Instant cooldown = waitings.get(playerName);
        return waitings.get(playerName) != null && Instant.now().isBefore(cooldown);
    }

    public static Instant removeInstant(String playerName) {
        return waitings.remove(playerName);
    }

    public static Duration getRemainedTime(String playerName) {
        Instant cooldown = waitings.get(playerName);
        Instant now = Instant.now();
        return cooldown != null ? Duration.between(cooldown, now) : Duration.ZERO;
    }

}
