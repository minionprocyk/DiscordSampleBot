package com.procyk.industries.sql;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.procyk.industries.command.Command;

import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class CRUD {
    private final String url;
    private final Logger logger = Logger.getLogger(CRUD.class.getName());

    @Inject
    public CRUD(@Named("jdbc_url")String url) {
        this.url=url;
    }
    public void saveAllCommands(Map<String,String> commands) {
        String sql = "insert into commands(name, value) values(?,?)";
        dropCommandsTable();
        createCommandsTable();
        try(Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement statement = conn.prepareStatement(sql);
            for(Map.Entry<String,String> entry : commands.entrySet()) {
                statement.setString(1,entry.getKey());
                statement.setString(2,entry.getValue());
                statement.executeUpdate();
            }


            conn.commit();
        }catch(SQLException e) {
            logger.severe("Failed to save all commands");
        }
    }
    public Set<Command> getCommands() {
        Set<Command> commandList = new HashSet<>();
        String sql = "Select * from commands";

        try(Connection conn = getConnection()) {
            Statement statement = conn.createStatement();

            ResultSet resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                String name = resultSet.getString("name");
                String value = resultSet.getString("value");
                Command command = new Command(name,value);
                commandList.add(command);
            }

        } catch(SQLException e) {
            logger.severe("Failed to retrieve items from Commands Table");
        }
        return commandList;
    }

    public void addCommand(Command command) {
        String sql = "INSERT INTO commands (name, value) VALUES(?,?)";
        try(Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1,command.getKey());
            statement.setString(2,command.getValue());
            statement.executeUpdate();
        } catch(SQLException e) {
            logger.severe("Failed to create commands table");
        }
    }

    public void removeCommand(Command command) {
        String sql = "DELETE FROM commands where name=?";
        try(Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1,command.getKey());
            statement.execute();
        } catch(SQLException e) {
            logger.severe("Failed to create commands table");
        }
    }

    public void dropCommandsTable() {
        String sql = "drop table if exists commands";
        try(Connection conn = getConnection()) {
            Statement statement = conn.createStatement();
            statement.executeUpdate(sql);
        } catch(SQLException e) {
            logger.severe("Failed to drop commands table");
        }
    }
    public void createCommandsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS commands ("
                +" id integer PRIMARY KEY, "
                +" name text unique not null, "
                +" value text not null);";
        try(Connection conn = getConnection()) {
            Statement statement = conn.createStatement();
            statement.executeUpdate(sql);
        } catch(SQLException e) {
            logger.severe("Failed to create commands table");
        }
    }
    private Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try{
            conn = DriverManager.getConnection(url);
            if(conn!=null) {
                DatabaseMetaData meta = conn.getMetaData();
                logger.info("Driver name is: "+meta.getDriverName());
            }
        } catch(SQLException e) {
            logger.severe("Failed to connect to SQLITE");
        }
        return conn!=null ? conn : null;
    }
    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try(Connection conn = DriverManager.getConnection(url)) {
               if(conn!=null) {
                   DatabaseMetaData meta = conn.getMetaData();
                   logger.info("Driver name is: "+meta.getDriverName());

               }
        } catch(SQLException e) {
             logger.severe("Failed to connect to SQLITE");
        }
    }
}
