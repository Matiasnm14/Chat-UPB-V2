package edu.upb.chatupb_v2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public final class UserIdentity {
    private static final String USER_ID_FILE = System.getProperty("user.home")
            + java.io.File.separator + ".chatupb_user_id";

    private UserIdentity() {
    }

    public static UUID loadOrCreateUserId() {
        Path path = Paths.get(USER_ID_FILE);
        if (Files.exists(path)) {
            try {
                String raw = Files.readString(path).trim();
                if (!raw.isBlank()) {
                    return UUID.fromString(raw);
                }
            } catch (Exception ignored) {
            }
        }
        UUID created = UUID.randomUUID();
        try {
            Files.writeString(path, created.toString());
        } catch (IOException ignored) {
        }
        return created;
    }
}
