package su.nexmedia.engine.nms;

import com.google.common.collect.Multimap;
import io.netty.channel.Channel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class V1_19_R1 implements NMS {

    @Override
    @NotNull
    public Channel getChannel(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle().connection.getConnection().channel;
    }

    @Override
    public void sendPacket(@NotNull Player player, @NotNull Object packet) {
        ((CraftPlayer) player).getHandle().connection.send((Packet<?>) packet);
    }

    @Override
    public void sendAttackPacket(@NotNull Player p, int id) {
        CraftPlayer player = (CraftPlayer) p;
        Entity entity = player.getHandle();
        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(entity, id);
        player.getHandle().connection.send(packet);
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Block block) {
        BlockPos position = new BlockPos(block.getX(), block.getY(), block.getZ());
        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        ServerPlayerGameMode manager = entityPlayer.gameMode;
        return manager.destroyBlock(position);
    }

    @Override
    public float getBlockDurability(@NotNull Block block) {
        BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
        BlockBehaviour.BlockStateBase blockData = ((CraftWorld) block.getWorld()).getHandle().getBlockState(pos);
        return blockData.getBlock().getExplosionResistance();
    }

    @Override
    public float getBlockStrength(@NotNull Block block) {
        BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
        BlockBehaviour.BlockStateBase blockData = ((CraftWorld) block.getWorld()).getHandle().getBlockState(pos);
        return blockData.destroySpeed;
    }

    @Override
    @NotNull
    public List<ItemStack> getBlockDrops(@NotNull Block block, @NotNull Player player, @NotNull ItemStack tool) {
        BlockPos position = new BlockPos(block.getX(), block.getY(), block.getZ());

        ServerLevel nmsWorld = ((CraftWorld) player.getWorld()).getHandle();
        BlockState nmsData = nmsWorld.getBlockState(position);
        BlockEntity nmsTile = nmsWorld.getBlockEntity(position);
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(tool);

        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        return net.minecraft.world.level.block.Block.getDrops(
                        nmsData, nmsWorld.getMinecraftWorld(), position, nmsTile, entityPlayer, nmsItem)
                .stream().map(CraftItemStack::asBukkitCopy).toList();
    }

    @Override
    @NotNull
    public String toJSON(@NotNull ItemStack item) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        String json = nmsItem.save(new CompoundTag()).toString();
        if (json.length() > Short.MAX_VALUE) {
            ItemStack item2 = new ItemStack(item.getType());
            return toJSON(item2);
        }
        return json;
    }

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
        } catch (IOException e) {
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

    @Deprecated
    @Override
    @NotNull
    public String fixColors(@NotNull String str) {
        str = str.replace("\n", "%n%"); // CraftChatMessage wipes all lines out.

        Component baseComponent = CraftChatMessage.fromStringOrNull(str);
        String singleColor = CraftChatMessage.fromComponent(baseComponent);
        return singleColor.replace("%n%", "\n");
    }

    @Nullable
    private Multimap<Attribute, AttributeModifier> getAttributes(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        Multimap<Attribute, AttributeModifier> attMap = null;

        if (item instanceof ArmorItem armorItem) {
            attMap = armorItem.getDefaultAttributeModifiers(armorItem.getSlot());
        } else if (item instanceof DiggerItem diggerItem) {
            attMap = diggerItem.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
        } else if (item instanceof SwordItem swordItem) {
            attMap = swordItem.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
        } else if (item instanceof TridentItem tridentItem) {
            attMap = tridentItem.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
        }

        return attMap;
    }

    private double getAttributeValue(@NotNull ItemStack item, @NotNull Attribute base) {
        Multimap<Attribute, AttributeModifier> attMap = this.getAttributes(item);
        if (attMap == null) return 0D;

        Collection<AttributeModifier> att = attMap.get(base);
        return att.isEmpty() ? 0 : att.stream().findFirst().get().getAmount();
    }

    @Override
    public boolean isWeapon(@NotNull ItemStack itemStack) {
        return this.isSword(itemStack) || this.isAxe(itemStack) || this.isTrident(itemStack);
    }

    @Override
    public boolean isSword(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof SwordItem;
    }

    @Override
    public boolean isAxe(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof AxeItem;
    }

    @Override
    public boolean isTrident(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof TridentItem;
    }

    @Override
    public boolean isPickaxe(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof PickaxeItem;
    }

    @Override
    public boolean isShovel(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ShovelItem;
    }

    @Override
    public boolean isHoe(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof HoeItem;
    }

    @Override
    public boolean isTool(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof DiggerItem;
    }

    @Override
    public boolean isArmor(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ArmorItem;
    }

    private boolean isArmorSlot(@NotNull ItemStack itemStack, @NotNull EquipmentSlot slot) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        if (item instanceof ArmorItem armor) {
            return armor.getSlot() == slot;
        }
        return false;
    }

    @Override
    public boolean isHelmet(@NotNull ItemStack itemStack) {
        return this.isArmorSlot(itemStack, EquipmentSlot.HEAD);
    }

    @Override
    public boolean isChestplate(@NotNull ItemStack itemStack) {
        return this.isArmorSlot(itemStack, EquipmentSlot.CHEST);
    }

    @Override
    public boolean isLeggings(@NotNull ItemStack itemStack) {
        return this.isArmorSlot(itemStack, EquipmentSlot.LEGS);
    }

    @Override
    public boolean isBoots(@NotNull ItemStack itemStack) {
        return this.isArmorSlot(itemStack, EquipmentSlot.FEET);
    }

    @Override
    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, Attributes.ATTACK_DAMAGE);
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, Attributes.MOVEMENT_SPEED);
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, Attributes.ARMOR);
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, Attributes.ARMOR_TOUGHNESS);
    }
}
