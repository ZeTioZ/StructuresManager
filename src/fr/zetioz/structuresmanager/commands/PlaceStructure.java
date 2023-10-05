package fr.zetioz.structuresmanager.commands;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.SchematicUtilsWE7;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class PlaceStructure
{
	private final StructuresManager instance;
	private final YamlConfiguration messages;
	private final String prefix;

	public PlaceStructure(StructuresManager instance, YamlConfiguration messages)
	{
		this.instance = instance;
		this.messages = messages;
		this.prefix = messages.getString("prefix");
	}

	public boolean command(Player player, String[] args, String label)
	{
		if(player.hasPermission("structuresmanager.place"))
		{
			final Map<String, Structure> structuresCache = instance.getStructuresCache();
			if(!structuresCache.containsKey(args[2]))
			{
				final boolean result = SchematicUtilsWE7.pasteSchematic(instance, args[1], player.getLocation());
				if(result)
				{
					sendMessage(player, messages.getStringList("structure-place-success"), prefix);
					final Structure newStruct = new Structure(args[1], args[2], player.getLocation());
					structuresCache.put(args[2], newStruct);
				}
				else
				{
					sendMessage(player, messages.getStringList("errors.structure-place-failed"), prefix);
				}
			}
			else
			{
				sendMessage(player, messages.getStringList("errors.structure-already-existing"), prefix, "{label}", label, "{struct_name}", args[1]);
			}
		}
		else
		{
			sendMessage(player, messages.getStringList("errors.not-enough-permission"), prefix);
		}
		return true;
	}
}
