package me.freitas.x1.listeners;

import me.freitas.x1.managers.DuelManager;
import me.freitas.x1.utils.MessageUtils; // Importando a classe MessageUtils
import me.freitas.x1.PrimeLeagueX1; // Importando a classe PrimeLeagueX1
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener responsável por gerenciar eventos dentro do sistema de X1.
 */
public class DuelListener implements Listener {

    private final PrimeLeagueX1 plugin;

    public DuelListener(PrimeLeagueX1 plugin) {
        this.plugin = plugin;
    }

    /**
     * 🔥 Permite o ataque SOMENTE entre adversários no X1 e impede interferências externas.
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player atingido = (Player) event.getEntity();
        Player atacante = null;

        if (event.getDamager() instanceof Player) {
            atacante = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                atacante = (Player) proj.getShooter();
            }
        }

        if (atacante == null) return;

        boolean atacanteEmDuelo = DuelManager.estaEmDuelo(atacante.getName());
        boolean atingidoEmDuelo = DuelManager.estaEmDuelo(atingido.getName());

        // 🚫 Se o jogador atacado está em X1 e o atacante não, bloquear ataque
        if (atingidoEmDuelo && !atacanteEmDuelo) {
            atacante.sendMessage(MessageUtils.getMessage("noAttackDuringDuel").replace("{player}", atingido.getName()));
            event.setCancelled(true);
            return;
        }

        // 🚫 Se o atacante está em X1 e está atacando um jogador que não está, bloquear ataque
        if (atacanteEmDuelo && !atingidoEmDuelo) {
            atacante.sendMessage(MessageUtils.getMessage("noAttackNonDuelist"));
            event.setCancelled(true);
            return;
        }

        // 🔥 Permite ataque SOMENTE entre adversários no X1
        if (atacanteEmDuelo && atingidoEmDuelo) {
            String adversarioEsperado = DuelManager.getAdversario(atacante.getName());

            if (!atingido.getName().equals(adversarioEsperado)) {
                atacante.sendMessage(MessageUtils.getMessage("onlyAttackOpponent").replace("{opponent}", adversarioEsperado));
                event.setCancelled(true);
            }
        }
    }

    /**
     * 🔥 Finaliza o duelo corretamente quando um jogador se desconecta.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player perdedor = event.getPlayer();

        if (DuelManager.estaEmDuelo(perdedor.getName())) {
            String adversarioNome = DuelManager.getAdversario(perdedor.getName());
            Player vencedor = adversarioNome != null ? Bukkit.getPlayer(adversarioNome) : null;

            if (vencedor != null) {
                vencedor.sendMessage(MessageUtils.getMessage("victoryOnDisconnect").replace("{player}", perdedor.getName()));
                DuelManager.finalizarDuelo(vencedor, perdedor);
            }
        }
    }

    /**
     * 🚀 Impede que projéteis de jogadores externos atinjam duelistas.
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;

        Player atirador = (Player) event.getEntity().getShooter();

        if (!DuelManager.estaEmDuelo(atirador.getName())) {
            event.getEntity().remove();
        }
    }

    /**
     * 🚫 Impede que jogadores dropem itens dentro do duelo.
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (DuelManager.estaEmDuelo(player.getName())) {
            event.setCancelled(true);
            player.sendMessage(MessageUtils.getMessage("noDropItemsDuringDuel"));
        }
    }

    /**
     * 🔥 Finaliza o duelo corretamente quando um jogador morre.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player perdedor = event.getEntity();

        if (DuelManager.estaEmDuelo(perdedor.getName())) {
            String adversarioNome = DuelManager.getAdversario(perdedor.getName());
            Player vencedor = adversarioNome != null ? Bukkit.getPlayer(adversarioNome) : null;

            if (vencedor != null) {
                vencedor.sendMessage(MessageUtils.getMessage("victoryAgainstPlayer").replace("{player}", perdedor.getName()));
                DuelManager.finalizarDuelo(vencedor, perdedor);
            }
        }
    }
}