package me.freitas.x1.managers;

import me.freitas.x1.PrimeLeagueX1;
import me.freitas.x1.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class StatsManager {
    private static File statsFile;
    private static int maxAdversariosVencidos;

    private static final Map<String, Integer> vitorias = new HashMap<String, Integer>();
    private static final Map<String, Integer> derrotas = new HashMap<String, Integer>();
    private static final Map<String, Integer> streaks = new HashMap<String, Integer>();
    private static final Map<String, Integer> maiorStreak = new HashMap<String, Integer>();
    private static final Map<String, Integer> totalJogos = new HashMap<String, Integer>();
    private static final Map<String, Set<String>> adversariosVencidos = new HashMap<String, Set<String>>();

    // ✅ Método para carregar configurações
    public static void loadConfig(FileConfiguration config) {
        String statsPath = config.getString("stats-file-path", "plugins/PrimeLeagueX1/stats.txt");
        statsFile = new File(statsPath);
        if (!statsFile.getParentFile().exists()) {
            statsFile.getParentFile().mkdirs();
        }
        maxAdversariosVencidos = config.getInt("max-adversarios-vencidos", 10);
    }

    // ✅ Método genérico para inicializar estatísticas do jogador
    private static void inicializarStats(String nome) {
        if (!vitorias.containsKey(nome)) vitorias.put(nome, 0);
        if (!derrotas.containsKey(nome)) derrotas.put(nome, 0);
        if (!streaks.containsKey(nome)) streaks.put(nome, 0);
        if (!maiorStreak.containsKey(nome)) maiorStreak.put(nome, 0);
        if (!totalJogos.containsKey(nome)) totalJogos.put(nome, 0);
        if (!adversariosVencidos.containsKey(nome)) adversariosVencidos.put(nome, new HashSet<String>());
    }

    // ✅ Método seguro para converter Strings em Inteiros evitando erros de parsing
    private static int parseIntOrDefault(String value, int defaultValue) {
        try {
            if (value == null || value.equalsIgnoreCase("null")) {
                return defaultValue;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ✅ Adiciona vitória e atualiza streak
    public static void adicionarVitoria(Player jogador, Player adversario) {
        String nome = jogador.getName();
        String nomeAdversario = adversario.getName();
        inicializarStats(nome);
        inicializarStats(nomeAdversario);

        vitorias.put(nome, vitorias.get(nome) + 1);
        streaks.put(nome, streaks.get(nome) + 1);
        totalJogos.put(nome, totalJogos.get(nome) + 1);

        if (streaks.get(nome) > maiorStreak.get(nome)) {
            maiorStreak.put(nome, streaks.get(nome));
        }

        if (adversariosVencidos.get(nome).size() < maxAdversariosVencidos) {
            adversariosVencidos.get(nome).add(nomeAdversario);
        }

        salvarStats();
    }

    // ✅ Adiciona derrota e reseta streak
    public static void adicionarDerrota(Player jogador) {
        String nome = jogador.getName();
        inicializarStats(nome);

        derrotas.put(nome, derrotas.get(nome) + 1);
        streaks.put(nome, 0);
        totalJogos.put(nome, totalJogos.get(nome) + 1);

        salvarStats();
    }

    // ✅ Métodos para acessar estatísticas
    public static int getVitorias(String jogador) {
        return vitorias.containsKey(jogador) ? vitorias.get(jogador) : 0;
    }

    public static int getAdversariosVencidos(String jogador) {
        return adversariosVencidos.containsKey(jogador) ? adversariosVencidos.get(jogador).size() : 0;
    }

    public static int getDerrotas(String jogador) {
        return derrotas.containsKey(jogador) ? derrotas.get(jogador) : 0;
    }

    public static int getStreak(String jogador) {
        return streaks.containsKey(jogador) ? streaks.get(jogador) : 0;
    }

    public static int getMaiorStreak(String jogador) {
        return maiorStreak.containsKey(jogador) ? maiorStreak.get(jogador) : 0;
    }

    public static int getTotalJogos(String jogador) {
        return totalJogos.containsKey(jogador) ? totalJogos.get(jogador) : 0;
    }

    public static LinkedHashMap<String, Integer> getTopVitorias() {
        List<Map.Entry<String, Integer>> lista = new ArrayList<Map.Entry<String, Integer>>(vitorias.entrySet());

        // Ordenar do maior para o menor número de vitórias
        Collections.sort(lista, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return b.getValue().compareTo(a.getValue()); // Ordena do maior para o menor
            }
        });

        LinkedHashMap<String, Integer> ranking = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : lista) {
            ranking.put(entry.getKey(), entry.getValue());
        }

        return ranking;
    }

    // ✅ Salva estatísticas no arquivo
    public static void salvarStats() {
        if (statsFile == null || !statsFile.exists()) {
            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) {
                Bukkit.getLogger().severe("Arquivo de estatísticas não encontrado!");
            }
            return;
        }

        try (FileWriter writer = new FileWriter(statsFile)) {
            for (String jogador : vitorias.keySet()) {
                writer.write(jogador + ":V:" + (vitorias.containsKey(jogador) ? vitorias.get(jogador) : 0) + "\n");
                writer.write(jogador + ":D:" + (derrotas.containsKey(jogador) ? derrotas.get(jogador) : 0) + "\n");
                writer.write(jogador + ":S:" + (streaks.containsKey(jogador) ? streaks.get(jogador) : 0) + "\n");
                writer.write(jogador + ":MS:" + (maiorStreak.containsKey(jogador) ? maiorStreak.get(jogador) : 0) + "\n");
                writer.write(jogador + ":TJ:" + (totalJogos.containsKey(jogador) ? totalJogos.get(jogador) : 0) + "\n");

                writer.write(jogador + ":AV:" + joinCompat(",", adversariosVencidos.get(jogador)) + "\n");
            }

            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) {
                Bukkit.getLogger().info("Estatísticas salvas com sucesso!");
            }
        } catch (IOException e) {
            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) {
                Bukkit.getLogger().severe("Erro ao salvar estatísticas!");
            }
        }
    }

    // ✅ Carrega estatísticas do arquivo
    public static void carregarStats() {
        if (statsFile == null || !statsFile.exists()) {
            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) {
                Bukkit.getLogger().severe("Arquivo de estatísticas não encontrado!");
            }
            return;
        }

        try (Scanner scanner = new Scanner(statsFile)) {
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine();
                String[] partes = linha.split(":");
                if (partes.length < 3) continue;
                String jogador = partes[0];
                String tipo = partes[1];
                String valor = partes[2];
                inicializarStats(jogador);
                switch (tipo) {
                    case "V":
                        vitorias.put(jogador, parseIntOrDefault(valor, 0));
                        break;
                    case "D":
                        derrotas.put(jogador, parseIntOrDefault(valor, 0));
                        break;
                    case "S":
                        streaks.put(jogador, parseIntOrDefault(valor, 0));
                        break;
                    case "MS":
                        maiorStreak.put(jogador, parseIntOrDefault(valor, 0));
                        break;
                    case "TJ":
                        totalJogos.put(jogador, parseIntOrDefault(valor, 0));
                        break;
                    case "AV":
                        adversariosVencidos.get(jogador).addAll(Arrays.asList(valor.split(",")));
                        break;
                }
            }

            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) {
                Bukkit.getLogger().info("Estatísticas carregadas com sucesso!");
            }
        } catch (IOException e) {
            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) {
                Bukkit.getLogger().severe("Erro ao carregar estatísticas!");
            }
        }
    }

    public static String joinCompat(String delimiter, Set<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (sb.length() > 0) sb.append(delimiter);
            sb.append(value);
        }
        return sb.toString();
    }
}