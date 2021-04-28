package me.lowlauch.multipleworlds;

import net.minecraft.server.v1_16_R3.SharedConstants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import protocolsupport.api.ProtocolSupportAPI;

public class Protocol
{
    public static Integer getClientProtocolVersion(Player player)
    {
        if(Main.getInstance().getProtocolSupportInstalled())
            return ProtocolSupportAPI.getProtocolVersion(player).getId();
        else
        {
            Bukkit.getLogger().info("ProtocolSupport is not installed! Cannot get player client version.\nPlease install ProtocolSupport!");
            return -1;
        }
    }

    public static Integer getServerProtocolVersion()
    {
        return SharedConstants.getGameVersion().getProtocolVersion();
    }
}
