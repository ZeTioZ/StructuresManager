package fr.zetioz.structuresmanager.commands;

import fr.zetioz.coreutils.FilesManagerUtils;
import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.WorldGuardHook;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;
import java.util.stream.Collectors;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class MakeWorldGuardRegion
{
	private final StructuresManager instance;
	private YamlConfiguration messages;
	private YamlConfiguration config;
	private String prefix;

	public MakeWorldGuardRegion(StructuresManager instance, YamlConfiguration config, YamlConfiguration messages)
	{
		this.instance = instance;
		this.config = config;
		this.messages = messages;
		this.prefix = messages.getString("prefix");
	}

	public boolean command(CommandSender sender, String[] args, String label)
	{
		if(sender.hasPermission("structuresmanager.makewgregion"))
		{
			final Map<String, Structure> structuresCache = instance.getStructuresCache();
			if(structuresCache.containsKey(args[1]))
			{
				final Structure struct = structuresCache.get(args[1]);
				final Map<String, Boolean> regionFlags = config.getConfigurationSection("wg-region-flags").getValues(false).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> (boolean) e.getValue()));
				WorldGuardHook.makeStructRegion(instance, struct, regionFlags, null, null);
				struct.hasRegion(true);
				sendMessage(sender, messages.getStringList("wg-region-make-success"), prefix, "{struct_name}", args[1]);
			}
			else
			{
				sendMessage(sender, messages.getStringList("errors.structure-not-existing"), prefix, "{label}", label, "{struct_name}", args[1]);
			}
		}
		else
		{
			sendMessage(sender, messages.getStringList("errors.not-enough-permission"), prefix);
		}
		return true;
	}
}
