package system.resources;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class Sender {

    private static final String EXCHANGE_NAME = "system";
    private ConnectionFactory factory;

    public Sender() {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
    }

    public void send(byte[] message, String routingKey) throws Exception {
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message);

        }
    }

    public static void main(String[] args) {
        final String exchange_name = "system";
        final String routing_key = "pedido_criado.boleto_criado.pedido_criado.pagamento_efetivado.pedido_despachado";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchange_name, BuiltinExchangeType.TOPIC);
            channel.basicPublish(exchange_name, routing_key, null, "inicia as filas".getBytes());

        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
        }
    }
}