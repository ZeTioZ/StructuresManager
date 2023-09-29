package fr.zetioz.structuresmanager.listeners;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.WorldGuardHook;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockPlaceListener implements Listener
{
	private final Map<String, Structure> structuresCache;
	private final Map<String, List<Location>> blocksLocationsCache;
	private final Map<String, List<Location>> blocksLocationsAddCache;
	private final Map<String, List<Location>> blocksLocationsRemoveCache;

	public BlockPlaceListener(final StructuresManager instance)
	{
		blocksLocationsCache = instance.getBlocksLocationsCache();
		blocksLocationsAddCache = instance.getBlocksLocationsAddCache();
		blocksLocationsRemoveCache = instance.getBlocksLocationsRemoveCache();
		structuresCache = instance.getStructuresCache();
	}

	@EventHandler
	public void onPlayerBlockPlace(final BlockPlaceEvent event)
	{
		if(event.isCancelled()) return;
		final Location location = event.getBlock().getLocation();
		final List<String> regionIDs = WorldGuardHook.getRegionIDs(location);
		for(String regionID : regionIDs)
		{
			if(!structuresCache.containsKey(regionID)) continue;
			final Structure structure = structuresCache.get(regionID);
			if(!structure.canBuild()
				|| (structure.isWhiteListActive()
					&& !structure.getBlocksWhiteList().contains(event.getBlock().getType()))
			)
			{
				event.setCancelled(true);
				return;
			}
			final List<Location> blocksLocations = blocksLocationsCache.getOrDefault(regionID, new ArrayList<>());
			final List<Location> blocksLocationsToAdd = blocksLocationsAddCache.getOrDefault(regionID, new ArrayList<>());
			final List<Location> blocksLocationsToRemove = blocksLocationsRemoveCache.getOrDefault(regionID, new ArrayList<>());

			if(blocksLocationsToAdd.contains(location) || blocksLocations.contains(location)) continue;
			blocksLocations.add(location);
			blocksLocationsToAdd.add(location);
			blocksLocationsToRemove.remove(location);
			blocksLocationsCache.put(regionID, blocksLocations);
			blocksLocationsAddCache.put(regionID, blocksLocationsToAdd);
			blocksLocationsRemoveCache.put(regionID, blocksLocationsToRemove);
		}
	}

	@EventHandler
	public void onPlayerBlockMultiPlace(BlockMultiPlaceEvent event)
	{
		final Location firstLocation = event.getBlockPlaced().getLocation();
		final Location secondLocation = event.getBlockAgainst().getLocation();
		final List<String> regionIDs = WorldGuardHook.getRegionIDs(firstLocation);
		for(String regionID : regionIDs)
		{
			if(!structuresCache.containsKey(regionID)) continue;
			final Structure structure = structuresCache.get(regionID);
			if(!structure.canBuild()
				|| (structure.isWhiteListActive()
					&& !structure.getBlocksWhiteList().contains(event.getBlock().getType()))
			)
			{
				event.setCancelled(true);
				return;
			}


			final List<Location> blocksLocations = blocksLocationsCache.getOrDefault(regionID, new ArrayList<>());
			final List<Location> blocksLocationsToAdd = blocksLocationsAddCache.getOrDefault(regionID, new ArrayList<>());
			final List<Location> blocksLocationsToRemove = blocksLocationsRemoveCache.getOrDefault(regionID, new ArrayList<>());

			if(blocksLocationsToAdd.contains(firstLocation) || blocksLocations.contains(firstLocation)) continue;
			blocksLocations.add(firstLocation);
			blocksLocationsToAdd.add(firstLocation);
			blocksLocationsToRemove.remove(firstLocation);
			blocksLocations.add(secondLocation);
			blocksLocationsToAdd.add(secondLocation);
			blocksLocationsToRemove.remove(secondLocation);
			blocksLocationsCache.put(regionID, blocksLocations);
			blocksLocationsAddCache.put(regionID, blocksLocationsToAdd);
			blocksLocationsRemoveCache.put(regionID, blocksLocationsToRemove);
		}
	}
}
