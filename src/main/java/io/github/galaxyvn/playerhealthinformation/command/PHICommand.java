package io.github.galaxyvn.playerhealthinformation.command;

import io.github.galaxyvn.playerhealthinformation.PlayerHealthInformation;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

public class PHICommand implements CommandExecutor {

    private static HashMap<UUID, BossBar> activeBossbar = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return true;
        if (!(sender instanceof Player)) {
            sender.sendMessage("Not player");
            return true;
        }

        if (args[0].equalsIgnoreCase("show")) {
            if (!sender.hasPermission("playerhealtinformation.command.show")) {
                sender.sendMessage("No permission");
                return true;
            }

            final Player target = Bukkit.getPlayer(args[1]);
            if (target == null) return true;

            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            final Audience player = PlayerHealthInformation.get().getAudience().player((Player) sender);
            final FileConfiguration config = PlayerHealthInformation.get().getConfig();
            Component name = LegacyComponentSerializer.legacyAmpersand().deserialize(
                    config.getString("boss-bar")
                            .replace("<player>", target.getName())
                            .replace("<current>", decimalFormat.format(target.getHealth()))
                            .replace("<max>", String.valueOf(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()))
            );
            float health = (float) (target.getHealth() / target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

            final BossBar healthBar = BossBar.bossBar(
                    name,
                    health,
                    BossBar.Color.valueOf(config.getString("boss-bar-color").toUpperCase()),
                    BossBar.Overlay.valueOf(config.getString("boss-bar-overlay").toUpperCase())
            );

            player.showBossBar(healthBar);
            activeBossbar.put(target.getUniqueId(), healthBar);

            new BukkitRunnable() {
                @Override
                public void run() {
                    healthBar.name(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            config.getString("boss-bar")
                                    .replace("<player>", target.getName())
                                    .replace("<current>", decimalFormat.format(target.getHealth()))
                                    .replace("<max>", String.valueOf(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()))
                    ));
                    healthBar.progress((float) (target.getHealth() / target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));

                    if (target.getHealth() <= 0.0) {
                        cancel();
                        player.hideBossBar(healthBar);
                    } else if (!target.isOnline()) {
                        cancel();
                        player.hideBossBar(healthBar);
                    }
                }
            }.runTaskTimerAsynchronously(PlayerHealthInformation.get(), 0, 20L);
            return true;
        }

        if (args[0].equalsIgnoreCase("hide")) {
            if (!sender.hasPermission("playerhealtinformation.command.hide")) {
                sender.sendMessage("No permission");
                return true;
            }

            final Player target = Bukkit.getPlayer(args[1]);
            if (target == null) return true;

            final Audience player = PlayerHealthInformation.get().getAudience().player((Player) sender);
            if (activeBossbar.containsKey(target.getUniqueId())) {
                player.hideBossBar(activeBossbar.get(target.getUniqueId()));
                return true;
            } else player.sendMessage(Component.text("Player not exist in active bar"));
        }

        return true;
    }
}