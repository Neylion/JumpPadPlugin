package se.fredsfursten.jumppadplugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

class JumpPadInfo {
	private Vector velocity;
	private Location location;
	private String name;
	private Player player;
	public JumpPadInfo(String name, Location location, Vector velocity, Player player)
	{
		this.velocity = velocity;
		this.name = name;
		this.location = location;
		this.player = player;
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
	public Player getPlayer() {
		return this.player;
	}
	public String getPlayerName() {
		return this.player.getDisplayName();
	}
}
