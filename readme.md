# 📌 PrimeLeagueX1 - Sistema de X1 para Servidores de Minecraft 1.5.2

## 📖 Sobre o Projeto

**PrimeLeagueX1** é um plugin desenvolvido para servidores de Minecraft 1.5.2, adicionando um sistema completo de duelos **X1**. O plugin permite que jogadores se desafiem para combates PvP em uma **arena fixa** ou no **local atual** do servidor. Ele também inclui **estatísticas**, **ranking**, **modo espectador**, **expiração de desafios**, e muito mais!

## 🛠️ Funcionalidades
- **📌 Duelo X1** - Desafie jogadores para batalhas PvP em arena fixa ou no local atual.
- **🏆 Ranking & Estatísticas** - Acompanhe vitórias, derrotas e streaks.
- **⏳ Expiração de Desafios** - Convites de X1 expiram após um tempo determinado.
- **👁️ Modo Espectador** - Staffs podem acompanhar os duelos.
- **⚙️ Configuração de Arena** - Admins podem definir a posição da arena.
- **🔄 Sistema de Revanche** - Após um duelo, jogadores podem solicitar revanche.
- **🚀 Integração com Economy (Futuro)** - Permitir apostas em X1.

## 📦 Instalação
1. Baixe o arquivo **PrimeLeagueX1.jar**
2. Adicione o arquivo à pasta `plugins/` do seu servidor.
3. Reinicie o servidor.
4. Configure as permissões e arena utilizando os comandos disponíveis.

## 💻 Comandos e Permissões

### 🎮 Comandos para Jogadores:
| Comando | Descrição |
|---------|------------|
| `/x1 desafiar <jogador>` | Desafia um jogador para um X1 |
| `/x1 aceitar` | Aceita um desafio pendente |
| `/x1 recusar` | Recusa um desafio pendente |
| `/x1 cancelar` | Cancela um desafio enviado |
| `/x1 stats` | Exibe as estatísticas do jogador |
| `/x1 top` | Mostra o ranking de melhores jogadores |
| `/x1 camarote` | Vai para o modo espectador |

### ⚙️ Comandos para Administradores:
| Comando | Descrição |
|---------|------------|
| `/x1 set pos1` | Define a posição inicial da arena |
| `/x1 set pos2` | Define a posição final da arena |
| `/x1 set camarote` | Define a área de espectadores |
| `/x1 reload` | Recarrega as configurações do plugin |

## 🔑 Permissões
| Permissão | Descrição |
|-------------|------------|
| `primeleaguex1.use` | Permite usar comandos de X1 |
| `primeleaguex1.admin` | Acesso aos comandos administrativos |

## 📂 Estrutura do Código
### 📌 Classes Principais:
- `X1Command.java` → Gerencia os comandos do plugin.
- `ChallengeManager.java` → Controla os desafios de X1.
- `DuelManager.java` → Gerencia o início e término dos duelos.
- `StatsManager.java` → Registra e exibe as estatísticas dos jogadores.
- `ArenaManager.java` → Configuração e gerenciamento das arenas de X1.
- `DuelListener.java` → Listener para eventos dentro dos duelos.

## 🛠️ Como Contribuir
1. **Faça um fork** deste repositório.
2. **Crie uma branch** (`git checkout -b minha-feature`)
3. **Commit suas mudanças** (`git commit -m 'Adicionei um novo recurso'`)
4. **Faça push para a branch** (`git push origin minha-feature`)
5. **Crie um Pull Request** 🚀

## 📜 Licença
Este projeto é de código aberto e licenciado sob a [MIT License](LICENSE).

## ✉ Contato
Caso tenha dúvidas ou sugestões, entre em contato pelo Discord ou envie um Pull Request!
=======
# 📌 PrimeLeagueX1 - Sistema de X1 para Servidores de Minecraft 1.5.2

## 📖 Sobre o Projeto

**PrimeLeagueX1** é um plugin desenvolvido para servidores de Minecraft 1.5.2, adicionando um sistema completo de duelos **X1**. O plugin permite que jogadores se desafiem para combates PvP em uma **arena fixa** ou no **local atual** do servidor. Ele também inclui **estatísticas**, **ranking**, **modo espectador**, **expiração de desafios**, e muito mais!

## 🛠️ Funcionalidades
- **📌 Duelo X1** - Desafie jogadores para batalhas PvP em arena fixa ou no local atual.
- **🏆 Ranking & Estatísticas** - Acompanhe vitórias, derrotas e streaks.
- **⏳ Expiração de Desafios** - Convites de X1 expiram após um tempo determinado.
- **👁️ Modo Espectador** - Staffs podem acompanhar os duelos.
- **⚙️ Configuração de Arena** - Admins podem definir a posição da arena.
- **🔄 Sistema de Revanche** - Após um duelo, jogadores podem solicitar revanche.
- **🚀 Integração com Economy (Futuro)** - Permitir apostas em X1.

## 📦 Instalação
1. Baixe o arquivo **PrimeLeagueX1.jar**
2. Adicione o arquivo à pasta `plugins/` do seu servidor.
3. Reinicie o servidor.
4. Configure as permissões e arena utilizando os comandos disponíveis.

## 💻 Comandos e Permissões

### 🎮 Comandos para Jogadores:
| Comando | Descrição |
|---------|------------|
| `/x1 desafiar <jogador>` | Desafia um jogador para um X1 |
| `/x1 aceitar` | Aceita um desafio pendente |
| `/x1 recusar` | Recusa um desafio pendente |
| `/x1 cancelar` | Cancela um desafio enviado |
| `/x1 stats` | Exibe as estatísticas do jogador |
| `/x1 top` | Mostra o ranking de melhores jogadores |
| `/x1 camarote` | Vai para o modo espectador |

### ⚙️ Comandos para Administradores:
| Comando | Descrição |
|---------|------------|
| `/x1 set pos1` | Define a posição inicial da arena |
| `/x1 set pos2` | Define a posição final da arena |
| `/x1 set camarote` | Define a área de espectadores |
| `/x1 reload` | Recarrega as configurações do plugin |

## 🔑 Permissões
| Permissão | Descrição |
|-------------|------------|
| `primeleaguex1.use` | Permite usar comandos de X1 |
| `primeleaguex1.admin` | Acesso aos comandos administrativos |

## 📂 Estrutura do Código
### 📌 Classes Principais:
- `X1Command.java` → Gerencia os comandos do plugin.
- `ChallengeManager.java` → Controla os desafios de X1.
- `DuelManager.java` → Gerencia o início e término dos duelos.
- `StatsManager.java` → Registra e exibe as estatísticas dos jogadores.
- `ArenaManager.java` → Configuração e gerenciamento das arenas de X1.
- `DuelListener.java` → Listener para eventos dentro dos duelos.

## 🛠️ Como Contribuir
1. **Faça um fork** deste repositório.
2. **Crie uma branch** (`git checkout -b minha-feature`)
3. **Commit suas mudanças** (`git commit -m 'Adicionei um novo recurso'`)
4. **Faça push para a branch** (`git push origin minha-feature`)
5. **Crie um Pull Request** 🚀

## 📜 Licença
Este projeto é de código aberto e licenciado sob a [MIT License](LICENSE).

## ✉ Contato
Caso tenha dúvidas ou sugestões, entre em contato pelo Discord ou envie um Pull Request!

>>>>>>> 7b92382 (🔥 Atualizações no PrimeLeagueX1:)
