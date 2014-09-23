package se.fredsfursten.jumppadplugin;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class AllJumpPads implements Listener {
	private static final String FILE_PATH = "plugins/JumpPad/jumppad_locations.bin";
	private static AllJumpPads singleton = null;

	private HashMap<String, JumpPadInfo> _jumpPadsByBlock = null;
	private HashMap<String, JumpPadInfo> _jumpPadsByName = null;
	private JavaPlugin _plugin = null;

	private AllJumpPads() {
	}

	static AllJumpPads get() {
		if (singleton == null) {
			singleton = new AllJumpPads();
		}
		return singleton;
	}

	void add(JumpPadInfo info) {
		_jumpPadsByBlock.put(info.getBlockHash(), info);
		_jumpPadsByName.put(info.getName(), info);
	}

	void remove(JumpPadInfo info) {
		_jumpPadsByName.remove(info.getName());
		_jumpPadsByBlock.remove(info.getBlockHash());
	}

	Collection<JumpPadInfo> getAll() {
		return _jumpPadsByName.values();
	}

	JumpPadInfo getByLocation(Location location) {
		if (_jumpPadsByBlock == null) return null;
		String position = JumpPadInfo.toBlockHash(location);
		if (!_jumpPadsByBlock.containsKey(position)) return null;
		return _jumpPadsByBlock.get(position);
	}

	JumpPadInfo getByName(String name) {
		if (!_jumpPadsByName.containsKey(name)) return null;
		return _jumpPadsByName.get(name);
	}

	void load(JavaPlugin plugin) {
		_plugin = plugin;

		_jumpPadsByBlock = new HashMap<String, JumpPadInfo>();
		_jumpPadsByName = new HashMap<String, JumpPadInfo>();

		ArrayList<JumpPadStorage> jumpPadStorageList = loadData(plugin);
		if (jumpPadStorageList == null) return;
		rememberAllData(jumpPadStorageList);
		this._plugin.getLogger().info(String.format("Loaded %d JumpPads", jumpPadStorageList.size()));
	}

	private ArrayList<JumpPadStorage> loadData(JavaPlugin plugin) {
		ArrayList<JumpPadStorage> jumpPadStorageList = null;
		try {
			jumpPadStorageList = SavingAndLoading.load(FILE_PATH);
		} catch (FileNotFoundException e) {
			plugin.getLogger().info("No jump pad data file found.");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			plugin.getLogger().info("Failed to load data.");
			return null;
		}
		return jumpPadStorageList;
	}

	private void rememberAllData(ArrayList<JumpPadStorage> jumpPadStorageList) {
		for (JumpPadStorage jumpPadStorage : jumpPadStorageList) {
			this.add(jumpPadStorage.getJumpPadInfo());
		}
	}

	void save() {
		ArrayList<JumpPadStorage> jumpPadStorageList = getAllData();
		boolean success = saveData(jumpPadStorageList);
		if (success) {
			this._plugin.getLogger().info(String.format("Saved %d JumpPads", jumpPadStorageList.size()));
		} else {
			this._plugin.getLogger().info("Failed to save data.");			
		}
	}

	private ArrayList<JumpPadStorage> getAllData() {
		ArrayList<JumpPadStorage> jumpPadStorageList = new ArrayList<JumpPadStorage>();
		for (JumpPadInfo jumpPadInfo : getAll()) {
			jumpPadStorageList.add(new JumpPadStorage(jumpPadInfo));
		}
		return jumpPadStorageList;
	}

	private boolean saveData(ArrayList<JumpPadStorage> jumpPadStorageList) {
		try {
			SavingAndLoading.save(jumpPadStorageList, FILE_PATH);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
