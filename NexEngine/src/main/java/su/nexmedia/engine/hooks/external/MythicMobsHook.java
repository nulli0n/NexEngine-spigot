package su.nexmedia.engine.hooks.external;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.hook.AbstractHook;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MythicMobsHook extends AbstractHook<NexEngine> {

    private MythicMobs mythicMobs;

    public MythicMobsHook(@NotNull NexEngine plugin, @NotNull String pluginName) {
        super(plugin, pluginName);
    }

    @Override
    public boolean setup() {
        this.mythicMobs = MythicMobs.inst();
        return true;
    }

    @Override
    public void shutdown() {

    }

    public boolean isMythicMob(@NotNull Entity e) {
        return mythicMobs.getAPIHelper().isMythicMob(e);
    }

    public String getMythicNameByEntity(@NotNull Entity e) {
        return mythicMobs.getAPIHelper().getMythicMobInstance(e).getType().getInternalName();
    }

    public MythicMob getMythicInstance(@NotNull Entity e) {
        return mythicMobs.getAPIHelper().getMythicMobInstance(e).getType();
    }

    public boolean isDropTable(@NotNull String table) {
        return mythicMobs.getDropManager().getDropTable(table).isPresent() && MythicMobs.inst().getDropManager().getDropTable(table).isPresent();
    }

    public double getLevel(@NotNull Entity e) {
        return mythicMobs.getAPIHelper().getMythicMobInstance(e).getLevel();
    }

    @NotNull
    public List<String> getMythicIds() {
        return new ArrayList<>(mythicMobs.getMobManager().getMobNames());
    }

    public void setSkillDamage(@NotNull Entity e, double d) {
        if (!isMythicMob(e))
            return;
        ActiveMob am1 = mythicMobs.getMobManager().getMythicMobInstance(e);
        am1.setLastDamageSkillAmount(d);
    }

    public void castSkill(@NotNull Entity e, @NotNull String skill) {
        mythicMobs.getAPIHelper().castSkill(e, skill);
    }

    public void killMythic(@NotNull Entity e) {
        if (!this.mythicMobs.getAPIHelper().getMythicMobInstance(e).isDead()) {
            this.mythicMobs.getAPIHelper().getMythicMobInstance(e).setDead();
            e.remove();
        }
    }

    public boolean isValid(@NotNull String name) {
        MythicMob koke = this.mythicMobs.getAPIHelper().getMythicMob(name);
        return koke != null;
    }

    @NotNull
    public String getName(@NotNull String mobId) {
        MythicMob koke = mythicMobs.getAPIHelper().getMythicMob(mobId);
        return koke != null ? koke.getDisplayName().get() : mobId;
    }

    @Nullable
    public Entity spawnMythicMob(@NotNull String name, @NotNull Location loc, int level) {
        try {
            MythicMob koke = mythicMobs.getAPIHelper().getMythicMob(name);
            // mm.getAPIHelper().getMythicMobInstance(e).setLevel(level);
            return mythicMobs.getAPIHelper().spawnMythicMob(koke, loc, level);
        } catch (InvalidMobTypeException e) {
            e.printStackTrace();
        }
        return null;
    }
}
