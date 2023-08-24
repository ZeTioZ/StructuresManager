package fr.zetioz.structuresmanager;

import fr.zetioz.coreutils.FilesManagerUtils;
import fr.zetioz.structuresmanager.commands.StructuresManagerCommand;
import fr.zetioz.structuresmanager.objects.Structure;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Getter
public final class StructuresManager extends JavaPlugin
{
	private static JavaPlugin plugin;
	private FilesManagerUtils filesManagerUtils;

	private Map<String, Structure> structuresCache;

	@Override
	public void onEnable()
	{
		plugin = this;
		structuresCache = new HashMap<>();
		filesManagerUtils = new FilesManagerUtils(this);
		ConfigurationSerialization.registerClass(Structure.class);

		filesManagerUtils.createSimpleYaml("config");
		filesManagerUtils.createSimpleYaml("messages");
		filesManagerUtils.createSimpleYaml("database");

		try
		{
			getCommand("structuresmanager").setExecutor(new StructuresManagerCommand(this));
			final YamlConfiguration database = filesManagerUtils.getSimpleYaml("database");
			database.getConfigurationSection("data").getKeys(false).forEach(key -> {
				final Structure structure = database.getSerializable("data." + key, Structure.class);
				structuresCache.put(key, structure);
			});

		}
		catch(Exception e)
		{
			e.printStackTrace();
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
