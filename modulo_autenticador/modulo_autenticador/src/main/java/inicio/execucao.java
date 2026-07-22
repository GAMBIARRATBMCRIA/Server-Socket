/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inicio;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.SwingWorker;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

/**
 *
 * @author LAB01
 */
public class execucao extends SwingWorker<Void, String> {

//    @Override
//    protected Void doInBackground() throws Exception {
//        String username, password, link;
//
//        username = tela.user.getText();
//        password = tela.senha.getText();
//        link = tela.link.getText();
//    
//        // List<String> redirectUrls = new ArrayList<>();
//        // Configuração do HttpClient
//        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//            HttpPost post = new HttpPost("https://login.ufpicafs.ufpi.br:6082/php/uid.php?vsys=1&rule=0&url=" + link);
//            System.out.println("mandado para:" + "https://login.ufpicafs.ufpi.br:6082/php/uid.php?vsys=1&rule=0&url=" + link);
//
//            // Configuração de timeout
//            RequestConfig requestConfig = RequestConfig.custom()
//                    .setConnectTimeout(5000) // Tempo de conexão
//                    .setSocketTimeout(5000) // Tempo de resposta
//                    .build();
//            post.setConfig(requestConfig);
//
//            // Configuração do corpo da requisição
//            String formData = String.format(
//                    "inputStr=&escapeUser=&preauthid=&user=%s&passwd=%s&ok=Login",
//                    username, password
//            );
//            post.setEntity(new StringEntity(formData));
//
//            // Configurar cabeçalhos para o formulário
//            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
//
//            // Executar a requisição 
//            System.out.println("enviando....");
//            HttpResponse response = httpClient.execute(post);
//
//            // Seguir redirecionamentos e armazenar URLs
//            boolean verificado = false;
//            while ((response.getStatusLine().getStatusCode() == 301
//                    || response.getStatusLine().getStatusCode() == 302) && verificado == false) {
//
//                System.out.println("redirecionado");
//
//                String location = response.getFirstHeader("Location").getValue();
//                //redirectUrls.add(location);  // Adicionar URL redirecionada à lista
//                //publish("Redirecionado para: " + location+"\n");
//                System.out.println("location:" + location);
//                if (location.contains(link)) {
//                    tela.texto.append("\n" + location);
//                    System.out.println("autenticado na rede");
//                    tela.mensagemlabel.setBackground(new java.awt.Color(255, 153, 51));
//
//                    //testando conexão com youtube para verificar funcionamento da internet
//                    publish("\nVerificando funcionamento da internet");
//
//                    HttpGet get = new HttpGet("https://www.youtube.com/");
//                    response = httpClient.execute(get);
//
//                    int statusCode = response.getStatusLine().getStatusCode();
//                    System.out.println("Status code: " + statusCode);
//
//                    if (statusCode == 200) {
//                        tela.mensagemlabel.setBackground(new java.awt.Color(0, 204, 0));
//                        verificado = true;
//                        publish("VERIFICAÇÃO FALHOU!!!");
//                    }
//
//                } else {
//                    publish("\nRedirecionado!");
//                    // Criar uma nova requisição para a URL redirecionada
//                    URI uri = new URI(location);
//                    HttpPost redirectPost = new HttpPost(uri);
//                    redirectPost.setEntity(new StringEntity(formData));
//                    redirectPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
//
//                    // Executar a requisição para a URL redirecionada
//                    response = httpClient.execute(redirectPost);
//                }
//
//                HttpGet get = new HttpGet("https://www.youtube.com/");
//                response = httpClient.execute(get);
//
//                int statusCode = response.getStatusLine().getStatusCode();
//                System.out.println("Status code: " + statusCode);
//
//                if (statusCode == 200) {
//                    tela.mensagemlabel.setBackground(new java.awt.Color(0, 204, 0));
//                    verificado = true;
//                    publish("VERIFICAÇÃO FALHOU!!!");
//                }
//
//            }
//
//        } catch (Exception e) {
//            publish("\nErr0:" + e.getMessage());
//            System.out.println("erro:" + e.getMessage());
//            if (e.getMessage().equalsIgnoreCase("Read timed out")) {
//                publish("AUTENTICAÇÃO FALHOU!!!");
//                tela.senha.setBackground(new java.awt.Color(255, 51, 51));
//                tela.user.setBackground(new java.awt.Color(255, 51, 51));
//            }
//        }
//        return null;
//    }
    @Override
    protected Void doInBackground() throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial((chain, authType) -> true) // Aceitar todos os certificados
                .build();

