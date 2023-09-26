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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BlockExplodeListener implements Listener
{
	private final Map<String, Structure> structuresCache;
	private final Map<String, List<Location>> blocksLocationsCache;
	private final Map<String, List<Location>> blocksLocationsAddCache;
	private final Map<String, List<Location>> blocksLocationsRemoveCache;

	public BlockExplodeListener(final StructuresManager instance)
	{
		structuresCache = instance.getStructuresCache();
		blocksLocationsCache = instance.getBlocksLocationsCache();
		blocksLocationsAddCache = instance.getBlocksLocationsRemoveCache();
		blocksLocationsRemoveCache = instance.getBlocksLocationsRemoveCache();
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event)
	{
		final List<Block> explodedBlocks = event.blockList();
		final Iterator<Block> blocksIterator = explodedBlocks.iterator();
		for(Block block = blocksIterator.next(); blocksIterator.hasNext();)
		{
			final Location blockLocation = block.getLocation();
			final List<String> regionIDs = WorldGuardHook.getRegionIDs(blockLocation);
			for(String regionID : regionIDs)
			{
				if(!structuresCache.containsKey(regionID)) continue;
				final Structure structure = structuresCache.get(regionID);
				if(!structure.canBuild())
				{
					blocksIterator.remove();
					break;
				}

				final List<Location> blocksLocations = blocksLocationsCache.getOrDefault(regionID, new ArrayList<>());
				final List<Location> blocksLocationsToAdd = blocksLocationsAddCache.getOrDefault(regionID, new ArrayList<>());

				if(!(blocksLocations.contains(blockLocation) || blocksLocationsToAdd.contains(blockLocation)))
				{
					blocksIterator.remove();
					break;
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

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event)
	{
		final List<Block> explodedBlocks = event.blockList();
		final Iterator<Block> blocksIterator = explodedBlocks.iterator();
		for(Block block = blocksIterator.next(); blocksIterator.hasNext();)
		{
			final Location blockLocation = block.getLocation();
			final List<String> regionIDs = WorldGuardHook.getRegionIDs(blockLocation);
			for(String regionID : regionIDs)
			{
				if(!structuresCache.containsKey(regionID)) continue;
				final Structure structure = structuresCache.get(regionID);
				if(!structure.canBuild())
				{
					blocksIterator.remove();
					break;
				}

				final List<Location> blocksLocations = blocksLocationsCache.getOrDefault(regionID, new ArrayList<>());
				final List<Location> blocksLocationsToAdd = blocksLocationsAddCache.getOrDefault(regionID, new ArrayList<>());

				if(!(blocksLocations.contains(blockLocation) || blocksLocationsToAdd.contains(blockLocation)))
				{
					blocksIterator.remove();
					break;
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
}
