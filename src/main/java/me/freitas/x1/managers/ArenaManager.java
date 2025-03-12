package me.freitas.x1.managers;

import me.freitas.x1.PrimeLeagueX1;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import me.freitas.x1.utils.MessageUtils;

import java.io.File;
import java.io.IOException;

public class ArenaManager {
    private static Location pos1;
    private static Location pos2;
    private static Location camarote;

    private static final File configFile = new File("plugins/PrimeLeagueX1/config.yml");
    private static FileConfiguration config;

    static {
        boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            if (debug) {
                Bukkit.getLogger().severe(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.config_nao_encontrado")));
            }
        }
    }

    public static void setPos1(Location loc) {
        if (loc == null) return;
        pos1 = loc;
        config.set("arena.pos1", locToString(loc));
        saveConfig();
        boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
        if (debug) {
            Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena_set_pos1").replace("{location}", locToString(loc))));
        }
    }

    public static void setPos2(Location loc) {
        if (loc == null) return;
        pos2 = loc;
        config.set("arena.pos2", locToString(loc));
        saveConfig();
        boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
        if (debug) {
            Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena_set_pos2").replace("{location}", locToString(loc))));
        }
    }

    public static void setCamarote(Location loc) {
        if (loc == null) return;
        camarote = loc;
        config.set("arena.camarote", locToString(loc));
        saveConfig();
        boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
        if (debug) {
            Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena_set_camarote").replace("{location}", locToString(loc))));
        }
    }

    public static Location getPos1() {
        return pos1;
    }

    public static Location getPos2() {
        return pos2;
    }

    public static Location getCamarote() {
        return camarote;
    }

    public static boolean arenaDefinida() {
        return pos1 != null && pos2 != null;
    }

    private static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
            if (debug) {
                Bukkit.getLogger().severe(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.erro_salvar")));
            }
            e.printStackTrace();
        }
    }

    public static void carregarArena() {
        if (config == null) {
            boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
            if (debug) {
                Bukkit.getLogger().severe(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.erro_carregar_config")));
            }
            return;
        }

        if (config.contains("arena.pos1")) {
            pos1 = stringToLoc(config.getString("arena.pos1"));
            if (pos1 != null) {
                boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
                if (debug) {
                    Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.loaded_pos1").replace("{location}", locToString(pos1))));
                }
            } else {
                boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
                if (debug) {
                    Bukkit.getLogger().severe(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.erro_pos1")));
                }
            }
        }
        if (config.contains("arena.pos2")) {
            pos2 = stringToLoc(config.getString("arena.pos2"));
            if (pos2 != null) {
                boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
                if (debug) {
                    Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.loaded_pos2").replace("{location}", locToString(pos2))));
                }
            } else {
                boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
                if (debug) {
                    Bukkit.getLogger().severe(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.erro_pos2")));
                }
            }
        }
        if (config.contains("arena.camarote")) {
            camarote = stringToLoc(config.getString("arena.camarote"));
            if (camarote != null) {
                boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
                if (debug) {
                    Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.loaded_camarote").replace("{location}", locToString(camarote))));
                }
            } else {
                boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
                if (debug) {
                    Bukkit.getLogger().severe(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.erro_camarote")));
                }
            }
        }
    }

    private static String locToString(Location loc) {
        if (loc == null || loc.getWorld() == null) return "Desconhecido";
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    private static Location stringToLoc(String str) {
        if (str == null) return null;
        String[] coords = str.split(",");
        if (coords.length < 4) return null;

        World world = Bukkit.getWorld(coords[0]);
        if (world == null) {
            boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
            if (debug) {
                Bukkit.getLogger().severe(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.world_not_found").replace("{world}", coords[0])));
            }
            return null;
        }

        try {
            double x = Double.parseDouble(coords[1]);
            double y = Double.parseDouble(coords[2]);
            double z = Double.parseDouble(coords[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            boolean debug = PrimeLeagueX1.getInstance().getConfig().getBoolean("debug");
            if (debug) {
                Bukkit.getLogger().severe(ChatColor.translateAlternateColorCodes('&', MessageUtils.getMensagem("arena.erro_coordenadas")));
            }
            return null;
        }
    }
}