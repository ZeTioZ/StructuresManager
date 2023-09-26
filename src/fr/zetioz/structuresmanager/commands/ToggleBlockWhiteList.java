package fr.zetioz.structuresmanager.commands;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class ToggleBlockWhiteList
{
	private final StructuresManager instance;
	private YamlConfiguration messages;
	private String prefix;

	public ToggleBlockWhiteList(StructuresManager instance, YamlConfiguration messages)
	{
		this.instance = instance;
		this.messages = messages;
		this.prefix = messages.getString("prefix");
	}

	public boolean command(CommandSender sender, String regionID, boolean value)
	{
		final Map<String, Structure> structuresCache = instance.getStructuresCache();
		if(!structuresCache.containsKey(regionID))
		{
			sendMessage(sender, messages.getStringList("errors.structure-not-existing"), prefix);
			return true;
		}
		final Structure structure = structuresCache.get(regionID);
		final boolean isWhiteListActive = structure.isWhiteListActive();
		if(isWhiteListActive == value)
		{
			sendMessage(sender, messages.getStringList("errors.structure-already-whitelist-status"), prefix, "{status}", (value ? "enabled" : "disabled"));
			return true;
		}
		structure.isWhiteListActive(value);
		return true;
	}
}
