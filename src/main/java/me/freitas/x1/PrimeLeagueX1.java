package me.freitas.x1;

import me.freitas.x1.commands.X1Command;
import me.freitas.x1.listeners.DuelListener;
import me.freitas.x1.managers.StatsManager;
import me.freitas.x1.managers.ArenaManager;
import me.freitas.x1.managers.ChallengeManager;
import me.freitas.x1.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import static me.freitas.x1.utils.MessageUtils.debugMessages;

public class PrimeLeagueX1 extends JavaPlugin {

    private static PrimeLeagueX1 instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        carregarConfiguracoes();
        carregarMensagens();
        debugMessages();
        getLogger().info(getMensagem("plugin.ativado"));

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
        getLogger().info(getMensagem("plugin.desativado"));

        // Salvar estatísticas ao desligar o servidor
        StatsManager.salvarStats();
        getLogger().info(getMensagem("stats.salvas"));
    }

    public static PrimeLeagueX1 getInstance() {
        return instance;
    }

    /**
     * Registra todos os comandos do plugin.
     */
    private void registrarComandos() {
        if (getCommand("x1") != null) {
            getCommand("x1").setExecutor(new X1Command(this));
            getLogger().info(getMensagem("comando.x1_registrado"));
        } else {
            getLogger().severe(getMensagem("comando.erro_registro"));
        }
    }

    /**
     * Registra todos os eventos do plugin.
     */
    private void registrarEventos() {
        getServer().getPluginManager().registerEvents(new X1Command(this), this);
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        getLogger().info(getMensagem("eventos.registrados"));
    }

    /**
     * Carrega arenas e estatísticas do X1.
     */
    private void carregarDados() {
        getLogger().info(getMensagem("dados.carregando"));

        ArenaManager.carregarArena();
        StatsManager.carregarStats();

        getLogger().info(getMensagem("dados.carregados"));
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

        getLogger().info(getMensagem("desafios.verificacao"));
    }

    private void carregarConfiguracoes() {
        reloadConfig();
    }

    private void carregarMensagens() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }

        // 🔥 Força o recarregamento do arquivo para garantir que todas as mensagens estão disponíveis
        MessageUtils.reloadMessages(this);
        getLogger().info("Mensagens do PrimeLeagueX1 carregadas com sucesso!");
    }

    public String getMensagem(String chave) {
        return MessageUtils.getMessage(chave); // Usar MessageUtils para obter a mensagem
    }
}