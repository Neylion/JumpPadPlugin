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
	private double velocityX;
	private double velocityY;
	private double velocityZ;
	private double locationX;
	private double locationY;
	private double locationZ;
	private UUID worldId;
	private String name;
	private UUID playerId;
	
	public JumpPadStorage(String name, Location location, Vector velocity, Player player)
	{
		this.name = name;
		this.locationX =location.getX();
		this.locationY = location.getY();
		this.locationZ = location.getZ();
		this.worldId = location.getWorld().getUID();
		this.velocityX = velocity.getX();
		this.velocityY = velocity.getY();
		this.velocityZ = velocity.getZ();
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
		return new Location(getWorld(), this.locationX, this.locationY, this.locationZ);
	}
	
	public Vector getVelocity()
	{
		return new Vector(this.velocityX, this.velocityY, this.velocityZ);
	}
	
	public Player getPlayer()
	{
		return Bukkit.getServer().getPlayer(this.playerId);
	}
	
	public JumpPadInfo getJumpPadInfo()
	{
		return new JumpPadInfo(this.name, getLocation(), getVelocity(), getPlayer());
	}
}
