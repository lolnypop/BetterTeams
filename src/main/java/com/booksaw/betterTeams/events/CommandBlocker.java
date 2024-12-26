package com.booksaw.betterTeams.events;

import com.booksaw.betterTeams.Main;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class CommandBlocker implements Listener {

    private final long griefPreventionCombatDuration;
    private final Main plugin;
    private final List<String> restrictedCommands;

    public CommandBlocker(Main plugin) {
        // get the combat expire time from griefprevention's API
        this.griefPreventionCombatDuration = GriefPrevention.instance.config_pvp_combatTimeoutSeconds * 1000L;
        // get the list of commands to restrict while in combat from the config.yml
        this.restrictedCommands = plugin.getConfig().getStringList("restrictBetterTeamsCommandsDuringGPCombat");
        // plugin instance
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID playerID = player.getUniqueId();
        String command = event.getMessage().toLowerCase();

        String[] commandParts = command.split(" ");
        String baseCommand = commandParts[0];

        // go through the commands in the config.yml
        for (String restrictedCommand : restrictedCommands) {
            if (baseCommand.startsWith(restrictedCommand.toLowerCase()) || command.equals(restrictedCommand.toLowerCase())) {
                PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(playerID);

                long remainingTime = getCombatTimeRemaining(playerData); // get the remaining time left
                int secondsLeft = (int) (remainingTime / 1000); // convert timings from milliseconds to seconds

                // check for player's combat status
                if (isInCombat(playerData)) {
                    event.setCancelled(true); // restrict the command

                    String restrictedCommandMessage = ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("cantUseCommandInCombatMessage",
                                    "&cYou cannot use {command} while in combat! {timeLeft} seconds left"));

                    player.sendMessage(restrictedCommandMessage.replace("{command}", restrictedCommand).replace("{timeLeft}", String.valueOf(secondsLeft)));
                }
                break; // break statement for the for loop duh
            }
        }
    }

    // method for checking if player is in combat
    public boolean isInCombat(PlayerData playerData) {
        Instant lastCombatTime = Instant.ofEpochMilli(playerData.lastPvpTimestamp);
        Duration combatDuration = Duration.ofMillis(griefPreventionCombatDuration);
        Instant combatEndTime = lastCombatTime.plus(combatDuration);

        return Instant.now().isBefore(combatEndTime); // return true if player is IN combat
    }

    // method for calculating the remaining time left
    public long getCombatTimeRemaining(PlayerData playerData) {
        Instant lastCombatTime = Instant.ofEpochMilli(playerData.lastPvpTimestamp);
        Duration combatDuration = Duration.ofMillis(griefPreventionCombatDuration);
        Instant combatEndTime = lastCombatTime.plus(combatDuration);

        Duration remainingTime = Duration.between(Instant.now(), combatEndTime);
        long millisLeft = Math.max(remainingTime.toMillis(), 0); // remaining time left & making sure there is no negative numbers

        return (millisLeft + 999);
    }
}
