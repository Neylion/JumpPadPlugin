package se.fredsfursten.jumppadplugin;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class Jumper implements Listener {
	private static Jumper singleton = null;

	private HashMap<Player, Player> _playersThatHasBeenInformedToReadTheRules = null;
	private HashMap<Player, JumpPadInfo> _playersInJumpUp = null;
	private HashMap<Player, Player> _playersWithTemporaryJumpPause = null;
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
		_playersThatHasBeenInformedToReadTheRules = new HashMap<Player, Player>();
		_playersWithTemporaryJumpPause = new HashMap<Player, Player>();
		_playersInJumpUp = new HashMap<Player, JumpPadInfo>();
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
		if (hasTemporaryJumpPause(player)) return;

		jumpUp(player, info);
	}

	private void jumpUp(Player player, JumpPadInfo info) {
		Vector upwards = new Vector(0.0, info.getVelocity().getY(), 0.0);
		player.setVelocity(upwards);
		_playersInJumpUp.put(player, info);
	}

	boolean maybeShootForward(Player player, Location from, Location to) {
		if (!isInAir(player)) return false;
		if (!isGoingDown(from, to)) return false;
		shootForward(player);
		return true;
	}

	boolean isGoingDown(Location from, Location to) {
		return to.getY() < from.getY();
	}

	private void shootForward(Player player) {
		JumpPadInfo info = _playersInJumpUp.get(player);
		_playersInJumpUp.remove(player);
		Vector velocity = new Vector(info.getVelocity().getX(), player.getVelocity().getY(), info.getVelocity().getZ());
		player.setVelocity(velocity);
	}

	private void maybeTellPlayerToReadTheRules(Player player) {
		if (shouldReadRules(player)) {
			player.sendMessage("Please read the global rules (/rules) to get access to the jump pads.");
			mustReadRules(player, true);
		}
	}

	void playerCanJump(Player player, boolean canJump) {
		if (canJump){
			if (!hasTemporaryJumpPause(player)) {
				_playersWithTemporaryJumpPause.put(player, player);
			}
		} else {
			if (hasTemporaryJumpPause(player)) {
				_playersWithTemporaryJumpPause.remove(player);
			}
		}
	}

	private void mustReadRules(Player player, boolean mustReadRules) {
		if (mustReadRules) {
			if (!shouldReadRules(player)) {
				_playersThatHasBeenInformedToReadTheRules.put(player, player);
			}
		} else {
			if (shouldReadRules(player)) {
				_playersThatHasBeenInformedToReadTheRules.remove(player);
			}
		}
	}

	private boolean shouldReadRules(Player player) {
		return !_playersThatHasBeenInformedToReadTheRules.containsKey(player);
	}

	private boolean hasTemporaryJumpPause(Player player) {
		return _playersWithTemporaryJumpPause.containsKey(player);
	}

	private boolean hasReadRules(Player player) {
		return player.hasPermission("jumppad.jump");
	}

	boolean isInAir(Player player) {
		return _playersInJumpUp.containsKey(player);
	}
}
