package com.logginghub.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLUtils
{
    public static void execute(String sql, Connection connection)
    {
        Statement shutdown = null;
        try
        {
            shutdown = connection.createStatement();
            shutdown.execute(sql);
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(String.format("Failed to execute SQL [%s]",
                                                     sql),
                                       sqle);
        }
        finally
        {
            if (shutdown != null)
            {
                try
                {
                    shutdown.close();
                }
                catch (SQLException e)
                {}
            }
        }
    }
}
