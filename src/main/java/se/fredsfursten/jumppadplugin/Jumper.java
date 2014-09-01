package se.fredsfursten.jumppadplugin;

import java.util.HashMap;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Jumper implements Listener {
	private static Jumper singleton = null;

	private HashMap<String, Vector> _jumpPads = null;

	private Jumper() {
	}

	public static Jumper get()
	{
		if (singleton == null) {
			singleton = new Jumper();
		}
		return singleton;
	}

	public void enable(JavaPlugin plugin){
		_jumpPads = new HashMap<String, Vector>();
	}
	
	public void maybeJump(Player player, Location location) {
		if (!player.hasPermission("jumppad.jump")) {
			player.sendMessage("Please read the global rules (/rules) to get access to the jump pads.");
			return;
		}
		Vector jumpVector = jumpPadVector(location);
		if (jumpVector == null) return;
		player.setVelocity(jumpVector);
	}

	private Vector jumpPadVector(Location currentLocation) {
		if (_jumpPads == null) return null;
		String position = convert(currentLocation);
		if (!_jumpPads.containsKey(position)) return null;
		return _jumpPads.get(position);
	}
	
	private String convert(Location location)
	{
		return String.format("%d;%d;%d", location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public boolean add(Player player, String[] args)
	{
		if (!hasMandatoryPermission(player, "jumppad.add")) return false;
		if (args.length < 3) {
			player.sendMessage("Incomplete command..");
			return false;
		}
		try {
			double upSpeed = Double.parseDouble(args[1]);
			double forwardSpeed = Double.parseDouble(args[2]);
			Location location = player.getLocation();
			double yaw = location.getYaw();
			double rad = yaw*Math.PI/180.0;
			double vectorX = -Math.sin(rad)*forwardSpeed;
			double vectorY = upSpeed;
			double vectorZ = Math.cos(rad)*forwardSpeed;
			Vector jumpVector = new Vector(vectorX, vectorY, vectorZ);
			String hash = convert(location);
			_jumpPads.put(hash, jumpVector);
			return true;
		} catch (Exception e) {
			player.sendMessage("Could not parse the two numbers (up speed and forward speed).");
			return false;
		}
	}

	public boolean remove(Player player)
	{
		if (!hasMandatoryPermission(player, "jumppad.remove")) return false;
		_jumpPads = new HashMap<String, Vector>();
		return true;
	}
	
	private boolean hasMandatoryPermission(Player player, String permission)
	{
		if (player.hasPermission(permission)) return true;
		player.sendMessage("You must have permission " + permission);
		return false;
	}
}
