package fr.zetioz.structuresmanager.commands;

import fr.zetioz.coreutils.FilesManagerUtils;
import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.FileNotFoundException;
import java.util.Map;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class ToggleBlockWhiteList implements FilesManagerUtils.ReloadableFiles
{
	private final StructuresManager instance;
	private YamlConfiguration messages;
	private String prefix;

	public ToggleBlockWhiteList(StructuresManager instance) throws FileNotFoundException
	{
		this.instance = instance;
		instance.getFilesManagerUtils().addReloadable(this);
		reloadFiles();
	}

	@Override
	public void reloadFiles() throws FileNotFoundException
	{
		this.messages = instance.getFilesManagerUtils().getSimpleYaml("messages");
		this.prefix = messages.getString("prefix");
	}

	public boolean command(CommandSender sender, String regionID, boolean value)
	{
		final Map<String, Structure> structuresCache = instance.getStructuresCache();
		if(!structuresCache.containsKey(regionID))
		{
			sendMessage(sender, "errors.structure-not-existing", prefix);
			return true;
		}
		final Structure structure = structuresCache.get(regionID);
		final boolean isWhiteListActive = structure.isWhiteListActive();
		if(isWhiteListActive == value)
		{
			sendMessage(sender, "errors.structure-already-whitelist-" + (value ? "enabled" : "disabled"), prefix);
			return true;
		}
		structure.isWhiteListActive(value);
		return true;
	}
}
