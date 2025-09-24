package shayegan8.github.database;

import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MDatabase {

    @Getter
    private Connection connection;

    public MDatabase() {
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/ChatPlugin/chatdb");
            PreparedStatement pStatement = connection.prepareStatement("CREATE TABLE IF NOT EXIST groups (groupname TEXT);");
            pStatement.executeUpdate();
            pStatement = connection.prepareStatement("CREATE TABLE IF NOT EXIST players (groupname TEXT references groups(groupname), playername TEXT, playertag TEXT);");
            pStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

}
