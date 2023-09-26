package fr.zetioz.structuresmanager.listeners;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.WorldGuardHook;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockBreakListener implements Listener
{
	private final Map<String, Structure> structuresCache;
	private final Map<String, List<Location>> blocksLocationsCache;
	private final Map<String, List<Location>> blocksLocationsAddCache;
	private final Map<String, List<Location>> blocksLocationsRemoveCache;

	public BlockBreakListener(final StructuresManager instance)
	{
		blocksLocationsCache = instance.getBlocksLocationsCache();
		blocksLocationsAddCache = instance.getBlocksLocationsRemoveCache();
		blocksLocationsRemoveCache = instance.getBlocksLocationsRemoveCache();
		structuresCache = instance.getStructuresCache();
	}

	@EventHandler
	public void onPlayerBlockBreak(final BlockBreakEvent event)
	{
		final Location blockLocation = event.getBlock().getLocation();
		final List<String> regionIDs = WorldGuardHook.getRegionIDs(blockLocation);
		for(String regionID : regionIDs)
		{
			if(!structuresCache.containsKey(regionID)) continue;
			final Structure structure = structuresCache.get(regionID);
			if(!structure.canBuild())
			{
				event.setCancelled(true);
				return;
			}

			final List<Location> blocksLocations = blocksLocationsCache.getOrDefault(regionID, new ArrayList<>());
			final List<Location> blocksLocationsToAdd = blocksLocationsAddCache.getOrDefault(regionID, new ArrayList<>());

			if(!(blocksLocations.contains(blockLocation) || blocksLocationsToAdd.contains(blockLocation)))
			{
				event.setCancelled(true);
				return;
			}

			final List<Location> blocksLocationsToRemove = blocksLocationsRemoveCache.getOrDefault(regionID, new ArrayList<>());
			blocksLocationsToAdd.remove(blockLocation);
			if(blocksLocations.isEmpty() || !blocksLocations.contains(blockLocation)) continue;
			blocksLocations.remove(blockLocation);
			blocksLocationsToRemove.add(blockLocation);
			blocksLocationsRemoveCache.put(regionID, blocksLocationsToRemove);
		}
	}
}
