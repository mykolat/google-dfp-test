package Brightcom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection
{
    public static Connection getConnection() throws SQLException, ClassNotFoundException
    {
        //Class.forName("com.mysql.jdbc.Driver");

        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dfp_test", "root", "pass");

        return connection;
    }

    public static void main(String[] args)
    {
        try
        {
            getConnection();
        }
        catch (SQLException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}