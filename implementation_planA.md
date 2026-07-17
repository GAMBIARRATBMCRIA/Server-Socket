# Plano de Refatoração (Opção A): Duas Conexões (Texto / Vídeo Independente)

Este plano descreve a abordagem de refatoração de baixo impacto. Ele preserva o funcionamento perfeito do código legado de texto (comandos, bloqueios) e injeta uma nova estrutura robusta (porta secundária) exclusiva para os Módulos de Vídeo e Controle.

## 1. Padronização de Controle Simultâneo de Vídeo
* Ao disparar o controle simultâneo, o servidor envia um comando (via socket de texto atual) instruindo o grupo a travar a resolução (ex: 1024x768) na placa de vídeo, ou realizar o *resize* da imagem capturada localmente para esse tamanho. Assim, um clique no pixel (100,100) do painel de controle refletirá no local exato em todos os PCs.

## 2. Refatoração do Cliente (Sistema de Módulos no Código de Texto)
A grande cadeia de `if-else if` da classe `ExecutarComando.java` será desmembrada para permitir a adição de novas funcionalidades (módulos) sem tocar no núcleo do programa. Usaremos o **Command Pattern**.

1. **`IModulo.java`**: Interface com os métodos `boolean podeProcessar(String comando)` e `void executar(String comando, ArmazenarInstancias contexto)`.
2. **`ModuloRegistry.java`**: Classe que carrega os módulos (`ModuloCmd`, `ModuloBloqueio`, `ModuloServicos`, `ModuloRemoteDesktop`). 
3. **O Despacho:** O arquivo `ExecutarComando.java` apenas iterará sobre os módulos perguntando qual sabe processar a string recebida.

## 3. O Novo Módulo de Acesso Remoto (Segunda Conexão)
* O `ModuloRemoteDesktop` do cliente, quando acionado pelo socket de texto, não enviará imagens pela mesma conexão.
* Ele iniciará uma Thread separada que abre um novo Socket e conecta na porta de Vídeo do Servidor (Ex: 5001).
* Esta segunda conexão usará um fluxo puramente binário (`DataOutputStream`) para enviar JPEGs (telas) em alta frequência, isolando o tráfego pesado e garantindo que comandos de bloqueio/desbloqueio do socket de texto não sofram atrasos (bottlenecks).
* A leitura dos eventos de mouse também se dará via input stream deste socket secundário.

## 4. Ajustes no Servidor
* A classe `ExecucaoAtividadesServer` e a `ComunicacaoClienteServer` continuarão funcionando normalmente, sem precisar de alterações na base de comunicação via string.
* Criaremos um **novo ServerSocket (Porta 5001)** dedicado.
* Este novo servidor terá um ouvinte que, ao receber conexões, criará as sessões de vídeo, desenhando-as num painel (Grid) para visualização em tempo real de várias máquinas ao mesmo tempo.
