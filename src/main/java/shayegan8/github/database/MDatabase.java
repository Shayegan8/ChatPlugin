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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public class MDatabase {

	private Connection connection = null;
	private final String name;

	private final static String path = ChatPlugin.getPlugin(ChatPlugin.class).getDataFolder() + "/database";

	public MDatabase(String name) {
		this.name = name;
		try {
			Path path_ = Paths.get(path);
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
					+ "(groupName TEXT references groups(groupName) ON DELETE SET NULL,"
					+ " playerUUID BLOB PRIMARY KEY, " + "playerTag TEXT, " + "inGroup BOOLEAN, " + "muted BOOLEAN, "
					+ "invited BOOLEAN);");
			pStatement.executeUpdate();
			pStatement = connection.prepareStatement("INSERT INTO groups (groupName) VALUES (?)");
			pStatement.setString(1, "none");
			pStatement.executeUpdate();
		} catch (SQLException | IOException e) {
			throw new IllegalStateException(e);
		}
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
				PreparedStatement pStatement = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT groupName FROM groups");
				ResultSet query = pStatement.executeQuery();
				while (query.next())
					list.add(query.getString("groupName"));
				return list;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		});
	}

	public static void createGroup(String groupName) {
		Thread.ofVirtual().start(() -> {
			try {
				PreparedStatement pStatement = ChatPlugin.mDB.getConnection().prepareStatement(
						"INSERT INTO groups SELECT ? WHERE NOT EXISTS (SELECT 1 FROM groups WHERE groupName = ?)");
				pStatement.setString(1, groupName);
				pStatement.setString(2, groupName);
				pStatement.executeUpdate();
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		});
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
		});
	}

	public static void deleteGroup(String groupName) {
		Thread.ofVirtual().start(() -> {
			try {
				PreparedStatement pStatement = ChatPlugin.mDB.getConnection()
						.prepareStatement("DELETE FROM groups WHERE groupName=?");
				pStatement.setString(1, groupName);
				pStatement.executeUpdate();
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		});
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
		});
	}

	public static CompletableFuture<Boolean> playerHasGroup(UUID playerUUID) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT groupName FROM players WHERE playerUUID = ?");
				pState.setBytes(1, convertToBlob(playerUUID));
				var has = false;
				var query = pState.executeQuery();
				while (query.next())
					has = true;
				return has;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));

			}
		});
	}

	public static String getPlayerGroupBlocking(UUID playerUUID) {
		try {
			PreparedStatement pState = ChatPlugin.mDB.getConnection()
					.prepareStatement("SELECT groupName FROM players WHERE playerUUID = ?");
			String groupName = null;
			pState.setBytes(1, convertToBlob(playerUUID));
			var query = pState.executeQuery();
			while (query.next())
				groupName = query.getString("groupName");
			System.out.println(4);
			return groupName;
		} catch (SQLException e) {
			throw new IllegalStateException(Arrays.toString(e.getStackTrace()));

		}
	}

	public static void setPlayerGroup(UUID playerUUID, String groupName) {
		Thread.ofVirtual().start(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("INSERT INTO players (groupName, playerUUID) VALUES (?, ?)"
								+ " ON CONFLICT(playerUUID) DO UPDATE SET groupName=excluded.groupName");
				System.out.println(1);
				pState.setString(1, groupName);
				System.out.println(2);
				pState.setBytes(2, convertToBlob(playerUUID));
				System.out.println(3);
				pState.executeUpdate();
				System.out.println(4);
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));
			}
		});
	}

	public static void setPlayerTag(UUID playerUUID, String playerTag) {
		Thread.ofVirtual().start(() -> {
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
		});
	}

	public static void setPlayerInGroup(UUID playerUUID, boolean inGroup) {
		Thread.ofVirtual().start(() -> {
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
		});
	}

	public static void setPlayerMuted(UUID playerUUID, boolean muted) {
		Thread.ofVirtual().start(() -> {
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
		});
	}

	public static void setPlayerInvited(UUID playerUUID, boolean invited) {
		Thread.ofVirtual().start(() -> {
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
		});
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
		});
	}

	public static String getPlayerTagBlocking(UUID playerUUID) {
		try {
			PreparedStatement pState = ChatPlugin.mDB.getConnection()
					.prepareStatement("SELECT playerTag FROM players WHERE playerUUID = ?");
			String playerTag = null;
			pState.setBytes(1, convertToBlob(playerUUID));
			var query = pState.executeQuery();
			while (query.next())
				playerTag = query.getString("groupName");
			return playerTag;
		} catch (SQLException e) {
			throw new IllegalStateException(Arrays.toString(e.getStackTrace()));

		}
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
		});
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
		});
	}

	private static byte[] convertToBlob(UUID playerUUID) {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
		buffer.putLong(playerUUID.getMostSignificantBits());
		buffer.putLong(playerUUID.getLeastSignificantBits());
		return buffer.array();
	}

	public static CompletableFuture<Boolean> isPlayerInvited(UUID playerUUID) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement pState = ChatPlugin.mDB.getConnection()
						.prepareStatement("SELECT invited FROM players WHERE playerUUID=?");
				Boolean invited = null;
				pState.setBytes(1, convertToBlob(playerUUID));
				ResultSet query = pState.executeQuery();
				while (query.next())
					invited = query.getBoolean("invited");
				return invited;
			} catch (SQLException e) {
				throw new IllegalStateException(Arrays.toString(e.getStackTrace()));

			}
		});
	}
}