package system.receiver;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.*;

public abstract class Receiver {

    private static final String EXCHANGE_NAME = "system";
    private ConnectionFactory factory;
    private List<String> bindingKeys;
    Connection connection;
    Channel channel;

    public Receiver(List<String> bindingKeys) {
        factory = new ConnectionFactory();
        this.bindingKeys = bindingKeys;
        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (Exception e) {
            System.err.println("Erro ao criar o tópico: " + e.getMessage());
        }
    }

    public void init() {
        try {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            String queueName = channel.queueDeclare().getQueue();

            for (String bindingKey : bindingKeys) {
                channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
                System.out.println("Fila criada e vinculada ao tópico: " + bindingKey);
            }
            System.out.println("Esperando mensagens...\n");
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                byte[] message = delivery.getBody();
                // Informa de que canal a mensagem foi recebida
                System.out.println("Mensagem recebida do canal: " + delivery.getEnvelope().getRoutingKey());
                processMessage(message, delivery.getEnvelope().getRoutingKey());
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            System.err.println("Erro ao receber mensagem: " + e.getMessage());
        }
    }

    public abstract void processMessage(byte[] payload, String routingKey);

}