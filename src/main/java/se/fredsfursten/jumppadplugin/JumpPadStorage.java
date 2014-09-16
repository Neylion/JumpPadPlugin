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
	private UUID creatorId;
	private String creatorName;
	
	public JumpPadStorage(String name, Location location, Vector velocity, UUID creatorId, String creatorName)
	{
		this.name = name;
		this.locationX =location.getX();
		this.locationY = location.getY();
		this.locationZ = location.getZ();
		this.worldId = location.getWorld().getUID();
		this.velocityX = velocity.getX();
		this.velocityY = velocity.getY();
		this.velocityZ = velocity.getZ();
		this.creatorId = creatorId;
		this.creatorName = creatorName;
		
		Player creator = getCreator();
		if (creator != null){
			this.creatorName = creator.getName();
		}
	}
	
	public JumpPadStorage(JumpPadInfo jumpPadInfo)
	{
		this(jumpPadInfo.getName(), jumpPadInfo.getLocation(), jumpPadInfo.getVelocity(), jumpPadInfo.getCreatorId(), jumpPadInfo.getCreatorName());
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
	
	public Player getCreator()
	{
		return Bukkit.getServer().getPlayer(this.creatorId);
	}
	
	public JumpPadInfo getJumpPadInfo()
	{
		Player creator = getCreator();
		if (creator != null)
		{
			this.creatorName = creator.getName();
		}
		return new JumpPadInfo(this.name, getLocation(), getVelocity(), this.creatorId, this.creatorName);
	}
}
