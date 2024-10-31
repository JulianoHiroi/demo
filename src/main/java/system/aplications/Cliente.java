package system.aplications;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import system.resources.Message;
import system.resources.Receiver;
import system.resources.Sender;

import java.io.File;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.io.ObjectInputStream;

public class Cliente extends Receiver {

    private String name;
    private Sender sender;
    private static final Scanner scanner = new Scanner(System.in);
    private PrivateKey chavePrivadaPropria;
    private PublicKey chavePublicaTesouraria;
    private PublicKey chavePublicaMotoboy;
    private static final String ROUTINGKEY = "pedido_criado.#"; // Tópicos para enviar mensagem
    private static final List<String> BINDING_KEYS = Arrays.asList("#.boleto_criado.#", "#.pedido_entregue"); // Tópicos
                                                                                                              // para
                                                                                                              // receber
    // mensagem

    public Cliente(String name) {
        super(BINDING_KEYS);
        this.name = name;
        getKeys();
        sender = new Sender();
        System.out.println();
        System.out.println("############################");
        System.out.println("##    Cliente iniciado!   ##");
        System.out.println("############################");
    }

    // Método para carregar as chaves privadas e públicas
    private void getKeys() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(
                    new FileInputStream("keys" + File.separator + "clienteKeys" + File.separator + "private.key"));
            chavePrivadaPropria = (PrivateKey) inputStream.readObject();
            inputStream.close();

            inputStream = new ObjectInputStream(
                    new FileInputStream("keys" + File.separator + "tesourariaKeys" + File.separator + "public.key"));
            chavePublicaTesouraria = (PublicKey) inputStream.readObject();
            inputStream.close();

            inputStream = new ObjectInputStream(
                    new FileInputStream("keys" + File.separator + "motoboyKeys" + File.separator + "public.key"));
            chavePublicaMotoboy = (PublicKey) inputStream.readObject();
            inputStream.close();

        } catch (Exception e) {
            System.err.println("Erro ao carregar chaves: " + e.getMessage());
        }
    }

    public String getName() {
        return name;
    }

    // Método para enviar pedido
    public void sendOrder(String pedido, String routingKey) {
        // Enviar ordem para a tesouraria
        try {

            Message message = new Message(pedido, chavePrivadaPropria);
            sender.send(message.getPayload(), routingKey);
            System.out.println("Pedido enviado com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao enviar pedido: " + e.getMessage());
        }
    }

    public void sendDelivered(String pedido, String routingKey) {
        // Enviar ordem para a tesouraria
        try {
            Message message = new Message(pedido, chavePrivadaPropria);
            sender.send(message.getPayload(), routingKey);
            System.out.println("Pagamento enviado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao enviar pedido: " + e.getMessage());
        }
    }

    @Override
    public void processMessage(byte[] payload, String routingKey) {
        System.out.println("Mensagem recebida do canal: " + routingKey);
        if (routingKey.equals("#.boleto_criado.#")) {
            System.out.println("\n#################################\n");
            System.out.println("Mensagem recebida do canal: " + routingKey);
            processBilling(payload);
            System.out.println("\n#################################\n");
        } else if (routingKey.equals("#.pedido_entregue")) {
            System.out.println("\n#################################\n");
            System.out.println("Mensagem recebida do canal: " + routingKey);
            processDelivered(payload);
            System.out.println("\n#################################\n");

        } else {
            System.out.println("Erro! Topico nao reconhecido: " + routingKey);
        }
    }

    private void processDelivered(byte[] payload) {
        Message message = null;

        try {

            message = new Message(payload, chavePublicaMotoboy);
            System.out.println("Pedido entregue: " + message.getTexto());

        } catch (RuntimeException e) {
            System.err.println("Erro de Assinatura na mensagem: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro na assinatura: " + e.getMessage());
        }
    }

    private void processBilling(byte[] payload) {
        Message message = null;
        try {

            message = new Message(payload, chavePublicaTesouraria);
            System.out.println("\nBoleto recebido: " + message.getTexto());
            System.out.println("Processando pagamento...\n");
            doWork(1000);
            sendDelivered(message.getTexto(), "#.pagamento_efetivado.#");

        } catch (RuntimeException e) {
            System.err.println("Erro de Assinatura na mensagem: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro na assinatura: " + e.getMessage());
        }
    }

    public void requestOrders() {
        while (true) {
            System.out.println("[main] pode digitar novo pedido!");
            String pedido = scanner.next();
            if (pedido.equals(" "))
                break;
            sendOrder(pedido, ROUTINGKEY);
        }

    }

    public static void main(String[] args) {
        Cliente cliente = new Cliente("Juliano");
        new Thread(() -> {
            cliente.init();
        }).start();
        cliente.doWork(5);
        new Thread(() -> {
            cliente.requestOrders();
        }).start();
    }

}
