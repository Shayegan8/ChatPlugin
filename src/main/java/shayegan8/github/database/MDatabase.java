package shayegan8.github.database;

import lombok.Getter;
import shayegan8.github.ChatPlugin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public class MDatabase {

    private final Connection connection;

    private final static String path = ChatPlugin.getPlugin(ChatPlugin.class).getDataFolder() + "/database";

    public MDatabase() {
        try{
            Path path_ = Paths.get(path);
            if(Files.notExists(path_))
                Files.createDirectory(path_);

            connection = DriverManager.getConnection("jdbc:sqlite:plugins/ChatPlugin/database/chatdb");
            PreparedStatement pStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS groups (groupName TEXT);");
            pStatement.executeUpdate();
            pStatement = connection
                    .prepareStatement
                            ("CREATE TABLE IF NOT EXISTS players " +
                                    "(groupName TEXT references groups(groupName) ON DELETE SET NULL," +
                                    " playerUUID BLOB, " +
                                    "playerTag TEXT, " +
                                    "inGroup BOOLEAN, " +
                                    "muted BOOLEAN, " +
                                    "invited BOOLEAN);");
            pStatement.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] convertToBlob(UUID playerUUID) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(playerUUID.getMostSignificantBits());
        buffer.putLong(playerUUID.getLeastSignificantBits());
        return buffer.array();
    }

    /**
     * @Maybe-unused
     */
    private static UUID convertToUUID(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    public static CompletableFuture<List<String>> getGroupList() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> list = new ArrayList<>();
                PreparedStatement pStatement = ChatPlugin.mDB.getConnection().prepareStatement("SELECT groupName FROM groups");
                ResultSet query = pStatement.executeQuery();
                while(query.next())
                    list.add(query.getString("groupName"));
                return list;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void createGroup(String groupName) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement pStatement = ChatPlugin.mDB.getConnection().prepareStatement("INSERT OR IGNORE INTO groups (groupname) VALUES (?)");
                pStatement.setString(1, groupName);
                pStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void deleteGroup(String groupName) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement pStatement = ChatPlugin.mDB.getConnection().prepareStatement("ALTER TABLE groups DROP COLUMN ?");
                pStatement.setString(1, groupName);
                pStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<String> getPlayerGroup(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("SELECT groupName FROM players WHERE playerUUID = ?");
                String groupName = null;
                pState.setBytes(1, convertToBlob(playerUUID));
                var query = pState.executeQuery();
                while (query.next())
                    groupName = query.getString("groupName");
                return groupName;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static String getPlayerGroupBlocking(UUID playerUUID) {
        try {
            PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("SELECT groupName FROM players WHERE playerUUID = ?");
            String groupName = null;
            pState.setBytes(1, convertToBlob(playerUUID));
            var query = pState.executeQuery();
            while (query.next())
                groupName = query.getString("groupName");
            return groupName;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setPlayerGroup(UUID playerUUID, String groupName) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("UPDATE players SET groupName=? WHERE playerUUID=?");
                pState.setString(1, groupName);
                pState.setBytes(2, convertToBlob(playerUUID));
                pState.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void setPlayerTag(UUID playerUUID, String playerTag) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("UPDATE players SET playerTag=? WHERE playerUUID=?");
                pState.setString(1, playerTag);
                pState.setBytes(2, convertToBlob(playerUUID));
                pState.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void setPlayerInGroup(UUID playerUUID, boolean inGroup) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("UPDATE players SET inGroup=? WHERE playerUUID=?");
                pState.setBoolean(1, inGroup);
                pState.setBytes(2, convertToBlob(playerUUID));
                pState.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void setPlayerMuted(UUID playerUUID, boolean muted) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("UPDATE players SET muted=? WHERE playerUUID=?");
                pState.setBoolean(1, muted);
                pState.setBytes(2, convertToBlob(playerUUID));
                pState.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void setPlayerInvited(UUID playerUUID, boolean invited) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("UPDATE players SET invited=? WHERE playerUUID=?");
                pState.setBoolean(1, invited);
                pState.setBytes(2, convertToBlob(playerUUID));
                pState.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<String> getPlayerTag(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("SELECT playerTag FROM players WHERE playerUUID=?");
                String playerTag = null;
                pState.setBytes(2, convertToBlob(playerUUID));
                ResultSet query = pState.executeQuery();
                while (query.next())
                    playerTag = query.getString("playerTag");
                return playerTag;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static String getPlayerTagBlocking(UUID playerUUID) {
        try {
            PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("SELECT playerTag FROM players WHERE playerUUID = ?");
            String playerTag = null;
            pState.setBytes(1, convertToBlob(playerUUID));
            var query = pState.executeQuery();
            while (query.next())
                playerTag = query.getString("groupName");
            return playerTag;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<Boolean> isGroupExist(String groupName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("SELECT groupName FROM groups WHERE groupName=?");
                pState.setString(1, groupName);
                ResultSet query = pState.executeQuery();
                return query.next();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Boolean> isPlayerInGroup(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("SELECT inGroup FROM players WHERE playerUUID=?");
                Boolean inGroup = null;
                pState.setBytes(2, convertToBlob(playerUUID));
                ResultSet query = pState.executeQuery();
                while (query.next())
                    inGroup = query.getBoolean("inGroup");
                return inGroup;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Boolean> isPlayerMuted(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("SELECT muted FROM players WHERE playerUUID=?");
                Boolean muted = null;
                pState.setBytes(2, convertToBlob(playerUUID));
                ResultSet query = pState.executeQuery();
                while (query.next())
                    muted = query.getBoolean("muted");
                return muted;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Boolean> isPlayerInvited(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement pState = ChatPlugin.mDB.getConnection().prepareStatement("SELECT invited FROM players WHERE playerUUID=?");
                Boolean invited = null;
                pState.setBytes(2, convertToBlob(playerUUID));
                ResultSet query = pState.executeQuery();
                while (query.next())
                    invited = query.getBoolean("invited");
                return invited;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}