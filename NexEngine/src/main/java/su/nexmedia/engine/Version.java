package su.nexmedia.engine;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.StringUtil;

public enum Version {

    // KEEP VERSIONS LIST FROM SMALLER TO GREATER
    UNKNOWN("Unknown", true),
    V1_17_R1("1.17.1", true),
    V1_18_R2("1.18.2"),
    @Deprecated V1_19_R1("1.19.2", true),
    @Deprecated V1_19_R2("1.19.3", true),
    V1_19_R3("1.19.4"),
    V1_20_R1("1.20.1")
    ;

    private static Version current;

    private final boolean deprecated;
    private final String  localized;

    Version(@NotNull String localized) {
        this(localized, false);
    }

    Version(@NotNull String localized, boolean deprecated) {
        this.localized = localized;
        this.deprecated = deprecated;
    }

    @NotNull
    public static Version getCurrent() {
        if (current == null) {
            String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
            String versionRaw = split[split.length - 1];
            current = StringUtil.getEnum(versionRaw, Version.class).orElse(UNKNOWN);
        }
        return current;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    @NotNull
    public String getLocalized() {
        return localized;
    }

    /*public static final Version CURRENT;

    static {
        String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        String versionRaw = split[split.length - 1];

        try {
            CURRENT = Version.valueOf(versionRaw.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw e;
        }
    }*/

    public boolean isLower(@NotNull Version version) {
        return this.ordinal() < version.ordinal();
    }

    public boolean isHigher(@NotNull Version version) {
        return this.ordinal() > version.ordinal();
    }

    public static boolean isAtLeast(@NotNull Version version) {
        return version.isCurrent() || getCurrent().isHigher(version);
    }

    public static boolean isAbove(@NotNull Version version) {
        return getCurrent().isHigher(version);
    }

    public static boolean isBehind(@NotNull Version version) {
        return getCurrent().isLower(version);
    }

    public boolean isCurrent() {
        return this == Version.getCurrent();
    }
}
