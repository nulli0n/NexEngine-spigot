package su.nexmedia.engine.utils.values;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class UniFormatter {

    private final DecimalFormat formatter;

    private final String format;
    private final RoundingMode rounding;

    public UniFormatter(String format, RoundingMode rounding) {
        this.format = format;
        this.rounding = rounding;
        this.formatter = new DecimalFormat(format, new DecimalFormatSymbols(Locale.US));
        this.formatter.setRoundingMode(rounding);
    }

    @NotNull
    public static UniFormatter of(String format, RoundingMode rounding) {
        return new UniFormatter(format, rounding);
    }

    @NotNull
    public static UniFormatter read(@NotNull JYML cfg, @NotNull String path) {
        String format = cfg.getString(path + ".Format_Pattern", "#,###.##");
        RoundingMode rounding = RoundingMode.valueOf(cfg.getString(path + ".Rounding_Mode", "half_even").toUpperCase());
        return of(format, rounding);
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Format", this.getFormat());
        cfg.set(path + ".Rounding", this.getRounding().name().toLowerCase());
    }

    public String getFormat() {
        return this.format;
    }

    public RoundingMode getRounding() {
        return this.rounding;
    }

    public String format(double value) {
        return this.formatter.format(value);
    }
}
