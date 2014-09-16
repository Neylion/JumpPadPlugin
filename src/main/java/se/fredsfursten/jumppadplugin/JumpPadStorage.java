package se.fredsfursten.jumppadplugin;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

class JumpPadStorage implements Serializable {
	private static final long serialVersionUID = 1L;
	private Vector velocity;
	private Vector location;
	private UUID worldId;
	private String name;
	private UUID playerId;
	
	public JumpPadStorage(String name, Location location, Vector velocity, Player player)
	{
		this.name = name;
		this.location = new Vector(location.getX(), location.getY(), location.getZ());
		this.worldId = location.getWorld().getUID();
		this.velocity = velocity.clone();
		this.playerId = player.getUniqueId();
	}
	
	public JumpPadStorage(JumpPadInfo jumpPadInfo)
	{
		this(jumpPadInfo.getName(), jumpPadInfo.getLocation(), jumpPadInfo.getVelocity(), jumpPadInfo.getPlayer());
	}
	
	public World getWorld()
	{
		return Bukkit.getServer().getWorld(this.worldId);
	}
	
	public Location getLocation()
	{
		return new Location(getWorld(), this.location.getX(), this.location.getY(), this.location.getZ());
	}
	
	public Player getPlayer()
	{
		return Bukkit.getServer().getPlayer(this.playerId);
	}
	
	public JumpPadInfo getJumpPadInfo()
	{
		return new JumpPadInfo(this.name, getLocation(), this.velocity, getPlayer());
	}
}
