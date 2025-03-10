package me.freitas.x1;

import me.freitas.x1.commands.X1Command;
import me.freitas.x1.listeners.DuelListener;
import me.freitas.x1.managers.StatsManager;
import me.freitas.x1.managers.ArenaManager;
import me.freitas.x1.managers.ChallengeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class PrimeLeagueX1 extends JavaPlugin {

    private static PrimeLeagueX1 instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info(ChatColor.GREEN + "[PrimeLeagueX1] Plugin ativado com sucesso!");
        getServer().getPluginManager().registerEvents(new X1Command(), this);

        // Registrar comandos e eventos
        registrarComandos();
        registrarEventos();

        // Carregar dados essenciais
        carregarDados();

        // Iniciar tarefas automáticas
        iniciarTarefasPeriodicas();
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "[PrimeLeagueX1] Plugin desativado.");

        // Salvar estatísticas ao desligar o servidor
        StatsManager.salvarStats();
        getLogger().info(ChatColor.YELLOW + "[PrimeLeagueX1] Estatísticas salvas com sucesso!");
    }

    public static PrimeLeagueX1 getInstance() {
        return instance;
    }

    /**
     * Registra todos os comandos do plugin.
     */
    private void registrarComandos() {
        if (getCommand("x1") != null) {
            getCommand("x1").setExecutor(new X1Command());
            getLogger().info(ChatColor.GREEN + "[PrimeLeagueX1] Comando /x1 registrado com sucesso.");
        } else {
            getLogger().severe("[PrimeLeagueX1] Erro ao registrar o comando /x1!");
        }
    }

    /**
     * Registra todos os eventos do plugin.
     */
    private void registrarEventos() {
        getServer().getPluginManager().registerEvents(new DuelListener(), this);
        getServer().getPluginManager().registerEvents(new X1Command(), this);
        getLogger().info(ChatColor.GREEN + "[PrimeLeagueX1] Eventos do X1 registrados com sucesso.");
    }

    /**
     * Carrega arenas e estatísticas do X1.
     */
    private void carregarDados() {
        getLogger().info(ChatColor.YELLOW + "[PrimeLeagueX1] Carregando arenas e estatísticas...");

        ArenaManager.carregarArena();
        StatsManager.carregarStats();

        getLogger().info(ChatColor.YELLOW + "[PrimeLeagueX1] Arena e estatísticas carregadas com sucesso!");
    }

    /**
     * Inicia tarefas assíncronas periódicas, como verificação de desafios expirados.
     */
    private void iniciarTarefasPeriodicas() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                ChallengeManager.verificarExpiracao();
            }
        }, 0L, 100L); // Executa a cada 5 segundos

        getLogger().info(ChatColor.BLUE + "[PrimeLeagueX1] Verificação de desafios expirados ativada.");
    }
}