package fr.zetioz.structuresmanager.commands;

import fr.zetioz.coreutils.FilesManagerUtils;
import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class ToggleBuildableRegion
{
	private final StructuresManager instance;
	private YamlConfiguration messages;
	private String prefix;

	public ToggleBuildableRegion(StructuresManager instance, YamlConfiguration messages)
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
		final boolean canBuild = structure.canBuild();
		if(canBuild == value)
		{
			sendMessage(sender, messages.getStringList("errors.structure-already-can-build-status"), prefix, "{status}", (value ? "enabled" : "disabled"));
			return true;
		}
		structure.canBuild(value);
		return true;
	}
}
