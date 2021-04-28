package me.lowlauch.multipleworlds;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Objects;

public class Commands implements CommandExecutor
{
    private void savePlayerData(Player p)
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
        boolean permission = commandSender.isOp() || commandSender.hasPermission("multipleworlds.admin");
        if(commandLabel.equalsIgnoreCase("mworld") && permission)
        {
            if(args.length >= 1)
            {
                switch(args[0].toLowerCase())
                {
                    case "create": // Creates a new dimension
                        if(args.length == 2)
                        {
                            // Create overworld, nether and the end
                            Bukkit.getServer().dispatchCommand(commandSender, "mv create " + args[1] + " normal");
                            Bukkit.getServer().dispatchCommand(commandSender, "mv create " + args[1] + "_nether nether");
                            Bukkit.getServer().dispatchCommand(commandSender, "mv create " + args[1] + "_the_end end");

                            // Workaround for multiverse end portals not working
                            Bukkit.getServer().dispatchCommand(commandSender, "mv modify set respawnWorld " + args[1] + " " + args[1] + "_the_end");

                            // Set right respawnworld for nether
                            Bukkit.getServer().dispatchCommand(commandSender, "mvm set respawnworld " + args[1] + " " + args[1] + "_nether");

                            // Allow this dimension to be switchable
                            Main.getInstance().getConfig().set("mw." + args[1] + ".switchable", true);

                            // Only allow people with permission to switch to this world
                            Main.getInstance().getConfig().set("mw." + args[1] + ".everyone", false);

                            // Allow only server version per default
                            Main.getInstance().getConfig().set("mw." + args[1] + ".allowedversions", "754");

                            Main.getInstance().getConfig().set("mw." + args[1] + ".allowedplayers", "a");

                            Main.getInstance().saveConfig();

                        } else
                            commandSender.sendMessage(ChatColor.RED + "You have to put in a world name!");

                        return true;

                    case "allowprotocol": // Allows a specific minecraft version for a world
                        if(args.length == 4)
                        {
                            String world = args[1];

                            if(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false"))
                            {
                                String allowedVersions = Main.getInstance().getConfig().getString("mw." + world + ".allowedversions");

                                // Allow or disallow
                                boolean allow = Boolean.parseBoolean(args[2]);
                                if(allow)
                                {
                                    Main.getInstance().getConfig().set("mw." + world + ".allowedversions", allowedVersions + " " + args[3]);
                                } else
                                {
                                    assert allowedVersions != null;
                                    Main.getInstance().getConfig().set("mw." + world + ".allowedversions", allowedVersions.replaceAll(args[3], ""));
                                }
                                Main.getInstance().saveConfig();

                                String s = args[2].replaceAll("true", "§aallowed ");
                                s = s.replaceAll("false", "§cdisallowed ");

                                commandSender.sendMessage(ChatColor.GREEN + "Sucessfully " + s + " Protocol Version " + args[3]);
                            }
                        } else
                            commandSender.sendMessage(ChatColor.RED + "Provide a world, \"true\" or \"false\" and a protocol version");

                        return true;

                    case "everyone": // Allows everyone or disallows everyone to switch to a specific world
                        if(args.length == 3)
                        {
                            String world = args[1];

                            if(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false"))
                            {
                                Main.getInstance().getConfig().set("mw." + world + ".everyone", Boolean.parseBoolean(args[2]));
                                Main.getInstance().saveConfig();
                                commandSender.sendMessage(ChatColor.GREEN + "Sucessfully added value to " + ChatColor.GOLD + args[2]);
                            }
                        } else
                            commandSender.sendMessage(ChatColor.RED + "Provide a world and \"true\" or \"false\"");

                        return true;

                    case "switchable": // Enables or disables switching to a specific world
                        if(args.length == 3)
                        {
                            String world = args[1];

                            if(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false"))
                            {
                                Main.getInstance().getConfig().set("mw." + world + ".switchable", Boolean.parseBoolean(args[1]));
                                Main.getInstance().saveConfig();
                                commandSender.sendMessage(ChatColor.GREEN + "Sucessfully changed value to " + ChatColor.GOLD + args[2]);
                            }
                        } else
                            commandSender.sendMessage(ChatColor.RED + "Provide a world and \"true\" or \"false\"");

                        return true;

                    case "setfallback": // Sets the world a player will be sent to if the version isn't the servers version
                        Player p = (Player) commandSender;
                        Main.getInstance().getConfig().set("mw.fallback", p.getLocation());
                        Main.getInstance().saveConfig();

                        commandSender.sendMessage(ChatColor.GREEN + "Sucessfully set the fallback location to the current location");

                        return true;

                    case "allowplayer": // Allows/Disallows a certain player to switch to a world
                        if(args.length == 4)
                        {
                            String world = args[1];

                            String playerUUID;
                            if(Bukkit.getPlayer(args[3]) == null)
                                playerUUID = Bukkit.getOfflinePlayer(args[3]).getUniqueId().toString();
                            else
                                playerUUID = Objects.requireNonNull(Bukkit.getPlayer(args[3])).getUniqueId().toString();

                            if(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false"))
                            {
                                String allowedPlayers = Main.getInstance().getConfig().getString("mw." + world + ".allowedplayers");

                                // Allow or disallow
                                boolean allow = Boolean.parseBoolean(args[2]);
                                assert allowedPlayers != null;
                                if(allow)
                                {
                                    if(!allowedPlayers.contains(playerUUID))
                                        Main.getInstance().getConfig().set("mw." + world + ".allowedplayers", allowedPlayers + " " + playerUUID);
                                } else
                                {
                                    Main.getInstance().getConfig().set("mw." + world + ".allowedplayers", allowedPlayers.replaceAll(playerUUID, ""));
                                }

                                Main.getInstance().saveConfig();

                                String s = args[2].replaceAll("true", "§aallowed ");
                                s = s.replaceAll("false", "§cdisallowed ");

                                commandSender.sendMessage(ChatColor.GREEN + "Sucessfully " + s + ChatColor.GOLD + playerUUID
                                        + " to switch to world " + ChatColor.GOLD + world);
                            }
                        } else
                            commandSender.sendMessage(ChatColor.RED + "Provide a world, \"true\" or \"false\" and a player name");

                        return true;

                    case "default":
                        return false;

                }
            } else
                return false;
        }

        // Command to switch worlds
        if(commandLabel.equalsIgnoreCase("switch"))
        {
            Player p = (Player) commandSender;

            if(args.length >= 1)
            {
                // Args have to be at least once
                permission = Objects.requireNonNull(Main.getInstance().getConfig().getString("mw." + args[0] + ".allowedplayers")).contains(p.getUniqueId().toString())
                        || Main.getInstance().getConfig().getBoolean("mw." + args[0] + ".everyone");

                // Check if world is even switchable
                if(!Main.getInstance().getConfig().getBoolean("mw." + args[0] + ".switchable"))
                {
                    commandSender.sendMessage(ChatColor.RED + "This world is not switchable!");
                    return true;
                }

                String playerVersion = "latest";

                // This feature is only useful if the server even supports multiple game versions
                if(Main.getInstance().getProtocolSupportInstalled())
                    playerVersion = Integer.toString(Protocol.getClientProtocolVersion(p));

                // Check if player has correct version
                if(!playerVersion.equalsIgnoreCase("latest"))
                {
                    Bukkit.getLogger().info("Player with protocol version " + playerVersion + " tried to switch dimensions.");

                    String allowedVersions = Main.getInstance().getConfig().getString("mw." + args[0] + ".allowedversions");
                    assert allowedVersions != null;
                    String[] versions = allowedVersions.split(" ");
                    boolean allow = false;
                    for(String version : versions)
                    {
                        if(version.equalsIgnoreCase(playerVersion))
                        {
                            allow = true;
                            break;
                        }
                    }

                    if(!allow)
                    {
                        commandSender.sendMessage(ChatColor.RED + "You do not have the correct minecraft version (" + playerVersion + ")");
                        return true;
                    }
                }
                // Switch world
                if(permission && !p.getWorld().getName().endsWith("_nether") && !p.getWorld().getName().endsWith("_end"))
                {
                    savePlayerData(p);

                    // Switch world
                    // Load the data
                    String uuid = p.getUniqueId().toString();
                    String loadPath = uuid + "." + args[0];
                    String destInventoryString = Main.getInstance().getConfig().getString(loadPath + ".inventory");

                    if(destInventoryString != null)
                    {
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
                } else
                {
                    commandSender.sendMessage(ChatColor.RED + "You do not have permission to travel to this world!");
                }
            }
        }

        return false;
    }
}
