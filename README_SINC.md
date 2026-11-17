# Compilação e Execução — Módulos de Sincronização

## Pré-requisitos
- Java JDK 8 ou superior no PATH

---

## server_assistente_prova

### Compilar
```bat
cd server_assistente_prova
mkdir build 2>nul
javac -encoding UTF-8 -d build src\serversinc\*.java
```

### Empacotar JAR
```bat
jar cfm servidor_sinc.jar manifest_sinc.mf -C build .
```

### Executar
```bat
java -jar servidor_sinc.jar
```
> O servidor cria automaticamente a pasta `sinc_arquivos\` e o arquivo `sinc_registro.txt`
> no diretório onde o JAR é executado.

---

## cliente_assistente_prova

### Compilar
```bat
cd cliente_assistente_prova
mkdir build 2>nul
javac -encoding UTF-8 -d build src\clientesinc\*.java
```

### Empacotar JAR
```bat
jar cfm cliente_sinc.jar manifest_sinc.mf -C build .
```

### Executar
```bat
java -jar cliente_sinc.jar
```
> O cliente descobre o servidor automaticamente via UDP broadcast.
> Monitora `C:\Users\{usuario}\Documents` recursivamente.

---

## Notas
- Ambos os módulos usam **apenas a biblioteca padrão do Java (JRE 8)** — nenhuma dependência externa.
- O `server_assistente_prova` deve rodar no mesmo computador que o `ServerCliente_13_12`
  (servidor principal), mas como processo completamente independente.
- Portas utilizadas: `12347/TCP` (sincronização) e `8889/UDP` (descoberta automática).

