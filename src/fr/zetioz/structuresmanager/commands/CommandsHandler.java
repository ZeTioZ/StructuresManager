package fr.zetioz.structuresmanager.commands;

import fr.zetioz.coreutils.FilesManagerUtils;
import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class CommandsHandler implements TabExecutor, FilesManagerUtils.ReloadableFiles
{
	private final StructuresManager instance;
	private YamlConfiguration messages;
	private YamlConfiguration config;
	private String prefix;

	public CommandsHandler(StructuresManager instance) throws FileNotFoundException
	{
		this.instance = instance;
		instance.getFilesManagerUtils().addReloadable(this);
		reloadFiles();
	}

	@Override
	public void reloadFiles() throws FileNotFoundException
	{
		this.messages = instance.getFilesManagerUtils().getSimpleYaml("messages");
		this.config = instance.getFilesManagerUtils().getSimpleYaml("config");
		this.prefix = messages.getString("prefix");
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("structuresmanager"))
		{
			if(args.length == 1)
			{
				if(args[0].equalsIgnoreCase("help"))
				{
					sendMessage(sender, messages.getStringList("help-command"), prefix, "{label}", label);
					return true;
				}
				if(args[0].equalsIgnoreCase("reload"))
				{
					if(sender.hasPermission("structuresmanager.reload"))
					{
						instance.getFilesManagerUtils().reloadAllSimpleYaml();
						sendMessage(sender, messages.getStringList("reload-command"), prefix);
					}
					else
					{
						sendMessage(sender, messages.getStringList("errors.not-enough-permission"), prefix);
					}
					return true;
				}
			}
			else if(args.length == 2)
			{
				if(args[0].equalsIgnoreCase("makewgregion"))
				{
					return new MakeWorldGuardRegion(instance, config, messages).command(sender, args, label);
				}
				else if(args[0].equalsIgnoreCase("reset"))
				{
					return new ResetStructure(instance, config, messages).command(sender, args, label);
				}

				if(!(sender instanceof final Player player))
				{
					sendMessage(sender, messages.getStringList("errors.must-be-a-player"), prefix);
					return true;
				}

				if(args[0].equalsIgnoreCase("save"))
				{
					return new SaveStructure(instance, messages).command(player, args);
				}
				else if(args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport"))
				{
					return new TeleportToStructure(instance, messages).command(player, args, label);
				}
			}
			else if(args.length == 3)
			{
				if(args[0].equalsIgnoreCase("delete"))
				{
					if(args[1].equalsIgnoreCase("save"))
					{
						return new DeleteSave(instance, messages).command(sender, args, getStructuresFiles());
					}
					else if(args[1].equalsIgnoreCase("structure"))
					{
						return new DeleteStructure(instance, messages).command(sender, args, label);
					}
				}
				else if(args[0].equalsIgnoreCase("toggle"))
				{
					final Structure structure = instance.getStructuresCache().get(args[2]);
					if(args[1].equalsIgnoreCase("build"))
					{
						final boolean canBuild = structure.canBuild();
						return new ToggleBuildableRegion(instance, messages).command(sender, args, !canBuild);
					}
					else if(args[1].equalsIgnoreCase("whitelist"))
					{
						final boolean whitelist = structure.isWhiteListActive();
						return new ToggleBlocksWhitelist(instance, messages).command(sender, args, !whitelist);
					}
				}

				if (!(sender instanceof final Player player))
				{
					sendMessage(sender, messages.getStringList("errors.must-be-a-player"), prefix);
					return true;
				}

				if(args[0].equalsIgnoreCase("place"))
				{
					return new PlaceStructure(instance, messages).command(player, args, label);
				}
			}
			else if(args.length == 4)
			{
				if(args[0].equalsIgnoreCase("whitelist"))
				{
					if(args[1].equalsIgnoreCase("add"))
					{
						return new AddMaterialToWhitelist(instance, messages).command(sender, args, label);
					}
					else if(args[1].equalsIgnoreCase("remove"))
					{
						return new RemoveMaterialFromWhitelist(instance, messages).command(sender, args, label);
					}
				}
			}
			sendMessage(sender, messages.getStringList("help-command"), prefix, "{label}", label);
			return true;
		}
		return false;
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
	{
		if(command.getName().equalsIgnoreCase("structuresmanager"))
		{
			final List<String> firstArgList = new ArrayList<>();
			final List<String> completions = new ArrayList<>();

			if(sender.hasPermission("structuresmanager.reload")) firstArgList.addAll(List.of("help", "reload", "place", "save", "delete", "makewgregion", "reset", "whitelist", "toggle"));

			if(args.length == 1)
			{
				StringUtil.copyPartialMatches(args[0], firstArgList, completions);
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("makewgregion"))
			{
				StringUtil.copyPartialMatches(args[1], instance.getStructuresCache().keySet(), completions);
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("whitelist"))
			{
				StringUtil.copyPartialMatches(args[1], List.of("add", "remove"), completions);
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("toggle"))
			{
				StringUtil.copyPartialMatches(args[1], List.of("build", "whitelist"), completions);
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("reset"))
			{
				final Map<String, Structure> structuresCache = instance.getStructuresCache();
				final List<String> structNames = new ArrayList<>();
				structNames.add("all");
				structNames.addAll(structuresCache.keySet());
				StringUtil.copyPartialMatches(args[1], structNames, completions);
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("delete"))
			{
				final List<String> deleteTypes = List.of("save", "structure");
				StringUtil.copyPartialMatches(args[1], deleteTypes, completions);
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("place"))
			{
				StringUtil.copyPartialMatches(args[1], getStructuresFiles(), completions);
			}
			else if(args.length == 2 && (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport")))
			{
				StringUtil.copyPartialMatches(args[1], instance.getStructuresCache().keySet(), completions);
			}
			else if(args.length == 3 && args[0].equalsIgnoreCase("delete"))
			{
				if(args[1].equalsIgnoreCase("save"))
				{
					StringUtil.copyPartialMatches(args[2], getStructuresFiles(), completions);
				}
				else if(args[1].equalsIgnoreCase("structure"))
				{
					StringUtil.copyPartialMatches(args[2], instance.getStructuresCache().keySet(), completions);
				}
			}
			else if(args.length == 3 && args[0].equalsIgnoreCase("toggle"))
			{
				if(args[1].equalsIgnoreCase("build") || args[1].equalsIgnoreCase("whitelist"))
				{
					StringUtil.copyPartialMatches(args[2], instance.getStructuresCache().keySet(), completions);
				}
			}
			else if(args.length == 3 && args[0].equalsIgnoreCase("whitelist"))
			{
				if(args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))
				{
					StringUtil.copyPartialMatches(args[2], instance.getStructuresCache().keySet(), completions);
				}
			}
			else if(args.length == 4 && args[0].equalsIgnoreCase("whitelist"))
			{
				if(args[1].equalsIgnoreCase("add"))
				{
					final Structure structure = instance.getStructuresCache().get(args[2]);
					final List<String> materials = new ArrayList<>(Stream.of(Material.values()).map(Enum::name).toList());
					materials.removeAll(structure.getBlocksWhiteList());
					StringUtil.copyPartialMatches(args[3], materials, completions);
				}
				else if(args[1].equalsIgnoreCase("remove"))
				{
					final Structure structure = instance.getStructuresCache().get(args[2]);
					StringUtil.copyPartialMatches(args[3], structure.getBlocksWhiteList(), completions);
				}
			}
			Collections.sort(completions);
			return completions;
		}
		return new ArrayList<>();
	}

	private Set<String> getStructuresFiles()
	{
		final File dataDirectory = new File(instance.getDataFolder(), "structures");
		if(dataDirectory.listFiles() == null) return Set.of();
		Set<String> files = Stream.of(Objects.requireNonNull(dataDirectory.listFiles()))
				.filter(file -> !file.isDirectory() && file.getName().endsWith(".schem"))
				.map(File::getName)
				.collect(Collectors.toSet());
		return files.stream().map(file -> file.replace(".schem", "")).collect(Collectors.toSet());
	}
}
