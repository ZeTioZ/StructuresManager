package fr.zetioz.structuresmanager.commands;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.SchematicUtilsWE7;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class ResetStructure
{
	private final StructuresManager instance;
	private YamlConfiguration messages;
	private YamlConfiguration config;
	private String prefix;

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
						instance.getBlocksLocationsCache().remove(structName);
						instance.getBlocksLocationsAddCache().remove(structName);
						instance.getBlocksLocationsRemoveCache().remove(structName);
						if(config.getBoolean("debug"))
						{
							sendMessage(sender, messages.getStringList("structure-reloaded"), prefix, "{struct_name}", structName);
						}
					}
					else
					{
						sendMessage(sender, messages.getStringList("errors.structure-reload-failed"), prefix, "{struct_name}", structName);
					}
				}
				sendMessage(sender, messages.getStringList("structures-reload-success"), prefix);
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
						instance.getBlocksLocationsCache().remove(structName);
						instance.getBlocksLocationsAddCache().remove(structName);
						instance.getBlocksLocationsRemoveCache().remove(structName);
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
