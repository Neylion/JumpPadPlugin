package se.fredsfursten.jumppadplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Jumper implements Listener {
	private static Jumper singleton = null;

	private Vector _jumpVector = null;
	private Location _jumpLocation = null;

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
	}
	
	public void maybeJump(Player player, Location location) {
		if (_jumpLocation == null) return;
		if (_jumpVector == null) return;
		if (shouldJump(location)) {
			doJump(player);
		}
	}

	private void doJump(Player player) {
		player.setVelocity(_jumpVector);
	}

	private boolean shouldJump(Location currentLocation) {

		return (_jumpLocation != null)
				&& (currentLocation.getBlockX() == _jumpLocation.getBlockX())
				&& (currentLocation.getBlockY() == _jumpLocation.getBlockY())
				&& (currentLocation.getBlockZ() == _jumpLocation.getBlockZ());
	}


	public boolean add(Player player, String[] args)
	{
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
			_jumpVector = new Vector(vectorX, vectorY, vectorZ);
			_jumpLocation = location;
			return true;
		} catch (Exception e) {
			player.sendMessage("Could not parse the two numbers.");
			return false;
		}
	}

	public boolean remove(Player player)
	{
		_jumpLocation = null;
		return true;
	}
}
