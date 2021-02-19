package me.lowlauch.multipleworlds;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin
{
    private static Main instance;

    public static Main getInstance()
    {
        return instance;
    }

    public void onEnable()
    {
        instance = this;

        getLogger().info("Plugin wurde aktiviert");

        Commands commands = new Commands();
        Objects.requireNonNull(this.getCommand("mw")).setExecutor(commands);
        Objects.requireNonNull(this.getCommand("switch")).setExecutor(commands);

        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
    }

    public void onDisable()
    {
        getLogger().info("Plugin wurde deaktiviert!");
    }
}
