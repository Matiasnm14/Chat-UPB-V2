/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.model.network;

import edu.upb.chatupb_v2.controller.Mediator;
import edu.upb.chatupb_v2.model.entities.comands.*;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author rlaredo
 */
public class SocketClient extends Thread {
    private final Socket socket;

    public String getUID() {
        return uid;
    }

    private String name;
    @Setter
    private String uid;
    @Getter
    private final String ip;
    private final DataOutputStream dout;
    private final BufferedReader br;
    private final Object sendLock = new Object();
    private final Object closeLock = new Object();
    private boolean closedNotified = false;
    private final List<SocketListener> listeners = new ArrayList<>();

    private static final String CODE_HELLO = "004";
    private static final String CODE_ACCEPT_HELLO = "005";
    private static final String CODE_SECURITY_LIST = "014";
    private static final String CODE_SECURITY_SELECTED = "015";
    private static final String ALG_AES128 = "AES128";
    private static final String ALG_AES256 = "AES256";
    private static final List<String> SUPPORTED_CIPHERS = List.of(ALG_AES256, ALG_AES128);
    private static final SecureRandom RNG = new SecureRandom();
    private final boolean initiator;
    private volatile boolean securityReady = false;
    private volatile boolean securityHelloSent = false;
    private String securityAlgorithm;
    private SecretKeySpec securityKey;
    private final Deque<String> pendingMessages = new ArrayDeque<>();

//    @Getter
//    private final Map<String, SocketListener> listener = new HashMap<>();


