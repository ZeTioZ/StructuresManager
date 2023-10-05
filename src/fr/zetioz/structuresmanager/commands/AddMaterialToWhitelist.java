package fr.zetioz.structuresmanager.commands;

import fr.zetioz.coreutils.EnumCheckUtils;
import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class AddMaterialToWhitelist
{
	private final StructuresManager instance;
	private final YamlConfiguration messages;
	private final String prefix;

	public AddMaterialToWhitelist(StructuresManager instance, YamlConfiguration messages)
	{
		this.instance = instance;
		this.messages = messages;
		this.prefix = messages.getString("prefix");
	}

	public boolean command(CommandSender sender, String[] args, String label)
	{
		if(sender.hasPermission("structuresmanager.whitelist.add.material"))
		{
			final Map<String, Structure> structuresCache = instance.getStructuresCache();
			if(!structuresCache.containsKey(args[2]))
			{
				sendMessage(sender, messages.getStringList("errors.structure-not-existing"), prefix, "{label}", label, "{struct_name}", args[2]);
				return true;
			}
			final String material = args[3].toUpperCase();
			if(!EnumCheckUtils.isValidEnum(Material.class, material))
			{
				sendMessage(sender, messages.getStringList("errors.material-not-existing"), prefix, "{label}", label, "{material}", material);
				return true;
			}

			final Structure structure = structuresCache.get(args[2]);

			if(structure.getBlocksWhiteList().contains(material))
			{
				sendMessage(sender, messages.getStringList("errors.material-already-whitelisted"), prefix, "{label}", label, "{material}", material);
				return true;
			}

			structure.getBlocksWhiteList().add(material);
			sendMessage(sender, messages.getStringList("material-add-success"), prefix, "{label}", label, "{material}", material, "{struct_name}", args[2]);
		}
		else
		{
			sendMessage(sender, messages.getStringList("errors.not-enough-permission"), prefix);
		}
		return true;
	}
}
