package se.fredsfursten.jumppadplugin;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class Jumper implements Listener {
	private static Jumper singleton = null;

	private HashMap<Player, Player> _informedPlayers = null;
	private HashMap<Player, JumpPadInfo> _inAirPlayers = null;
	private HashMap<Player, Player> _noJumpPlayers = null;
	private AllJumpPads _allJumpPads = null;

	private Jumper() {
		_allJumpPads = AllJumpPads.get();
	}

	static Jumper get()
	{
		if (singleton == null) {
			singleton = new Jumper();
		}
		return singleton;
	}

	void enable(){
		_informedPlayers = new HashMap<Player, Player>();
		_noJumpPlayers = new HashMap<Player, Player>();
		_inAirPlayers = new HashMap<Player, JumpPadInfo>();
	}

	void disable() {
	}

	void maybeJumpUp(Player player, Location location) {
		JumpPadInfo info = _allJumpPads.getByLocation(location);
		if (info == null) {
			mustReadRules(player, true);
			playerCanJump(player, true);
			return;
		}
		if (!hasReadRules(player)) {
			maybeTellPlayerToReadTheRules(player);
			return;
		}
		if (canPlayerJump(player)) return;

		jumpUp(player, info);
	}

	private void jumpUp(Player player, JumpPadInfo info) {
		Vector upwards = new Vector(0.0, info.getVelocity().getY(), 0.0);
		player.setVelocity(upwards);
		_inAirPlayers.put(player, info);
	}

	boolean maybeShootForward(Player player, Location from, Location to) {
		if (!_inAirPlayers.containsKey(player)) return false;
		if (to.getY() >= from.getY()) return false;
		shootForward(player);
		return true;
	}

	private void shootForward(Player player) {
		JumpPadInfo info = _inAirPlayers.get(player);
		_inAirPlayers.remove(player);
		Vector velocity = new Vector(info.getVelocity().getX(), player.getVelocity().getY(), info.getVelocity().getZ());
		player.setVelocity(velocity);
	}

	private void maybeTellPlayerToReadTheRules(Player player) {
		if (shouldReadRules(player)) {
			player.sendMessage("Please read the global rules (/rules) to get access to the jump pads.");
			mustReadRules(player, true);
		}
	}

	private boolean canPlayerJump(Player player) {
		return _noJumpPlayers.containsKey(player);
	}

	void playerCanJump(Player player, boolean canJump) {
		if (canJump){
			if (!canPlayerJump(player)) {
				_noJumpPlayers.put(player, player);
			}
		} else {
			if (canPlayerJump(player)) {
				_noJumpPlayers.remove(player);
			}
		}
	}

	private boolean shouldReadRules(Player player) {
		return !_informedPlayers.containsKey(player);
	}

	private void mustReadRules(Player player, boolean mustReadRules) {
		if (mustReadRules) {
			if (!shouldReadRules(player)) {
				_informedPlayers.put(player, player);
			}
		} else {
			if (shouldReadRules(player)) {
				_informedPlayers.remove(player);
			}
		}
	}

	private boolean hasReadRules(Player player) {
		return player.hasPermission("jumppad.jump");
	}
}
