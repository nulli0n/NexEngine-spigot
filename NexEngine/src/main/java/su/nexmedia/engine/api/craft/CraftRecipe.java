package su.nexmedia.engine.api.craft;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;

@Deprecated
public class CraftRecipe extends AbstractRecipe {

    private final boolean     isShaped;
    private final String[]    shape;
    private final ItemStack[] ingredients;

    public CraftRecipe(@NotNull NexPlugin<?> plugin, @NotNull String id, @NotNull ItemStack result, boolean isShaped) {
        super(plugin, id, result);
        this.isShaped = isShaped;
        this.shape = new String[]{"ABC", "DEF", "GHI"};
        this.ingredients = new ItemStack[(int) Math.pow(this.shape.length, 2)];
        for (int index = 0; index < this.ingredients.length; index++) {
            this.ingredients[index] = new ItemStack(Material.AIR);
        }
    }

    public boolean isShaped() {
        return this.isShaped;
    }

    public ItemStack[] getIngredients() {
        return this.ingredients;
    }

    @NotNull
    public String[] getShape() {
        return this.shape;
    }

    @Override
    public void addIngredient(int pos, @Nullable ItemStack item) {
        if (pos >= Math.pow(shape.length, 2)) {
            throw new IllegalArgumentException("Ingredient slot is out of shape size!");
        }

        if (item == null) item = new ItemStack(Material.AIR);
        this.ingredients[pos] = item;
    }

    @Override
    @NotNull
    public Recipe getRecipe() {
        ItemStack result = this.getResult();
        NamespacedKey key = this.getKey();
        ItemStack[] ingredients = this.getIngredients();

        if (this.isShaped()) {
            char[] shapeChars = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};

            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape(this.getShape());

            for (int pos = 0; pos < ingredients.length; pos++) {
                char letter = shapeChars[pos];
                ItemStack ingredient = ingredients[pos];
                if (ingredient.hasItemMeta()) {
                    recipe.setIngredient(letter, new RecipeChoice.ExactChoice(ingredient));
                }
                else {
                    recipe.setIngredient(letter, ingredient.getType());
                }
            }
            return recipe;
        }

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        for (ItemStack ingredient : ingredients) {
            if (ingredient.hasItemMeta()) {
                recipe.addIngredient(new RecipeChoice.ExactChoice(ingredient));
            }
            else {
                recipe.addIngredient(ingredient.getType());
            }
        }
        return recipe;
    }
}
