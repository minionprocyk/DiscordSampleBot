package com.procyk.industries.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.procyk.industries.command.Command;
import com.procyk.industries.module.JDBCUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SQLCRUD implements CRUDable {
    private final String url;
    private final Logger logger = LoggerFactory.getLogger(SQLCRUD.class);
    private final String SQL_ERROR="Failed to create commands table";
    @Inject
    public SQLCRUD(@JDBCUrl String url) {
        this.url=url;
    }
    public void saveAllCommands(Map<String,String> commands) {
        String sql = "insert into commands(name, value) values(?,?)";
        dropCommandsTable();
        createCommandsTable();
        try(Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for(Map.Entry<String,String> entry : commands.entrySet()) {
                statement.setString(1,entry.getKey());
                statement.setString(2,entry.getValue());
                statement.executeUpdate();
            }


            conn.commit();
        }catch(SQLException e) {
            logger.error("Failed to save all commands");
        }
    }
    public Set<Command> getCommands() {
        Set<Command> commandList = new HashSet<>();
        String sql = "Select * from commands";

        try(Connection conn = getConnection();
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)) {

            while(resultSet.next()) {
                String name = resultSet.getString("name");
                String value = resultSet.getString("value");
                Command command = new Command(name,value);
                commandList.add(command);
            }

        } catch(SQLException e) {
            logger.error("Failed to retrieve items from Commands Table");
        }
        return commandList;
    }

    public void addCommand(Command command) {
        String sql = "INSERT INTO commands (name, value) VALUES(?,?)";
        try(Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1,command.getKey());
            statement.setString(2,command.getValue());
            statement.executeUpdate();
        } catch(SQLException e) {
            logger.error(SQL_ERROR,e);
        }
    }

    public void removeCommand(Command command) {
        String sql = "DELETE FROM commands where name=?";
        try(Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1,command.getKey());
            statement.execute();
        } catch(SQLException e) {
            logger.error(SQL_ERROR,e);
        }
    }

    public void dropCommandsTable() {
        String sql = "drop table if exists commands";
        try(Connection conn = getConnection();
            Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
        } catch(SQLException e) {
            logger.error(SQL_ERROR,e);
        }
    }
    public void createCommandsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS commands ("
                +" id integer PRIMARY KEY, "
                +" name text unique not null, "
                +" value text not null);";
        try(Connection conn = getConnection();
            Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
        } catch(SQLException e) {
            logger.error(SQL_ERROR,e);
        }
    }
    private Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.error("Failed to created JDBC",e);
        }
        try{
            conn = DriverManager.getConnection(url);
            DatabaseMetaData meta = conn.getMetaData();
            logger.info("Driver name is: {}", meta.getDriverName());
        } catch(SQLException e) {
            logger.error("Failed to connect to SQLITE",e);
        }
        if(conn==null)
            throw new NullPointerException("JDBC Connection could not be created");
        return conn;
    }
    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.error("Failed to create JDBC",e);
        }
        try(Connection conn = DriverManager.getConnection(url)) {
               if(conn!=null) {
                   DatabaseMetaData meta = conn.getMetaData();
                   logger.info("Driver name is: {}", meta.getDriverName());

               }
        } catch(SQLException e) {
             logger.error("Failed to connect to SQLITE",e);
        }
    }
}
