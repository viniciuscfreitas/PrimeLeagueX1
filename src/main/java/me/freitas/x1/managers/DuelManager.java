package me.freitas.x1.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import me.freitas.elo.api.PrimeLeagueEloAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DuelManager {
    private static final Map<String, Location> localizacaoOriginal = new HashMap<String, Location>();
    private static final Map<String, String> duelosAtivos = new HashMap<String, String>();

    // Adicionado para controle de vitórias diárias e consecutivas
    private static final Map<String, Integer> vitoriasDiarias = new HashMap<String, Integer>();
    private static final Map<String, Integer> vitoriasSeguidas = new HashMap<String, Integer>();
    private static final Map<String, Long> tempoUltimaVitoria = new HashMap<String, Long>();
    private static final long RESET_DIARIO_HORAS = 24;
    private static final long RESET_CONSECUTIVAS_HORAS = 12;

    public static void iniciarDuelo(Player desafiante, Player alvo, boolean usarArena) {
        salvarLocalizacaoOriginal(desafiante);
        salvarLocalizacaoOriginal(alvo);
        iniciarContadorRegressivo(desafiante, alvo, usarArena);
    }

    public static boolean estaEmDuelo(String playerName) {
        return duelosAtivos.containsKey(playerName);
    }

    public static String getAdversario(String playerName) {
        return duelosAtivos.get(playerName);
    }

    public static void verificarDistancia(final Player desafiante, final Player alvo) {
        Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("PrimeLeagueX1"), new Runnable() {
            @Override
            public void run() {
                if (!duelosAtivos.containsKey(desafiante.getName()) || !duelosAtivos.containsKey(alvo.getName())) {
                    return;
                }
                double distancia = desafiante.getLocation().distance(alvo.getLocation());
                if (distancia > 20) {
                    desafiante.sendMessage(ChatColor.RED + "❌ Você se afastou demais do seu oponente! O X1 foi cancelado.");
                    alvo.sendMessage(ChatColor.RED + "❌ Seu oponente se afastou demais! O X1 foi cancelado.");
                    cancelarDuelo(desafiante);
                }
            }
        }, 0L, 40L);
    }

    public static void cancelarDuelo(Player jogador) {
        String adversarioNome = duelosAtivos.get(jogador.getName());
        if (adversarioNome != null) {
            Player adversario = Bukkit.getPlayer(adversarioNome);
            if (adversario != null) {
                adversario.sendMessage(ChatColor.RED + "❌ Seu oponente saiu do duelo! Você venceu.");
                finalizarDuelo(adversario, jogador);
            }
        }
        restaurarLocalizacao(jogador);
        removerDuelo(jogador, Bukkit.getPlayer(adversarioNome));
    }

    public static void finalizarDuelo(Player vencedor, Player perdedor) {
        enviarTitulo(vencedor, ChatColor.GOLD + "🏆 VITÓRIA!", "Você venceu o X1!");
        enviarTitulo(perdedor, ChatColor.RED + "❌ DERROTA!", "Tente novamente!");

        vencedor.playSound(vencedor.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
        perdedor.playSound(perdedor.getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);

        vencedor.sendMessage(ChatColor.GOLD + "Você venceu o duelo contra " + perdedor.getName() + "!");
        perdedor.sendMessage(ChatColor.RED + "Você perdeu o duelo contra " + vencedor.getName() + "!");

        // 📌 Atualização de Elo 📌
        atualizarElo(vencedor, perdedor);

        // 📌 Finalizar duelo normalmente 📌
        anunciarFimDuelo(vencedor, perdedor);
        restaurarLocalizacao(vencedor);
        restaurarLocalizacao(perdedor);
        removerDuelo(vencedor, perdedor);
    }

    private static void atualizarElo(Player vencedor, Player perdedor) {
        String key = vencedor.getName() + ":" + perdedor.getName();
        long agora = System.currentTimeMillis();
        long ultimaVitoria = tempoUltimaVitoria.containsKey(key) ? tempoUltimaVitoria.get(key) : 0L;
        int vitoriasHoje = vitoriasDiarias.containsKey(key) ? vitoriasDiarias.get(key) : 0;
        int vitoriasSeguidasAtual = vitoriasSeguidas.containsKey(key) ? vitoriasSeguidas.get(key) : 0;

        // Resetar contador diário e consecutivo
        if (agora - ultimaVitoria > TimeUnit.HOURS.toMillis(RESET_DIARIO_HORAS)) {
            vitoriasDiarias.put(key, 0);
            vitoriasSeguidas.put(key, 0);
        }

        // Definir ganho/perda de Elo com base na diferença e redução gradual
        int eloVencedor = PrimeLeagueEloAPI.getElo(vencedor);
        int eloPerdedor = PrimeLeagueEloAPI.getElo(perdedor);
        int diferencaElo = eloVencedor - eloPerdedor;

        int baseGanho = 30;
        int reducao = Math.min(20, vitoriasSeguidasAtual * 10); // Redução progressiva (-10 a cada vitória seguida)
        int deltaGanho = Math.max(0, baseGanho - reducao);
        int deltaPerda = Math.min(50, 30 + Math.max(0, (eloVencedor - eloPerdedor) / 20));

        // Bloqueia ganho de Elo após 3 vitórias diárias contra o mesmo player
        if (vitoriasHoje >= 3) {
            deltaGanho = 0;
        }

        PrimeLeagueEloAPI.updateElo(vencedor, PrimeLeagueEloAPI.getElo(vencedor) + deltaGanho);
        int novoEloPerdedor = Math.max(0, PrimeLeagueEloAPI.getElo(perdedor) - deltaPerda);
        PrimeLeagueEloAPI.updateElo(perdedor, novoEloPerdedor);

        // Atualiza estatísticas
        tempoUltimaVitoria.put(key, agora);
        vitoriasDiarias.put(key, vitoriasHoje + 1);
        vitoriasSeguidas.put(key, vitoriasSeguidasAtual + 1);

        // Exibir nova pontuação de Elo
        if (deltaGanho > 0) {
            vencedor.sendMessage(ChatColor.GREEN + "📈 Você ganhou " + deltaGanho + " de Elo!");
        } else {
            vencedor.sendMessage(ChatColor.YELLOW + "⚠ Você atingiu o limite diário de vitórias contra " + perdedor.getName() + " e não recebeu Elo.");
        }

        vencedor.sendMessage(ChatColor.GREEN + "Seu novo Elo: " + ChatColor.AQUA + PrimeLeagueEloAPI.getElo(vencedor));
        perdedor.sendMessage(ChatColor.RED + "📉 Seu novo Elo: " + ChatColor.AQUA + PrimeLeagueEloAPI.getElo(perdedor));
    }

    private static void salvarLocalizacaoOriginal(Player player) {
        localizacaoOriginal.put(player.getName(), player.getLocation());
    }

    private static void restaurarLocalizacao(Player player) {
        if (localizacaoOriginal.containsKey(player.getName())) {
            player.teleport(localizacaoOriginal.get(player.getName()));
        }
    }

    public static void enviarTitulo(Player player, String titulo, String subtitulo) {
        player.sendMessage(ChatColor.GOLD + titulo);
        player.sendMessage(ChatColor.YELLOW + subtitulo);
    }

    public static void anunciarDuelo(Player desafiante, Player alvo) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "🔥 X1 INICIANDO! 🔥");
        Bukkit.broadcastMessage(ChatColor.YELLOW + desafiante.getName() + " ⚔ " + alvo.getName());
    }

    public static void anunciarFimDuelo(Player vencedor, Player perdedor) {
        Bukkit.broadcastMessage(ChatColor.GREEN + "🏆 O duelo acabou! Vencedor: " + vencedor.getName());
        Bukkit.broadcastMessage(ChatColor.RED + "💀 Perdedor: " + perdedor.getName());
    }

    private static void removerDuelo(Player jogador1, Player jogador2) {
        duelosAtivos.remove(jogador1.getName());
        duelosAtivos.remove(jogador2.getName());
    }

    private static void iniciarContadorRegressivo(final Player desafiante, final Player alvo, final boolean usarArena) {
        final int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("PrimeLeagueX1"), new Runnable() {
            int contador = 3;

            @Override
            public void run() {
                if (contador > 0) {
                    String numero = ChatColor.RED + String.valueOf(contador);
                    enviarTitulo(desafiante, numero, "Prepare-se!");
                    enviarTitulo(alvo, numero, "Prepare-se!");
                    desafiante.playSound(desafiante.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                    alvo.playSound(alvo.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                    contador--;
                } else {
                    Bukkit.getScheduler().cancelTask(taskId[0]);

                    enviarTitulo(desafiante, ChatColor.GREEN + "LUTE!", "Boa sorte!");
                    enviarTitulo(alvo, ChatColor.GREEN + "LUTE!", "Boa sorte!");
                    desafiante.playSound(desafiante.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
                    alvo.playSound(alvo.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);

                    duelosAtivos.put(desafiante.getName(), alvo.getName());
                    duelosAtivos.put(alvo.getName(), desafiante.getName());

                    if (usarArena) {
                        if (ArenaManager.arenaDefinida()) {
                            desafiante.teleport(ArenaManager.getPos1());
                            alvo.teleport(ArenaManager.getPos2());
                            desafiante.sendMessage(ChatColor.GREEN + "📍 Você foi teleportado para a arena fixa!");
                            alvo.sendMessage(ChatColor.GREEN + "📍 Você foi teleportado para a arena fixa!");
                        } else {
                            desafiante.sendMessage(ChatColor.RED + "⚠ A arena ainda não foi configurada! O duelo será no local atual.");
                            alvo.sendMessage(ChatColor.RED + "⚠ A arena ainda não foi configurada! O duelo será no local atual.");
                            verificarDistancia(desafiante, alvo);
                        }
                    } else {
                        desafiante.sendMessage(ChatColor.YELLOW + "⚔ O duelo será no local atual! Não se afaste do seu oponente.");
                        alvo.sendMessage(ChatColor.YELLOW + "⚔ O duelo será no local atual! Não se afaste do seu oponente.");
                        verificarDistancia(desafiante, alvo);
                    }

                    anunciarDuelo(desafiante, alvo);
                }
            }
        }, 0L, 20L);
    }
}