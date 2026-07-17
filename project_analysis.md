# Análise do Projeto: Controlador de PC Remoto

## 1. O que o projeto faz
O projeto é um sistema de administração remota focado em laboratórios de informática. Ele permite que um servidor central controle, gerencie e monitore múltiplos computadores (clientes) conectados na mesma rede local. As funcionalidades principais atuais incluem:
* **Execução Remota:** Rodar comandos no terminal (CMD/PowerShell) das máquinas clientes remotamente ou scripts em lote.
* **Comunicação:** Envio de mensagens de texto para avisos em tela cheia aos usuários.
* **Controle de Acesso:** Bloqueio e desbloqueio da tela do usuário e do acesso à internet via alteração no registro do Windows e DNS.
* **Monitoramento:** Listagem de serviços do Windows em execução.
* **Transferência de Dados:** Envio de arquivos do servidor para os clientes (verificando hash SHA-256).
* **Manutenção:** Atualização remota do software cliente e desligamento do próprio cliente.

## 2. Como ele trabalha
A arquitetura é do tipo Cliente-Servidor comunicando-se diretamente via sockets TCP.

### Servidor (`ServerTCP_MAC`)
* **Inicialização e Cadastro:** O servidor escuta portas TCP e possui uma interface Swing para a gerência. Ao receber uma conexão, ele valida o cliente baseado no endereço MAC contra uma base de texto (`lerTxt`).
* **Fluxo de Conexão:** Ao autenticar, uma Thread dedicada (`ComunicacaoClienteServer.java`) é levantada para cada máquina cliente. Esta thread lê e envia pacotes mantendo conexão persistente.
* **Despacho de Ações (`ExecucaoAtividadesServer.java`):** O operador dispara comandos na UI. O servidor pega a string de comando (por exemplo `"cmd: ipconfig"`, ou `"pr:bloqueioTela"`) e encaminha pelo `PrintWriter out` associado aos clientes alvo (podendo enviar para o Laboratório inteiro, Grupo/Setor, Bancada ou MAC específico).

### Cliente (`ClienteTCP_MAC_Maven`)
* **Self-Boot:** O cliente descobre o processo ativo, levanta, lê seu MAC Address (usado como ID), conecta no IP do servidor e envia seu MAC para autenticação.
* **Escuta Contínua (`ClienteTCP_MAC.java`):** Fica num loop `while(instancias.getSocket().isConnected())` aguardando strings via `BufferedReader in`. Caso comece com `tipodadoarquivo:`, intercepta para tratar arquivo binário. Senão, acumula numa lista e despacha.
* **Execução (`ExecutarComando.java`):** Para o processamento padrão, uma thread é criada para processar o texto e repassar o texto para os métodos alvo através de uma longa verificação condicional (`if (mensagem.contains("cmd:") ... else if ...)`).

---

## 3. Necessidades de Refatoração (Criação de Módulos)
Usando as diretrizes de desenvolvimento da skill `SKILL.md` (**Karpathy Guidelines**), identificamos problemas arquiteturais que impedem "Surgical Changes" (modificações cirúrgicas) e violam o "Simplicity First" caso quiséssemos escalar.

Para que **seja possível adicionar mais módulos** sem quebrar o que já existe (Princípio Aberto-Fechado / Open-Closed Principle), o seguinte precisa ser feito:

### A. Substituição do "If-Else" Monolítico por Padrão de Comandos (Command / Strategy Pattern)
* **Problema:** Em `ExecutarComando.java`, a linha 48 a 92 contém dezenas de checagens em cascata (Ex: `if(mensagemRecebida.contains("cmd:")) { executarCmd(...) }`). Se quisermos adicionar um módulo novo, somos obrigados a alterar o arquivo principal do programa, arriscando gerar bugs em funcionalidades que já estão prontas.
* **Solução:** 
  1. Definir uma **Interface** de módulo. Ex: `interface IModulo { boolean podeExecutar(String comando); void executar(String comando); }`.
  2. Cada funcionalidade (Comando CMD, Bloqueio de Tela, Manipulação de Internet, etc) virará uma classe separada implementando essa interface.
  3. No `ExecutarComando`, teremos apenas um *Registry* ou *Router*, que varre uma lista de módulos carregados e diz: "Quem sabe processar esse comando?". O módulo responde `true` e assume a execução. 
  4. Adicionar um módulo futuro agora significa **criar apenas 1 arquivo novo** e registrá-lo.

### B. Padronizar o Protocolo de Mensagens (Think Before Coding)
* **Problema:** A forma que o Servidor avisa o cliente é muito acoplada ao prefixo solto em String. Ora usa `cmd:`, ora usa `pr:`, ora usa `msgcliente:`. Quando mais módulos forem criados, mais bagunçado ficará.
* **Solução:** Antes de sair escrevendo módulos, definir um protocolo limpo de comunicação. Se não for possível usar JSON para simplificar (ex: `{"modulo": "Bloqueador", "acao": "Internet", "args": []}`), deve-se padronizar os delimitadores como `NOME_DO_MODULO|ACAO|DADOS_ADICIONAIS`.

### C. Desacoplamento da Lógica com a Tela no Servidor
* **Problema:** A lógica de envio no servidor e de feedback invoca diretamente os elementos estáticos da Interface de Usuário (`ExecucaoAtividadesTela.publicarAvido()`), tornando difícil gerar módulos que enviem seus próprios resultados customizados.
* **Solução:** Implementar eventos (Observer/Listener Pattern) na classe `ExecucaoAtividadesServer`, assim quando um módulo retornar um aviso, ele emite o evento e as telas cadastradas que o exibem, sem que as lógicas saibam a existência da interface (UI).
