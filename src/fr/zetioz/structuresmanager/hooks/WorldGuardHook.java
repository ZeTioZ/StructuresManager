package fr.zetioz.structuresmanager.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.objects.Structure;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.util.List;
import java.util.Map;

import static fr.zetioz.structuresmanager.hooks.SchematicUtilsWE7.getPasteSelection;
import static fr.zetioz.structuresmanager.hooks.SchematicUtilsWE7.getSchematicClipboard;

public class WorldGuardHook
{
	public static void makeStructRegion(StructuresManager instance, Structure struct, Map<String, Boolean> flags, List<String> members, List<String> owners)
	{
		final String structureName = struct.getName();
		final String structureId = struct.getId();
		final Location structLocation = struct.getLocation();
		final File dataDirectory = new File(instance.getDataFolder(), "structures");
		final File schematicFile = new File(dataDirectory, structureName + ".schem");
		final Clipboard clipboard = getSchematicClipboard(schematicFile.getPath());

		if(clipboard == null)
		{
			instance.getLogger().severe("The schematic file " + schematicFile.getName() + " is corrupted or doesn't exist.");
			return;
		}
		final BlockVector3[] pasteSelection = getPasteSelection(clipboard, structLocation);
		final ProtectedCuboidRegion region = new ProtectedCuboidRegion(structureId, pasteSelection[0], pasteSelection[1]);

		flags.forEach((flag, value) ->
		{
			Flag<?> stateFlag = WorldGuard.getInstance().getFlagRegistry().get(flag);
			region.setFlag((StateFlag) stateFlag, value ? StateFlag.State.ALLOW : StateFlag.State.DENY);
		});
		WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(structLocation.getWorld())).addRegion(region);
	}

	public static void deleteRegion(World regionWorld, String regionName) throws StorageException
	{
		if(regionExists(new Location(regionWorld, 0, 0, 0), regionName))
		{
			WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(regionWorld)).removeRegion(regionName);
		}
	}

	public static boolean regionExists(Location location, String regionName)
	{
		return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld())).getRegion(regionName) != null;
	}

	public static String regionNameGenerator(Location location, String baseName)
	{
		int i = 1;
		String name;
		do
		{
			name = baseName + "_" + i;
		}
		while(regionExists(location, baseName + "_" + i++));
		return name;
	}

	public static List<String> getRegionIDs(Location location)
	{
		return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld())).getApplicableRegionsIDs(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
	}
}
