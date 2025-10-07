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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
public class MDatabase {

	private Connection connection = null;
	private final String name;

	private final static String PATH = ChatPlugin.getInstance().getDataFolder() + "/database";

	public MDatabase(String name) {
		this.name = name;
		try {
			final Path path_ = Paths.get(PATH);
			if (Files.notExists(path_))
				Files.createDirectory(path_);
			switch (name) {
			case "sqlite":
				connection = DriverManager.getConnection("jdbc:sqlite:plugins/ChatPlugin/database/chatdb");
				break;
			case "postgresql":
				connection = DriverManager.getConnection(
						"jdbc:postgresql://" + ChatPlugin.configuration.getString("db.host",
								"localhost" + ":" + ChatPlugin.configuration.getString("db.port", "3306") + "/"
										+ ChatPlugin.configuration.getString("db.name", "chatp")),
						ChatPlugin.configuration.getString("db.userName", "test"),
						ChatPlugin.configuration.getString("db.password", "1234"));
				break;
			}
			PreparedStatement pStatement = connection
					.prepareStatement("CREATE TABLE IF NOT EXISTS groups (groupName TEXT);");
			pStatement.executeUpdate();
			pStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS players "
					+ "(groupName TEXT references groups(groupName) ON DELETE SET DEFAULT,"
					+ " playerUUID BLOB PRIMARY KEY, " + "playerTag TEXT, " + "inGroup BOOLEAN, " + "muted BOOLEAN, "
					+ "invited BOOLEAN, requested TEXT);");
			pStatement.executeUpdate();
			pStatement = connection.prepareStatement("INSERT INTO groups (groupName) VALUES (?)");
			pStatement.setString(1, "none");
			pStatement.executeUpdate();
		} catch (SQLException | IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static UUID convertToUUID(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		long mostSignificantBits = buffer.getLong();
		long leastSignificantBits = buffer.getLong();
		return new UUID(mostSignificantBits, leastSignificantBits);
	}

	public static CompletableFuture<List<String>> getGroupList() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				final List<String> list = new ArrayList<>();
				PreparedStatement pStatement = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT groupName FROM groups");
				ResultSet query = pStatement.executeQuery();
				while (query.next())
					list.add(query.getString("groupName"));
				return Collections.unmodifiableList(list);
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static void createGroup(String groupName) {
		CompletableFuture.runAsync(() -> {
			try {
				PreparedStatement pStatement = ChatPlugin.mDB.getConnection().prepareStatement(
						"INSERT INTO groups SELECT ? WHERE NOT EXISTS (SELECT 1 FROM groups WHERE groupName = ?)");
				pStatement.setString(1, groupName);
				pStatement.setString(2, groupName);
				pStatement.executeUpdate();
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static CompletableFuture<Boolean> isGroupExist(String groupName) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				boolean check = false;
				PreparedStatement pStatement = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT groupName FROM groups WHERE groupName=?");
				pStatement.setString(1, groupName);
				ResultSet query = pStatement.executeQuery();
				while (query.next())
					check = true;
				return check;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static void deleteGroup(String groupName) {
		CompletableFuture.runAsync(() -> {
			try {
				PreparedStatement pStatement = ChatPlugin.mDB.getConnection()
						.prepareStatement("DELETE FROM groups WHERE groupName=?");
				pStatement.setString(1, groupName);
				pStatement.executeUpdate();
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static CompletableFuture<String> getPlayerGroup(UUID playerUUID) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT groupName FROM players WHERE playerUUID = ?");
				String groupName = null;
				pState.setBytes(1, convertToBlob(playerUUID));
				var query = pState.executeQuery();
				while (query.next())
					groupName = query.getString("groupName");
				return groupName;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));

			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static CompletableFuture<List<UUID>> getPlayersByGroup(String groupName) {
		return CompletableFuture.supplyAsync(() -> {
			List<UUID> list = new ArrayList<UUID>();
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT playerUUID FROM players WHERE groupName = ?");
				pState.setString(1, groupName);
				var query = pState.executeQuery();
				while (query.next())
					list.add(convertToUUID(query.getBytes("playerUUID")));
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
			return list.stream().collect(Collectors.toUnmodifiableList());
		}, ChatPlugin.EVIRTUAL);
	}

	public static CompletableFuture<Boolean> playerHasGroup(UUID playerUUID) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT groupName FROM players WHERE playerUUID = ?");
				pState.setBytes(1, convertToBlob(playerUUID));
				var has = false;
				var query = pState.executeQuery();
				while (!query.next())
					has = true;
				return has;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));

			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static void setPlayerGroup(UUID playerUUID, String groupName) {
		CompletableFuture.runAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("INSERT INTO players (groupName, playerUUID) VALUES (?, ?)"
								+ " ON CONFLICT(playerUUID) DO UPDATE SET groupName=excluded.groupName");
				pState.setString(1, groupName);
				pState.setBytes(2, convertToBlob(playerUUID));
				pState.executeUpdate();
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static void setPlayerTag(UUID playerUUID, String playerTag) {
		CompletableFuture.runAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("INSERT INTO players (playerTag, playerUUID) VALUES (?, ?)"
								+ " ON CONFLICT(playerUUID) DO UPDATE SET playerTag=excluded.playerTag");
				pState.setString(1, playerTag);
				pState.setBytes(2, convertToBlob(playerUUID));
				pState.executeUpdate();
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static void setPlayerInGroup(UUID playerUUID, boolean inGroup) {
		CompletableFuture.runAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("INSERT INTO players (inGroup, playerUUID) VALUES (?, ?)"
								+ " ON CONFLICT(playerUUID) DO UPDATE SET inGroup=excluded.inGroup");
				pState.setBoolean(1, inGroup);
				pState.setBytes(2, convertToBlob(playerUUID));
				pState.executeUpdate();
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static void setPlayerMuted(UUID playerUUID, boolean muted) {
		CompletableFuture.runAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("INSERT INTO players (muted, playerUUID) VALUES (?, ?)"
								+ " ON CONFLICT(playerUUID) DO UPDATE SET muted=excluded.muted");
				pState.setBoolean(1, muted);
				pState.setBytes(2, convertToBlob(playerUUID));
				pState.executeUpdate();
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static void setPlayerInvited(UUID playerUUID, boolean invited) {
		CompletableFuture.runAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("INSERT INTO players (invited, playerUUID) VALUES (?, ?)"
								+ " ON CONFLICT(playerUUID) DO UPDATE SET invited=excluded.invited");
				pState.setBoolean(1, invited);
				pState.setBytes(2, convertToBlob(playerUUID));
				pState.executeUpdate();
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static void setPlayerRequest(UUID playerUUID, String groupName) {
		CompletableFuture.runAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("INSERT INTO players (requested, playerUUID) VALUES (?, ?)"
								+ " ON CONFLICT(playerUUID) DO UPDATE SET requested=excluded.requested");
				pState.setString(1, groupName);
				pState.setBytes(2, convertToBlob(playerUUID));
				pState.executeUpdate();
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static CompletableFuture<String> getPlayerTag(UUID playerUUID) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT playerTag FROM players WHERE playerUUID=?");
				String playerTag = null;
				pState.setBytes(1, convertToBlob(playerUUID));
				ResultSet query = pState.executeQuery();
				while (query.next())
					playerTag = query.getString("playerTag");
				return playerTag;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));

			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static CompletableFuture<String> getPlayerRequest(UUID playerUUID) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT requested FROM players WHERE playerUUID=?");
				String playerTag = null;
				pState.setBytes(1, convertToBlob(playerUUID));
				ResultSet query = pState.executeQuery();
				while (query.next())
					playerTag = query.getString("requested");
				return playerTag;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		}, ChatPlugin.EVIRTUAL);
	}
	
	public static CompletableFuture<UUID> getGroupAdmin(String groupName) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT playerUUID FROM players WHERE groupName=? AND playerTag=?");
				UUID playerUUID = null;
				pState.setString(1, groupName);
				pState.setString(2, "admin");
				ResultSet query = pState.executeQuery();
				while (query.next())
					playerUUID = convertToUUID(query.getBytes("playerUUID"));
				return playerUUID;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static CompletableFuture<Boolean> isPlayerInGroup(UUID playerUUID) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT inGroup FROM players WHERE playerUUID=?");
				Boolean inGroup = null;
				pState.setBytes(1, convertToBlob(playerUUID));
				ResultSet query = pState.executeQuery();
				while (query.next())
					inGroup = query.getBoolean("inGroup");
				return inGroup;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));

			}
		}, ChatPlugin.EVIRTUAL);
	}

	public static CompletableFuture<Boolean> isPlayerMuted(UUID playerUUID) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT muted FROM players WHERE playerUUID=?");
				Boolean muted = null;
				pState.setBytes(1, convertToBlob(playerUUID));
				ResultSet query = pState.executeQuery();
				while (query.next())
					muted = query.getBoolean("muted");
				return muted;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));

			}
		}, ChatPlugin.EVIRTUAL);
	}

	private static byte[] convertToBlob(UUID playerUUID) {
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putLong(playerUUID.getMostSignificantBits());
		buffer.putLong(playerUUID.getLeastSignificantBits());
		return buffer.array();
	}

	public static CompletableFuture<Boolean> isPlayerInvited(UUID playerUUID) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT invited FROM players WHERE playerUUID=?");
				boolean invited = false;
				pState.setBytes(1, convertToBlob(playerUUID));
				ResultSet query = pState.executeQuery();
				while (query.next())
					invited = query.getBoolean("invited");
				return invited;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));

			}
		}, ChatPlugin.EVIRTUAL);
	}
}