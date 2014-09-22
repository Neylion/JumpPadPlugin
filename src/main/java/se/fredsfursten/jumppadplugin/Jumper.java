package se.fredsfursten.jumppadplugin;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Jumper implements Listener {
	private static final String FILE_PATH = "plugins/JumpPad/jumppad_locations.bin";
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
		_plugin = plugin;

		_jumpPadsByBlock = new HashMap<String, JumpPadInfo>();
		_jumpPadsByName = new HashMap<String, JumpPadInfo>();
		_informedPlayers = new HashMap<Player, Player>();
		_noJumpPlayers = new HashMap<Player, Player>();

		ArrayList<JumpPadStorage> jumpPadStorageList;
		try {
			jumpPadStorageList = SavingAndLoading.load(FILE_PATH);
		} catch (FileNotFoundException e) {
			plugin.getLogger().info("No jump pad data found.");
			return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			plugin.getLogger().info("Failed to load data.");
			return;
		}
		for (JumpPadStorage jumpPadStorage : jumpPadStorageList) {
			this.addInfo(null, jumpPadStorage.getJumpPadInfo());
		}
		this._plugin.getLogger().info(String.format("Loaded %d JumpPads", jumpPadStorageList.size()));
	}

	public void save() {
		ArrayList<JumpPadStorage> jumpPadStorageList = new ArrayList<JumpPadStorage>();
		for (JumpPadInfo jumpPadInfo : _jumpPadsByBlock.values()) {
			jumpPadStorageList.add(new JumpPadStorage(jumpPadInfo));
		}
		try {
			SavingAndLoading.save(jumpPadStorageList, FILE_PATH);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this._plugin.getLogger().info("Failed to save data.");
			return;
		}
		this._plugin.getLogger().info(String.format("Saved %d JumpPads", jumpPadStorageList.size()));
	}

	public void maybeJump(Player player, Location location) {
		JumpPadInfo info = findJumpPadInfo(location);
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
		
		// Nudge the player off the ground to always have no friction (being in the air)
		player.teleport(player.getLocation().add(0.0,0.01,0.0));
		player.setVelocity(info.getVelocity());
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

	private JumpPadInfo findJumpPadInfo(Location currentLocation) {
		if (_jumpPadsByBlock == null) return null;
		String position = JumpPadInfo.toBlockHash(currentLocation);
		if (!_jumpPadsByBlock.containsKey(position)) return null;
		return _jumpPadsByBlock.get(position);
	}

	public boolean addCommand(Player player, String[] args)
	{
		if (!hasMandatoryPermission(player, "jumppad.add")) return true;
		if ((args.length < 3) || (args.length > 4)) {
			player.sendMessage("/jumppad add <name> <up speed> [<forward speed>]");
			return true;
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
		String upSpeed = args[2];
		String forwardSpeed = "0.0";
		if (args.length > 3)
		{
			forwardSpeed = args[3];
		}
		
		try {
			location = player.getLocation();
			velocityVector = convertToVelocityVector(location, Double.parseDouble(upSpeed), Double.parseDouble(forwardSpeed));
		} catch (Exception e) {
			player.sendMessage("/jumppad add <name> <up speed> [<forward speed>]");
			return true;
		}
		try {
			JumpPadInfo newInfo = new JumpPadInfo(name, location, velocityVector, player.getUniqueId(), player.getName());
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

	public boolean editCommand(Player player, String[] args)
	{
		if (!hasMandatoryPermission(player, "jumppad.edit")) return true;
		JumpPadInfo info = findJumpPadInfo(player.getLocation());
		if (info == null) {
			player.sendMessage("You must go to a jumppad before you edit the jumppad. Use /jumppad goto <name>.");	
			return true;
		}
		if ((args.length < 2) || (args.length > 3)) {
			player.sendMessage("/jumppad edit <up speed> [<forward speed>]");
			return true;
		}	

		

		Location location;
		Vector velocityVector;
		String upSpeed = args[1];
		String forwardSpeed = "0.0";
		if (args.length > 2)
		{
			forwardSpeed = args[2];
		}
		
		try {
			location = player.getLocation();
			velocityVector = convertToVelocityVector(location, Double.parseDouble(upSpeed), Double.parseDouble(forwardSpeed));
		} catch (Exception e) {
			player.sendMessage("/jumppad edit <up speed> [<forward speed>]");
			return true;
		}
		try {
			JumpPadInfo newInfo = new JumpPadInfo(info.getName(), location, velocityVector, player.getUniqueId(), player.getName());
			addInfo(player, newInfo);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
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

	public boolean removeCommand(Player player, String[] args)
	{
		if (!hasMandatoryPermission(player, "jumppad.remove")) return true;
		if (args.length < 2) {
			player.sendMessage("/jumppad remove <name>");
			return true;
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

	public boolean gotoCommand(Player player, String[] args)
	{
		if (!hasMandatoryPermission(player, "jumppad.goto")) return true;
		if (args.length < 2) {
			player.sendMessage("/jumppad goto <name>");
			return true;
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

	public boolean listCommand(Player player)
	{
		if (!hasMandatoryPermission(player, "jumppad.list")) return true;

		player.sendMessage("Jump pads:");
		for (JumpPadInfo info : _jumpPadsByName.values()) {
			player.sendMessage(String.format("%s (%s)", info.getName(), info.getCreatorName()));
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
