package system.motoboy;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import system.mensagem.Message;
import system.receiver.Receiver;
import system.restaurante.Restaurante;
import system.sender.Sender;

public class Motoboy extends Receiver {

    private PublicKey chavePublicaRestaurante;
    private PrivateKey chavePrivadaPropria;
    private Sender sender;
    private static final Scanner scanner = new Scanner(System.in);

    private static final String ROUTINGKEY = "#.pedido_entregue";
    private static final List<String> BINDING_KEYS = Arrays.asList("#.pedido_despachado.#");

    public Motoboy() {
        super(BINDING_KEYS);
        getKeys();
        sender = new Sender();
        System.out.println();
        System.out.println("############################");
        System.out.println("##   Motoboy iniciado!    ##");
        System.out.println("############################");
    }

    private void getKeys() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(
                    new FileInputStream("keys" + File.separator + "motoboyKeys" + File.separator + "private.key"));
            chavePrivadaPropria = (PrivateKey) inputStream.readObject();
            inputStream.close();

            inputStream = new ObjectInputStream(
                    new FileInputStream("keys" + File.separator + "restauranteKeys" + File.separator + "public.key"));
            chavePublicaRestaurante = (PublicKey) inputStream.readObject();
            inputStream.close();
        } catch (Exception e) {
            System.err.println("Erro ao carregar chaves: " + e.getMessage());
        }
    }

    @Override
    public void processMessage(byte[] payload, String routingKey) {
        Message message = null;
        try {
            System.out.println("\n#################################\n");
            message = new Message(payload, chavePublicaRestaurante);
            System.out.println("\nPedido despachado recebido: " + message.getTexto() + "\n");
            System.out.println("Enviando pedido...");
            doWork(1000);

            sendConfirmation(message.getTexto(), ROUTINGKEY);
            System.out.println("\n#################################\n");
        } catch (RuntimeException e) {
            System.err.println("Erro de Assinatura na mensagem: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro na assinatura: " + e.getMessage());
        }
    }

    private void sendConfirmation(String message, String routingKey) {
        try {
            Message msg = new Message(message, chavePrivadaPropria);
            sender.send(msg.getPayload(), routingKey);
            System.out.println("Pedido entregue: " + message);
        } catch (Exception e) {
            System.err.println("Erro ao enviar confirmação: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Motoboy motoboy = new Motoboy();
        motoboy.init();
    }
}
