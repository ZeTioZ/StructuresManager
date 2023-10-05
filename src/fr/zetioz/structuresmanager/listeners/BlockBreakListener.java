package fr.zetioz.structuresmanager.listeners;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.WorldGuardHook;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Location;
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.*;

public class BlockBreakListener implements Listener
{
	private final StructuresManager instance;

	public BlockBreakListener(final StructuresManager instance)
	{
		this.instance = instance;
	}

	@EventHandler
	public void onPlayerBlockBreak(final BlockBreakEvent event)
	{
		final Map<String, Set<Location>> blocksLocationsCache = instance.getBlocksLocationsCache();
		final Map<String, Set<Location>> blocksLocationsAddCache = instance.getBlocksLocationsAddCache();
		final Map<String, Set<Location>> blocksLocationsRemoveCache = instance.getBlocksLocationsRemoveCache();
		final Map<String, Structure> structuresCache = instance.getStructuresCache();

		final List<Location> blockPartsLocations = new ArrayList<>();
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
				final Set<Location> blocksLocations = blocksLocationsCache.getOrDefault(regionID, new HashSet<>());
				final Set<Location> blocksLocationsToAdd = blocksLocationsAddCache.getOrDefault(regionID, new HashSet<>());


				if(!structure.canBuild() || !(blocksLocations.contains(blockPartLocation) || blocksLocationsToAdd.contains(blockPartLocation)))
				{
					event.setCancelled(true);
					return;
				}

				final Set<Location> blocksLocationsToRemove = blocksLocationsRemoveCache.getOrDefault(regionID, new HashSet<>());
				blocksLocationsToAdd.remove(blockPartLocation);

				if(blocksLocations.isEmpty() || !blocksLocations.contains(blockPartLocation)) continue;
				blocksLocationsToRemove.add(blockPartLocation);
				blocksLocationsRemoveCache.put(regionID, blocksLocationsToRemove);
			}
		}
	}
}
