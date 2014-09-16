package se.fredsfursten.jumppadplugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

class JumpPadInfo {
	private Vector velocity;
	private Location location;
	private String name;
	private UUID creatorId;
	private String creatorName;
	
	public JumpPadInfo(String name, Location location, Vector velocity, UUID creatorId, String creatorName)
	{
		this.velocity = velocity;
		this.name = name;
		this.location = location;
		this.creatorId = creatorId;
		this.creatorName = creatorName;
	}
	
	public Vector getVelocity() {
		return this.velocity;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public String getBlockHash() {
		return JumpPadInfo.toBlockHash(this.location);
	}

	public static String toBlockHash(Location location)
	{
		return String.format("%d;%d;%d", location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	public Player getCreator()
	{
		return Bukkit.getServer().getPlayer(this.creatorId);
	}
	
	public String getCreatorName() {
		return this.creatorName;
	}
	
	public UUID getCreatorId() {
		return this.creatorId;
	}
}
