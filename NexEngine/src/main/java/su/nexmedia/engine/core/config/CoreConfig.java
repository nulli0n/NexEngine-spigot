package su.nexmedia.engine.core.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.config.ConfigTemplate;
import su.nexmedia.engine.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class CoreConfig extends ConfigTemplate {

    public static String MODULES_PATH_INTERNAL = "/modules/";
    @Deprecated
    public static String MODULES_PATH_EXTERNAL = "/modules/_external/";

    private static Map<String, String> LOCALE_WORLD_NAMES;

    public CoreConfig(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @NotNull
    public static String getWorldName(@NotNull String world) {
        return LOCALE_WORLD_NAMES.getOrDefault(world, world);
    }

    @Override
    public void load() {
        this.cfg.addMissing("Locale.World_Names.world", "World");
        this.cfg.addMissing("Locale.World_Names.world_nether", "Nether");
        this.cfg.addMissing("Locale.World_Names.world_the_end", "The End");

        LOCALE_WORLD_NAMES = new HashMap<>();
        this.cfg.getSection("Locale.World_Names").forEach(world -> {
            String name = StringUtil.color(this.cfg.getString("Locale.World_Names." + world, world));
            LOCALE_WORLD_NAMES.put(world, name);
        });
    }
}
