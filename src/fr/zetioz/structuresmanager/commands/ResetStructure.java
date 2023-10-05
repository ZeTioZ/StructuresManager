package fr.zetioz.structuresmanager.commands;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.SchematicUtilsWE7;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class ResetStructure
{
	private final StructuresManager instance;
	private final YamlConfiguration messages;
	private final YamlConfiguration config;
	private final String prefix;

	public ResetStructure(StructuresManager instance, YamlConfiguration config, YamlConfiguration messages)
	{
		this.instance = instance;
		this.config = config;
		this.messages = messages;
		this.prefix = messages.getString("prefix");
	}

	public boolean command(CommandSender sender, String[] args, String label)
	{
		if(sender.hasPermission("structuresmanager.reset"))
		{
			final Map<String, Structure> structuresCache = instance.getStructuresCache();
			if(args[1].equalsIgnoreCase("all"))
			{
				for(final Map.Entry<String, Structure> entry : structuresCache.entrySet())
				{
					final String structName = entry.getKey();
					final Structure struct = entry.getValue();
					final boolean result = SchematicUtilsWE7.pasteSchematic(instance, struct.getName(), struct.getLocation());
					if(result)
					{
						final Set<Location> blocksLocationToRemove = instance.getBlocksLocationsRemoveCache().getOrDefault(structName, new HashSet<>());
						blocksLocationToRemove.addAll(instance.getBlocksLocationsCache().getOrDefault(structName, new HashSet<>()));
						instance.getBlocksLocationsRemoveCache().put(structName, blocksLocationToRemove);
						instance.getBlocksLocationsAddCache().remove(structName);
						if(config.getBoolean("debug"))
						{
							sendMessage(sender, messages.getStringList("structure-reset"), prefix, "{struct_name}", structName);
						}
					}
					else
					{
						sendMessage(sender, messages.getStringList("errors.structure-reset-failed"), prefix, "{struct_name}", structName);
					}
				}
				sendMessage(sender, messages.getStringList("structures-reset-success"), prefix);
			}
			else
			{
				if(structuresCache.containsKey(args[1]))
				{
					final String structName = args[1];
					final Structure struct = structuresCache.get(structName);
					final boolean result = SchematicUtilsWE7.pasteSchematic(instance, struct.getName(), struct.getLocation());
					if(result)
					{
						final Set<Location> blocksLocationToRemove = instance.getBlocksLocationsRemoveCache().getOrDefault(structName, new HashSet<>());
						blocksLocationToRemove.addAll(instance.getBlocksLocationsCache().getOrDefault(structName, new HashSet<>()));
						instance.getBlocksLocationsRemoveCache().put(structName, blocksLocationToRemove);
						instance.getBlocksLocationsAddCache().remove(structName);
						sendMessage(sender, messages.getStringList("structure-reloaded"), prefix, "{struct_name}", args[1]);
					}
					else
					{
						sendMessage(sender, messages.getStringList("errors.structure-reload-failed"), prefix, "{struct_name}", args[1]);
					}
				}
				else
				{
					sendMessage(sender, messages.getStringList("errors.structure-not-existing"), prefix, "{label}", label, "{struct_name}", args[1]);
				}
			}
		}
		else
		{
			sendMessage(sender, messages.getStringList("errors.not-enough-permission"), prefix);
		}
		return true;
	}
}
