package fr.zetioz.structuresmanager.commands;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.SchematicUtilsWE7;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class TeleportToStructure
{
	private final StructuresManager instance;
	private YamlConfiguration messages;
	private String prefix;

	public TeleportToStructure(StructuresManager instance, YamlConfiguration messages)
	{
		this.instance = instance;
		this.messages = messages;
		this.prefix = messages.getString("prefix");
	}

	public boolean command(Player player, String[] args, String label)
	{
		if(player.hasPermission("structuresmanager.teleport"))
		{
			final Map<String, Structure> structuresCache = instance.getStructuresCache();
			if(structuresCache.containsKey(args[1]))
			{
				final Structure struct = structuresCache.get(args[1]);
				final Location loc = struct.getLocation();
				player.teleport(loc.clone().add(0, 1, 0));
				sendMessage(player, messages.getStringList("teleport-to-structure-success"), prefix, "{struct_name}", args[1]);
			}
			else
			{
				sendMessage(player, messages.getStringList("errors.structure-not-existing"), prefix, "{label}", label, "{struct_name}", args[1]);
			}
		}
		else
		{
			sendMessage(player, messages.getStringList("errors.not-enough-permission"), prefix);
		}
		return true;
	}
}
