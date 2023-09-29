package fr.zetioz.structuresmanager.hooks;

import java.io.*;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.world.block.BlockState;
import fr.zetioz.structuresmanager.StructuresManager;
import org.bukkit.Location;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SchematicUtilsWE7
{
	private SchematicUtilsWE7()
	{
	}

	/**
	 * Save a schematic from a selection
	 *
	 * @param instance     The StructuresManager instance
	 * @param player       The player who save the schematic
	 * @param filename     The name of the schematic
	 * @param saveLocation The location where the schematic will be saved
	 * @return true if the schematic is saved, false if the selection is null
	 */
	public static boolean saveSchematic(StructuresManager instance, Player player, String filename, Location saveLocation)
	{
		final World weWorld = new BukkitWorld(saveLocation.getWorld());
		final Region selection = WorldEdit.getInstance().getSessionManager().findByName(player.getName()).getSelection(weWorld);
		if(selection == null) return false;
		final File dataDirectory = new File(instance.getDataFolder(), "structures");
		if(!dataDirectory.exists()) dataDirectory.mkdirs();
		final File file = new File(dataDirectory, filename + ".schem");
		try
		{
			final BlockArrayClipboard clipboard = new BlockArrayClipboard(selection);
			final Extent source = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);
			final Extent destination = clipboard;
			final ForwardExtentCopy copy = new ForwardExtentCopy(source, selection, clipboard.getOrigin(), destination, selection.getMinimumPoint());
			copy.setSourceMask(new ExistingBlockMask(source));
			Operations.completeLegacy(copy);
			final ClipboardFormat format = ClipboardFormats.findByFile(file);
			try(ClipboardWriter writer = format.getWriter(new FileOutputStream(file)))
			{
				writer.write(clipboard);
			}
		}
		catch(WorldEditException | IOException ex)
		{
			instance.getLogger().severe("Something went wrong while saving the schematic to file:" + ex.getMessage());
		}
		return true;
	}

	/**
	 * Delete a schematic at a given location (replace all blocks by air)
	 *
	 * @param instance      The StructuresManager instance
	 * @param schematicName The name of the schematic to delete
	 * @param pasteLoc      The location where the schematic is pasted from
	 * @return true if the schematic file exists, false otherwise
	 */
	public static boolean deleteSchematic(StructuresManager instance, String schematicName, Location pasteLoc)
	{
		final File dataDirectory = new File(instance.getDataFolder(), "structures");
		if(!dataDirectory.exists()) dataDirectory.mkdirs();
		final File schematic = new File(dataDirectory, schematicName + ".schem");
		final World world = new BukkitWorld(pasteLoc.getWorld());

		final Clipboard clipboard = getSchematicClipboard(schematic.getPath());
		if(clipboard == null) return false;
		final BlockVector3[] pasteSelection = getPasteSelection(clipboard, pasteLoc);
		final Region region = new CuboidRegion(world, pasteSelection[0], pasteSelection[1]);

		try(EditSession editSession = WorldEdit.getInstance().newEditSession(world))
		{
			final BlockState air = BukkitAdapter.adapt(Material.AIR.createBlockData());
			editSession.setBlocks(region, air);
		}
		return true;
	}

	/**
	 * Paste a schematic at a given location
	 *
	 * @param instance      The StructuresManager instance
	 * @param schematicName The name of the schematic to paste
	 * @param pasteLoc      The location to paste the schematic at
	 * @return true if the schematic file exists successfully, false otherwise
	 */
	public static boolean pasteSchematic(StructuresManager instance, String schematicName, Location pasteLoc)
	{
		final File dataDirectory = new File(instance.getDataFolder(), "structures");
		if(!dataDirectory.exists()) dataDirectory.mkdirs();
		final File schematic = new File(dataDirectory, schematicName + ".schem");
		final World world = new BukkitWorld(pasteLoc.getWorld());

		final Clipboard clipboard = getSchematicClipboard(schematic.getPath());
		if(clipboard == null) return false;

		try(EditSession editSession = WorldEdit.getInstance().newEditSession(world))
		{
			Operation operation = new ClipboardHolder(clipboard).createPaste(editSession).to(BlockVector3.at(pasteLoc.getBlockX(), pasteLoc.getBlockY(), pasteLoc.getBlockZ())).build();
			Operations.complete(operation);
		}
		return true;
	}

	/**
	 * Get the clipboard of a schematic
	 *
	 * @param schematicPath The path of the schematic
	 * @return The clipboard of the schematic
	 */
	public static Clipboard getSchematicClipboard(String schematicPath)
	{
		try
		{
			final File schematic = new File(schematicPath);

			final Clipboard clipboard;
			final ClipboardFormat format = ClipboardFormats.findByFile(schematic);
			try(ClipboardReader reader = format.getReader(new FileInputStream(schematic)))
			{
				clipboard = reader.read();
			}

			return clipboard;
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the selection of a schematic pasted at a given location
	 *
	 * @param clipboard      The clipboard of the schematic
	 * @param structLocation The location from where the schematic is pasted
	 * @return The selection of the schematic in a {@link BlockVector3} array with the lower point at index 0 and the upper point at index 1
	 */
	public static BlockVector3[] getPasteSelection(@NotNull Clipboard clipboard, @NotNull Location structLocation)
	{
		final BlockVector3 minPoint = clipboard.getMinimumPoint();
		final BlockVector3 maxPoint = clipboard.getMaximumPoint();
		final BlockVector3 origin = clipboard.getOrigin();
		final BlockVector3 translationVectorFromOrigin = BlockVector3.at(structLocation.getX(), structLocation.getY(), structLocation.getZ()).subtract(origin);
		final BlockVector3 lowerPoint = minPoint.add(translationVectorFromOrigin);
		final BlockVector3 upperPoint = maxPoint.add(translationVectorFromOrigin);
		return new BlockVector3[]{lowerPoint, upperPoint};
	}
}