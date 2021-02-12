package me.lowlauch.multipleworlds;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class Commands implements CommandExecutor
{

    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args)
    {
        // Command to switch worlds
        if(commandLabel.equalsIgnoreCase("mworld")
                || commandLabel.equalsIgnoreCase("switch")
                || commandLabel.equalsIgnoreCase("mw"))
        {
            Player p = (Player) commandSender;

            // Args have to be at least once
            if(p.hasPermission("multipleworlds.switch") && args.length >= 1)
            {
                // Save the previous data world first
                String uuid = p.getUniqueId().toString();
                String savePath = uuid + "." + p.getWorld().getName();
                String loadPath = uuid + "." + args[0];

                // Create variables for everything we want to save
                String inventoryString = StringInventory.itemStackArrayToBase64(p.getInventory().getContents());
                String armorString = StringInventory.itemStackArrayToBase64(p.getInventory().getArmorContents());
                String enderChestString = StringInventory.itemStackArrayToBase64(p.getEnderChest().getContents());
                ItemStack rightHandItem = p.getInventory().getItemInOffHand();
                double xp = p.getExp();
                int level = p.getLevel();

                Location loc = p.getLocation();

                Location bedspawn = p.getBedSpawnLocation();

                // Save the data
                Main.getInstance().getConfig().set(savePath + ".inventory", inventoryString);
                Main.getInstance().getConfig().set(savePath + ".armor", armorString);
                Main.getInstance().getConfig().set(savePath + ".offhand", rightHandItem);
                Main.getInstance().getConfig().set(savePath + ".enderchest", enderChestString);

                Main.getInstance().getConfig().set(savePath + ".xp", xp);
                Main.getInstance().getConfig().set(savePath + ".level", level);

                Main.getInstance().getConfig().set(savePath + ".location", loc);
                Main.getInstance().getConfig().set(savePath + ".bedspawn", bedspawn);

                Main.getInstance().saveConfig();
                Main.getInstance().reloadConfig();

                // Switch world
                // Load the data
                String destInventoryString = Main.getInstance().getConfig().getString(loadPath + ".inventory");

                if(destInventoryString != null) {
                    String destArmorString = Main.getInstance().getConfig().getString(loadPath + ".armor");
                    String destEnderChestString = Main.getInstance().getConfig().getString(loadPath + ".enderchest");
                    double destXp = Main.getInstance().getConfig().getDouble(loadPath + ".xp");
                    int destLevel = Main.getInstance().getConfig().getInt(loadPath + ".level");

                    Location destLoc = Main.getInstance().getConfig().getLocation(loadPath + ".location");
                    Location destSpawnworldloc = Main.getInstance().getConfig().getLocation(loadPath + ".bedspawn");

                    assert destLoc != null;
                    p.teleport(destLoc);
                    p.setBedSpawnLocation(destSpawnworldloc, true);

                    p.setLevel(destLevel);
                    p.setExp((float) destXp);

                    p.getInventory().clear();

                    try {
                        p.getInventory().setContents(StringInventory.itemStackArrayFromBase64(destInventoryString));
                        p.getInventory().setArmorContents(StringInventory.itemStackArrayFromBase64(destArmorString));
                        p.getEnderChest().setContents(StringInventory.itemStackArrayFromBase64(destEnderChestString));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    p.getInventory().setItemInOffHand(Main.getInstance().getConfig().getItemStack(loadPath + ".offhand"));


                    p.updateInventory();
                } else
                {
                    Location destLocation = new Location(Bukkit.getServer().getWorld(args[0]), 0, 80, 0);
                    p.teleport(destLocation);
                    p.setLevel(0);
                    p.setExp(0f);

                    p.getInventory().clear();
                    Bukkit.getServer().dispatchCommand(commandSender, "switch " + args[0]);
                }

                commandSender.sendMessage("§7Welcome to" + "§6" + args[0] + "§7!");
                return true;
            }
        }

        return false;
    }
}
