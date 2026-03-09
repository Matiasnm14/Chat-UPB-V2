# Security Notes (014/015 + AES)

This file documents the security handshake and where encryption happens.
It also includes line-by-line comments for the key methods so you can
defend the logic without cluttering production code.

## Handshake summary
1. Client connects by socket.
2. Client sends `014|AES256,AES128`.
3. Server selects one algorithm at random and responds:
   `015|AES256|<base64_key>`.
4. Both sides store the key and mark the channel as secure.
5. All subsequent messages are encrypted.

## 014 - SecurityHello format
```
014|AES256,AES128
```

## 015 - SecuritySelected format
```
015|AES256|<base64_key>
```

## Where encryption happens
- **Before sending**: `send()` decides whether to send plain or encrypted.
- **When receiving**: `run()` decrypts any message that is not 014/015.

## Line-by-line: send(...)
```java
public void send(String message) throws IOException {        // Public method to send a message
    if (message == null || message.isBlank()) {              // Ignore null/empty messages
        return;                                              // Exit early
    }
    String trimmed = trimLineEndings(message);               // Remove trailing \n or \r\n
    logSendHelloIfNeeded(trimmed);                           // Optional log for 004/005
    if (isHandshakeMessage(trimmed)) {                       // If message is 014 or 015
        sendPlain(trimmed);                                  // Send it in plain text
        return;                                              // Do not encrypt handshake
    }
    if (!securityReady) {                                    // If secure channel not ready yet
        enqueuePending(trimmed);                             // Queue message until key is set
        return;                                              // Exit; will send later
    }
    sendEncrypted(trimmed);                                  // Encrypt and send normal messages
}
```

## Line-by-line: handleSecurityHello(...)
```java
private void handleSecurityHello(SecurityHello hello) {      // Server receives 014
    if (hello == null || securityReady) {                    // Ignore if null or already secure
        return;
    }
    logSecurity("Recibido 014 con algoritmos: "              // Log offered algorithms
            + String.join(",", hello.getAlgorithms()));
    String selected = chooseAlgorithm(hello.getAlgorithms());// Pick a supported algorithm
    if (selected == null) {                                  // If no common algorithm
        logSecurity("No hay algoritmos compatibles.");       // Log and close
        close();
        return;
    }
    byte[] keyBytes = generateKey(selected);                 // Generate AES key bytes
    String keyEncoded = Base64.getEncoder()                  // Encode key as Base64
            .encodeToString(keyBytes);
    logSecurity("Seleccionado " + selected                  // Log chosen algorithm and key
            + " con llave(Base64): " + keyEncoded);
    SecuritySelected response =                              // Build 015 response
            new SecuritySelected(selected, keyEncoded);
    sendPlain(trimLineEndings(response.createFormat()));     // Send 015 in plain text
    configureSecurity(selected, keyBytes);                   // Store key and mark secure
    flushPendingMessages();                                  // Send queued messages
}
```

## Line-by-line: handleSecuritySelected(...)
```java
private void handleSecuritySelected(SecuritySelected selected) {
    if (selected == null || securityReady) {                 // Ignore if null or already secure
        return;
    }
    String algorithm = normalizeAlgorithm(selected.getAlgorithm()); // Normalize algorithm
    logSecurity("Recibido 015 con algoritmo: " + algorithm          // Log selection + key
            + " y llave(Base64): " + selected.getKey());
    if (!isSupported(algorithm)) {                           // Reject unsupported algorithm
        logSecurity("Algoritmo no soportado: " + algorithm);
        close();
        return;
    }
    byte[] keyBytes = Base64.getDecoder()                    // Decode key from Base64
            .decode(selected.getKey());
    if (!validateKeySize(algorithm, keyBytes)) {             // Validate key size
        logSecurity("Tamaño de llave invalido para " + algorithm);
        close();
        return;
    }
    configureSecurity(algorithm, keyBytes);                  // Store key and mark secure
    flushPendingMessages();                                  // Send queued messages
}
```

## Line-by-line: encrypt(...) / decrypt(...)
```java
private String encrypt(String plaintext) throws Exception {  // Encrypt a message
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// AES-CBC with padding
    byte[] iv = new byte[16];                                // 16-byte IV
    RNG.nextBytes(iv);                                       // Randomize IV
    cipher.init(Cipher.ENCRYPT_MODE, securityKey,            // Init cipher with key + IV
            new IvParameterSpec(iv));
    byte[] encrypted = cipher.doFinal(plaintext              // Encrypt UTF-8 bytes
            .getBytes(StandardCharsets.UTF_8));
    byte[] combined = new byte[iv.length + encrypted.length];// IV + ciphertext
    System.arraycopy(iv, 0, combined, 0, iv.length);         // Copy IV first
    System.arraycopy(encrypted, 0, combined, iv.length,      // Copy ciphertext after IV
            encrypted.length);
    return Base64.getEncoder().encodeToString(combined);     // Send as Base64 string
}

private String decrypt(String payload) throws Exception {    // Decrypt a message
    byte[] combined = Base64.getDecoder().decode(payload);   // Decode Base64
    byte[] iv = new byte[16];                                // Extract IV
    byte[] encrypted = new byte[combined.length - 16];       // Extract ciphertext
    System.arraycopy(combined, 0, iv, 0, 16);                // Copy IV
    System.arraycopy(combined, 16, encrypted, 0,             // Copy ciphertext
            encrypted.length);
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// AES-CBC with padding
    cipher.init(Cipher.DECRYPT_MODE, securityKey,            // Init with key + IV
            new IvParameterSpec(iv));
    byte[] decrypted = cipher.doFinal(encrypted);            // Decrypt
    return new String(decrypted, StandardCharsets.UTF_8);    // UTF-8 string
}
```

## Note on key transport
The assignment explicitly sends the AES key inside the `015` command.
In real systems you would protect this with a key exchange (e.g., DH).
