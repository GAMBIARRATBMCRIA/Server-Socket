# Plano de Refatoração (Opção B): Refatoração Total com Protocolo Único de Pacotes

Este plano descreve a mudança arquitetural **total** da camada de rede. O objetivo é modernizar a infraestrutura para suportar pacotes binários (preparando o terreno para streaming de vídeo/acesso remoto em tempo real).

## 1. Padronização de Controle Simultâneo de Vídeo
* Ao disparar o controle/visualização simultânea, o comando enviado para o grupo instruirá os clientes a travarem suas resoluções em um tamanho base (ex: 1024x768) ou forçará a compressão das imagens capturadas para esse tamanho antes do envio. Isso garantirá a uniformidade dos cliques do mouse.

## 2. Mudança de Protocolo de Rede (Obrigatório)
**Contexto:** Transição da leitura/escrita baseada em texto (`BufferedReader.readLine()`) para um Protocolo Baseado em Pacotes puros usando `DataInputStream` e `DataOutputStream`.

Todo envio seguirá uma estrutura estrita em bytes:
* `[TIPO DO PACOTE - int de 4 bytes]` (Ex: 1 = Comando, 2 = Arquivo, 3 = Vídeo, 4 = Mouse/Teclado)
* `[TAMANHO DO PAYLOAD - int de 4 bytes]`
* `[PAYLOAD - array de bytes]`

## 3. Refatoração do Cliente (Sistema de Módulos)
A grande cadeia de `if-else if` da classe `ExecutarComando.java` será desmembrada usando o **Command Pattern**.

1. **`IModulo.java`**: Interface com os métodos `String getIdentificador()` e `void executar(byte[] payload, ArmazenarInstancias inst)`.
2. **`ModuloRegistry.java`**: Registra todos os módulos disponíveis (`ModuloCmd`, `ModuloServicos`, etc). Ao chegar um pacote do Tipo 1 (Texto), ele faz o roteamento para o módulo adequado.
3. **Módulo de Acesso Remoto (`ModuloRemoteDesktop`)**: Quando ativado, captura a tela (via `java.awt.Robot`), redimensiona (se solicitado pelo grupo), empacota no formato binário (Tipo 3) e envia para o socket atual.

## 4. Refatoração do Servidor (Reescrita do I/O)
* **Reescrita do Envio:** Modificar TODOS os métodos de `ExecucaoAtividadesServer.java` (`enviarComando`, `enviarMensagemTexto`, `enviarArquivo`) para envelopar os dados no formato Binário `[TIPO][TAMANHO][DADOS]`.
* **Reescrita do Recebimento:** `ComunicacaoClienteServer.java` mudará seu loop infinito para ler bytes com `DataInputStream`. Ele terá um roteador que:
   - Se o pacote for de Texto (Status de serviço, Logs), despacha para a interface de log/chat.
   - Se o pacote for de Vídeo (Tipo 3), despacha a matriz de bytes para a Janela/Grid de Monitoramento que os renderizará.
