package system.mensagem;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import javax.crypto.Cipher;

public class Message {

    private byte[] assinatura;
    private String texto;
    private byte[] payload;

    public Message(String texto, PrivateKey chavePrivada) {
        this.texto = texto;
        this.assinatura = sign(texto, chavePrivada);
        // junta a assinatura e o texto em um único array de bytes
        payload = new byte[assinatura.length + texto.getBytes().length];
        System.arraycopy(assinatura, 0, payload, 0, assinatura.length);
        System.arraycopy(texto.getBytes(), 0, payload, assinatura.length, texto.getBytes().length);
    }

    public Message(byte[] payload, PublicKey chavePublica) {
        // Pega os primeiros 128 bytes e atribui à assinatura
        this.assinatura = new byte[128];
        System.arraycopy(payload, 0, this.assinatura, 0, 128);
        // Pega o restante do payload e atribui ao texto
        byte[] textoBytes = new byte[payload.length - 128];
        System.arraycopy(payload, 128, textoBytes, 0, payload.length - 128);
        this.texto = new String(textoBytes);
        if (!verify(texto, assinatura, chavePublica)) {
            throw new RuntimeException("Assinatura inválida");
        } else {
            System.out.println("Mensagem com assinatura válida");
        }
    }

    public String getTexto() {
        return texto;
    }

    public byte[] getPayload() {
        return payload;
    }

    private byte[] sign(String texto, PrivateKey chavePrivada) {
        byte[] assinatura = null;
        try {
            // Usa o algoritmo de assinatura com SHA-256 e RSA
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(chavePrivada);
            signature.update(texto.getBytes());
            assinatura = signature.sign(); // Gera a assinatura
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assinatura;
    }

    private boolean verify(String texto, byte[] assinatura, PublicKey chavePublica) {
        boolean isValid = false;
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(chavePublica);
            signature.update(texto.getBytes());
            isValid = signature.verify(assinatura); // Verifica a assinatura
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isValid;
    }

    public static void main(String[] args) {
        String PATH_CHAVE_PRIVADA = "./keys/private.key";
        String PATH_CHAVE_PUBLICA = "./keys2/public.key";

        try {
            // Leitura da chave pública
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(PATH_CHAVE_PUBLICA));
            final PublicKey chavePublica = (PublicKey) inputStream.readObject();
            inputStream.close();

            // Leitura da chave privada
            inputStream = new ObjectInputStream(new FileInputStream(PATH_CHAVE_PRIVADA));
            final PrivateKey chavePrivada = (PrivateKey) inputStream.readObject();
            inputStream.close();

            // Cria a mensagem assinada
            Message message = new Message("Mensagem secreta", chavePrivada);
            System.out.println("Texto: " + message.getTexto());

            // Processa a mensagem recebida
            Message receivedMessage = new Message(message.getPayload(), chavePublica);
            System.out.println("Texto Verificado: " + receivedMessage.getTexto());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Arquivo de chave não encontrado.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao ler o arquivo de chave.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Classe não encontrada durante a desserialização.");
        }
    }

}