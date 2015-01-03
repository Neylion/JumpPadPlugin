package se.fredsfursten.jumppadplugin;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class Jumper {
	private static Jumper singleton = null;

	private HashMap<UUID, UUID> playersThatHasBeenInformedToReadTheRules = null;
	private HashMap<UUID, JumpPadInfo> playersInJump = null;
	private HashMap<UUID, UUID> playersWithTemporaryJumpPause = null;
	private AllJumpPads allJumpPads = null;

	private Jumper() {
		this.allJumpPads = AllJumpPads.get();
	}

	static Jumper get()
	{
		if (singleton == null) {
			singleton = new Jumper();
		}
		return singleton;
	}

	void enable(){
		this.playersThatHasBeenInformedToReadTheRules = new HashMap<UUID, UUID>();
		this.playersWithTemporaryJumpPause = new HashMap<UUID, UUID>();
		this.playersInJump = new HashMap<UUID, JumpPadInfo>();
	}

	void disable() {
	}

	void maybeJump(Player player, Location location) {
		JumpPadInfo info = this.allJumpPads.getByLocation(location);
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

		jump(player, info);
	}

	private void jump(Player player, JumpPadInfo info) {
		this.playersInJump.put(player.getUniqueId(), info);
		Vector jumpPadVelocity = info.getVelocity();
		Vector velocity = new Vector(jumpPadVelocity.getX(), jumpPadVelocity.getY(), jumpPadVelocity.getZ());
		player.setVelocity(velocity);
	}

	boolean isGoingDown(Location from, Location to) {
		return to.getY() < from.getY();
	}

	private void maybeTellPlayerToReadTheRules(Player player) {
		if (shouldReadRules(player)) {
			player.sendMessage("Please read the global rules (/rules) to get access to the jump pads.");
			mustReadRules(player, true);
		}
	}

	void playerCanJump(Player player, boolean canJump) {
		if (canJump){
			if (hasTemporaryJumpPause(player)) {
				this.playersWithTemporaryJumpPause.remove(player.getUniqueId());
			}
		} else {
			if (!hasTemporaryJumpPause(player)) {
				this.playersWithTemporaryJumpPause.put(player.getUniqueId(), player.getUniqueId());
			}
		}
	}

	private void mustReadRules(Player player, boolean mustReadRules) {
		if (mustReadRules) {
			if (!shouldReadRules(player)) {
				this.playersThatHasBeenInformedToReadTheRules.put(player.getUniqueId(), player.getUniqueId());
			}
		} else {
			if (shouldReadRules(player)) {
				this.playersThatHasBeenInformedToReadTheRules.remove(player.getUniqueId());
			}
		}
	}

	private boolean shouldReadRules(Player player) {
		return !this.playersThatHasBeenInformedToReadTheRules.containsKey(player.getUniqueId());
	}

	private boolean hasTemporaryJumpPause(Player player) {
		return this.playersWithTemporaryJumpPause.containsKey(player.getUniqueId());
	}

	private boolean hasReadRules(Player player) {
		return player.hasPermission("jumppad.jump");
	}

	boolean isInAir(Player player) {
		return this.playersInJump.containsKey(player.getUniqueId());
	}

	public Vector getPlayerJumpPadVelocity(Player player, double yVelocity) {
		JumpPadInfo info = this.playersInJump.get(player.getUniqueId());
		if (info == null) return null;
		if (yVelocity == 0.0) {
			this.playersInJump.remove(player.getUniqueId());
			return null;
		}
		return info.getVelocity();
	}
}
