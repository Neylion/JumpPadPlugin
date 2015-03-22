package se.fredsfursten.plugintools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Misc {
	public static Block getFirstBlockOfMaterial(Material material, Location location, int maxDistance) {
		int x1 = location.getBlockX(); 
		int y1 = location.getBlockY();
		int z1 = location.getBlockZ();

		World world = location.getWorld();

		for (int distance = 0; distance < maxDistance; distance++) {
			for (int xPoint = x1-distance; xPoint <= x1+distance; xPoint++) {
				int currentDistance = Math.abs(xPoint-x1);
				boolean okDistanceX = (currentDistance == distance);
				for (int yPoint = y1-distance; yPoint <= y1+distance; yPoint++) {
					currentDistance = Math.abs(yPoint-y1);
					boolean okDistanceY = (currentDistance == distance);
					for (int zPoint = z1-distance; zPoint <= z1+distance; zPoint++) {
						currentDistance = Math.abs(zPoint-z1);
						boolean okDistanceZ = (currentDistance == distance);
						if (!okDistanceX && !okDistanceY && !okDistanceZ) continue;
						Block currentBlock = world.getBlockAt(xPoint, yPoint, zPoint);
						if (currentBlock.getType() == material) return currentBlock;
					}
				}
			}
		}

		return null;
	}
	
	public static void executeCommand(String command)
	{
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
	}
}
