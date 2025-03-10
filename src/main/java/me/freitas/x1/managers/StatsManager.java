package me.freitas.x1.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class StatsManager {
    private static final File statsFile = new File("plugins/PrimeLeagueX1/stats.txt");

    private static final Map<String, Integer> vitorias = new HashMap<String, Integer>();
    private static final Map<String, Integer> derrotas = new HashMap<String, Integer>();
    private static final Map<String, Integer> streaks = new HashMap<String, Integer>();
    private static final Map<String, Integer> maiorStreak = new HashMap<String, Integer>();
    private static final Map<String, Integer> totalJogos = new HashMap<String, Integer>();
    private static final Map<String, Set<String>> adversariosVencidos = new HashMap<String, Set<String>>();

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

        adversariosVencidos.get(nome).add(nomeAdversario);

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
        try {
            if (!statsFile.exists()) {
                statsFile.getParentFile().mkdirs();
                statsFile.createNewFile();
            }

            FileWriter writer = new FileWriter(statsFile);
            for (String jogador : vitorias.keySet()) {
                writer.write(jogador + ":V:" + vitorias.get(jogador) + "\n");
                writer.write(jogador + ":D:" + derrotas.get(jogador) + "\n");
                writer.write(jogador + ":S:" + streaks.get(jogador) + "\n");
                writer.write(jogador + ":MS:" + maiorStreak.get(jogador) + "\n");
                writer.write(jogador + ":TJ:" + totalJogos.get(jogador) + "\n");

                writer.write(jogador + ":AV:" + joinCompat(",", adversariosVencidos.get(jogador)) + "\n");
            }
            writer.close();

        } catch (IOException e) {
            Bukkit.getLogger().severe("Erro ao salvar estatísticas do X1!");
        }
    }

    // ✅ Carrega estatísticas do arquivo
    public static void carregarStats() {
        if (!statsFile.exists()) return;

        try {
            Scanner scanner = new Scanner(statsFile);
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine();
                String[] partes = linha.split(":");

                if (partes.length < 3) continue;

                String jogador = partes[0];
                String tipo = partes[1];
                String valor = partes[2];

                inicializarStats(jogador);

                if (tipo.equals("V")) vitorias.put(jogador, parseIntOrDefault(valor, 0));
                else if (tipo.equals("D")) derrotas.put(jogador, parseIntOrDefault(valor, 0));
                else if (tipo.equals("S")) streaks.put(jogador, parseIntOrDefault(valor, 0));
                else if (tipo.equals("MS")) maiorStreak.put(jogador, parseIntOrDefault(valor, 0));
                else if (tipo.equals("TJ")) totalJogos.put(jogador, parseIntOrDefault(valor, 0));
                else if (tipo.equals("AV")) adversariosVencidos.get(jogador).addAll(Arrays.asList(valor.split(",")));
            }
            scanner.close();

            Bukkit.getLogger().info("Estatísticas do X1 carregadas com sucesso!");

        } catch (IOException e) {
            Bukkit.getLogger().severe("Erro ao carregar estatísticas do X1!");
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