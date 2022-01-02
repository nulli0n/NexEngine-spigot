package su.nexmedia.engine.api.type;

import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.WaterMob;
import org.jetbrains.annotations.NotNull;

@Deprecated
public enum MobGroup {

    ANIMAL, MONSTER, NPC, WATER, AMBIENT, OTHER,
    ;

    @NotNull
    public static MobGroup getMobGroup(@NotNull Entity entity) {
        if (entity instanceof Animals) {
            return MobGroup.ANIMAL;
        }
        if (entity instanceof Monster) {
            return MobGroup.MONSTER;
        }
        if (entity instanceof Ambient) {
            return MobGroup.AMBIENT;
        }
        if (entity instanceof WaterMob) {
            return MobGroup.WATER;
        }
        if (entity instanceof NPC) {
            return MobGroup.NPC;
        }
        return MobGroup.OTHER;
    }
}