    public String getNombre() {
        return name;
    }

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
        addListener(Mediator.getInstance());
        this.initiator = false;
    }

    public SocketClient(String ip) throws IOException {
        this.socket = new Socket(ip, 1900);
        this.ip = ip;
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
        addListener(Mediator.getInstance());
        this.initiator = true;
        sendSecurityHello();
    }


    //ALGO
    public interface SocketListener {
        default void onInvitationReceived(Invitation invitation) {}
        default void onAcceptReceived(Accept accept) {}
        default void onDeclineReceived(Decline decline) {}
        default void onHelloReceived(Hello hello) {}
        default void onAcceptHelloReceived(AcceptHello acceptHello) {}
        default void onDeclineHelloReceived(DeclineHello declineHello) {}
        default void onChatReceived(Chat chat) {}
        default void onConfirmedReceived(ConfirmRecived confirmRecived) {}
        default void onDeleteMessageReceived(DeleteMessage deleteMessage) {}
        default void onBuzzingReceived(Buzzing buzzing) {}
        default void onPinMessageReceived(PinMessage pinMessage) {}
        default void onUniqueMessageReceived(UniqueMessage uniqueMessage) {}
        default void onThemeReceived(Theme theme) {}
        default void onGoodByeReceived(GoodBye goodBye) {}
        default void onSocketClosed(SocketClient client) {}
    }

    public void addListener(SocketListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(SocketListener listener) {
        listeners.remove(listener);
    }

    public void setClient(String name, String key) {
//        this.listener.put(key, listener);
        this.uid = key;
        this.name = name;
    }

    @Override
    public void run() {
        try {
            String raw;
            while ((raw = br.readLine()) != null) {
                String message = raw;
                if (securityReady && !isHandshakeMessage(raw)) {
                    try {
                        message = decrypt(raw);
                    } catch (Exception e) {
                        System.out.println("No se pudo desencriptar mensaje: " + e.getMessage());
                        continue;
                    }
                }
                String[] split = message.split(Pattern.quote("|"));
                if (split.length == 0) {
                    continue;
                }

                switch (split[0]) {
                    case CODE_SECURITY_LIST: {
                        try {
                            SecurityHello hello = SecurityHello.parse(message);
                            handleSecurityHello(hello);
                        } catch (Exception e) {
                            close();
                        }
                        break;
                    }
                    case CODE_SECURITY_SELECTED: {
                        try {
                            SecuritySelected selected = SecuritySelected.parse(message);
                            handleSecuritySelected(selected);
                        } catch (Exception e) {
                            close();
                        }
                        break;
                    }
                    case "001": {
                        if (!securityReady) {
                            break;
                        }
                        Invitation inv = Invitation.parse(message);
                        this.name = inv.getUserName();
                        this.uid = inv.getIdUser();
                        for (SocketListener listener : listeners) {
                            listener.onInvitationReceived(inv);
                        }

                        break;
                    }
                    case "002": {
                        if (!securityReady) {
                            break;
                        }
                        Accept acp = Accept.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onAcceptReceived(acp);
                        }

                        break;
                    }
                    case "003": {
                        if (!securityReady) {
                            break;
                        }
                        Decline dec = Decline.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onDeclineReceived(dec);
                        }

                        break;
                    }
                    case "004": {
                        if (!securityReady) {
                            break;
                        }
                        Hello hel = Hello.parse(message);
                        logReceiveHello(CODE_HELLO, hel.getIdUser());
                        for (SocketListener listener : listeners) {
                            listener.onHelloReceived(hel);
                        }

                        break;
                    }
                    case "005": {
                        if (!securityReady) {
                            break;
                        }
                        AcceptHello acpHel = AcceptHello.parse(message);
                        logReceiveHello(CODE_ACCEPT_HELLO, acpHel.getIdUser());
                        for (SocketListener listener : listeners) {
                            listener.onAcceptHelloReceived(acpHel);
                        }

                        break;
                    }
                    case "006": {
                        if (!securityReady) {
                            break;
                        }
                        DeclineHello decHel = DeclineHello.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onDeclineHelloReceived(decHel);
                        }
                        break;
                    }
                    case "007": {
                        if (!securityReady) {
                            break;
                        }
                        Chat cht = Chat.parse(message);

                        for (SocketListener listener : listeners) {
                            listener.onChatReceived(cht);
                        }

                        break;
                    }
                    case "008": {
                        if (!securityReady) {
                            break;
                        }
                        ConfirmRecived conRec = ConfirmRecived.parse(message);

                        for (SocketListener listener : listeners) {
                            listener.onConfirmedReceived(conRec);
                        }

                        break;
                    }
                    case "009": {
                        if (!securityReady) {
                            break;
                        }
                        DeleteMessage delMes = DeleteMessage.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onDeleteMessageReceived(delMes);
                        }
                        break;
                    }
                    case "010": {
                        if (!securityReady) {
                            break;
                        }
                        Buzzing buz = Buzzing.parse(message);

                        for (SocketListener listener : listeners) {
                            listener.onBuzzingReceived(buz);
                        }

                        break;
                    }
                    case "011": {
                        if (!securityReady) {
                            break;
                        }
                        PinMessage pinMes = PinMessage.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onPinMessageReceived(pinMes);
                        }
                        break;
                    }
                    case "012": {
                        if (!securityReady) {
                            break;
                        }
                        UniqueMessage uniMes = UniqueMessage.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onUniqueMessageReceived(uniMes);
                        }
                        break;
                    }
                    case "013": {
                        if (!securityReady) {
                            break;
                        }
                        Theme thm = Theme.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onThemeReceived(thm);
                        }
                        break;
                    }
                    case "0018": {
                        if (!securityReady) {
                            break;
                        }
                        GoodBye gb = GoodBye.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onGoodByeReceived(gb);
                        }
                    }
                }
            }
        } catch (SocketException socketException) {
            System.out.println("Socket cerrado ");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            notifyClosed();
        }
    }

    private void notifyClosed() {
        synchronized (closeLock) {
            if (closedNotified) {
                return;
            }
            closedNotified = true;
        }
        for (SocketListener listener : listeners) {
            listener.onSocketClosed(this);
        }
    }

    public void send(String message) throws IOException {
        if (message == null || message.isBlank()) {
            return;
        }
        String trimmed = trimLineEndings(message);
        logSendHelloIfNeeded(trimmed);
        if (isHandshakeMessage(trimmed)) {
            sendPlain(trimmed);
            return;
        }
        if (!securityReady) {
            enqueuePending(trimmed);
            return;
        }
        sendEncrypted(trimmed);
    }

    public void close() {
        try {
            this.socket.close();
            this.br.close();
            this.dout.close();
        } catch (SocketException socketException) {
            System.out.println("Se ha cerrado el socket");
        } catch (Exception e) {
            System.out.println(getAllStackTraces());
        } finally {
            notifyClosed();
        }
    }

    private void sendSecurityHello() throws IOException {
        if (!initiator || securityHelloSent) {
            return;
        }
        SecurityHello hello = new SecurityHello(SUPPORTED_CIPHERS);
        logSecurity("Enviando 014 con algoritmos soportados: " + String.join(",", SUPPORTED_CIPHERS));
        sendPlain(trimLineEndings(hello.createFormat()));
        securityHelloSent = true;
    }

    private void handleSecurityHello(SecurityHello hello) {
        if (hello == null || securityReady) {
            return;
        }
        logSecurity("Recibido 014 con algoritmos: " + String.join(",", hello.getAlgorithms()));
        String selected = chooseAlgorithm(hello.getAlgorithms());
        if (selected == null) {
            logSecurity("No hay algoritmos compatibles. Cerrando socket.");
            close();
            return;
        }
        byte[] keyBytes = generateKey(selected);
        String keyEncoded = Base64.getEncoder().encodeToString(keyBytes);
        logSecurity("Seleccionado " + selected + " con llave(Base64): " + keyEncoded);
        SecuritySelected response = new SecuritySelected(selected, keyEncoded);
        try {
            sendPlain(trimLineEndings(response.createFormat()));
        } catch (IOException e) {
            System.out.println("No se pudo enviar respuesta de seguridad: " + e.getMessage());
            close();
            return;
        }
        configureSecurity(selected, keyBytes);
        flushPendingMessages();
    }

    private void handleSecuritySelected(SecuritySelected selected) {
        if (selected == null || securityReady) {
            return;
        }
        String algorithm = normalizeAlgorithm(selected.getAlgorithm());
        logSecurity("Recibido 015 con algoritmo: " + algorithm + " y llave(Base64): " + selected.getKey());
        if (!isSupported(algorithm)) {
            logSecurity("Algoritmo no soportado: " + algorithm + ". Cerrando socket.");
            close();
            return;
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(selected.getKey());
        } catch (IllegalArgumentException ex) {
            close();
            return;
        }
        if (!validateKeySize(algorithm, keyBytes)) {
            logSecurity("Tamaño de llave inválido para " + algorithm + ". Cerrando socket.");
            close();
            return;
        }
        configureSecurity(algorithm, keyBytes);
        flushPendingMessages();
    }

    private void configureSecurity(String algorithm, byte[] keyBytes) {
        String normalized = normalizeAlgorithm(algorithm);
        this.securityAlgorithm = normalized;
        this.securityKey = new SecretKeySpec(keyBytes, "AES");
        this.securityReady = true;
        logSecurity("Canal seguro listo con " + normalized + ".");
    }

    private String chooseAlgorithm(List<String> clientAlgorithms) {
        if (clientAlgorithms == null || clientAlgorithms.isEmpty()) {
            return null;
        }
        List<String> candidates = new ArrayList<>();
        for (String supported : SUPPORTED_CIPHERS) {
            for (String offered : clientAlgorithms) {
                String normalized = normalizeAlgorithm(offered);
                if (supported.equals(normalized)) {
                    candidates.add(supported);
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(RNG.nextInt(candidates.size()));
    }

    private boolean isSupported(String algorithm) {
        if (algorithm == null) {
            return false;
        }
        for (String supported : SUPPORTED_CIPHERS) {
            if (supported.equals(algorithm)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeAlgorithm(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private byte[] generateKey(String algorithm) {
        int size = keySizeFor(algorithm);
        byte[] key = new byte[size];
        RNG.nextBytes(key);
        return key;
    }

    private boolean validateKeySize(String algorithm, byte[] keyBytes) {
        if (keyBytes == null) {
            return false;
        }
        return keyBytes.length == keySizeFor(algorithm);
    }

    private int keySizeFor(String algorithm) {
        if (ALG_AES256.equals(algorithm)) {
            return 32;
        }
        return 16;
    }

    private void enqueuePending(String message) {
        synchronized (pendingMessages) {
            pendingMessages.addLast(message);
        }
    }

    private void flushPendingMessages() {
        List<String> pending = new ArrayList<>();
        synchronized (pendingMessages) {
            while (!pendingMessages.isEmpty()) {
                pending.add(pendingMessages.removeFirst());
            }
        }
        for (String message : pending) {
            try {
                send(message);
            } catch (IOException e) {
                System.out.println("No se pudo enviar mensaje pendiente: " + e.getMessage());
            }
        }
    }

    private boolean isHandshakeMessage(String payload) {
        if (payload == null) {
            return false;
        }
        return payload.startsWith(CODE_SECURITY_LIST + "|")
                || payload.startsWith(CODE_SECURITY_SELECTED + "|");
    }

    private void sendPlain(String payload) throws IOException {
        String data = payload;
        if (!data.endsWith("\n") && !data.endsWith("\r\n")) {
            data += System.lineSeparator();
        }
        try {
            synchronized (sendLock) {
                dout.write(data.getBytes(StandardCharsets.UTF_8));
                dout.flush();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void sendEncrypted(String payload) throws IOException {
        if (securityKey == null) {
            return;
        }
        String encrypted;
        try {
            encrypted = encrypt(payload);
        } catch (Exception e) {
            throw new IOException("No se pudo encriptar mensaje", e);
        }
        logSecurity("Enviando mensaje cifrado (" + securityAlgorithm + "), bytes Base64: " + encrypted.length());
        sendPlain(encrypted);
    }

    private String encrypt(String plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        RNG.nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, securityKey, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    private String decrypt(String payload) throws Exception {
        byte[] combined = Base64.getDecoder().decode(payload);
        if (combined.length <= 16) {
            throw new IllegalArgumentException("Payload encriptado invalido");
        }
        byte[] iv = new byte[16];
        byte[] encrypted = new byte[combined.length - 16];
        System.arraycopy(combined, 0, iv, 0, 16);
        System.arraycopy(combined, 16, encrypted, 0, encrypted.length);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, securityKey, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encrypted);
        String clear = new String(decrypted, StandardCharsets.UTF_8);
        logSecurity("Mensaje descifrado correctamente.");
        return clear;
    }

    private void logSecurity(String message) {
        System.out.println("[SECURITY][" + (ip != null ? ip : "local") + "] " + message);
    }

    private void logSendHelloIfNeeded(String payload) {
        String trimmed = trimLineEndings(payload);
        String[] parts = trimmed.split(Pattern.quote("|"));
        if (parts.length == 0) {
            return;
        }
        String code = parts[0];
        if (!CODE_HELLO.equals(code) && !CODE_ACCEPT_HELLO.equals(code)) {
            return;
        }
        String id = parts.length > 1 ? parts[1] : "";
        logSendHello(code, id);
    }

    private void logSendHello(String code, String id) {
        String label = CODE_HELLO.equals(code) ? "Hello" : "AcceptHello";
        System.out.println("Enviando " + code + " (" + label + ") a id " + safeId(id));
    }

    private void logReceiveHello(String code, String id) {
        String label = CODE_HELLO.equals(code) ? "Hello" : "AcceptHello";
        System.out.println("Recibido " + code + " (" + label + ") de id " + safeId(id));
    }

    private String safeId(String id) {
        if (id == null || id.isBlank()) {
            return "(sin id)";
        }
        return id;
    }

    private String trimLineEndings(String text) {
        if (text == null) {
            return "";
        }
        int end = text.length();
        while (end > 0) {
            char c = text.charAt(end - 1);
            if (c == '\n' || c == '\r') {
                end--;
            } else {
                break;
            }
        }
        return text.substring(0, end);
    }
}
