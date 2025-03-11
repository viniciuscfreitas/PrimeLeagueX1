package me.freitas.x1.managers;
import me.freitas.x1.PrimeLeagueX1;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChallengeManager {
    private static final Map<String, Boolean> desafiosAtivos = new HashMap<String, Boolean>();
    private static final Map<String, Boolean> desafiosArena = new HashMap<String, Boolean>();
    private static final Map<String, String> desafios = new HashMap<String, String>();
    private static final Map<String, Long> desafiosExpiracao = new HashMap<String, Long>();
    private static final long TEMPO_EXPIRACAO = 30 * 1000; // 30 segundos

    /**
     * Desafia um jogador para um X1.
     */
    public static void desafiar(Player desafiante, Player alvo, boolean usarArena) {
        if (desafiante.equals(alvo)) {
            desafiante.sendMessage(ChatColor.RED + "❌ Você não pode desafiar a si mesmo para um X1!");
            return;
        }

        if (desafios.containsKey(desafiante.getName())) {
            desafiante.sendMessage(ChatColor.RED + "⚠ Você já desafiou um jogador! Aguarde a resposta.");
            return;
        }

        if (desafios.containsValue(desafiante.getName())) {
            desafiante.sendMessage(ChatColor.RED + "⚠ Você já foi desafiado! Aceite ou recuse primeiro.");
            return;
        }

        // Se o X1 for no local atual, verificar distância entre os jogadores
        if (!usarArena) {
            if (!desafiante.getWorld().equals(alvo.getWorld())) {
                desafiante.sendMessage(ChatColor.RED + "❌ Você está em um mundo diferente de " + alvo.getName() + " e não pode desafiá-lo para um X1 local!");
                return;
            }

            // Declarando a variável ANTES do if
            double distancia = desafiante.getLocation().distance(alvo.getLocation());

            if (distancia > 10) {
                desafiante.sendMessage(ChatColor.RED + "❌ Você está muito longe de " + alvo.getName() + " para desafiá-lo!");
                return;
            }
        }

        desafios.put(desafiante.getName(), alvo.getName());
        desafiosExpiracao.put(desafiante.getName(), System.currentTimeMillis() + TEMPO_EXPIRACAO);
        desafiosExpiracao.put(alvo.getName(), System.currentTimeMillis() + TEMPO_EXPIRACAO);
        desafiosArena.put(desafiante.getName(), usarArena);

        enviarMensagemDesafio(desafiante, alvo, usarArena);
        agendarExpiracaoDesafio(desafiante, alvo);
    }

    /**
     * Aceita um desafio pendente.
     */
    public static void aceitar(Player alvo) {
        String desafianteNome = encontrarDesafiante(alvo.getName());

        if (desafianteNome == null) {
            alvo.sendMessage(ChatColor.RED + "❌ Você não tem desafios pendentes!");
            return;
        }

        Player desafiante = Bukkit.getPlayer(desafianteNome);
        if (desafiante == null) {
            alvo.sendMessage(ChatColor.RED + "⚠ O jogador que te desafiou não está mais online.");
            desafios.remove(desafianteNome);
            return;
        }

        desafios.remove(desafianteNome);
        desafiosExpiracao.remove(alvo.getName());

        boolean usarArena = desafiosArena.containsKey(desafiante.getName()) ? desafiosArena.get(desafiante.getName()) : false;
        DuelManager.iniciarDuelo(desafiante, alvo, usarArena);
    }

    /**
     * Recusa um desafio pendente e limpa corretamente os registros.
     */
    public static void recusar(Player alvo) {
        String desafianteNome = encontrarDesafiante(alvo.getName());

        if (desafianteNome == null) {
            alvo.sendMessage(ChatColor.RED + "❌ Você não tem desafios pendentes!");
            return;
        }

        Player desafiante = Bukkit.getPlayer(desafianteNome);
        if (desafiante != null) {
            desafiante.sendMessage(ChatColor.RED + "❌ " + alvo.getName() + " recusou seu desafio!");
        }

        // Remover desafio corretamente de todas as listas
        desafios.remove(desafianteNome);
        desafiosExpiracao.remove(alvo.getName());
        desafiosArena.remove(desafianteNome);
        desafiosAtivos.remove(desafianteNome);
        desafiosAtivos.remove(alvo.getName());

        alvo.sendMessage(ChatColor.GREEN + "✅ Você recusou o desafio.");
    }

    /**
     * Cancela um desafio pendente e remove todas as referências corretamente.
     */
    public static void cancelarDesafio(Player desafiante) {
        if (!desafios.containsKey(desafiante.getName())) {
            desafiante.sendMessage(ChatColor.RED + "❌ Você não tem desafios para cancelar.");
            return;
        }

        String alvoNome = desafios.get(desafiante.getName());

        if (alvoNome == null) {
            desafiante.sendMessage(ChatColor.RED + "⚠ O desafio já foi removido ou expirou.");
            return;
        }

        Player alvo = Bukkit.getPlayer(alvoNome);

        if (alvo != null) {
            alvo.sendMessage(ChatColor.RED + "⚠ " + desafiante.getName() + " cancelou o desafio.");
        }

        // ✅ **Remove corretamente de todos os mapas**
        desafios.remove(desafiante.getName());
        desafios.remove(alvoNome);
        desafiosExpiracao.remove(desafiante.getName());
        desafiosExpiracao.remove(alvoNome);
        desafiosArena.remove(desafiante.getName());
        desafiosArena.remove(alvoNome);
        desafiosAtivos.remove(desafiante.getName());
        desafiosAtivos.remove(alvoNome);

        desafiante.sendMessage(ChatColor.GREEN + "✅ Você cancelou o desafio.");
    }

    /**
     * Verifica e remove desafios expirados automaticamente.
     */
    public static void verificarExpiracao() {
        long agora = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = desafiosExpiracao.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (agora > entry.getValue()) {
                String alvo = entry.getKey();
                String desafiante = desafios.get(alvo);

                if (desafiante != null) {
                    enviarMensagemExpiracao(desafiante, alvo);
                }

                // ✅ Remover desafio corretamente de TODOS os mapas
                desafios.remove(alvo);
                desafios.remove(desafiante);
                desafiosArena.remove(alvo);
                desafiosArena.remove(desafiante);
                desafiosAtivos.remove(alvo);
                desafiosAtivos.remove(desafiante);

                iterator.remove(); // ✅ Agora o remove() ocorre via Iterator, evitando o erro.
            }
        }
    }

    /**
     * Retorna se um jogador tem um desafio ativo.
     */
    public static boolean temDesafioAtivo(Player player) {
        return desafios.containsKey(player.getName()) || desafios.containsValue(player.getName());
    }

    /**
     * Retorna o desafiante de um jogador, caso ele tenha sido desafiado.
     */
    private static String encontrarDesafiante(String alvoNome) {
        for (Map.Entry<String, String> entry : desafios.entrySet()) {
            if (entry.getValue().equals(alvoNome)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Envia mensagens aos jogadores envolvidos em um desafio.
     */
    private static void enviarMensagemDesafio(Player desafiante, Player alvo, boolean usarArena) {
        desafiante.sendMessage(ChatColor.AQUA + "[X1] " + ChatColor.GREEN + "🎯 Você desafiou " + ChatColor.RED + alvo.getName() + ChatColor.GREEN + " para um X1!");

        if (usarArena) {
            alvo.sendMessage(ChatColor.AQUA + "[X1] " + ChatColor.RED + desafiante.getName() + ChatColor.AQUA + " desafiou você para um X1 na ARENA!");
        } else {
            alvo.sendMessage(ChatColor.AQUA + "[X1] " + ChatColor.RED + desafiante.getName() + ChatColor.AQUA + " desafiou você para um X1 no LOCAL ATUAL!");
        }

        alvo.sendMessage(ChatColor.AQUA + "[X1] " + ChatColor.YELLOW + "Digite: /x1 aceitar " + desafiante.getName() + ChatColor.AQUA + " para aceitar!");
    }

    /**
     * Envia mensagens ao desafiante e ao alvo quando um desafio expira.
     */
    private static void enviarMensagemExpiracao(String desafiante, String alvo) {
        Player desafiantePlayer = Bukkit.getPlayer(desafiante);
        Player alvoPlayer = Bukkit.getPlayer(alvo);

        if (alvoPlayer != null) {
            alvoPlayer.sendMessage(ChatColor.RED + "⏳ O desafio de X1 expirou.");
        }
        if (desafiantePlayer != null) {
            desafiantePlayer.sendMessage(ChatColor.RED + "⏳ Seu desafio contra " + alvo + " expirou.");
        }
    }

    public static void agendarExpiracaoDesafio(final Player desafiante, final Player desafiado) {
        Bukkit.getScheduler().runTaskLater(PrimeLeagueX1.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (desafios.containsKey(desafiante.getName())) {
                    desafiante.sendMessage(ChatColor.RED + "⏳ O desafio expirou!");
                    desafiado.sendMessage(ChatColor.RED + "⏳ Seu desafio contra " + desafiante.getName() + " expirou!");

                    // ✅ **Removendo corretamente de TODOS os mapas**
                    desafios.remove(desafiante.getName());
                    desafios.remove(desafiado.getName());
                    desafiosExpiracao.remove(desafiante.getName());
                    desafiosExpiracao.remove(desafiado.getName());
                    desafiosArena.remove(desafiante.getName());
                    desafiosArena.remove(desafiado.getName());
                    desafiosAtivos.remove(desafiante.getName());
                    desafiosAtivos.remove(desafiado.getName());
                }
            }
        }, 20 * 30); // Expira após 30 segundos (600 ticks)
    }

    public static void removerDesafio(String desafiante, String desafiado) {
        desafiosAtivos.remove(desafiante);
        desafiosAtivos.remove(desafiado);
    }
}