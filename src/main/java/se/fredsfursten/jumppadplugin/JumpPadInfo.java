package se.fredsfursten.jumppadplugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

class JumpPadInfo {
	private Vector velocityVector;
	private Location location;
	private String name;
	private Player player;
	public JumpPadInfo(String name, Location location, Vector velocityVector, Player player)
	{
		this.velocityVector = velocityVector;
		this.name = name;
		this.location = location;
		this.player = player;
	}
	public Vector getVelocityVector() {
		return this.velocityVector;
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
	public Player getPlayer() {
		return this.player;
	}
	public String getPlayerName() {
		return this.player.getDisplayName();
	}
}
