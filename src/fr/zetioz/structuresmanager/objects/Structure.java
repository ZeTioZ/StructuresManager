package fr.zetioz.structuresmanager.objects;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@SerializableAs("Structure")
public class Structure implements ConfigurationSerializable
{
	private final String name;
	private final String id;
	private final Location location;
	private final List<Material> blocksWhiteList;

	@Accessors(fluent = true)
	private boolean hasRegion;
	@Accessors(fluent = true)
	private boolean isWhiteListActive;
	@Accessors(fluent = true)
	private boolean canBuild;

	public Structure(String name, String id, Location location)
	{
		this.name = name;
		this.id = id;
		this.location = location;
		this.blocksWhiteList = new ArrayList<>();
		this.hasRegion = false;
		this.isWhiteListActive = false;
		this.canBuild = false;
	}

	public Structure(Map<String, Object> map)
	{
		this.name = (String) map.get("name");
		this.id = (String) map.get("id");
		this.location = Location.deserialize((Map<String, Object>) map.get("location"));
		this.blocksWhiteList = (List<Material>) map.get("blocksWhiteList");
		this.hasRegion = (boolean) map.get("hasRegion");
		this.isWhiteListActive = (boolean) map.get("isWhiteListActive");
		this.canBuild = (boolean) map.get("canBuild");
	}

	public Structure(String name, String id, Location location, List<Material> blocksWhiteList, boolean hasRegion, boolean isWhiteListActive, boolean canBuild)
	{
		this.name = name;
		this.id = id;
		this.location = location;
		this.blocksWhiteList = blocksWhiteList;
		this.hasRegion = hasRegion;
		this.isWhiteListActive = isWhiteListActive;
		this.canBuild = canBuild;
	}

	@NotNull
	@Override
	public Map<String, Object> serialize()
	{
		return Map.of(
				"name", name,
				"id", id,
				"location", location.serialize(),
				"blocksWhiteList", blocksWhiteList,
				"hasRegion", hasRegion,
				"isWhiteListActive", isWhiteListActive,
				"canBuild", canBuild
		);
	}

	@NotNull
	public static Structure deserialize(@NotNull Map<String, Object> map)
	{
		final String name = (String) map.get("name");
		final String id = (String) map.get("id");
		final Location location = Location.deserialize((Map<String, Object>) map.get("location"));
		final List<Material> blocksWhiteList = (List<Material>) map.get("blocksWhiteList");
		final boolean hasRegion = (boolean) map.get("hasRegion");
		final boolean isWhiteListActive = (boolean) map.get("isWhiteListActive");
		final boolean canBuild = (boolean) map.get("canBuild");
		return new Structure(name, id, location, blocksWhiteList, hasRegion, isWhiteListActive, canBuild);
	}
}
