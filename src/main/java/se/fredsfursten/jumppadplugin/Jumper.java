package se.fredsfursten.jumppadplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

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
		if (shouldJump(location)) {
			player.setFireTicks(2000);
			doJump(player);
		} else {
			player.sendMessage(String.format("Jump location: (%d,%d,%d), your location: (%d,%d,%d)",
					_jumpLocation.getBlockX(),
					_jumpLocation.getBlockY(),
					_jumpLocation.getBlockZ(),
					location.getBlockX(),
					location.getBlockY(),
					location.getBlockZ()
					));
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
		if (args.length < 4) {
			player.sendMessage("Incomplete command..");
			return false;
		}
		try {
			double velocityX = Double.parseDouble(args[1]);
			double velocityY = Double.parseDouble(args[2]);
			double velocityZ = Double.parseDouble(args[3]);
			_jumpVector = new Vector(velocityX, velocityY, velocityZ);
			_jumpLocation = player.getLocation();	
			return true;
		} catch (Exception e) {
			player.sendMessage("Could not parse the three numbers.");
			return false;
		}
	}

	public boolean remove(Player player)
	{
		_jumpLocation = null;
		return true;
	}
}
