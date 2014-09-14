package se.fredsfursten.jumppadplugin;

import java.util.Collection;
import java.util.HashMap;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Jumper implements Listener {
	private static Jumper singleton = null;
	static String rulesCommand = "/rules";

	private HashMap<String, JumpPadInfo> _jumpPadsByBlock = null;
	private HashMap<String, JumpPadInfo> _jumpPadsByName = null;
	private HashMap<Player, Player> _informedPlayers = null;
	private HashMap<Player, Player> _noJumpPlayers = null;
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

	public void load(JavaPlugin plugin){
		JumpPadInfoPersistance.get().onEnable(plugin);
		_plugin = plugin;

		_jumpPadsByBlock = new HashMap<String, JumpPadInfo>();
		_jumpPadsByName = new HashMap<String, JumpPadInfo>();
		_informedPlayers = new HashMap<Player, Player>();
		_noJumpPlayers = new HashMap<Player, Player>();

		JumpPadInfoPersistance persistance = JumpPadInfoPersistance.get();
		Collection<JumpPadInfo> infos = persistance.load();
		for (JumpPadInfo info : infos) {
			this.addInfo(null, info);
		}
		plugin.getLogger().info("Loaded data");
	}

	public void save(){
		JumpPadInfoPersistance persistance = JumpPadInfoPersistance.get();
		for (JumpPadInfo info : _jumpPadsByBlock.values()) {
			persistance.create(info);
		}
		this._plugin.getLogger().info("Saved data");
	}

	public void maybeJump(Player player, Location location) {
		JumpPadInfo info = jumpPadInfo(location);
		if (info == null) {
			forgetThatWeToldPlayerAboutTheRules(player);
			forgetNoJumpPlayer(player);
			return;
		}
		if (!hasReadRules(player)) {
			maybeTellPlayerToReadTheRules(player);
			return;
		}
		if (_noJumpPlayers.containsKey(player)) return;
		player.setVelocity(info.getVelocityVector());
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

	private void forgetNoJumpPlayer(Player player) {
		if (_noJumpPlayers.containsKey(player)) {
			_noJumpPlayers.remove(player);
		}
	}

	private void forgetThatWeToldPlayerAboutTheRules(Player player) {
		if (_informedPlayers.containsKey(player)) {
			_informedPlayers.remove(player);
		}
	}

	private JumpPadInfo jumpPadInfo(Location currentLocation) {
		if (_jumpPadsByBlock == null) return null;
		String position = JumpPadInfo.toBlockHash(currentLocation);
		if (!_jumpPadsByBlock.containsKey(position)) return null;
		return _jumpPadsByBlock.get(position);
	}

	public boolean add(Player player, String[] args)
	{
		if (!hasMandatoryPermission(player, "jumppad.add")) return true;
		if (args.length < 4) {
			player.sendMessage("Incomplete command..");
			return false;
		}

		String name = args[1];
		JumpPadInfo info = findJumpPadByName(player, name);
		if (info != null)
		{
			player.sendMessage("Jumppad already exists: " + name);
			return true;		
		}		

		Location location;
		Vector velocityVector;
		try {
			location = player.getLocation();
			velocityVector = convertToVelocityVector(location, Double.parseDouble(args[2]), Double.parseDouble(args[3]));
		} catch (Exception e) {
			player.sendMessage("Could not parse the two numbers (up speed and forward speed).");
			return false;
		}
		try {
			JumpPadInfo newInfo = new JumpPadInfo(name, location, velocityVector, player);
			addInfo(player, newInfo);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void addInfo(Player player, JumpPadInfo newInfo) {
		_jumpPadsByBlock.put(newInfo.getBlockHash(), newInfo);
		_jumpPadsByName.put(newInfo.getName(), newInfo);
		if (player != null) {
			_noJumpPlayers.put(player, player);
		}
	}

	public boolean edit(Player player, String[] args)
	{
		if (!hasMandatoryPermission(player, "jumppad.edit")) return true;
		if (args.length < 4) {
			player.sendMessage("Incomplete command..");
			return false;
		}

		String name = args[1];
		JumpPadInfo info = findJumpPadByName(player, name);
		if (info == null)
		{
			player.sendMessage("Unknown jumppad: " + name);
			return true;			
		}		

		try {
			Vector velocityVector = convertToVelocityVector(info.getLocation(), Double.parseDouble(args[2]), Double.parseDouble(args[3]));
			JumpPadInfo newInfo = new JumpPadInfo(name, info.getLocation(), velocityVector, player);
			_jumpPadsByBlock.put(newInfo.getBlockHash(), newInfo);
			_jumpPadsByName.put(newInfo.getName(), newInfo);
			return true;
		} catch (Exception e) {
			player.sendMessage("Could not parse the two numbers (up speed and forward speed).");
			return false;
		}
	}

	private Vector convertToVelocityVector(Location location, double upSpeed, double forwardSpeed) {
		double yaw = location.getYaw();
		double rad = yaw*Math.PI/180.0;
		double vectorX = -Math.sin(rad)*forwardSpeed;
		double vectorY = upSpeed;
		double vectorZ = Math.cos(rad)*forwardSpeed;
		Vector jumpVector = new Vector(vectorX, vectorY, vectorZ);
		return jumpVector;
	}

	public boolean remove(Player player, String[] args)
	{
		if (!hasMandatoryPermission(player, "jumppad.remove")) return true;
		if (args.length < 2) {
			player.sendMessage("Incomplete command..");
			return false;
		}
		String name = args[1];
		JumpPadInfo info = findJumpPadByName(player, name);
		if (info == null)
		{
			player.sendMessage("Unknown jumppad: " + name);
			return true;			
		}
		_jumpPadsByName.remove(name);
		_jumpPadsByBlock.remove(info.getBlockHash());
		return true;
	}

	public boolean gotoJumpPad(Player player, String[] args)
	{
		if (!hasMandatoryPermission(player, "jumppad.goto")) return true;
		if (args.length < 2) {
			player.sendMessage("Incomplete command..");
			return false;
		}
		String name = args[1];
		JumpPadInfo info = findJumpPadByName(player, name);
		if (info == null)
		{
			player.sendMessage("Unknown jumppad: " + name);
			return true;			
		}
		player.teleport(info.getLocation());
		_noJumpPlayers.put(player, player);
		return true;
	}

	public boolean list(Player player)
	{
		if (!hasMandatoryPermission(player, "jumppad.list")) return true;

		player.sendMessage("Jump pads:");
		for (JumpPadInfo info : _jumpPadsByName.values()) {
			player.sendMessage(String.format("%s (%s)", info.getName(), info.getPlayerName()));
		}
		return true;
	}

	private JumpPadInfo findJumpPadByName(Player player, String name) {
		if (!_jumpPadsByName.containsKey(name)) return null;
		return _jumpPadsByName.get(name);
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
