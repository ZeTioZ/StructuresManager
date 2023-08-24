package fr.zetioz.structuresmanager.objects;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@Setter
@SerializableAs("Structure")
public class Structure implements ConfigurationSerializable
{
	private final String name;
	private final String id;
	private final Location location;

	@Accessors(fluent = true)
	private boolean hasRegion;

	public Structure(String name, String id, Location location)
	{
		this.name = name;
		this.id = id;
		this.location = location;
		this.hasRegion = false;
	}

	public Structure(Map<String, Object> map)
	{
		this.name = (String) map.get("name");
		this.id = (String) map.get("id");
		this.location = Location.deserialize((Map<String, Object>) map.get("location"));
		this.hasRegion = (boolean) map.get("hasRegion");
	}

	public Structure(String name, String id, Location location, boolean hasRegion)
	{
		this.name = name;
		this.id = id;
		this.location = location;
		this.hasRegion = hasRegion;
	}

	@NotNull
	@Override
	public Map<String, Object> serialize()
	{
		return Map.of(
				"name", name,
				"id", id,
				"location", location.serialize(),
				"hasRegion", hasRegion
		);
	}

	@NotNull
	public static Structure deserialize(@NotNull Map<String, Object> map)
	{
		final String name = (String) map.get("name");
		final String id = (String) map.get("id");
		final Location location = Location.deserialize((Map<String, Object>) map.get("location"));
		final boolean hasRegion = (boolean) map.get("hasRegion");
		return new Structure(name, id, location, hasRegion);
	}
}
