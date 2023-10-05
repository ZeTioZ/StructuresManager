package fr.zetioz.structuresmanager.commands;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class ToggleBlocksWhitelist
{
	private final StructuresManager instance;
	private final YamlConfiguration messages;
	private final String prefix;

	public ToggleBlocksWhitelist(StructuresManager instance, YamlConfiguration messages)
	{
		this.instance = instance;
		this.messages = messages;
		this.prefix = messages.getString("prefix");
	}

	public boolean command(CommandSender sender, String[] args, boolean value)
	{
		if(sender.hasPermission("structuresmanager.toggle.whitelist"))
		{
			final Map<String, Structure> structuresCache = instance.getStructuresCache();
			if(!structuresCache.containsKey(args[2]))
			{
				sendMessage(sender, messages.getStringList("errors.structure-not-existing"), prefix);
				return true;
			}
			final Structure structure = structuresCache.get(args[2]);
			final boolean isWhiteListActive = structure.isWhiteListActive();
			if(isWhiteListActive == value)
			{
				sendMessage(sender, messages.getStringList("errors.structure-already-whitelist-status"), prefix, "{status}", (value ? "enabled" : "disabled"));
				return true;
			}
			structure.isWhiteListActive(value);
			sendMessage(sender, messages.getStringList("whitelist-toggle-success"), prefix, "{status}", (value ? "enabled" : "disabled"), "{struct_name}", args[2]);
		}
		else
		{
			sendMessage(sender, messages.getStringList("errors.not-enough-permission"), prefix);
		}
		return true;
	}
}
