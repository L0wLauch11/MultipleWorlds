package me.lowlauch.multipleworlds;

import net.minecraft.server.v1_16_R3.PacketListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.util.Objects;

public class EventListener implements Listener
{
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e)
    {
        Player p = e.getPlayer();
        p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4); // Set attack speed value to default

        // Mismatching client to server versions
        int playerVersion = Protocol.getClientProtocolVersion(e.getPlayer());
        if(Main.getInstance().getProtocolSupportInstalled() && playerVersion != Protocol.getServerProtocolVersion())
        {
            // Check if player version is <1.9
            if(playerVersion < 107)
                p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16); // 1.8 like pvp cooldown

            // Check if player is allowed to be in current world
            Location fallback = Main.getInstance().getConfig().getLocation("mw.fallback");
            String currentWorldName = Objects.requireNonNull(e.getPlayer().getLocation().getWorld()).getName();

            String allowedVersions = Main.getInstance().getConfig().getString("mw." + currentWorldName + ".allowedversions");
            assert allowedVersions != null;
            String[] versions = allowedVersions.split(" ");
            boolean allow = false;
            for(String version : versions)
                if(version.equalsIgnoreCase(Integer.toString(playerVersion)))
                    return;

            // Send player message if no fallback world is provided
            if(fallback == null)
            {
                p.kickPlayer("No fallback world is specified. Contact an admin.");
                return;
            }

            // Teleport the player to fallback location if recent world wasn't allowed and save recent world data
            Commands.savePlayerData(p);

            String loadPath = p.getUniqueId().toString() + "." + fallback.getWorld().getName();
            p.teleport(fallback);

            // Load data of fallback world
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
                p.setLevel(destLevel);
                p.setExp((float) destXp);

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
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                p.getInventory().setItemInOffHand(Main.getInstance().getConfig().getItemStack(loadPath + ".offhand"));
                p.updateInventory();
            }

            // Finally, send a message to the player
            e.getPlayer().sendMessage(ChatColor.RED + "You were sent to a fallback world because of your minecraft version");
        }
    }
}
