package me.freitas.x1.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ArenaManager {
    private static Location pos1;
    private static Location pos2;
    private static Location camarote;

    private static final File arenaFile = new File("plugins/PrimeLeagueX1/arena.txt");

    /**
     * Define a posição 1 da arena.
     */
    public static void setPos1(Location loc) {
        if (loc == null) return;
        pos1 = loc;
        Bukkit.getLogger().info("[X1] Posição 1 da arena definida: " + locToString(loc));
        salvarArena();
    }

    /**
     * Define a posição 2 da arena.
     */
    public static void setPos2(Location loc) {
        if (loc == null) return;
        pos2 = loc;
        Bukkit.getLogger().info("[X1] Posição 2 da arena definida: " + locToString(loc));
        salvarArena();
    }

    /**
     * Define a localização do camarote.
     */
    public static void setCamarote(Location loc) {
        if (loc == null) return;
        camarote = loc;
        Bukkit.getLogger().info("[X1] Camarote definido: " + locToString(loc));
        salvarArena();
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

    /**
     * Salva as coordenadas da arena no arquivo de configuração.
     */
    private static void salvarArena() {
        try {
            if (!arenaFile.exists()) {
                arenaFile.getParentFile().mkdirs();
                arenaFile.createNewFile();
            }

            FileWriter writer = new FileWriter(arenaFile);
            if (pos1 != null) writer.write("pos1:" + locToString(pos1) + "\n");
            if (pos2 != null) writer.write("pos2:" + locToString(pos2) + "\n");
            if (camarote != null) writer.write("camarote:" + locToString(camarote) + "\n");

            writer.flush();
            writer.close();

            Bukkit.getLogger().info("[X1] Arena salva com sucesso!");

        } catch (IOException e) {
            Bukkit.getLogger().severe("[X1] Erro ao salvar a arena do X1!");
            e.printStackTrace();
        }
    }

    /**
     * Carrega as coordenadas da arena do arquivo de configuração.
     */
    public static void carregarArena() {
        if (!arenaFile.exists()) return;
        try {
            Scanner scanner = new Scanner(arenaFile);
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine();
                String[] partes = linha.split(":");
                if (partes.length < 2) continue;

                Location loc = stringToLoc(partes[1]);
                if (loc == null) continue;

                switch (partes[0]) {
                    case "pos1":
                        pos1 = loc;
                        Bukkit.getLogger().info("[X1] Posição 1 carregada: " + locToString(loc));
                        break;
                    case "pos2":
                        pos2 = loc;
                        Bukkit.getLogger().info("[X1] Posição 2 carregada: " + locToString(loc));
                        break;
                    case "camarote":
                        camarote = loc;
                        Bukkit.getLogger().info("[X1] Camarote carregado: " + locToString(loc));
                        break;
                }
            }
            scanner.close();
            Bukkit.getLogger().info("[X1] Arena carregada com sucesso!");

        } catch (IOException e) {
            Bukkit.getLogger().severe("[X1] Erro ao carregar a arena do X1!");
            e.printStackTrace();
        }
    }

    /**
     * Converte uma localização em String para salvar no arquivo.
     */
    private static String locToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    /**
     * Converte uma String salva no arquivo em um objeto Location.
     */
    private static Location stringToLoc(String str) {
        String[] coords = str.split(",");
        if (coords.length < 4) return null;

        World world = Bukkit.getWorld(coords[0]);
        if (world == null) {
            Bukkit.getLogger().severe("[X1] Erro: Mundo '" + coords[0] + "' não encontrado! Arena não foi carregada corretamente.");
            return null;
        }

        try {
            double x = Double.parseDouble(coords[1]);
            double y = Double.parseDouble(coords[2]);
            double z = Double.parseDouble(coords[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().severe("[X1] Erro ao converter coordenadas para número.");
            return null;
        }
    }
}