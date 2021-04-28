package me.lowlauch.multipleworlds;

import net.minecraft.server.v1_16_R3.PacketListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

public class EventListener implements Listener
{
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e)
    {
        // Mismatching client to server versions
        int playerVersion = Protocol.getClientProtocolVersion(e.getPlayer());
        if(Main.getInstance().getProtocolSupportInstalled() && playerVersion != Protocol.getServerProtocolVersion())
        {
            Location fallback = Main.getInstance().getConfig().getLocation("mw.fallback");
            String currentWorldName = Objects.requireNonNull(e.getPlayer().getLocation().getWorld()).getName();

            String allowedVersions = Main.getInstance().getConfig().getString("mw." + currentWorldName + ".allowedversions");
            assert allowedVersions != null;
            String[] versions = allowedVersions.split(" ");
            boolean allow = false;
            for(String version : versions)
            {
                if(version.equalsIgnoreCase(Integer.toString(playerVersion)))
                {
                    allow = true;
                    break;
                }
            }

            if(fallback != null && !allow)
            {
                e.getPlayer().teleport(fallback);
                e.getPlayer().sendMessage(ChatColor.RED + "You were sent to a fallback world because of your minecraft version");
            } else
            {
                e.getPlayer().kickPlayer("No fallback world is specified. Contact an admin.");
            }
        }
    }
}
