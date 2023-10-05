package fr.zetioz.structuresmanager.listeners;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.WorldGuardHook;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Location;
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.*;

public class BlockPlaceListener implements Listener
{
	private final StructuresManager instance;

	public BlockPlaceListener(final StructuresManager instance)
	{
		this.instance = instance;
	}

	@EventHandler
	public void onPlayerBlockPlace(final BlockPlaceEvent event)
	{
		final Map<String, Set<Location>> blocksLocationsCache = instance.getBlocksLocationsCache();
		final Map<String, Set<Location>> blocksLocationsAddCache = instance.getBlocksLocationsAddCache();
		final Map<String, Set<Location>> blocksLocationsRemoveCache = instance.getBlocksLocationsRemoveCache();
		final Map<String, Structure> structuresCache = instance.getStructuresCache();

		final Location location = event.getBlock().getLocation();
		final List<String> regionIDs = WorldGuardHook.getRegionIDs(location);
		for(String regionID : regionIDs)
		{
			if(!structuresCache.containsKey(regionID)) continue;
			final Structure structure = structuresCache.get(regionID);
			if(!structure.canBuild()
				|| (structure.isWhiteListActive()
					&& !structure.getBlocksWhiteList().contains(event.getBlock().getType().name()))
			)
			{
				event.setCancelled(true);
				return;
			}

			final Set<Location> blocksLocations = blocksLocationsCache.getOrDefault(regionID, new HashSet<>());
			final Set<Location> blocksLocationsToAdd = blocksLocationsAddCache.getOrDefault(regionID, new HashSet<>());
			final Set<Location> blocksLocationsToRemove = blocksLocationsRemoveCache.getOrDefault(regionID, new HashSet<>());

			if(blocksLocationsToAdd.contains(location) || blocksLocations.contains(location)) continue;
			blocksLocationsToAdd.add(location);
			blocksLocationsToRemove.remove(location);
			blocksLocationsAddCache.put(regionID, blocksLocationsToAdd);
			blocksLocationsRemoveCache.put(regionID, blocksLocationsToRemove);
		}
	}

	@EventHandler
	public void onPlayerBlockMultiPlace(BlockMultiPlaceEvent event)
	{
		final Map<String, Set<Location>> blocksLocationsCache = instance.getBlocksLocationsCache();
		final Map<String, Set<Location>> blocksLocationsAddCache = instance.getBlocksLocationsAddCache();
		final Map<String, Set<Location>> blocksLocationsRemoveCache = instance.getBlocksLocationsRemoveCache();
		final Map<String, Structure> structuresCache = instance.getStructuresCache();

		final Set<Location> blockPartsLocations = new HashSet<>();
		blockPartsLocations.add(event.getBlock().getLocation());

		if (event.getBlock().getBlockData() instanceof final Bed bed)
		{
			final Location bedLocation = event.getBlock().getLocation();
			if (bed.getPart() == Bed.Part.HEAD)
			{
				bedLocation.subtract(bed.getFacing().getDirection());
			}
			else
			{
				bedLocation.add(bed.getFacing().getDirection());
			}
			blockPartsLocations.add(bedLocation);
		}

		for(Location blockPartLocation : blockPartsLocations)
		{
			final List<String> regionIDs = WorldGuardHook.getRegionIDs(blockPartLocation);
			for(String regionID : regionIDs)
			{
				if(!structuresCache.containsKey(regionID)) continue;
				final Structure structure = structuresCache.get(regionID);
				if(!structure.canBuild()
						|| (structure.isWhiteListActive()
						&& !structure.getBlocksWhiteList().contains(event.getBlock().getType().name()))
				)
				{
					event.setCancelled(true);
					return;
				}

				final Set<Location> blocksLocations = blocksLocationsCache.getOrDefault(regionID, new HashSet<>());
				final Set<Location> blocksLocationsToAdd = blocksLocationsAddCache.getOrDefault(regionID, new HashSet<>());
				final Set<Location> blocksLocationsToRemove = blocksLocationsRemoveCache.getOrDefault(regionID, new HashSet<>());

				if(blocksLocationsToAdd.contains(blockPartLocation) || blocksLocations.contains(blockPartLocation)) continue;
				blocksLocationsToAdd.add(blockPartLocation);
				blocksLocationsToRemove.remove(blockPartLocation);
				blocksLocationsAddCache.put(regionID, blocksLocationsToAdd);
				blocksLocationsRemoveCache.put(regionID, blocksLocationsToRemove);
			}
		}
	}
}
