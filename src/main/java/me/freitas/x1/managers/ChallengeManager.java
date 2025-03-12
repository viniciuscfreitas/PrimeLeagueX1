package me.freitas.x1.managers;
import me.freitas.x1.PrimeLeagueX1;

import me.freitas.x1.utils.MessageUtils;
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
    private static final Map<String, Boolean> desafiosPendentes = new HashMap<>();
    private static long TEMPO_EXPIRACAO;

    static {
        reloadConfig();
    }

    public static void reloadConfig() {
        TEMPO_EXPIRACAO = PrimeLeagueX1.getInstance().getConfig().getLong("x1_timeout", 180) * 1000;
    }

    /**
     * Desafia um jogador para um X1.
     */
    public static void desafiar(Player desafiante, Player alvo, boolean usarArena) {
        if (desafiosPendentes.containsKey(desafiante.getName())) {
            desafiante.sendMessage(MessageUtils.getMessage("x1.desafio.pendente"));
            return;
        }

        if (desafiante.equals(alvo)) {
            desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.auto"));
            return;
        }

        if (desafios.containsKey(desafiante.getName())) {
            desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.jaDesafiou"));
            return;
        }

        if (desafios.containsValue(desafiante.getName())) {
            desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.jaFoiDesafiado"));
            return;
        }

        // Se o X1 for no local atual, verificar distância entre os jogadores
        if (!usarArena) {
            if (!desafiante.getWorld().equals(alvo.getWorld())) {
                desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.mundoDiferente").replace("{player}", alvo.getName()));
                return;
            }

            // Declarando a variável ANTES do if
            double distancia = desafiante.getLocation().distance(alvo.getLocation());

            if (distancia > 10) {
                desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.muitoLonge").replace("{player}", alvo.getName()));
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
            alvo.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.semDesafios"));
            return;
        }

        Player desafiante = Bukkit.getPlayer(desafianteNome);
        if (desafiante == null) {
            alvo.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.jogadorOffline"));
            desafios.remove(desafianteNome);
            return;
        }

        desafios.remove(desafianteNome);
        desafiosExpiracao.remove(alvo.getName());

        boolean usarArena = desafiosArena.containsKey(desafiante.getName()) ? desafiosArena.get(desafiante.getName()) : false;

        // ✅ NOVA VERIFICAÇÃO: Impede X1 se a arena não estiver configurada
        if (usarArena && (ArenaManager.getPos1() == null || ArenaManager.getPos2() == null)) {
            desafiante.sendMessage(MessageUtils.getMessage("arena.nao_configurada"));
            alvo.sendMessage(MessageUtils.getMessage("arena.nao_configurada"));
            return;
        }

        DuelManager.iniciarDuelo(desafiante, alvo, usarArena);
    }

    /**
     * Recusa um desafio pendente e limpa corretamente os registros.
     */
    public static void recusar(Player alvo) {
        String desafianteNome = encontrarDesafiante(alvo.getName());

        if (desafianteNome == null) {
            alvo.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.semDesafios"));
            return;
        }

        Player desafiante = Bukkit.getPlayer(desafianteNome);
        if (desafiante != null) {
            desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.recusou").replace("{player}", alvo.getName()));
        }

        // Remover desafio corretamente de todas as listas
        desafios.remove(desafianteNome);
        desafiosExpiracao.remove(alvo.getName());
        desafiosArena.remove(desafianteNome);
        desafiosAtivos.remove(desafianteNome);
        desafiosAtivos.remove(alvo.getName());

        alvo.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.recusouSucesso"));
    }

    /**
     * Cancela um desafio pendente e remove todas as referências corretamente.
     */
    public static void cancelarDesafio(Player desafiante) {
        if (!desafios.containsKey(desafiante.getName())) {
            desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.semDesafiosCancelar"));
            return;
        }

        String alvoNome = desafios.get(desafiante.getName());

        if (alvoNome == null) {
            desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.jaRemovido"));
            return;
        }

        Player alvo = Bukkit.getPlayer(alvoNome);

        desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.cancelado").replace("{player}", desafiante.getName()));

        // ✅ **Remove corretamente de todos os mapas**
        desafios.remove(desafiante.getName());
        desafios.remove(alvoNome);
        desafiosExpiracao.remove(desafiante.getName());
        desafiosExpiracao.remove(alvoNome);
        desafiosArena.remove(desafiante.getName());
        desafiosArena.remove(alvoNome);
        desafiosAtivos.remove(desafiante.getName());
        desafiosAtivos.remove(alvoNome);

        desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.cancelouSucesso"));
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
        desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.enviar")
                .replace("{player}", alvo.getName()));

        if (usarArena) {
            alvo.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.receber.arena")
                    .replace("{player}", desafiante.getName()));
        } else {
            alvo.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.receber.local")
                    .replace("{player}", desafiante.getName()));
        }

        alvo.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.como_aceitar")
                .replace("{player}", desafiante.getName()));
    }

    /**
     * Envia mensagens ao desafiante e ao alvo quando um desafio expira.
     */
    private static void enviarMensagemExpiracao(String desafiante, String alvo) {
        Player desafiantePlayer = Bukkit.getPlayer(desafiante);
        Player alvoPlayer = Bukkit.getPlayer(alvo);

        if (alvoPlayer != null) {
            alvoPlayer.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.expirou"));
        }
        if (desafiantePlayer != null) {
            desafiantePlayer.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.expirouDesafiante").replace("{player}", alvo));
        }
    }

    public static void agendarExpiracaoDesafio(final Player desafiante, final Player desafiado) {
        Bukkit.getScheduler().runTaskLater(PrimeLeagueX1.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (desafios.containsKey(desafiante.getName())) {
                    desafiante.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.expirouDesafio"));
                    desafiado.sendMessage(PrimeLeagueX1.getInstance().getMensagem("mensagens.desafio.expirouDesafiado").replace("{player}", desafiante.getName()));

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
        }, 20 * (TEMPO_EXPIRACAO / 1000)); // Expira após o tempo configurado
    }

    public static void removerDesafio(String desafiante, String desafiado) {
        desafiosAtivos.remove(desafiante);
        desafiosAtivos.remove(desafiado);
    }
}