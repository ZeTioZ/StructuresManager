package fr.zetioz.structuresmanager.databases;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.databases.Database;
import fr.zetioz.structuresmanager.databases.SQLite;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.FileNotFoundException;

public class DatabaseWrapper
{
	/**
	 * Database wrapper to get the right database type from the configuration file
	 *
	 * @param instance Main plugin instance
	 * @return the retrieved {@link Database} instance from the configuration file
	 * @throws FileNotFoundException if the configuration file isn't found
	 */
	public static Database getDatabase(final StructuresManager instance) throws FileNotFoundException
	{
		final YamlConfiguration config = instance.getFilesManagerUtils().getSimpleYaml("config");
		final String dbname = config.getString("database.dbname", "database");

		if(config.getString("database.type", "SQLite").equalsIgnoreCase("MySQL"))
		{
			final String host = config.getString("database.host", "localhost");
			final String port = config.getString("database.port", "3306");
			final String tablePrefix = config.getString("database.table_prefix", "zombiecore_");
			final String username = config.getString("database.username", "root");
			final String password = config.getString("database.password", "root");
			//			return new MySQL(plugin, host, port, dbname, tablePrefix, username, password);
			return null;
		}
		else
		{
			return new SQLite(instance, dbname);
		}
	}
}