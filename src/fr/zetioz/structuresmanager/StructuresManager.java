package fr.zetioz.structuresmanager;

import fr.zetioz.coreutils.FilesManagerUtils;
import fr.zetioz.structuresmanager.commands.CommandsHandler;
import fr.zetioz.structuresmanager.databases.Database;
import fr.zetioz.structuresmanager.databases.DatabaseWrapper;
import fr.zetioz.structuresmanager.listeners.BlockBreakListener;
import fr.zetioz.structuresmanager.listeners.BlockExplodeListener;
import fr.zetioz.structuresmanager.listeners.BlockPlaceListener;
import fr.zetioz.structuresmanager.objects.Structure;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public final class StructuresManager extends JavaPlugin
{
	private static JavaPlugin plugin;
	private FilesManagerUtils filesManagerUtils;
	private Database database;

	private Map<String, Structure> structuresCache;
	private Map<String, List<Location>> blocksLocationsCache;
	private Map<String, List<Location>> blocksLocationsAddCache;
	private Map<String, List<Location>> blocksLocationsRemoveCache;

	@Override
	public void onEnable()
	{
		plugin = this;
		structuresCache = new HashMap<>();
		blocksLocationsCache = new HashMap<>();
		blocksLocationsAddCache = new HashMap<>();
		blocksLocationsRemoveCache = new HashMap<>();
		filesManagerUtils = new FilesManagerUtils(this);
		ConfigurationSerialization.registerClass(Structure.class);

		filesManagerUtils.createSimpleYaml("config");
		filesManagerUtils.createSimpleYaml("messages");
		filesManagerUtils.createSimpleYaml("database");

		try
		{
			database = DatabaseWrapper.getDatabase(this);
			blocksLocationsCache = this.database.getAllRegionsBlocksLocations();

			getCommand("structuresmanager").setExecutor(new CommandsHandler(this));
			final YamlConfiguration yamlDatabase = filesManagerUtils.getSimpleYaml("database");
			yamlDatabase.getConfigurationSection("data").getKeys(false).forEach(key -> {
				final Structure structure = yamlDatabase.getSerializable("data." + key, Structure.class);
				structuresCache.put(key, structure);
			});

			registerEvents(new BlockBreakListener(this), new BlockExplodeListener(this), new BlockPlaceListener(this));
		}
		catch(Exception e)
		{
			getLogger().severe("An error occurred while loading the plugin: " + e.getMessage());
		}
	}

	@Override
	public void onDisable()
	{
		try
		{
			filesManagerUtils.getSimpleYaml("database").set("data", structuresCache);
			filesManagerUtils.saveSimpleYaml("database");
		}
		catch(FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		filesManagerUtils = null;
		plugin = null;
	}

	private void registerEvents(Listener... listeners)
	{
		for(Listener listener : listeners)
		{
			Bukkit.getPluginManager().registerEvents(listener, this);
		}
	}
}
