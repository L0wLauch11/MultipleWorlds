package me.lowlauch.multipleworlds;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin
{
    private static Main instance;
    private final boolean protocolSupportInstalled = getServer().getPluginManager().getPlugin("ProtocolSupport") != null;

    public static Main getInstance()
    {
        return instance;
    };

    public boolean getProtocolSupportInstalled()
    {
        return protocolSupportInstalled;
    }

    public void onEnable()
    {
        instance = this;

        getLogger().info("Plugin enabled");

        Commands commands = new Commands();
        Objects.requireNonNull(this.getCommand("mworld")).setExecutor(commands);
        Objects.requireNonNull(this.getCommand("switch")).setExecutor(commands);

        getServer().getPluginManager().registerEvents(new EventListener(), this);

        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
    }

    public void onDisable()
    {
        getLogger().info("Plugin disabled!");
    }
}
