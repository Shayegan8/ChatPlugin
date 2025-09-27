package shayegan8.github.commands;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CooldownManager {

    private final Map<String, Instant> waitings = new HashMap<>();

    public void setCooldown(String playerName, Duration time) {
        waitings.put(playerName, Instant.now().plus(time));
    }

    public boolean hasCooldown(String playerName) {
        return waitings.get(playerName) != null && Instant.now().isBefore(waitings.get(playerName));
    }
    
    public Long getRemained(String playerName) {
        return Duration.between(waitings.get(playerName), Instant.now()).getSeconds();
    }
}
