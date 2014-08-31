package se.fredsfursten.jumppadplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import se.fredsfursten.jumppadplugin.Jumper;

public final class JumpPadPlugin extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		getLogger().info("onEnable has been invoked!");
		getServer().getPluginManager().registerEvents(this, this);		
		Jumper.get().enable(this);
	}

	@Override
	public void onDisable() {
		getLogger().info("onDisable has been invoked!");
	}

	@EventHandler
	public void maybeJump(PlayerMoveEvent e) {
		Jumper.get().maybeJump(e.getPlayer(), e.getTo());
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
			return Jumper.get().add(player, args);
		} else if (command.equals("remove")) {
			return Jumper.get().remove(player);
		} else {
			sender.sendMessage("Could not understand command.");
			return false;
		}
	}
}
