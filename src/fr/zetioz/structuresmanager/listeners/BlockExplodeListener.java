package fr.zetioz.structuresmanager.listeners;

import fr.zetioz.structuresmanager.StructuresManager;
import fr.zetioz.structuresmanager.hooks.WorldGuardHook;
import fr.zetioz.structuresmanager.objects.Structure;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.*;

public class BlockExplodeListener implements Listener
{
	private final StructuresManager instance;

	public BlockExplodeListener(final StructuresManager instance)
	{
		this.instance = instance;
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event)
	{
		final Map<String, Structure> structuresCache = instance.getStructuresCache();
		final Map<String, Set<Location>> blocksLocationsCache = instance.getBlocksLocationsCache();
		final Map<String, Set<Location>> blocksLocationsAddCache = instance.getBlocksLocationsAddCache();
		final Map<String, Set<Location>> blocksLocationsRemoveCache = instance.getBlocksLocationsRemoveCache();

		final List<Block> explodedBlocks = event.blockList();
		final Iterator<Block> blocksIterator = explodedBlocks.iterator();
		while(blocksIterator.hasNext())
		{
			final Block block = blocksIterator.next();
			if(block == null || block.getType().isAir()) continue;
			final Location blockLocation = block.getLocation();
			final List<String> regionIDs = WorldGuardHook.getRegionIDs(blockLocation);
			for(String regionID : regionIDs)
			{
				if(!structuresCache.containsKey(regionID)) continue;
				final Structure structure = structuresCache.get(regionID);
				final Set<Location> blocksLocations = blocksLocationsCache.getOrDefault(regionID, new HashSet<>());
				final Set<Location> blocksLocationsToAdd = blocksLocationsAddCache.getOrDefault(regionID, new HashSet<>());

				if(!structure.canBuild() || (!blocksLocations.contains(blockLocation) && !blocksLocationsToAdd.contains(blockLocation)))
				{
					blocksIterator.remove();
					break;
				}

				final Set<Location> blocksLocationsToRemove = blocksLocationsRemoveCache.getOrDefault(regionID, new HashSet<>());
				blocksLocationsToAdd.remove(blockLocation);
				if(blocksLocations.isEmpty() || !blocksLocations.contains(blockLocation)) continue;
				blocksLocationsToRemove.add(blockLocation);
				blocksLocationsRemoveCache.put(regionID, blocksLocationsToRemove);
			}
		}
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event)
	{
		final Map<String, Structure> structuresCache = instance.getStructuresCache();
		final Map<String, Set<Location>> blocksLocationsCache = instance.getBlocksLocationsCache();
		final Map<String, Set<Location>> blocksLocationsAddCache = instance.getBlocksLocationsAddCache();
		final Map<String, Set<Location>> blocksLocationsRemoveCache = instance.getBlocksLocationsRemoveCache();

		final List<Block> explodedBlocks = event.blockList();
		final Iterator<Block> blocksIterator = explodedBlocks.iterator();
		while(blocksIterator.hasNext())
		{
			final Block block = blocksIterator.next();
			if(block == null || block.getType().isAir()) continue;
			final Location blockLocation = block.getLocation();
			final List<String> regionIDs = WorldGuardHook.getRegionIDs(blockLocation);
			for(String regionID : regionIDs)
			{
				if(!structuresCache.containsKey(regionID)) continue;
				final Structure structure = structuresCache.get(regionID);
				final Set<Location> blocksLocations = blocksLocationsCache.getOrDefault(regionID, new HashSet<>());
				final Set<Location> blocksLocationsToAdd = blocksLocationsAddCache.getOrDefault(regionID, new HashSet<>());

				if(!structure.canBuild() || (!blocksLocations.contains(blockLocation) && !blocksLocationsToAdd.contains(blockLocation)))
				{
					blocksIterator.remove();
					break;
				}

				final Set<Location> blocksLocationsToRemove = blocksLocationsRemoveCache.getOrDefault(regionID, new HashSet<>());
				blocksLocationsToAdd.remove(blockLocation);
				if(blocksLocations.isEmpty() || !blocksLocations.contains(blockLocation)) continue;
				blocksLocationsToRemove.add(blockLocation);
				blocksLocationsRemoveCache.put(regionID, blocksLocationsToRemove);
			}
		}
	}
}
