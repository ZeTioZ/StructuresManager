package fr.zetioz.structuresmanager.commands;

import com.sk89q.worldguard.protection.managers.storage.StorageException;
import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.SchematicUtilsWE7;
import fr.zetioz.structuresmanager.hooks.WorldGuardHook;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class DeleteStructure
{
	private final StructuresManager instance;
	private final YamlConfiguration messages;
	private final String prefix;

	public DeleteStructure(StructuresManager instance, YamlConfiguration messages)
	{
		this.instance = instance;
		this.messages = messages;
		this.prefix = messages.getString("prefix");
	}

	public boolean command(CommandSender sender, String[] args, String label)
	{
		if(sender.hasPermission("structuresmanager.delete.structure"))
		{
			final Map<String, Structure> structuresCache = instance.getStructuresCache();
			if(structuresCache.containsKey(args[2]))
			{
				final Structure struct = structuresCache.get(args[2]);
				final boolean result = SchematicUtilsWE7.deleteSchematic(instance, struct.getName(), struct.getLocation());
				if(result)
				{
					sendMessage(sender, messages.getStringList("structure-delete-success"), prefix, "{struct_name}", args[2]);
					if(struct.hasRegion())
					{
						try
						{
							WorldGuardHook.deleteRegion(struct.getLocation().getWorld(), struct.getId());
						}
						catch(StorageException e)
						{
							throw new RuntimeException(e);
						}
					}
					structuresCache.remove(args[2]);
				}
				else
				{
					sendMessage(sender, messages.getStringList("errors.structure-delete-failed"), prefix, "{struct_name}", args[2]);
				}
			}
			else
			{
				sendMessage(sender, messages.getStringList("errors.structure-not-existing"), prefix, "{label}", label, "{struct_name}", args[2]);
			}
		}
		else
		{
			sendMessage(sender, messages.getStringList("errors.not-enough-permission"), prefix);
		}
		return true;
	}
}
