package su.nexmedia.engine.nms;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.math.BigInteger;

public class V1_19_R3 implements NMS {

    @Override
    @NotNull
    public String getNBTTag(@NotNull ItemStack item) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        CompoundTag compound = nmsItem.getTag();
        return compound == null ? "null" : compound.toString();
    }

    @Override
    @Nullable
    public String toBase64(@NotNull ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);
        CompoundTag nbtTagCompoundItem = new CompoundTag();

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        nmsItem.save(nbtTagCompoundItem);

        try {
            NbtIo.write(nbtTagCompoundItem, dataOutput);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    @Nullable
    public ItemStack fromBase64(@NotNull String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

        CompoundTag nbtTagCompoundRoot;
        try {
            nbtTagCompoundRoot = NbtIo.read(new DataInputStream(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.of(nbtTagCompoundRoot);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    @NotNull
    public ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        // CraftItemStack craftItem = (CraftItemStack) item;
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        ServerPlayer nmsPlayer = player != null ? ((CraftPlayer) player).getHandle() : null;
        nmsStack.hurt(amount, RandomSource.create(), nmsPlayer);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public void updateMenuTitle(@NotNull Player player, @NotNull String title) {
        Inventory menu = player.getOpenInventory().getTopInventory();
        MenuType<?> type = switch (menu.getType()) {
            case DISPENSER, DROPPER, WORKBENCH -> MenuType.GENERIC_3x3;
            case FURNACE -> MenuType.FURNACE;
            case ENCHANTING -> MenuType.ENCHANTMENT;
            case BREWING -> MenuType.BREWING_STAND;
            case MERCHANT -> MenuType.MERCHANT;
            case ENDER_CHEST, BARREL -> MenuType.GENERIC_9x3;
            case ANVIL -> MenuType.ANVIL;
            case SMITHING -> MenuType.SMITHING;
            case BEACON -> MenuType.BEACON;
            case HOPPER -> MenuType.HOPPER;
            case SHULKER_BOX -> MenuType.SHULKER_BOX;
            case BLAST_FURNACE -> MenuType.BLAST_FURNACE;
            case LECTERN -> MenuType.LECTERN;
            case SMOKER -> MenuType.SMOKER;
            case LOOM -> MenuType.LOOM;
            case CARTOGRAPHY -> MenuType.CARTOGRAPHY_TABLE;
            case GRINDSTONE -> MenuType.GRINDSTONE;
            case STONECUTTER -> MenuType.STONECUTTER;
            case COMPOSTER, PLAYER, CREATIVE, CRAFTING, CHISELED_BOOKSHELF, JUKEBOX -> null;
            default -> switch (menu.getSize()) {
                case 9 -> MenuType.GENERIC_9x1;
                case 18 -> MenuType.GENERIC_9x2;
                case 36 -> MenuType.GENERIC_9x4;
                case 45 -> MenuType.GENERIC_9x5;
                case 54 -> MenuType.GENERIC_9x6;
                default -> MenuType.GENERIC_9x3;
            };
        };
        if (type == null) return;

        ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
        ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(serverPlayer.containerMenu.containerId, type, CraftChatMessage.fromStringOrNull(title));
        serverPlayer.connection.send(packet);
        player.updateInventory();
    }
}
