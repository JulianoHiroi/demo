package system.tesouraria;

import system.mensagem.Message;
import system.receiver.Receiver;
import system.sender.Sender;

import java.io.File;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Arrays;

public class Tesouraria extends Receiver {

    private Sender sender;
    PrivateKey chavePrivadaPropria;
    PublicKey chavePublicaCliente;

    private static final String ROUTINGKEY = "#.boleto_criado.#"; // Alterado para os tópicos para enviar mensagem
    private static final List<String> BINDING_KEYS = Arrays.asList("pedido_criado.#");

    public Tesouraria() {
        super(BINDING_KEYS);
        sender = new Sender();
        getKeys();
        System.out.println();
        System.out.println("############################");
        System.out.println("##  Tesouraria iniciada!  ##");
        System.out.println("############################");
    }

    // Método para carregar as chaves privadas e públicas
    private void getKeys() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(
                    new FileInputStream("keys" + File.separator + "tesourariaKeys" + File.separator + "private.key"));
            chavePrivadaPropria = (PrivateKey) inputStream.readObject();
            inputStream.close();

            inputStream = new ObjectInputStream(
                    new FileInputStream("keys" + File.separator + "clienteKeys" + File.separator + "public.key"));
            chavePublicaCliente = (PublicKey) inputStream.readObject();
            inputStream.close();
        } catch (Exception e) {
            System.err.println("Erro ao carregar chaves: " + e.getMessage());
        }
    }

    @Override
    public void processMessage(byte[] payload, String routingKey) {
        Message message = null;
        try {
            message = new Message(payload, chavePublicaCliente);
            System.out.println("\nPedido recebido: " + message.getTexto() + "\n");
            System.out.println("Processando pedido...");
            doWork();

            sendBilling(message.getTexto(), ROUTINGKEY);
        } catch (InterruptedException e) {
            System.err.println("Erro ao processar pedido: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Erro de Assinatura na mensagem: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro na assinatura: " + e.getMessage());
        }
    }

    public void doWork() throws InterruptedException {
        Thread.sleep(1000); // Simulando trabalho
    }

    // Método para enviar o boleto
    public void sendBilling(String pedido, String routingKey) {
        try {
            Message message = new Message(pedido, chavePrivadaPropria);
            sender.send(message.getPayload(), routingKey);
            System.out.println("Boleto enviado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao enviar boleto: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Tesouraria tesouraria = new Tesouraria();
        tesouraria.init();
    }
}
