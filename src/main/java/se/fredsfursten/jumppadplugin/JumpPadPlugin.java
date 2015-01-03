package se.fredsfursten.jumppadplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import se.fredsfursten.jumppadplugin.Jumper;

public final class JumpPadPlugin extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);		
		Jumper.get().enable();
		Commands.get().enable(this);
	}

	@Override
	public void onDisable() {
		Jumper.get().disable();
		Commands.get().disable();
	}

	@EventHandler
	public void maybeJump(PlayerMoveEvent event) {
    	Jumper.get().maybeJump(event.getPlayer(), event.getTo());
	}

	@EventHandler
	public void maybeFreezePlayer(PlayerVelocityEvent event) {
		if (event.isCancelled()) return;
		
		Vector eventVelocity = event.getVelocity();
		Jumper jumper = Jumper.get();
		Vector jumpPadVelocity = jumper.getPlayerJumpPadVelocity(event.getPlayer(), eventVelocity.getY());
		if (jumpPadVelocity == null) return;

		// Don't let the user change the X and Z velocity.
		eventVelocity.setX(jumpPadVelocity.getX());
		eventVelocity.setZ(jumpPadVelocity.getZ());
		event.setVelocity(eventVelocity);
	}

	@EventHandler
	public void listenToCommands(PlayerCommandPreprocessEvent event) {
		Commands.get().listenToCommands(event.getPlayer(), event.getMessage());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player!");
			return false;
		}
		if (args.length < 1) {
			sender.sendMessage("Incomplete command...");
			return false;
		}

		Player player = (Player) sender;

		String command = args[0].toLowerCase();
		if (command.equals("add")) {
			Commands.get().addCommand(player, args);
		} else if (command.equals("remove")) {
			Commands.get().removeCommand(player, args);
		} else if (command.equals("edit")) {
			Commands.get().editCommand(player, args);
		} else if (command.equals("list")) {
			Commands.get().listCommand(player);
		} else if (command.equals("goto")) {
			Commands.get().gotoCommand(player, args);
		} else {
			sender.sendMessage("Could not understand command.");
			return false;
		}
		return true;
	}
}
