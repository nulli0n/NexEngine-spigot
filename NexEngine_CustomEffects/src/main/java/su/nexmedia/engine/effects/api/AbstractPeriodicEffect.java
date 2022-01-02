package su.nexmedia.engine.effects.api;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;

import java.util.function.UnaryOperator;

public abstract class AbstractPeriodicEffect extends AbstractExpirableEffect {

    public static final String PLACEHOLDER_INTERVAL = "%effect_interval%";
    private long   lastTrigger;
    private double interval;

    protected AbstractPeriodicEffect(@NotNull Builder<?> builder) {
        super(builder);
        this.interval = builder.interval;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> super.replacePlaceholders().apply(str)
            .replace(PLACEHOLDER_INTERVAL, NumberUtil.format(this.getInterval()))
            ;
    }

    @Override
    public boolean trigger(boolean force) {
        if (super.trigger(force)) {
            this.tick();
            return true;
        }
        return false;
    }

    public final double getInterval() {
        return this.interval;
    }

    public final long getLastTriggerTime() {
        return this.lastTrigger;
    }

    public final boolean isReady() {
        return System.currentTimeMillis() > (this.getLastTriggerTime() + (this.getInterval() * 1000D));
    }

    private final void tick() {
        this.lastTrigger = System.currentTimeMillis();
    }

    public abstract static class Builder<B extends Builder<B>> extends AbstractExpirableEffect.Builder<B> {

        private double interval;

        public Builder(double lifeTime, double interval) {
            super(lifeTime);
            this.interval = interval;
        }

        @Override
        @NotNull
        public abstract AbstractPeriodicEffect build();

        @Override
        @NotNull
        protected abstract B self();
    }
}
