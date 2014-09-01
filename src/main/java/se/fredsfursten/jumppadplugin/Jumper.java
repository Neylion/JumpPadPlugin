package se.fredsfursten.jumppadplugin;

import java.util.HashMap;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Jumper implements Listener {
	private static Jumper singleton = null;
	static String rulesCommand = "/rules";

	private HashMap<String, Vector> _jumpPads = null;
	private HashMap<Player, Player> _informedPlayers = null;
	private JavaPlugin _plugin = null;

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
		_plugin = plugin;
		_jumpPads = new HashMap<String, Vector>();
		_informedPlayers = new HashMap<Player, Player>();
	}

	public void maybeJump(Player player, Location location) {
		Vector jumpVector = jumpPadVector(location);
		if (jumpVector == null) return;
		if (!hasReadRules(player)) {
			maybeTellPlayerToReadTheRules(player);
			return;
		}
		player.setVelocity(jumpVector);
	}

	private boolean hasReadRules(Player player) {
		return player.hasPermission("jumppad.jump");
	}

	private void maybeTellPlayerToReadTheRules(Player player) {
		if (!_informedPlayers.containsKey(player)) {
			player.sendMessage("Please read the global rules (/rules) to get access to the jump pads.");
			_informedPlayers.put(player, player);
		}
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

	public static Vector calculateVelocity(Vector from, Vector to, int heightGain)
	{
		// Gravity of a potion
		double gravity = 0.115;

		// Block locations
		int endGain = to.getBlockY() - from.getBlockY();
		double horizDist = Math.sqrt(distanceSquared(from, to));

		// Height gain
		int gain = heightGain;

		double maxGain = gain > (endGain + gain) ? gain : (endGain + gain);

		// Solve quadratic equation for velocity
		double a = -horizDist * horizDist / (4 * maxGain);
		double b = horizDist;
		double c = -endGain;

		double slope = -b / (2 * a) - Math.sqrt(b * b - 4 * a * c) / (2 * a);

		// Vertical velocity
		double vy = Math.sqrt(maxGain * gravity);

		// Horizontal velocity
		double vh = vy / slope;

		// Calculate horizontal direction
		int dx = to.getBlockX() - from.getBlockX();
		int dz = to.getBlockZ() - from.getBlockZ();
		double mag = Math.sqrt(dx * dx + dz * dz);
		double dirx = dx / mag;
		double dirz = dz / mag;

		// Horizontal velocity components
		double vx = vh * dirx;
		double vz = vh * dirz;

		return new Vector(vx, vy, vz);
	}

	private static double distanceSquared(Vector from, Vector to)
	{
		double dx = to.getBlockX() - from.getBlockX();
		double dz = to.getBlockZ() - from.getBlockZ();

		return dx * dx + dz * dz;
	}

	public void listenToCommands(Player player, String message) {
		if (message.toLowerCase().startsWith(rulesCommand))
		{
			player.sendMessage("Getting permission");
			player.addAttachment(_plugin, "jumppad.jump", true);
		}
	}
}
