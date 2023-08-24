package fr.zetioz.structuresmanager.commands;

import com.sk89q.worldguard.protection.managers.storage.StorageException;
import fr.zetioz.coreutils.FilesManagerUtils;
import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.SchematicUtilsWE7;
import fr.zetioz.structuresmanager.hooks.WorldGuardHook;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Location;
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

public class StructuresManagerCommand implements TabExecutor, FilesManagerUtils.ReloadableFiles
{
	private final StructuresManager instance;
	private YamlConfiguration messages;
	private YamlConfiguration config;
	private String prefix;

	public StructuresManagerCommand(StructuresManager instance) throws FileNotFoundException
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
				if(!(sender instanceof final Player player))
				{
					sendMessage(sender, messages.getStringList("errors.must-be-a-player"), prefix);
					return true;
				}

				if(args[0].equalsIgnoreCase("save"))
				{
					if(sender.hasPermission("structuresmanager.save"))
					{
						final boolean result = SchematicUtilsWE7.saveSchematic(instance, player, args[1], player.getLocation());
						if(result)
						{
							sendMessage(sender, messages.getStringList("structure-save-success"), prefix);
						}
						else
						{
							sendMessage(sender, messages.getStringList("errors.structure-save-failed"), prefix);
						}
					}
					else
					{
						sendMessage(sender, messages.getStringList("errors.not-enough-permission"), prefix);
					}
					return true;
				}
				else if(args[0].equalsIgnoreCase("makewgregion"))
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
				if(args[0].equalsIgnoreCase("reset"))
				{
					if(sender.hasPermission("structuresmanager.reset"))
					{
						final Map<String, Structure> structuresCache = instance.getStructuresCache();
						if(args[1].equalsIgnoreCase("all"))
						{
							for(final Map.Entry<String, Structure> entry : structuresCache.entrySet())
							{
								final String structName = entry.getKey();
								final Structure struct = entry.getValue();
								final boolean result = SchematicUtilsWE7.pasteSchematic(instance, struct.getName(), struct.getLocation());
								if(result)
								{
									if(config.getBoolean("debug"))
									{
										sendMessage(sender, messages.getStringList("structure-reloaded"), prefix, "{struct_name}", structName);
									}
								}
								else
								{
									sendMessage(sender, messages.getStringList("errors.structure-reload-failed"), prefix, "{struct_name}", structName);
								}
							}
							sendMessage(sender, messages.getStringList("structures-reload-success"), prefix);
						}
						else
						{
							if(structuresCache.containsKey(args[1]))
							{
								final Structure struct = structuresCache.get(args[1]);
								final boolean result = SchematicUtilsWE7.pasteSchematic(instance, struct.getName(), struct.getLocation());
								if(result)
								{
									sendMessage(sender, messages.getStringList("structure-reloaded"), prefix, "{struct_name}", args[1]);
								}
								else
								{
									sendMessage(sender, messages.getStringList("errors.structure-reload-failed"), prefix, "{struct_name}", args[1]);
								}
							}
							else
							{
								sendMessage(sender, messages.getStringList("errors.structure-not-existing"), prefix, "{label}", label, "{struct_name}", args[1]);
							}
						}
					}
					else
					{
						sendMessage(sender, messages.getStringList("errors.not-enough-permission"), prefix);
					}
					return true;
				}
				else if(args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport"))
				{
					if(sender.hasPermission("structuresmanager.teleport"))
					{
						final Map<String, Structure> structuresCache = instance.getStructuresCache();
						if(structuresCache.containsKey(args[1]))
						{
							final Structure struct = structuresCache.get(args[1]);
							final Location loc = struct.getLocation();
							player.teleport(loc.clone().add(0, 1, 0));
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
			else if(args.length == 3)
			{
				if (!(sender instanceof final Player player))
				{
					sendMessage(sender, messages.getStringList("errors.must-be-a-player"), prefix);
					return true;
				}
				if(args[0].equalsIgnoreCase("place"))
				{
					if(sender.hasPermission("structuresmanager.place"))
					{
						final Map<String, Structure> structuresCache = instance.getStructuresCache();
						if(!structuresCache.containsKey(args[2]))
						{
							final boolean result = SchematicUtilsWE7.pasteSchematic(instance, args[1], player.getLocation());
							if(result)
							{
								sendMessage(sender, messages.getStringList("structure-place-success"), prefix);
								final Structure newStruct = new Structure(args[1], args[2], player.getLocation());
								structuresCache.put(args[2], newStruct);
							}
							else
							{
								sendMessage(sender, messages.getStringList("errors.structure-place-failed"), prefix);
							}
						}
						else
						{
							sendMessage(sender, messages.getStringList("errors.structure-already-existing"), prefix, "{label}", label, "{struct_name}", args[1]);
						}
					}
					else
					{
						sendMessage(sender, messages.getStringList("errors.not-enough-permission"), prefix);
					}
					return true;
				}
				else if(args[0].equalsIgnoreCase("delete"))
				{
					if(args[1].equalsIgnoreCase("save"))
					{
						if(sender.hasPermission("structuresmanager.delete.save"))
						{
							final File dataDirectory = new File(instance.getDataFolder(), "structures");
							if(dataDirectory.listFiles() == null)
							{
								sendMessage(sender, messages.getStringList("errors.structure-file-not-existing"), prefix, "{struct_name}", args[2]);
								return true;
							}
							Set<String> files =  getStructuresFiles();

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
					else if(args[1].equalsIgnoreCase("structure"))
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

			if(sender.hasPermission("structuresmanager.reload")) firstArgList.addAll(List.of("help", "reload", "place", "save", "delete", "makewgregion", "reset"));

			if(args.length == 1)
			{
				StringUtil.copyPartialMatches(args[0], firstArgList, completions);
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("makewgregion"))
			{
				StringUtil.copyPartialMatches(args[1], instance.getStructuresCache().keySet(), completions);
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
