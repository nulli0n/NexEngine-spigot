package su.nexmedia.engine.api.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.Reflex;
import su.nexmedia.engine.utils.StringUtil;

import java.lang.reflect.Field;
import java.util.*;

public abstract class LangTemplate {

    protected NexPlugin<?>        plugin;
    protected JYML                config;
    protected LangTemplate        parent;
    protected Map<String, String> customPlaceholders;

    public LangTemplate(@NotNull NexPlugin<?> plugin, @NotNull JYML config) {
        this(plugin, config, null);
    }

    public LangTemplate(@NotNull NexPlugin<?> plugin, @NotNull JYML config, @Nullable LangTemplate parent) {
        this.plugin = plugin;
        this.config = config;
        this.parent = parent;
        this.customPlaceholders = new HashMap<>();
    }

    public void setup() {
        this.load();
        this.config.saveChanges();

        for (String place : this.config.getSection("custom-placeholders")) {
            this.customPlaceholders.put("%" + place + "%", this.config.getString("custom-placeholders." + place));
        }
        this.customPlaceholders.values().removeIf(Objects::isNull);
    }

    protected void setupEnum(@NotNull Class<? extends Enum<?>> clazz) {
        if (!clazz.isEnum()) return;
        for (Object o : clazz.getEnumConstants()) {
            if (o == null) continue;

            String name = o.toString();
            String path = clazz.getSimpleName() + "." + name;
            String val = StringUtil.capitalizeFully(name.replace("_", " "));
            this.config.addMissing(path, val);
        }
    }

    @NotNull
    public String getEnum(@NotNull Enum<?> e) {
        String path = e.getDeclaringClass().getSimpleName() + "." + e.name();
        String locEnum = this.getCustom(path);
        if (locEnum == null && !this.plugin.isEngine()) {
            return NexPlugin.getEngine().lang().getEnum(e);
        }
        return locEnum == null ? "null" : locEnum;
    }

    @NotNull
    public String getPrefix() {
        return this.plugin.lang().Prefix.getMsgReady();
    }

    @Nullable
    public String getCustom(@NotNull String path) {
        String str = this.config.getString(path);
        return str == null ? null : StringUtil.color(str);
    }

    @NotNull
    public Map<String, String> getCustomPlaceholders() {
        return this.customPlaceholders;
    }

    private void load() {
        for (Field field : Reflex.getFields(this.getClass())) {
            if (!LangMessage.class.isAssignableFrom(field.getType())) {
                continue;
            }

            LangMessage langMessage;
            try {
                langMessage = (LangMessage) field.get(this);
            }
            catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            langMessage.setPath(field.getName()); // Set the path to String in config

            // Fill message fields from extended class with parent message field values.
            if (!field.getDeclaringClass().equals(this.getClass())) {
                LangMessage superField = (LangMessage) Reflex.getFieldValue(this.parent, field.getName());
                if (superField != null) {
                    langMessage.setMsg(superField.getMsg());
                    continue;
                }
            }

            String path = langMessage.getPath();
            JYML cfg = this.config;

            // Add missing lang node in config.
            if (!cfg.contains(path)) {
                String msg = langMessage.getDefaultMsg();
                String[] split = msg.split("\n");
                cfg.set(path, split.length > 1 ? Arrays.asList(split) : msg);
            }

            // Load message text from lang config
            String msgLoad;
            List<String> cList = cfg.getStringList(path);
            if (!cList.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                cList.forEach(line -> {
                    if (builder.length() > 0) {
                        builder.append("\\n");
                    }
                    builder.append(line);
                });
                msgLoad = builder.toString();
            }
            else {
                msgLoad = cfg.getString(path, "");
            }
            langMessage.setMsg(msgLoad);
        }
        this.config.saveChanges();
    }
}
