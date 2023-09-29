package fr.zetioz.structuresmanager.commands;

import fr.zetioz.coreutils.EnumCheckUtils;
import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class RemoveMaterialFromWhitelist
{
	private final StructuresManager instance;
	private YamlConfiguration messages;
	private String prefix;

	public RemoveMaterialFromWhitelist(StructuresManager instance, YamlConfiguration messages)
	{
		this.instance = instance;
		this.messages = messages;
		this.prefix = messages.getString("prefix");
	}

	public boolean command(CommandSender sender, String[] args, String label)
	{
		if(sender.hasPermission("structuresmanager.whitelist.remove.material"))
		{
			final Map<String, Structure> structuresCache = instance.getStructuresCache();
			if(!structuresCache.containsKey(args[2]))
			{
				sendMessage(sender, messages.getStringList("errors.structure-not-existing"), prefix, "{label}", label, "{struct_name}", args[2]);
				return true;
			}
			if(EnumCheckUtils.isValidEnum(Material.class, args[3]))
			{
				sendMessage(sender, messages.getStringList("errors.material-not-existing"), prefix, "{label}", label, "{material}", args[3]);
				return true;
			}

			final Structure structure = structuresCache.get(args[2]);
			final Material material = Material.valueOf(args[3]);

			if(!structure.getBlocksWhiteList().contains(material))
			{
				sendMessage(sender, messages.getStringList("errors.material-not-whitelisted"), prefix, "{label}", label, "{material}", args[3]);
				return true;
			}

			structure.getBlocksWhiteList().remove(material);
			sendMessage(sender, messages.getStringList("material-remove-success"), prefix, "{label}", label, "{material}", args[3], "{struct_name}", args[2]);
		}
		else
		{
			sendMessage(sender, messages.getStringList("errors.not-enough-permission"), prefix);
		}
		return true;
	}
}
