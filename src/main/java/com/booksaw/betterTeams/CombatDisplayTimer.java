package com.booksaw.betterTeams;

import com.booksaw.betterTeams.events.CommandBlocker;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatDisplayTimer {

    private final Main plugin;
    private final CommandBlocker commandBlocker;

    public CombatDisplayTimer(Main plugin, CommandBlocker commandBLocker) {
        this.plugin = plugin;
        this.commandBlocker = commandBLocker;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());

                    if (commandBlocker.isInCombat(playerData)) {
                        long remainingTime = commandBlocker.getCombatTimeRemaining(playerData);
                        int secondsLeft = (int) (remainingTime / 1000);

                        // send action bar text to player with time left
                        String displayTimeLeft = ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString("displayTimerMessage",
                                        "&cYou are in combat! {secondsLeft} seconds left"));

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent(displayTimeLeft.replace("{secondsLeft}", String.valueOf(secondsLeft))));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // refresh every 20 ticks
    }
}
