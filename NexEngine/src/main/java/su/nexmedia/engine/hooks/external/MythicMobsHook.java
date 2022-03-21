package su.nexmedia.engine.hooks.external;


import io.lumine.mythic.api.exceptions.InvalidMobTypeException;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.hook.AbstractHook;

import java.util.ArrayList;
import java.util.List;

public class MythicMobsHook extends AbstractHook<NexEngine> {

    private static MythicBukkit mythicMobs;

    public MythicMobsHook(@NotNull NexEngine plugin, @NotNull String pluginName) {
        super(plugin, pluginName);
    }

    @Override
    public boolean setup() {
        mythicMobs = MythicBukkit.inst();
        return true;
    }

    @Override
    public void shutdown() {

    }

    public static boolean isMythicMob(@NotNull Entity entity) {
        return mythicMobs.getAPIHelper().isMythicMob(entity);
    }

    @Nullable
    public static ActiveMob getMobInstance(@NotNull Entity entity) {
        return mythicMobs.getAPIHelper().getMythicMobInstance(entity);
    }

    @Nullable
    public static MythicMob getMobConfig(@NotNull Entity entity) {
        ActiveMob mob = getMobInstance(entity);
        return mob != null ? mob.getType() : null;
    }

    @Nullable
    public static MythicMob getMobConfig(@NotNull String mobId) {
        return mythicMobs.getAPIHelper().getMythicMob(mobId);
    }

    @Deprecated
    public static String getMythicNameByEntity(@NotNull Entity entity) {
        return getMobInternalName(entity);
    }

    @NotNull
    public static String getMobInternalName(@NotNull Entity entity) {
        MythicMob mythicMob = getMobConfig(entity);
        return mythicMob != null ? mythicMob.getInternalName() : "null";
    }

    @NotNull
    @Deprecated
    public static String getName(@NotNull String mobId) {
        return getMobDisplayName(mobId);
    }

    @NotNull
    public static String getMobDisplayName(@NotNull String mobId) {
        MythicMob mythicMob = getMobConfig(mobId);
        return mythicMob != null ? mythicMob.getDisplayName().get() : mobId;
    }

    @Deprecated
    public static MythicMob getMythicInstance(@NotNull Entity e) {
        return getMobConfig(e);
    }

    @Deprecated
    public static boolean isDropTable(@NotNull String table) {
        return mythicMobs.getDropManager().getDropTable(table).isPresent() && mythicMobs.getDropManager().getDropTable(table).isPresent();
    }

    @Deprecated
    public static double getLevel(@NotNull Entity e) {
        return getMobLevel(e);
    }

    public static double getMobLevel(@NotNull Entity entity) {
        ActiveMob mob = getMobInstance(entity);
        return mob != null ? mob.getLevel() : 0;
    }

    @NotNull
    public static List<String> getMobConfigIds() {
        return new ArrayList<>(mythicMobs.getMobManager().getMobNames());
    }

    @NotNull
    @Deprecated
    public static List<String> getMythicIds() {
        return getMobConfigIds();
    }

    @Deprecated
    public static void setSkillDamage(@NotNull Entity e, double d) {
        if (!isMythicMob(e))
            return;
        ActiveMob am1 = mythicMobs.getMobManager().getMythicMobInstance(e);
        am1.setLastDamageSkillAmount(d);
    }

    @Deprecated
    public static void castSkill(@NotNull Entity e, @NotNull String skill) {
        mythicMobs.getAPIHelper().castSkill(e, skill);
    }

    @Deprecated
    public static void killMythic(@NotNull Entity e) {
        killMob(e);
    }

    public static void killMob(@NotNull Entity entity) {
        ActiveMob mob = getMobInstance(entity);
        if (mob == null || mob.isDead()) return;

        mob.setDead();
        mob.remove();
        entity.remove();
    }

    @Deprecated
    public static boolean isValid(@NotNull String name) {
        return getMobConfig(name) != null;
    }

    @Nullable
    public static Entity spawnMythicMob(@NotNull String mobId, @NotNull Location location, int level) {
        return spawnMob(mobId, location, level);
    }

    @Nullable
    public static Entity spawnMob(@NotNull String mobId, @NotNull Location location, int level) {
        try {
            MythicMob mythicMob = getMobConfig(mobId);
            return mythicMobs.getAPIHelper().spawnMythicMob(mythicMob, location, level);
        }
        catch (InvalidMobTypeException e) {
            e.printStackTrace();
        }
        return null;
    }
}
