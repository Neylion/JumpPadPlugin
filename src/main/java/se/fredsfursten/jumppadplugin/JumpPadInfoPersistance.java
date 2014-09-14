package se.fredsfursten.jumppadplugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;


public class JumpPadInfoPersistance {

	private static JumpPadInfoPersistance singleton = null;
	private Connection _connection = null;
	private Plugin _plugin;

	private JumpPadInfoPersistance()  {
	}

	public void onEnable(Plugin plugin)
	{
		this._plugin = plugin;
		MySQL MySQL = new MySQL(plugin, "hetzner.havokoc.se", "3306", "jumppad", "jumppad", "bq69r9aL5Yp4EeHS");
		try {
			this._connection = MySQL.openConnection();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			plugin.getLogger().info(e.getMessage());
			e.printStackTrace();
		}
		plugin.getLogger().info("Connected to DB");
	}

	public static JumpPadInfoPersistance get()
	{
		if (singleton == null) {
			singleton = new JumpPadInfoPersistance();
		}
		return singleton;
	}

	public void create(JumpPadInfo newInfo) {
		try {
			Statement statement = this._connection.createStatement();
			String cmd = String.format("INSERT INTO jumppad (`name`" + 
					", `locationX`, `locationY`, `locationZ`" + 
					", `velocityX`, `velocityY`, `velocityZ`" +
					", `world`, `user`)" +
					" VALUES ('%s', %d, %d, %d, %d, %d, %d, '%s', '%s');",
					newInfo.getName(),
					newInfo.getLocation().getX(), newInfo.getLocation().getY(), newInfo.getLocation().getZ(),
					newInfo.getVelocityVector().getX(), newInfo.getVelocityVector().getY(), newInfo.getVelocityVector().getZ(),
					newInfo.getLocation().getWorld().getUID().toString(),
					newInfo.getPlayer().getUniqueId().toString());
			statement.executeUpdate(cmd);
			_plugin.getLogger().info(cmd);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Collection<JumpPadInfo> load() {
		ArrayList<JumpPadInfo> list = new ArrayList<JumpPadInfo>();
		Statement statement;
		try {
			statement = this._connection.createStatement();

			ResultSet res = statement.executeQuery("SELECT * FROM jumppad WHERE 1;");
			while (res.next())
			{
				String worldId = res.getString("world");
				UUID id = UUID.fromString(worldId);
				World world = Bukkit.getServer().getWorld(id);
				String userId = res.getString("user");
				Player player = Bukkit.getServer().getPlayer(UUID.fromString(userId));
				String name = res.getString("name");
				double x = res.getDouble("locationX");
				double y = res.getDouble("locationY");
				double z = res.getDouble("locationZ");
				Location location = new Location(world, x, y, z);
				x = res.getDouble("velocityX");
				y = res.getDouble("velocityY");
				z = res.getDouble("velocityZ");
				Vector vector = new Vector(x, y, z);
				JumpPadInfo info = new JumpPadInfo(name, location, vector, player);
				list.add(info);
			}		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return list;
	}
}
