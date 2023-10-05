package fr.zetioz.structuresmanager.listeners;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.WorldGuardHook;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.util.*;

public class BlockIgniteListener implements Listener
{
	private final StructuresManager instance;

	public BlockIgniteListener(final StructuresManager instance)
	{
		this.instance = instance;
	}

	@EventHandler
	public void onTNTPrime(BlockPhysicsEvent event)
	{
		if(!event.getChangedType().isAir()) return;

		final Map<String, Set<Location>> blocksLocationsCache = instance.getBlocksLocationsCache();
		final Map<String, Set<Location>> blocksLocationsAddCache = instance.getBlocksLocationsAddCache();
		final Map<String, Set<Location>> blocksLocationsRemoveCache = instance.getBlocksLocationsRemoveCache();
		final Map<String, Structure> structuresCache = instance.getStructuresCache();

		final Location blockLocation = event.getBlock().getLocation();

		final List<String> regionIDs = WorldGuardHook.getRegionIDs(blockLocation);
		for(String regionID : regionIDs)
		{
			if(!structuresCache.containsKey(regionID)) continue;
			final Structure structure = structuresCache.get(regionID);
			final Set<Location> blocksLocations = blocksLocationsCache.getOrDefault(regionID, new HashSet<>());
			final Set<Location> blocksLocationsToAdd = blocksLocationsAddCache.getOrDefault(regionID, new HashSet<>());


			if(!structure.canBuild() || !(blocksLocations.contains(blockLocation) || blocksLocationsToAdd.contains(blockLocation)))
			{
				event.setCancelled(true);
				return;
			}

			final Set<Location> blocksLocationsToRemove = blocksLocationsRemoveCache.getOrDefault(regionID, new HashSet<>());
			blocksLocationsToAdd.remove(blockLocation);

			if(blocksLocations.isEmpty() || !blocksLocations.contains(blockLocation)) continue;
			blocksLocationsToRemove.add(blockLocation);
			blocksLocationsRemoveCache.put(regionID, blocksLocationsToRemove);
		}
	}
}
