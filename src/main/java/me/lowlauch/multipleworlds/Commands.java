package me.lowlauch.multipleworlds;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class Commands implements CommandExecutor
{
    public void savePlayerData(Player p)
    {
        // Save the previous data world first
        String uuid = p.getUniqueId().toString();
        String savePath = uuid + "." + p.getWorld().getName();


        // Create variables for everything we want to save
        String inventoryString = StringInventory.itemStackArrayToBase64(p.getInventory().getContents());
        String armorString = StringInventory.itemStackArrayToBase64(p.getInventory().getArmorContents());
        String enderChestString = StringInventory.itemStackArrayToBase64(p.getEnderChest().getContents());
        ItemStack rightHandItem = p.getInventory().getItemInOffHand();

        double xp = p.getExp();
        int level = p.getLevel();
        double health = p.getHealth();
        double sat = p.getSaturation();
        double food = p.getFoodLevel();

        Location loc = p.getLocation();

        Location bedspawn = p.getBedSpawnLocation();

        // Save the data
        Main.getInstance().getConfig().set(savePath + ".inventory", inventoryString);
        Main.getInstance().getConfig().set(savePath + ".armor", armorString);
        Main.getInstance().getConfig().set(savePath + ".offhand", rightHandItem);
        Main.getInstance().getConfig().set(savePath + ".enderchest", enderChestString);

        Main.getInstance().getConfig().set(savePath + ".xp", xp);
        Main.getInstance().getConfig().set(savePath + ".level", level);
        Main.getInstance().getConfig().set(savePath + ".health", health);
        Main.getInstance().getConfig().set(savePath + ".sat", sat);
        Main.getInstance().getConfig().set(savePath + ".food", food);

        Main.getInstance().getConfig().set(savePath + ".location", loc);
        Main.getInstance().getConfig().set(savePath + ".bedspawn", bedspawn);

        Main.getInstance().saveConfig();
        Main.getInstance().reloadConfig();
    }


    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args)
    {
        // Admin command
        if(commandLabel.equalsIgnoreCase("mw") && commandSender.isOp())
        {
            // Automates creating a multiple new worlds
            if(args[0].equalsIgnoreCase("create"))
            {
                if(args.length == 2)
                {
                    Bukkit.getServer().dispatchCommand(commandSender, "mv create " + args[1]);
                    Bukkit.getServer().dispatchCommand(commandSender, "mv create " + args[1] + "_nether");
                    Bukkit.getServer().dispatchCommand(commandSender, "mv create " + args[1] + "_the_end");

                    // Workaround for multiverse end portals not working
                    Bukkit.getServer().dispatchCommand(commandSender, "mv modify set respawnWorld " + args[1] + " " + args[1] + "_the_end");

                } else
                    commandSender.sendMessage(ChatColor.RED + "You have to put in a world name!");

                return true;
            }
        }

        // Command to switch worlds
        if(commandLabel.equalsIgnoreCase("switch"))
        {
            Player p = (Player) commandSender;

            // Args have to be at least once
            if(p.hasPermission("multipleworlds.switch") && args.length >= 1
                    && !p.getWorld().getName().endsWith("_nether")
                    && !p.getWorld().getName().endsWith("_end"))
            {
                savePlayerData(p);

                // Switch world
                // Load the data
                String uuid = p.getUniqueId().toString();
                String loadPath = uuid + "." + args[0];
                String destInventoryString = Main.getInstance().getConfig().getString(loadPath + ".inventory");

                if(destInventoryString != null) {
                    String destArmorString = Main.getInstance().getConfig().getString(loadPath + ".armor");
                    String destEnderChestString = Main.getInstance().getConfig().getString(loadPath + ".enderchest");
                    double destXp = Main.getInstance().getConfig().getDouble(loadPath + ".xp");
                    int destLevel = Main.getInstance().getConfig().getInt(loadPath + ".level");
                    double destHealth = Main.getInstance().getConfig().getDouble(loadPath + ".health");
                    double destSaturation = Main.getInstance().getConfig().getDouble(loadPath + ".sat");
                    double destFood = Main.getInstance().getConfig().getDouble(loadPath + ".food");

                    Location destLoc = Main.getInstance().getConfig().getLocation(loadPath + ".location");
                    Location destSpawnworldloc = Main.getInstance().getConfig().getLocation(loadPath + ".bedspawn");
                    Location fallbackLoc = new Location(Bukkit.getServer().getWorld(args[0]), 0, 60, 0);

                    if(destLoc != null)
                    {
                        p.teleport(destLoc);
                    }
                    else
                    {
                        p.sendMessage(ChatColor.RED + "Your last coordinates could not be found. You will be teleported to 0, 0");
                        p.teleport(fallbackLoc);
                    }

                    if(destSpawnworldloc != null)
                        p.setBedSpawnLocation(destSpawnworldloc, true);
                    else
                        p.setBedSpawnLocation(fallbackLoc, true);

                    p.setLevel(destLevel);
                    p.setExp((float) destXp);

                    // Prevent the player from dying instantly
                    if(destHealth != 0)
                    {
                        p.setHealth(destHealth);
                        p.setSaturation((float) destSaturation);
                        p.setFoodLevel((int) destFood);
                    }

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
                    Location destLocation = new Location(Bukkit.getServer().getWorld(args[0]), 0, 60, 0);
                    p.teleport(destLocation);
                    p.setBedSpawnLocation(destLocation, true);
                    p.setLevel(0);
                    p.setExp(0f);

                    p.getInventory().clear();
                    p.updateInventory();
                    savePlayerData(p);
                }

                commandSender.sendMessage("§7Welcome to §6" + args[0] + "§7!");
                return true;
            }
        }

        return false;
    }
}
