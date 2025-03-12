package me.freitas.x1.utils;

import me.freitas.x1.PrimeLeagueX1;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class LoggerUtils {
    private static final boolean DEBUG_MODE = PrimeLeagueX1.getInstance().getConfig().getBoolean("logs.ativar_logs", true);

    public static void logInfo(String message) {
        if (DEBUG_MODE) {
            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) { Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e[PrimeLeagueX1] &f" + message)); }
        }
    }

    public static void logError(String message) {
        if (DEBUG_MODE) {
            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) { Bukkit.getLogger().severe(ChatColor.translateAlternateColorCodes('&', "&c[PrimeLeagueX1] &f" + message)); }
        }
    }

    public static void logDebug(String message) {
        if (DEBUG_MODE) {
            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) { Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', ChatColor.YELLOW + "[DEBUG] " + ChatColor.RESET + message)); }
        }
    }
}