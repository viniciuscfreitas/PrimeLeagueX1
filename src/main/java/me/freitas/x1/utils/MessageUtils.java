package me.freitas.x1.utils;

import me.freitas.x1.PrimeLeagueX1;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MessageUtils {

    private static FileConfiguration messages;
    private static File messagesFile;

    public static void initialize(JavaPlugin plugin) {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static String getMessage(String key) {
        if (messages.contains(key)) {
            return ChatColor.translateAlternateColorCodes('&', messages.getString(key));
        } else {
            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) {
                Bukkit.getLogger().severe(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.config_nao_encontrado")));
            }
            return "§cMensagem não encontrada: " + key;
        }
    }

    public static void reloadMessages(JavaPlugin plugin) {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        // 📌 Garantir que o arquivo existe antes de carregar
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        // 🔥 Força a recarga do arquivo
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        plugin.getLogger().info("Messages.yml recarregado com sucesso!");
    }

    public static void debugMessages() {
        if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) {
            Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.config_nao_encontrado")));
        }
        for (String key : messages.getKeys(true)) {
            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) {
                Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.config_nao_encontrado")) + " - " + key);
            }
        }
    }

    public static String getMensagem(String chave) {
        return PrimeLeagueX1.getInstance().getConfig().getString(chave);
    }
}
