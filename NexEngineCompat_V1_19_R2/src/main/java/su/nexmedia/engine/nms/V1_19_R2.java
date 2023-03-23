package su.nexmedia.engine.nms;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.math.BigInteger;

@Deprecated
public class V1_19_R2 implements NMS {

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
}
