package shayegan8.github.database;

import lombok.Getter;
import shayegan8.github.ChatPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class MDatabase {

    @Getter
    private Connection connection;

    public MDatabase() {
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/ChatPlugin/chatdb");
            PreparedStatement pStatement = connection.prepareStatement("CREATE TABLE IF NOT EXIST groups (groupName TEXT);");
            pStatement.executeUpdate();
            pStatement = connection.prepareStatement("CREATE TABLE IF NOT EXIST players (groupName TEXT references groups(groupName), playerName TEXT, playerTag TEXT, inGroup BOOLEAN);");
            pStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static CompletableFuture<String> getPlayerGroup(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("SELECT groupName FROM players WHERE playerName = '?'");
                String groupName = null;
                pState.setString(1, playerName);
                var query = pState.executeQuery();
                while (query.next())
                    groupName = query.getString("groupName");
                return groupName;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<String> getPlayerTag(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("SELECT playerTag FROM players WHERE playerName = ?");
                String playerTag = null;
                pState.setString(1, playerName);
                var query = pState.executeQuery();
                while (query.next())
                    playerTag = query.getString("playerTag");
                return playerTag;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Boolean> isPlayerInGroup(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("SELECT playerTag FROM players WHERE playerName = '?'");
                Boolean inGroup = null;
                pState.setString(1, playerName);
                var query = pState.executeQuery();
                while (query.next())
                    inGroup = query.getBoolean("inGroup");
                return inGroup;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
