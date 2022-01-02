package su.nexmedia.engine.api.craft;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;

public class FurnaceRecipe extends AbstractRecipe {

    private ItemStack input;
    private float     exp;
    private int       time;

    public FurnaceRecipe(@NotNull NexPlugin<?> plugin, @NotNull String id, @NotNull ItemStack result, float exp, double time) {
        super(plugin, id, result);
        this.exp = exp;
        this.time = (int) Math.max(1, 20D * time);
    }

    @NotNull
    public ItemStack getInput() {
        return this.input;
    }

    public float getExp() {
        return this.exp;
    }

    public int getTime() {
        return this.time;
    }

    public void addIngredient(@NotNull ItemStack ing) {
        this.addIngredient(0, ing);
    }

    @Override
    public void addIngredient(int slot, @Nullable ItemStack input) {
        if (input == null || input.getType() == Material.AIR) {
            throw new IllegalArgumentException("Input can not be null or AIR!");
        }
        this.input = input;
    }

    @Override
    @NotNull
    public Recipe getRecipe() {
        NamespacedKey key = this.getKey();
        ItemStack input = this.getInput();
        ItemStack result = this.getResult();
        float exp = this.getExp();
        int time = this.getTime();

        if (input.hasItemMeta()) {
            return new org.bukkit.inventory.FurnaceRecipe(key, result, new RecipeChoice.ExactChoice(input), exp, time);
        }
        return new org.bukkit.inventory.FurnaceRecipe(key, result, input.getType(), exp, time);
    }
}
