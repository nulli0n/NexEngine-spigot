package su.nexmedia.engine.api.data.serialize;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;
import su.nexmedia.engine.utils.ItemUtil;

import java.lang.reflect.Type;

public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack item, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("data64", ItemUtil.compress(item));
        return object;
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        return ItemUtil.decompress(object.get("data64").getAsString());
    }

}
