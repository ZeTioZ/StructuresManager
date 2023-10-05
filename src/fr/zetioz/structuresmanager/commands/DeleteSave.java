package fr.zetioz.structuresmanager.commands;

import fr.zetioz.structuresmanager.StructuresManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Set;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class DeleteSave
{
	private final StructuresManager instance;
	private final YamlConfiguration messages;
	private final String prefix;

	public DeleteSave(StructuresManager instance, YamlConfiguration messages)
	{
		this.instance = instance;
		this.messages = messages;
		this.prefix = messages.getString("prefix");
	}

	public boolean command(CommandSender sender, String[] args, Set<String> files)
	{
		if(sender.hasPermission("structuresmanager.delete.save"))
		{
			final File dataDirectory = new File(instance.getDataFolder(), "structures");
			if(dataDirectory.listFiles() == null)
			{
				sendMessage(sender, messages.getStringList("errors.structure-file-not-existing"), prefix, "{struct_name}", args[2]);
				return true;
			}

			if(files.contains(args[2]))
			{
				final File schematicFile = new File(dataDirectory, args[2] + ".schem");
				final boolean result = schematicFile.delete();
				if(result)
				{
					sendMessage(sender, messages.getStringList("structure-file-delete-success"), prefix, "{struct_name}", args[2]);
				}
				else
				{
					sendMessage(sender, messages.getStringList("errors.structure-file-delete-failed"), prefix, "{struct_name}", args[2]);
				}
			}
			else
			{
				sendMessage(sender, messages.getStringList("errors.structure-file-not-existing"), prefix, "{struct_name}", args[2]);
			}
		}
		else
		{
			sendMessage(sender, messages.getStringList("errors.not-enough-permission"), prefix);
		}
		return true;
	}
}
