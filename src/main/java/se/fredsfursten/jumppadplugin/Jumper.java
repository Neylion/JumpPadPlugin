package se.fredsfursten.jumppadplugin;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class Jumper {
	private static Jumper singleton = null;

	private HashMap<UUID, UUID> playersThatHasBeenInformedToReadTheRules = null;
	private HashMap<UUID, JumpPadInfo> playersAboutToJump = null;
	private HashMap<UUID, UUID> playersWithTemporaryJumpPause = null;
	private AllJumpPads allJumpPads = null;
	private JavaPlugin plugin = null;

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

	void enable(JavaPlugin plugin){
		this.plugin = plugin;
		this.playersThatHasBeenInformedToReadTheRules = new HashMap<UUID, UUID>();
		this.playersWithTemporaryJumpPause = new HashMap<UUID, UUID>();
		this.playersAboutToJump = new HashMap<UUID, JumpPadInfo>();
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
		if (isAboutToJump(player)) return;
		
		float oldWalkSpeed = stopPlayer(player);
		jumpSoon(player, info, oldWalkSpeed);
	}

	boolean isAboutToJump(Player player) {
		return this.playersAboutToJump.containsKey(player.getUniqueId());
	}

	void setPlayerIsAboutToJump(Player player, JumpPadInfo info, boolean isAboutToJump) {
		if (isAboutToJump) {
			if (isAboutToJump(player)) return;
			this.playersAboutToJump.put(player.getUniqueId(), info);
		} else {
			if (!isAboutToJump(player)) return;
			this.playersAboutToJump.remove(player.getUniqueId());
		}
	}

	private void jumpSoon(Player player, JumpPadInfo info, float oldWalkSpeed) {
		setPlayerIsAboutToJump(player, info, true);
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				if (!isAboutToJump(player)) return;
				setPlayerIsAboutToJump(player, info, false);
				player.setWalkSpeed(oldWalkSpeed);
				jump(player, info);
			}
		}, 20L);
	}

	private float stopPlayer(Player player) {
		player.setVelocity(new Vector(0.0, 0.0, 0.0));
		float walkSpeed = player.getWalkSpeed();
		player.setWalkSpeed(0.0F);
		return walkSpeed;
	}

	void jump(Player player, JumpPadInfo info) {
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
}