        // Criar o HttpClient com SSL desativado
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE) // Ignorar verificação de hostname
                .build();

        // Resto do seu código que utiliza o `httpClient`...
        String username, password, link;

        username = tela.user.getText();
        password = tela.senha.getText();
        link = tela.link.getText();

        try {
            HttpPost post = new HttpPost("https://login.ufpicafs.ufpi.br:6082/php/uid.php?vsys=1&rule=0&url=" + link);
            System.out.println("mandado para:" + "https://login.ufpicafs.ufpi.br:6082/php/uid.php?vsys=1&rule=0&url=" + link);

            // Configuração de timeout
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000) // Tempo de conexão
                    .setSocketTimeout(5000) // Tempo de resposta
                    .build();
            post.setConfig(requestConfig);

            // Configuração do corpo da requisição
            String formData = String.format(
                    "inputStr=&escapeUser=&preauthid=&user=%s&passwd=%s&ok=Login",
                    username, password
            );
            post.setEntity(new StringEntity(formData));

            // Configurar cabeçalhos para o formulário
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            // Executar a requisição 
            System.out.println("enviando....");
            HttpResponse response = httpClient.execute(post);
            // Seguir redirecionamentos e armazenar URLs
            boolean verificado = false;
            while ((response.getStatusLine().getStatusCode() == 301
                    || response.getStatusLine().getStatusCode() == 302) && verificado == false) {

                System.out.println("redirecionado");

                String location = response.getFirstHeader("Location").getValue();
                //redirectUrls.add(location);  // Adicionar URL redirecionada à lista
                //publish("Redirecionado para: " + location+"\n");
                System.out.println("location:" + location);
                if (location.equalsIgnoreCase(link)) {
                    tela.texto.append("\n" + location);
                    System.out.println("autenticado na rede");
                    tela.mensagemlabel.setBackground(new java.awt.Color(255, 153, 51));

                    //testando conexão com youtube para verificar funcionamento da internet
                    publish("\nVerificando funcionamento da internet");

                    HttpGet get = new HttpGet("https://www.youtube.com/");
                    response = httpClient.execute(get);

                    int statusCode = response.getStatusLine().getStatusCode();
                    System.out.println("Status code: " + statusCode);

                    if (statusCode == 200) {
                        tela.mensagemlabel.setBackground(new java.awt.Color(0, 204, 0));
                        verificado = true;
                        publish("VERIFICAÇÃO FALHOU!!!");
                    }

                } else {
                    publish("\nRedirecionado!");
                    // Criar uma nova requisição para a URL redirecionada
                    URI uri = new URI(location);
                    HttpPost redirectPost = new HttpPost(uri);
                    redirectPost.setEntity(new StringEntity(formData));
                    redirectPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

                    // Executar a requisição para a URL redirecionada
                    response = httpClient.execute(redirectPost);
                }

                HttpGet get = new HttpGet("https://www.youtube.com/");
                response = httpClient.execute(get);

                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("Status code: " + statusCode);

                if (statusCode == 200) {
                    tela.mensagemlabel.setBackground(new java.awt.Color(0, 204, 0));
                    verificado = true;
                    publish("VERIFICAÇÃO FALHOU!!!");
                }
            }
        } catch (Exception e) {
            publish("\nErro:" + e.getMessage());
            System.out.println("erro:" + e.getMessage());
            if (e.getMessage().equalsIgnoreCase("Read timed out")) {
                publish("AUTENTICAÇÃO FALHOU!!!");
                tela.senha.setBackground(new java.awt.Color(255, 51, 51));
                tela.user.setBackground(new java.awt.Color(255, 51, 51));
            }
        }
        return null;
    }

    @Override
    protected void process(List<String> chunks) {
        for (String message : chunks) {
            tela.texto.append(message + "\n");
        }
    }

    @Override
    protected void done() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(execucao.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }
}
