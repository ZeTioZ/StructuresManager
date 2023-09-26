package fr.zetioz.structuresmanager.commands;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.SchematicUtilsWE7;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class SaveStructure
{
	private final StructuresManager instance;
	private YamlConfiguration messages;
	private String prefix;

	public SaveStructure(StructuresManager instance, YamlConfiguration messages)
	{
		this.instance = instance;
		this.messages = messages;
		this.prefix = messages.getString("prefix");
	}

	public boolean command(Player player, String[] args)
	{
		if(player.hasPermission("structuresmanager.save"))
		{
			final boolean result = SchematicUtilsWE7.saveSchematic(instance, player, args[1], player.getLocation());
			if(result)
			{
				sendMessage(player, messages.getStringList("structure-save-success"), prefix);
			}
			else
			{
				sendMessage(player, messages.getStringList("errors.structure-save-failed"), prefix);
			}
		}
		else
		{
			sendMessage(player, messages.getStringList("errors.not-enough-permission"), prefix);
		}
		return true;
	}
}
