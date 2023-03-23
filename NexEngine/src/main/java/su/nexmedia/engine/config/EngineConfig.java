package su.nexmedia.engine.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.config.JYML;

import java.util.UUID;

public class EngineConfig {

    @NotNull
    public static UUID getIdForSkullTexture(@NotNull String base64) {
        JYML cfg = NexEngine.get().getConfig();

        UUID uuid;
        String idRaw = cfg.getString("Head_Texture_Cache." + base64, UUID.randomUUID().toString());
        try {
            uuid = UUID.fromString(idRaw);
        }
        catch (IllegalArgumentException e) {
            uuid = UUID.randomUUID();
        }

        cfg.addMissing("Head_Texture_Cache." + base64, uuid.toString());
        cfg.saveChanges();

        return uuid;
    }
}
