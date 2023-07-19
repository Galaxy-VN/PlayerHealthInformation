package io.github.galaxyvn.playerhealthinformation;

import io.github.galaxyvn.playerhealthinformation.command.PHICommand;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerHealthInformation extends JavaPlugin {

    private static PlayerHealthInformation plugin;
    private BukkitAudiences audience;

    public PlayerHealthInformation() {
        plugin = this;
    }

    public static PlayerHealthInformation get() {
        return plugin;
    }
    @Override
    public void onEnable() {
        audience = BukkitAudiences.create(this);

        saveDefaultConfig();
        reloadConfig();

        // PHICommand
        getCommand("playerhealtinformation").setExecutor(new PHICommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public BukkitAudiences getAudience() {
        return audience;
    }
}
