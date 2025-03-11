    package me.freitas.x1.commands;

    import me.freitas.x1.managers.StatsManager;
    import me.freitas.x1.managers.ArenaManager;
    import me.freitas.x1.managers.ChallengeManager;
    import me.freitas.x1.managers.DuelManager;
    import org.bukkit.Location;
    import org.bukkit.Bukkit;
    import org.bukkit.ChatColor;
    import org.bukkit.command.Command;
    import org.bukkit.command.CommandExecutor;
    import org.bukkit.command.CommandSender;
    import org.bukkit.entity.Player;
    import org.bukkit.Material;
    import org.bukkit.event.Listener;
    import org.bukkit.event.inventory.InventoryCloseEvent;
    import org.bukkit.inventory.Inventory;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.inventory.meta.ItemMeta;
    import org.bukkit.event.EventHandler;
    import org.bukkit.event.inventory.InventoryClickEvent;

    import java.util.HashMap;
    import java.util.Map;
    import java.util.Arrays;

    public class X1Command implements CommandExecutor, Listener {
        private static final Map<String, String> desafiosPendentes = new HashMap<String, String>();

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Apenas jogadores podem usar esse comando!");
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 0) {
                exibirAjuda(player);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "desafiar":
                    desafiarJogador(player, args);
                    break;
                case "aceitar":
                    aceitarDesafio(player);
                    break;
                case "recusar":
                    recusarDesafio(player);
                    break;
                case "cancelar":
                    cancelarDesafio(player);
                    break;
                case "stats":
                    exibirEstatisticas(player);
                    break;
                case "top":
                    exibirRanking(player);
                    break;
                case "set":
                    configurarArena(player, args);
                    break;
                case "camarote":
                    teleportarCamarote(player);
                    break;
                case "revanche":
                    solicitarRevanche(player);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Comando inválido! Use /x1 para ver os comandos disponíveis.");
            }

            return true;
        }

        private void desafiarJogador(Player player, String[] args) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Uso correto: /x1 desafiar <jogador>");
                return;
            }

            String desafianteNome = player.getName();
            String alvoNome = args[1];

            // ✅ **Impede abrir o menu se já tiver um desafio pendente**
            if (desafiosPendentes.containsKey(desafianteNome) || desafiosPendentes.containsValue(alvoNome)) {
                player.sendMessage(ChatColor.RED + "⚠ Você já desafiou " + alvoNome + "! Aguarde a resposta.");
                return;
            }

            // ✅ **Verifica no ChallengeManager se o desafio realmente foi cancelado**
            if (ChallengeManager.temDesafioAtivo(player)) {
                player.sendMessage(ChatColor.RED + "⚠ Você já tem um desafio pendente. Aguarde antes de desafiar novamente.");
                return;
            }

            Player target = Bukkit.getPlayer(alvoNome);
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "O jogador " + alvoNome + " não está online.");
                return;
            }

            if (desafianteNome.equalsIgnoreCase(alvoNome)) {
                player.sendMessage(ChatColor.RED + "Você não pode desafiar a si mesmo para um X1!");
                return;
            }

            if (DuelManager.estaEmDuelo(desafianteNome) || DuelManager.estaEmDuelo(alvoNome)) {
                player.sendMessage(ChatColor.RED + "Você ou seu adversário já estão em um X1!");
                return;
            }

            // ✅ **Agora o GUI só abre se o desafio foi realmente registrado**
            desafiosPendentes.put(desafianteNome, alvoNome);
            abrirMenuSelecao(player, target);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            Player player = (Player) event.getPlayer();
            Inventory menu = event.getInventory();

            // Verifica se o menu fechado é o de seleção do X1
            if (menu.getTitle().equals(ChatColor.DARK_BLUE + "Usar arena?")) {
                if (desafiosPendentes.containsKey(player.getName())) {
                    desafiosPendentes.remove(player.getName());
                    player.sendMessage(ChatColor.RED + "❌ Você fechou o menu do X1. Seu desafio foi cancelado.");
                }
            }
        }

        /**
         * Abre um menu para o jogador escolher entre X1 na Arena ou no Local Atual.
         */
        private void abrirMenuSelecao(Player player, Player desafiado) {
            Inventory menu = Bukkit.createInventory(null, 9, ChatColor.DARK_BLUE + "Usar arena?");

            // Lã verde para "Sim" (Arena Fixa)
            ItemStack simItem = new ItemStack(Material.WOOL, 1, (short) 5); // Cor verde
            ItemMeta simMeta = simItem.getItemMeta();
            if (simMeta != null) {
                simMeta.setDisplayName(ChatColor.GREEN + "✔ Sim");
                simMeta.setLore(Arrays.asList(ChatColor.YELLOW + "O duelo acontecerá na arena do servidor!"));
                simItem.setItemMeta(simMeta);
            }

            // Lã vermelha para "Não" (Local Atual)
            ItemStack naoItem = new ItemStack(Material.WOOL, 1, (short) 14); // Cor vermelha
            ItemMeta naoMeta = naoItem.getItemMeta();
            if (naoMeta != null) {
                naoMeta.setDisplayName(ChatColor.RED + "✖ Não");
                naoMeta.setLore(Arrays.asList(ChatColor.YELLOW + "O duelo acontecerá onde vocês estão!"));
                naoItem.setItemMeta(naoMeta);
            }

            menu.setItem(3, simItem);
            menu.setItem(5, naoItem);

            desafiosPendentes.put(player.getName(), desafiado.getName());

            player.openInventory(menu);
        }

        /**
         * Captura cliques no menu de seleção do X1 e impede que os itens sejam movidos.
         */
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Player player = (Player) event.getWhoClicked();
            Inventory menu = event.getInventory();

            // Verifica se o menu aberto é o de seleção do X1
            if (menu.getTitle().equals(ChatColor.DARK_BLUE + "Usar arena?")) {
                event.setCancelled(true); // Impede que o jogador remova os itens do menu

                ItemStack item = event.getCurrentItem();
                if (item == null || !item.hasItemMeta()) return;

                String desafiadoNome = desafiosPendentes.get(player.getName());
                if (desafiadoNome == null) return;

                Player desafiado = Bukkit.getPlayer(desafiadoNome);
                if (desafiado == null) {
                    player.sendMessage(ChatColor.RED + "❌ O jogador desafiado saiu do jogo!");
                    desafiosPendentes.remove(player.getName());
                    return;
                }

                // Verifica a opção escolhida
                if (item.getType() == Material.WOOL) {
                    short data = item.getDurability();

                    if (data == 5) { // Lã verde → Arena Fixa
                        player.sendMessage(ChatColor.GREEN + "✅ Você escolheu a Arena Fixa!");
                        ChallengeManager.desafiar(player, desafiado, true);
                    } else if (data == 14) { // Lã vermelha → Local Atual
                        player.sendMessage(ChatColor.YELLOW + "✅ Você escolheu o Local Atual!");
                        ChallengeManager.desafiar(player, desafiado, false);
                    }

                    desafiosPendentes.remove(player.getName()); // Remove o desafio pendente
                    player.closeInventory(); // Fecha o menu após a escolha
                }
            }
        }

        private void aceitarDesafio(Player player) {
            if (DuelManager.estaEmDuelo(player.getName())) {
                player.sendMessage(ChatColor.RED + "Você já está em um X1!");
                return;
            }

            ChallengeManager.aceitar(player);
        }

        private void recusarDesafio(Player player) {
            ChallengeManager.recusar(player);
        }

        private void cancelarDesafio(Player player) {
            if (!ChallengeManager.temDesafioAtivo(player)) {
                player.sendMessage(ChatColor.RED + "Você não tem desafios pendentes para cancelar.");
                return;
            }

            ChallengeManager.cancelarDesafio(player);
        }

        private void configurarArena(Player player, String[] args) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Uso correto: /x1 set pos1|pos2|camarote");
                return;
            }

            String tipo = args[1].toLowerCase();
            switch (tipo) {
                case "pos1":
                    ArenaManager.setPos1(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "✔ Posição 1 da arena definida com sucesso!");
                    break;
                case "pos2":
                    ArenaManager.setPos2(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "✔ Posição 2 da arena definida com sucesso!");
                    break;
                case "camarote":
                    ArenaManager.setCamarote(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "✔ Local do camarote definido com sucesso!");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Opção inválida! Use /x1 set pos1|pos2|camarote.");
            }
        }

        private void teleportarCamarote(Player player) {
            Location camarote = ArenaManager.getCamarote();
            if (camarote == null) {
                player.sendMessage(ChatColor.RED + "❌ O local do camarote ainda não foi configurado!");
                return;
            }

            player.teleport(camarote);
            player.sendMessage(ChatColor.YELLOW + "🚀 Você foi teleportado para o camarote para assistir aos duelos!");
        }

        private void solicitarRevanche(Player player) {
            player.sendMessage(ChatColor.YELLOW + "🔄 Sistema de revanche ainda será implementado!");
        }

        private void exibirRanking(Player player) {
            Map<String, Integer> topPlayers = StatsManager.getTopVitorias();
            if (topPlayers == null || topPlayers.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Ainda não há jogadores no ranking.");
                return;
            }

            player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage(ChatColor.GREEN + "🏆 RANKING DOS MELHORES JOGADORES 🏆");
            player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

            int posicao = 1;
            for (Map.Entry<String, Integer> entry : topPlayers.entrySet()) {
                player.sendMessage(ChatColor.YELLOW + "#" + posicao + " " + ChatColor.AQUA + entry.getKey() +
                        ChatColor.GRAY + " - " + ChatColor.GREEN + entry.getValue() + " Vitórias");
                posicao++;
            }
            player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        }

        private void exibirEstatisticas(Player player) {
            String nome = player.getName();
            int vitorias = StatsManager.getVitorias(nome);
            int derrotas = StatsManager.getDerrotas(nome);
            int streak = StatsManager.getStreak(nome);
            int maiorStreak = StatsManager.getMaiorStreak(nome);
            int totalJogos = StatsManager.getTotalJogos(nome);

            player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage(ChatColor.GREEN + "📊 SUAS ESTATÍSTICAS DE X1 📊");
            player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage(ChatColor.YELLOW + "🏆 Vitórias: " + ChatColor.GREEN + vitorias);
            player.sendMessage(ChatColor.YELLOW + "💀 Derrotas: " + ChatColor.RED + derrotas);
            player.sendMessage(ChatColor.YELLOW + "🔥 Streak Atual: " + ChatColor.AQUA + streak);
            player.sendMessage(ChatColor.YELLOW + "🏅 Maior Streak: " + ChatColor.GOLD + maiorStreak);
            player.sendMessage(ChatColor.YELLOW + "🎮 Total de X1 Jogados: " + ChatColor.BLUE + totalJogos);
            player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        }

        private void exibirAjuda(Player player) {
            player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage(ChatColor.GREEN + " 📜 Comandos do X1 📜");
            player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

            // Comandos disponíveis para todos os jogadores
            player.sendMessage(ChatColor.YELLOW + "/x1 desafiar <jogador> [arena/local] - Desafie um jogador para um X1.");
            player.sendMessage(ChatColor.YELLOW + "/x1 aceitar - Aceite um desafio de X1.");
            player.sendMessage(ChatColor.YELLOW + "/x1 recusar - Recuse um desafio de X1.");
            player.sendMessage(ChatColor.YELLOW + "/x1 cancelar - Cancele um desafio pendente.");
            player.sendMessage(ChatColor.YELLOW + "/x1 stats - Veja suas estatísticas de X1.");
            player.sendMessage(ChatColor.YELLOW + "/x1 top - Veja o ranking dos melhores duelistas.");
            player.sendMessage(ChatColor.YELLOW + "/x1 camarote - Vá para o camarote e assista aos duelos.");

            // Se o jogador for administrador, mostra os comandos avançados
            if (player.hasPermission("primeleaguex1.admin")) {
                player.sendMessage(ChatColor.RED + "⚙ Comandos de Administração:");
                player.sendMessage(ChatColor.RED + "/x1 set pos1 - Define a posição 1 da arena.");
                player.sendMessage(ChatColor.RED + "/x1 set pos2 - Define a posição 2 da arena.");
                player.sendMessage(ChatColor.RED + "/x1 set camarote - Define a posição do camarote.");
                player.sendMessage(ChatColor.RED + "/x1 reload - Recarrega as configurações do plugin.");
            }

            player.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        }

    }