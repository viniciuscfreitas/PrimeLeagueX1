package me.freitas.x1.managers;

import me.freitas.x1.PrimeLeagueX1;
import me.freitas.x1.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
// import me.freitas.config.ConfigManager;
// import me.freitas.config.Messages;
import org.bukkit.entity.Player;
import me.freitas.elo.api.PrimeLeagueEloAPI;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DuelManager {
    private static final Map<String, Location> localizacaoOriginal = new HashMap<>();
    private static final Map<String, String> duelosAtivos = new HashMap<String, String>();

    // Adicionado para controle de vitórias diárias e consecutivas
    private static final Map<String, Integer> vitoriasDiarias = new HashMap<String, Integer>();
    private static final Map<String, Integer> vitoriasSeguidas = new HashMap<String, Integer>();
    private static final Map<String, Long> tempoUltimaVitoria = new HashMap<String, Long>();
    private static long RESET_DIARIO_HORAS = 24; // Ajuste o valor correto
    private static long RESET_CONSECUTIVAS_HORAS = 24; // Ajuste o valor correto

    public static void iniciarDuelo(final Player desafiante, final Player alvo, boolean usarArena) {
        salvarLocalizacaoOriginal(desafiante);
        salvarLocalizacaoOriginal(alvo);
        iniciarContadorRegressivo(desafiante, alvo, usarArena);

        // Define a cor do nome e a tag [X1]
        definirCorNome(desafiante);
        definirCorNome(alvo);

        // Inicia a verificação de distância após a contagem regressiva
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("PrimeLeagueX1"), new Runnable() {
            @Override
            public void run() {
                verificarDistancia(desafiante, alvo);
            }
        }, 200L);

        // Inicia o temporizador do X1 com tempo configurável via config.yml
        int tempoLimite = (int) PrimeLeagueX1.getInstance().getConfig().getLong("x1_timeout", 30);
        iniciarTemporizadorDuelo(desafiante, alvo, tempoLimite);
    }

    public static void iniciarTemporizadorDuelo(final Player desafiante, final Player alvo, int tempoLimite) {
        final int[] tempoRestante = {tempoLimite};

        final int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("PrimeLeagueX1"), new Runnable() {
            @Override
            public void run() {
                if (!duelosAtivos.containsKey(desafiante.getName()) || !duelosAtivos.containsKey(alvo.getName())) {
                    Bukkit.getScheduler().cancelTask(tempoRestante[0]);
                    return;
                }

                // Enviar mensagem para ambos os jogadores
                if (tempoRestante[0] % 10 == 0 || tempoRestante[0] <= 5) { // Avisa a cada 10s e nos últimos 5s
                    String tempoMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.tempo_restante").replace("{segundos}", String.valueOf(tempoRestante[0]));
                    desafiante.sendMessage(tempoMsg);
                    alvo.sendMessage(tempoMsg);
                }

                if (tempoRestante[0] <= 0) {
                    String tempoExpirado = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.tempo_expirado");
                    String empate = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.empate");
                    String expiradoMsg = PrimeLeagueX1.getInstance().getMensagem("duelo.tempo_expirado");
                    desafiante.sendMessage(expiradoMsg);
                    alvo.sendMessage(expiradoMsg);

                    String dueloEmpateMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.empate")
                            .replace("{desafiante}", desafiante.getName())
                            .replace("{alvo}", alvo.getName());
                    Bukkit.broadcastMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.empate")
                            .replace("{desafiante}", desafiante.getName())
                            .replace("{alvo}", alvo.getName()));

                    // Restaurar localizações
                    restaurarLocalizacao(desafiante);
                    restaurarLocalizacao(alvo);

                    // Resetar tags e nomes
                    resetarCorNome(desafiante);
                    resetarCorNome(alvo);

                    // Remover duelo sem conceder vitória para ninguém
                    removerDuelo(desafiante, null);
                    removerDuelo(alvo, null);

                    // Cancelar a tarefa do temporizador
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                }

                tempoRestante[0]--;
            }
        }, 0L, 20L); // 20 ticks = 1 segundo
    }

    public static boolean estaEmDuelo(String playerName) {
        return duelosAtivos.containsKey(playerName);
    }

    public static String getAdversario(String playerName) {
        return duelosAtivos.get(playerName);
    }

    public static void verificarDistancia(final Player desafiante, final Player alvo) {
        Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("PrimeLeagueX1"), new Runnable() {
            private int desafianteForaTempo = 0;
            private int alvoForaTempo = 0;
            private final int TEMPO_LIMITE = 3; // Tempo de tolerância antes de considerar derrota

            @Override
            public void run() {
                if (!duelosAtivos.containsKey(desafiante.getName()) || !duelosAtivos.containsKey(alvo.getName())) {
                    return;
                }

                double distancia = desafiante.getLocation().distance(alvo.getLocation());

                boolean desafianteFora = distancia > 20;
                boolean alvoFora = distancia > 20;

                // Se ambos os jogadores saírem juntos, inicia a contagem para cancelar
                if (desafianteFora && alvoFora) {
                    desafianteForaTempo++;
                    alvoForaTempo++;

                    if (desafianteForaTempo >= TEMPO_LIMITE && alvoForaTempo >= TEMPO_LIMITE) {
                        String canceladoMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.cancelado_area");
                        desafiante.sendMessage(canceladoMsg);
                        alvo.sendMessage(canceladoMsg);
                        cancelarDuelo(desafiante);
                        cancelarDuelo(alvo);
                        return;
                    }
                    return;
                }

                // Se apenas um jogador sair, iniciar contagem
                if (desafianteFora) {
                    desafianteForaTempo++;
                } else {
                    desafianteForaTempo = 0;
                }

                if (alvoFora) {
                    alvoForaTempo++;
                } else {
                    alvoForaTempo = 0;
                }

                // Se qualquer um dos jogadores sair, cancelar o X1 ao invés de dar vitória
                    if (desafianteForaTempo >= TEMPO_LIMITE || alvoForaTempo >= TEMPO_LIMITE) {
                        desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.cancelado_area"));
                        alvo.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.cancelado_area"));
                        cancelarDuelo(desafiante);
                        cancelarDuelo(alvo);
                        return;
                    }
            }
        }, 0L, 20L); // Verifica a cada 1 segundo
    }

    public static void definirCorNome(Player jogador) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team time = scoreboard.getTeam(jogador.getName());

        if (time == null) {
            time = scoreboard.registerNewTeam(jogador.getName());
        }

        String prefixoX1 = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.prefixo_x1");
        time.setPrefix(ChatColor.translateAlternateColorCodes('&', PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.prefixo_x1")));
        time.setSuffix(ChatColor.RESET.toString()); // Reseta formatação extra
        time.setDisplayName(jogador.getName()); // Mantém o nome original
        time.addPlayer(jogador);
    }

    public static void cancelarDuelo(Player jogador) {
        String adversarioNome = duelosAtivos.get(jogador.getName());

        // 🔍 Se adversárioNome for null, o duelo já foi removido
        if (adversarioNome == null) {
            jogador.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.erro_cancelar"));
            removerDuelo(jogador, null);
            return;
        }

        Player adversario = Bukkit.getPlayer(adversarioNome);

        // 🔍 Se adversário ainda estiver online, notifica e finaliza o duelo
        if (adversario != null) {
            adversario.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.oponente_saiu"));
            finalizarDuelo(adversario, jogador);
        }

        restaurarLocalizacao(jogador);
        removerDuelo(jogador, adversario);
    }

    public static void resetarCorNome(Player jogador) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team time = scoreboard.getTeam(jogador.getName());

        if (time != null) {
            time.removePlayer(jogador);
            time.unregister();
        }
    }

    public static void finalizarDuelo(Player vencedor, Player perdedor) {
        String vitoriaTitulo = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.vitoria_titulo");
        String vitoriaSubtitulo = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.vitoria_subtitulo");
        String derrotaTitulo = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.derrota_titulo");
        String derrotaSubtitulo = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.derrota_subtitulo");

        // Enviando mensagens ao vencedor
        vencedor.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.titulo").replace("{titulo}", vitoriaTitulo));
        vencedor.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.subtitulo").replace("{subtitulo}", vitoriaSubtitulo));

        // Enviando mensagens ao perdedor
        perdedor.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.titulo").replace("{titulo}", derrotaTitulo));
        perdedor.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.subtitulo").replace("{subtitulo}", derrotaSubtitulo));

        vencedor.playSound(vencedor.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
        perdedor.playSound(perdedor.getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);

        String vitoriaMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.vitoria").replace("{player}", perdedor.getName());
        String derrotaMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.derrota").replace("{player}", vencedor.getName());

        vencedor.sendMessage(vitoriaMsg);
        perdedor.sendMessage(derrotaMsg);

        resetarCorNome(vencedor);
        resetarCorNome(perdedor);

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
        int eloVencedor = PrimeLeagueEloAPI.getElo(Bukkit.getPlayer(vencedor.getName()));
        int eloPerdedor = PrimeLeagueEloAPI.getElo(Bukkit.getPlayer(perdedor.getName()));
        int diferencaElo = eloVencedor - eloPerdedor;

        int baseGanho = 30;
        int reducao = Math.min(20, vitoriasSeguidasAtual * 10); // Redução progressiva (-10 a cada vitória seguida)
        int deltaGanho = Math.max(0, baseGanho - reducao);
        int deltaPerda = Math.min(50, 30 + Math.max(0, (eloVencedor - eloPerdedor) / 20));

        // Bloqueia ganho de Elo após 3 vitórias diárias contra o mesmo player
        if (vitoriasHoje >= 3) {
            deltaGanho = 0;
        }

        PrimeLeagueEloAPI.updateElo(vencedor, PrimeLeagueEloAPI.getElo(Bukkit.getPlayer(vencedor.getName())) + deltaGanho);
        int novoEloPerdedor = Math.max(0, PrimeLeagueEloAPI.getElo(Bukkit.getPlayer(perdedor.getName())) - deltaPerda);
        PrimeLeagueEloAPI.updateElo(perdedor, novoEloPerdedor);

        // Atualiza estatísticas
        tempoUltimaVitoria.put(key, agora);
        vitoriasDiarias.put(key, vitoriasHoje + 1);
        vitoriasSeguidas.put(key, vitoriasSeguidasAtual + 1);

        // Exibir nova pontuação de Elo
        if (deltaGanho > 0) {
            String eloGanhoMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.elo_ganho").replace("{elo}", String.valueOf(deltaGanho));
            vencedor.sendMessage(eloGanhoMsg);
        } else {
            String limiteEloMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.limite_elo").replace("{player}", perdedor.getName());
            vencedor.sendMessage(limiteEloMsg);
        }

        String novoEloVencedorMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.novo_elo").replace("{elo}", String.valueOf(PrimeLeagueEloAPI.getElo(vencedor)));
        vencedor.sendMessage(novoEloVencedorMsg);
        String novoEloPerdedorMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.novo_elo_perdedor").replace("{elo}", String.valueOf(PrimeLeagueEloAPI.getElo(perdedor)));
        perdedor.sendMessage(novoEloPerdedorMsg);
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
        player.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.titulo").replace("{titulo}", titulo));
        player.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.subtitulo").replace("{subtitulo}", subtitulo));
    }

    public static void anunciarDuelo(Player desafiante, Player alvo) {
        if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) { Bukkit.getLogger().info("🔍 Tentando buscar mensagem: mensagens.duelo.anuncio"); }

        String dueloAnuncioMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.anuncio");

        if (dueloAnuncioMsg == null || dueloAnuncioMsg.isEmpty()) {
            if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) { Bukkit.getLogger().severe("🚨 ERRO: A mensagem 'mensagens.duelo.anuncio' não foi encontrada no messages.yml!"); }
            return;
        }

        // Substituir corretamente os placeholders pelos nomes dos jogadores
        dueloAnuncioMsg = dueloAnuncioMsg.replace("{player1}", desafiante.getName())
                .replace("{player2}", alvo.getName());

        // Debug para verificar a mensagem final no console
        if (PrimeLeagueX1.getInstance().getConfig().getBoolean("debug")) { Bukkit.getLogger().info("📢 Mensagem final do anúncio: " + dueloAnuncioMsg); }

        // Enviar mensagem formatada para todos os jogadores
        Bukkit.broadcastMessage(dueloAnuncioMsg);
    }

    public static void anunciarFimDuelo(Player vencedor, Player perdedor) {
        Bukkit.broadcastMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.fim").replace("{vencedor}", vencedor.getName()).replace("{perdedor}", perdedor.getName()));
    }

    private static void removerDuelo(Player jogador1, Player jogador2) {
        duelosAtivos.remove(jogador1.getName());

        // 🔍 Verifica se jogador2 não é null antes de remover
        if (jogador2 != null) {
            duelosAtivos.remove(jogador2.getName());
        }
    }

    private static void iniciarContadorRegressivo(final Player desafiante, final Player alvo, final boolean usarArena) {
        final int[] taskId = new int[1];

        // 🏹 Teleporte e notificação dos jogadores
        if (usarArena) {
            if (ArenaManager.arenaDefinida()) {
                desafiante.teleport(ArenaManager.getPos1());
                alvo.teleport(ArenaManager.getPos2());
                String teleportadoArenaMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.teleportado_arena");
                desafiante.sendMessage(teleportadoArenaMsg);
                alvo.sendMessage(teleportadoArenaMsg);
            } else {
                String arenaNaoConfiguradaMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.arena_nao_configurada");
                desafiante.sendMessage(arenaNaoConfiguradaMsg);
                alvo.sendMessage(arenaNaoConfiguradaMsg);
            }
        } else {
            String dueloLocalMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.local_atual");
            desafiante.sendMessage(dueloLocalMsg);
            alvo.sendMessage(dueloLocalMsg);
        }

        // 🔒 Impedir dano entre os jogadores durante a contagem
        duelosAtivos.put(desafiante.getName(), "contagem");
        duelosAtivos.put(alvo.getName(), "contagem");

        // ✅ Inicia a contagem de 10 segundos
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("PrimeLeagueX1"), new Runnable() {
            int contador = 10;

            @Override
            public void run() {
                if (contador > 0) {
                    String contagemMsg = PrimeLeagueX1.getInstance().getMensagem("mensagens.duelo.contagem")
                            .replace("{segundos}", String.valueOf(contador));
                    desafiante.sendMessage(contagemMsg);
                    alvo.sendMessage(contagemMsg);
                    desafiante.playSound(desafiante.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);
                    alvo.playSound(alvo.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);
                    contador--;
                } else {
                    // 🛑 Para a contagem ao chegar a 0
                    Bukkit.getScheduler().cancelTask(taskId[0]);

                    // 🔓 Liberar os jogadores para lutarem
                    duelosAtivos.put(desafiante.getName(), alvo.getName());
                    duelosAtivos.put(alvo.getName(), desafiante.getName());

                    // 📢 Agora chamamos a função correta que já substitui os placeholders
                    anunciarDuelo(desafiante, alvo);

                    desafiante.playSound(desafiante.getLocation(), Sound.ANVIL_LAND, 1.0f, 1.0f);
                    alvo.playSound(alvo.getLocation(), Sound.ANVIL_LAND, 1.0f, 1.0f);
                }
            }
        }, 0L, 20L); // ⏳ 20 ticks = 1 segundo
    }
}