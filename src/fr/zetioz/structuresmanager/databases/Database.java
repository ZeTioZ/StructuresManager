package fr.zetioz.structuresmanager.databases;

import fr.zetioz.structuresmanager.StructuresManager;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

@Getter
public abstract class Database
{

	private final StructuresManager instance;
	private final String tablePrefix;
	@Getter(AccessLevel.NONE)
	protected Connection connection;

	public Database(final StructuresManager instance, String tablePrefix)
	{
		this.instance = instance;
		this.tablePrefix = tablePrefix;
	}

	/**
	 * Get the connection to the database
	 *
	 * @return Connection to the database
	 */
	public abstract Connection getSQLConnection();

	/**
	 * Load the database configuration
	 */
	public abstract void load();

	/**
	 * Get the saved blocks locations of all regions from the database
	 *
	 * @return a {@link Map} of {@link String} and {@link Set} of {@link Location} containing the saved blocks locations
	 */
	public Map<String, Set<Location>> getAllRegionsBlocksLocations()
	{
		final Map<String, Set<Location>> allRegionsBlocksLocations = new HashMap<>();
		try(Connection conn = getSQLConnection();
		    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + tablePrefix + "SAVED_BLOCK;");
		    ResultSet rs = stmt.executeQuery())
		{
			while(rs.next())
			{
				final String regionID = rs.getString("REGION_ID");
				final String worldName = rs.getString("WORLD");
				final World world = Bukkit.getWorld(worldName);
				if(world == null) continue;
				final double x = rs.getDouble("X");
				final double y = rs.getDouble("Y");
				final double z = rs.getDouble("Z");
				final Location blockLocation = new Location(world, x, y, z);

				allRegionsBlocksLocations.putIfAbsent(regionID, new HashSet<>());
				allRegionsBlocksLocations.get(regionID).add(blockLocation);
			}
		}
		catch (SQLException ex)
		{
			instance.getLogger().log(Level.SEVERE, "SQLite exception while getting the connection", ex);
		}
		return allRegionsBlocksLocations;
	}

	/**
	 * Get the saved blocks locations of the given region from the database
	 * Used to alleviate the SQL server
	 *
	 * @param regionID ID of the region to get the saved blocks locations from
	 * @return a {@link Map} of {@link String} and {@link Set} of {@link Location} containing the saved blocks locations
	 */
	public Set<Location> getRegionBlocksLocations(String regionID)
	{
		final Set<Location> regionBlocksLocations = new HashSet<>();
		try(Connection conn = getSQLConnection();
		    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + tablePrefix + "SAVED_BLOCK WHERE REGION_ID = " + regionID + ";");
		    ResultSet rs = stmt.executeQuery())
		{
			while(rs.next())
			{
				final String worldName = rs.getString("WORLD");
				final World world = Bukkit.getWorld(worldName);
				if(world == null) continue;
				final double x = rs.getDouble("X");
				final double y = rs.getDouble("Y");
				final double z = rs.getDouble("Z");
				final Location blockLocation = new Location(world, x, y, z);

				regionBlocksLocations.add(blockLocation);
			}
		}
		catch (SQLException ex)
		{
			instance.getLogger().log(Level.SEVERE, "SQLite exception while getting the connection", ex);
		}
		return regionBlocksLocations;
	}

	/**
	 * Remove the blocks locations stored in the remove cache from the database.
	 * Once called the remove cache is cleared.
	 */
	public void removeBlocksLocations()
	{
		final Connection conn = getSQLConnection();
		try
		{
			conn.setAutoCommit(false);
			try(PreparedStatement ps = conn.prepareStatement("DELETE FROM " + tablePrefix + "SAVED_BLOCK WHERE REGION_ID = ? AND WORLD = ? AND X = ? AND Y = ? AND Z = ?;"))
			{
				for(Map.Entry<String, Set<Location>> regionBlocksLocationsToRemove : instance.getBlocksLocationsRemoveCache().entrySet())
				{
					final String regionID = regionBlocksLocationsToRemove.getKey();
					for(final Location blockLocation : regionBlocksLocationsToRemove.getValue())
					{
						ps.setString(1, regionID);
						ps.setString(2, blockLocation.getWorld().getName());
						ps.setDouble(3, blockLocation.getX());
						ps.setDouble(4, blockLocation.getY());
						ps.setDouble(5, blockLocation.getZ());
						ps.addBatch();
					}
				}
				ps.executeBatch();
			}
			catch (SQLException ex)
			{
				conn.rollback();
				throw ex;
			}
			conn.commit();
			instance.getBlocksLocationsRemoveCache().clear();
		}
		catch (SQLException ex)
		{
			try
			{
				conn.rollback();
			}
			catch (SQLException ex1)
			{
				instance.getLogger().severe(ex1.getMessage());
			}
			instance.getLogger().log(Level.SEVERE, "SQL exception while saving blocks locations into database:", ex);
		} finally
		{
			try
			{
				conn.close();
			}
			catch(SQLException e)
			{
				instance.getLogger().log(Level.SEVERE, "SQL exception while closing the connection:", e);
			}
		}
	}

	/**
	 * Save the blocks locations stored in the add cache into the database.
	 * Once called the add cache is cleared.
	 */
	public void saveBlocksLocation()
	{
		final Connection conn = getSQLConnection();
		try
		{
			conn.setAutoCommit(false);
			try(PreparedStatement ps = conn.prepareStatement("REPLACE INTO " + tablePrefix + "SAVED_BLOCK(REGION_ID, WORLD, X, Y, Z) VALUES (?, ?, ?, ?, ?);"))
			{
				for(Map.Entry<String, Set<Location>> regionBlocksLocations : instance.getBlocksLocationsAddCache().entrySet())
				{
					final String regionID = regionBlocksLocations.getKey();
					for(Location blockLocation : regionBlocksLocations.getValue())
					{
						ps.setString(1, regionID);
						ps.setString(2, blockLocation.getWorld().getName());
						ps.setDouble(3, blockLocation.getX());
						ps.setDouble(4, blockLocation.getY());
						ps.setDouble(5, blockLocation.getZ());
						ps.addBatch();
					}
				}
				ps.executeBatch();
			}
			catch (SQLException ex)
			{
				conn.rollback();
				throw ex;
			}
			conn.commit();
			instance.getBlocksLocationsAddCache().clear();
		}
		catch (SQLException ex)
		{
			try
			{
				conn.rollback();
			}
			catch (SQLException ex1)
			{
				instance.getLogger().severe(ex1.getMessage());
			}
			instance.getLogger().log(Level.SEVERE, "SQL exception while saving blocks locations into database:", ex);
		} finally
		{
			try
			{
				conn.close();
			}
			catch(SQLException e)
			{
				instance.getLogger().log(Level.SEVERE, "SQL exception while closing the connection:", e);
			}
		}
	}

	/**
	 * Execute a query update with the given query string
	 *
	 * @param query Query to execute
	 */
	public void executeQueryUpdate(final String query)
	{
		try (Connection conn = getSQLConnection(); PreparedStatement ps = conn.prepareStatement(query))
		{
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			instance.getLogger().log(Level.SEVERE, "Could not execute query: " + query, e);
		}
	}
}
