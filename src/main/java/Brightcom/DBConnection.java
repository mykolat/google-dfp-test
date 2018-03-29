package Brightcom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection
{
    public static Connection getConnection() throws SQLException, ClassNotFoundException
    {
  
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dfp_test", "root", "pass");

        return connection;
    }
}