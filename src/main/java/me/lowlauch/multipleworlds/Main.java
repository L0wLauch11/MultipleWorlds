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

        Objects.requireNonNull(this.getCommand("mw")).setExecutor(new Commands());
        Objects.requireNonNull(this.getCommand("mworld")).setExecutor(new Commands());
        Objects.requireNonNull(this.getCommand("switch")).setExecutor(new Commands());

        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
    }

    public void onDisable()
    {
        getLogger().info("Plugin wurde deaktiviert!");
    }
}
