package fr.zetioz.structuresmanager.databases;

import fr.zetioz.structuresmanager.StructuresManager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import static java.sql.DriverManager.getConnection;

public class SQLite extends Database
{
    private final String dbname;
    private final String SQLCreateTable;

    public SQLite(final StructuresManager instance, final String dbname)
    {
        super(instance, "");
        this.dbname = dbname;
        SQLCreateTable =  "create table if not exists SAVED_BLOCK("
                + "     REGION_ID varchar(255) not null,"
                + "     WORLD varchar(255) not null,"
                + "     X numeric(32, 4) not null,"
                + "     Y numeric(32, 4) not null,"
                + "     Z numeric(32, 4) not null,"
                + "     primary key(REGION_ID)"
                + ");";
        load();
    }

    @Override
    public Connection getSQLConnection()
    {
        try
        {
            if(this.connection != null && !this.connection.isClosed())
            {
                return this.connection;
            }

            Class.forName("org.sqlite.JDBC");
            this.connection = getConnection("jdbc:sqlite:" + getInstance().getDataFolder().getAbsolutePath() + File.separator + dbname + ".db");
            this.connection.createStatement().executeUpdate("PRAGMA foreign_keys = ON;");
            return this.connection;
        }
        catch(final ClassNotFoundException e)
        {
            getInstance().getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        catch(final SQLException e)
        {
            getInstance().getLogger().log(Level.SEVERE, "SQLite exception on initialize:", e);
        }
        return null;
    }

    @Override
    public void load()
    {
        final File dataFolder = new File(getInstance().getDataFolder(), dbname + ".db");
        if(!dataFolder.exists())
        {
            try
            {
                if(!dataFolder.createNewFile())
                {
                    getInstance().getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
                }
            }
            catch(IOException e)
            {
                getInstance().getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
            }
        }
        try(Connection conn = getSQLConnection(); Statement s = conn.createStatement())
        {
            s.executeUpdate(SQLCreateTable);
            s.executeUpdate("PRAGMA foreign_keys = ON;");
        }
        catch(SQLException ex)
        {
            getInstance().getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
        }
    }
}