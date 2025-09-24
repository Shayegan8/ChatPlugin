package shayegan8.github.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import shayegan8.github.ChatPlugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class Grouping implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

    }

    private void checkGroup(String name) {
        CompletableFuture.runAsync(() -> {
            try(PreparedStatement pStatement = ChatPlugin.mDB.getConnection().prepareStatement("")) {
                pStatement.execute();
            } catch (SQLException e) {
                throw new IllegalStateException(e.getMessage());
            }

        });
    }
}
