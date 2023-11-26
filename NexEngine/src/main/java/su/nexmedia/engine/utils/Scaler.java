package su.nexmedia.engine.utils;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Deprecated
public class Scaler {

    private final int                      levelMin;
    private final int                      levelMax;
    private final TreeMap<Integer, Double> values;

    public Scaler(@NotNull JYML cfg, @NotNull String path, @NotNull String levelPlaceholder, int levelMin, int levelMax) {
        this.levelMin = levelMin;
        this.levelMax = levelMax;
        this.values = new TreeMap<>();

        // Load different values for each object level.
        Set<String> lvlKeys = cfg.getSection(path);
        if (!lvlKeys.isEmpty()) {
            for (String sLvl : lvlKeys) {
                int eLvl = StringUtil.getInteger(sLvl, 0);
                if (eLvl < this.getLevelMin() || eLvl > this.getLevelMax()) continue;

                String formula = cfg.getString(path + "." + sLvl, "0").replace(levelPlaceholder, sLvl);
                values.put(eLvl, Evaluator.evaluate(formula));
            }
            return;
        }

        // Load the single formula for all object levels.
        for (int lvl = this.getLevelMin(); lvl < (this.getLevelMax() + 1); lvl++) {
            String sLvl = String.valueOf(lvl);
            String exChance = cfg.getString(path, "").replace(levelPlaceholder, sLvl);
            if (exChance.isEmpty()) continue;

            values.put(lvl, Evaluator.evaluate(exChance));
        }
    }

    public int getLevelMin() {
        return this.levelMin;
    }

    public int getLevelMax() {
        return this.levelMax;
    }

    @NotNull
    public TreeMap<Integer, Double> getValues() {
        return this.values;
    }

    public double getValue(int level) {
        Map.Entry<Integer, Double> en = this.values.floorEntry(level);
        return en != null ? en.getValue() : 0D;
    }
}
