package su.nexmedia.engine.effects.entity;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.effects.NexCustomEffects;
import su.nexmedia.engine.api.task.AbstractTask;

import java.util.HashSet;

public class EntityStatsTask extends AbstractTask<NexCustomEffects> {

    public EntityStatsTask(@NotNull NexCustomEffects plugin) {
        super(plugin, 1L, false);
    }

    @Override
    public void action() {
        for (EntityStats stats : new HashSet<>(EntityStats.getAll())) {
            stats.triggerEffects();
        }
    }
}
