package system.restaurante;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import system.mensagem.Message;
import system.receiver.Receiver;
import system.tesouraria.Tesouraria;

import system.sender.Sender;

public class Restaurante extends Receiver {

    private PrivateKey chavePrivadaPropria;
    private PublicKey chavePublicaCliente;
    private static final List<String> BINDING_KEYS = Arrays.asList("#.pagamento_efetivado.#");
    private static final String ROUTINGKEY = "#.pedido_despachado";
    private Sender sender;

    public Restaurante() {
        super(BINDING_KEYS);
        getKeys();
        sender = new Sender();
        System.out.println();
        System.out.println("############################");
        System.out.println("##  Restaurante iniciado! ##");
        System.out.println("############################");
    }

    private void getKeys() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(
                    new FileInputStream("keys" + File.separator + "restauranteKeys" + File.separator + "private.key"));
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
            System.out.println("\nPagamento Efetivado recebido: " + message.getTexto() + "\n");
            System.out.println("Processando prato...");
            doWork();
            sendDish(message.getTexto(), ROUTINGKEY);
        } catch (InterruptedException e) {
            System.err.println("Erro ao processar pedido: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Erro de Assinatura na mensagem: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro na assinatura: " + e.getMessage());
        }

    }

    private void sendDish(String texto, String routingKey) {
        try {
            Message message = new Message(texto, chavePrivadaPropria);
            sender.send(message.getPayload(), routingKey);
            System.out.println("Prato despachado: " + texto);
        } catch (Exception e) {
            System.err.println("Erro ao enviar prato: " + e.getMessage());
        }
    }

    public void doWork() throws InterruptedException {
        Thread.sleep(3000); // Simulando trabalho
    }

    public static void main(String[] args) {
        Restaurante restaurante = new Restaurante();
        restaurante.init();
    }

}
