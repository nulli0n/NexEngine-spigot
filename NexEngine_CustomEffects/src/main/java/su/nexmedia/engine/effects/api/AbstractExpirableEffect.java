package su.nexmedia.engine.effects.api;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.TimeUtil;

import java.util.function.UnaryOperator;

public abstract class AbstractExpirableEffect extends AbstractEffect {

    public static final String PLACEHOLDER_LIFETIME = "%effect_lifetime%";
    public static final String PLACEHOLDER_DURATION = "%effect_duration%";
    protected long   endTime;
    protected double lifeTime;

    protected AbstractExpirableEffect(@NotNull Builder<?> builder) {
        super(builder);
        this.setLifeTime(builder.lifeTime);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> super.replacePlaceholders().apply(str)
            .replace(PLACEHOLDER_LIFETIME, NumberUtil.format(this.getLifeTime()))
            .replace(PLACEHOLDER_DURATION, TimeUtil.formatTimeLeft(this.endTime))
            ;
    }

	/*@Override
	public void trigger(boolean force) {
		super.trigger(force);
		if (this.isExpired()) {
			if (this.msgExpire != null) {
				this.target.sendMessage(this.msgExpire);
			}
			return;
		}
	}*/

    public final double getLifeTime() {
        return this.lifeTime;
    }

    public void setLifeTime(double lifeTime) {
        this.lifeTime = lifeTime;
        this.endTime = System.currentTimeMillis() + (long) (int) (this.getLifeTime() * 1000D);
    }

    public final boolean isPermanent() {
        return this.getLifeTime() < 0D && !this.isChargesLimited();
    }

    @Override
    public final boolean isExpired() {
        if (this.isPermanent()) return false;
        return super.isExpired() || (this.getLifeTime() > 0 && System.currentTimeMillis() >= this.endTime);
    }

    public abstract static class Builder<B extends Builder<B>> extends AbstractEffect.Builder<B> {

        private double lifeTime;

        public Builder(double lifeTime) {
            this.lifeTime = lifeTime;
        }

        @Override
        @NotNull
        public abstract AbstractExpirableEffect build();

        @Override
        @NotNull
        protected abstract B self();
    }
}
