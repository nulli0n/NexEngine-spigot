package su.nexmedia.engine.nms;

import com.google.common.collect.Multimap;
import io.netty.channel.Channel;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class V1_16_R3 implements NMS {

    @Override
    @NotNull
    public Channel getChannel(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
    }

    @Override
    public void sendPacket(@NotNull Player player, @NotNull Object packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet<?>) packet);
    }

    @Override
    public void sendAttackPacket(@NotNull Player p, int id) {
        CraftPlayer player = (CraftPlayer) p;
        net.minecraft.server.v1_16_R3.Entity entity = player.getHandle();
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(entity, id);
        player.getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Block block) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        return entityPlayer.playerInteractManager.breakBlock(position);
    }

    @Override
    public float getBlockStrength(@NotNull Block block) {
        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        BlockBase.BlockData blockData = ((CraftWorld)block.getWorld()).getHandle().getType(pos);
        return blockData.strength;
    }

    @Override
    public float getBlockDurability(@NotNull Block block) {
        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        BlockBase.BlockData blockData = ((CraftWorld)block.getWorld()).getHandle().getType(pos);
        return blockData.getBlock().getDurability();
    }

    @Override
    @NotNull
    public List<ItemStack> getBlockDrops(@NotNull Block block, @NotNull Player player, @NotNull ItemStack tool) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());

        World nmsWorld = ((org.bukkit.craftbukkit.v1_16_R3.CraftWorld) player.getWorld()).getHandle();
        IBlockData nmsData = nmsWorld.getType(position);
        TileEntity nmsTile = nmsWorld.getTileEntity(position);
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack.asNMSCopy(tool);

        EntityPlayer entityPlayer = ((org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer) player).getHandle();

        return net.minecraft.server.v1_16_R3.Block.getDrops(
            nmsData, nmsWorld.getMinecraftWorld(), position, nmsTile, entityPlayer, nmsItem)
            .stream().map(CraftItemStack::asBukkitCopy).toList();
    }

    @Override
    @NotNull
    public String toJSON(@NotNull ItemStack item) {
        NBTTagCompound c = new NBTTagCompound();
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        c = nmsItem.save(c);
        String js = c.toString();
        if (js.length() > 32767) {
            ItemStack item2 = new ItemStack(item.getType());
            return toJSON(item2);
        }

        return js;
    }

    @Override
    @NotNull
    public String getNBTTag(@NotNull ItemStack item) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.getTag();
        return compound == null ? "null" : compound.toString();
    }

    @Override
    @Nullable
    public String toBase64(@NotNull ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        NBTTagList nbtTagListItems = new NBTTagList();
        NBTTagCompound nbtTagCompoundItem = new NBTTagCompound();

        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        nmsItem.save(nbtTagCompoundItem);

        nbtTagListItems.add(nbtTagCompoundItem);

        try {
            NBTCompressedStreamTools.a(nbtTagCompoundItem, (DataOutput) dataOutput);
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

        NBTTagCompound nbtTagCompoundRoot;
        try {
            nbtTagCompoundRoot = NBTCompressedStreamTools.a((DataInput) new DataInputStream(inputStream));
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        net.minecraft.server.v1_16_R3.ItemStack nmsItem = net.minecraft.server.v1_16_R3.ItemStack.a(nbtTagCompoundRoot); // .createStack(nbtTagCompoundRoot);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    @NotNull
    public ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        // CraftItemStack craftItem = (CraftItemStack) item;
        net.minecraft.server.v1_16_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        EntityPlayer nmsPlayer = player != null ? ((CraftPlayer) player).getHandle() : null;
        nmsStack.isDamaged(amount, new Random(), nmsPlayer);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    @Deprecated
    @Override
    @NotNull
    public String fixColors(@NotNull String str) {
        str = str.replace("\n", "%n%"); // CraftChatMessage wipes all lines out.

        IChatBaseComponent baseComponent = CraftChatMessage.fromStringOrNull(str);
        String singleColor = CraftChatMessage.fromComponent(baseComponent);
        return singleColor.replace("%n%", "\n");
    }

    @Nullable
    private Multimap<AttributeBase, AttributeModifier> getAttributes(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        Multimap<AttributeBase, AttributeModifier> attMap = null;

        if (item instanceof ItemArmor tool) {
            attMap = tool.a(tool.b());
        }
        else if (item instanceof ItemTool tool) {
            attMap = tool.a(EnumItemSlot.MAINHAND);
        }
        else if (item instanceof ItemSword tool) {
            attMap = tool.a(EnumItemSlot.MAINHAND);
        }
        else if (item instanceof ItemTrident tool) {
            attMap = tool.a(EnumItemSlot.MAINHAND);
        }

        return attMap;
    }

    private double getAttributeValue(@NotNull ItemStack item, @NotNull AttributeBase base) {
        Multimap<AttributeBase, AttributeModifier> attMap = this.getAttributes(item);
        if (attMap == null) return 0D;

        Collection<AttributeModifier> att = attMap.get(base);
        return (att == null || att.isEmpty()) ? 0 : att.stream().findFirst().get().getAmount();// + 1;
    }

    @Override
    public boolean isWeapon(@NotNull ItemStack itemStack) {
        return this.isSword(itemStack) || this.isAxe(itemStack) || this.isTrident(itemStack);
    }

    @Override
    public boolean isSword(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemSword;
    }

    @Override
    public boolean isAxe(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemAxe;
    }

    @Override
    public boolean isTrident(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemTrident;
    }

    @Override
    public boolean isPickaxe(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemPickaxe;
    }

    @Override
    public boolean isShovel(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemSpade;
    }

    @Override
    public boolean isHoe(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemHoe;
    }

    @Override
    public boolean isTool(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemTool;
    }

    @Override
    public boolean isArmor(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemArmor;
    }

    private boolean isArmorSlot(@NotNull ItemStack itemStack, @NotNull EnumItemSlot slot) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        if (item instanceof ItemArmor armor) {
            return armor.b() == slot;
        }
        return false;
    }

    @Override
    public boolean isHelmet(@NotNull ItemStack itemStack) {
        return this.isArmorSlot(itemStack, EnumItemSlot.HEAD);
    }

    @Override
    public boolean isChestplate(@NotNull ItemStack itemStack) {
        return this.isArmorSlot(itemStack, EnumItemSlot.CHEST);
    }

    @Override
    public boolean isLeggings(@NotNull ItemStack itemStack) {
        return this.isArmorSlot(itemStack, EnumItemSlot.LEGS);
    }

    @Override
    public boolean isBoots(@NotNull ItemStack itemStack) {
        return this.isArmorSlot(itemStack, EnumItemSlot.FEET);
    }

    @Override
    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.ATTACK_DAMAGE);
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.ATTACK_SPEED);
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.ARMOR);
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.ARMOR_TOUGHNESS);
    }
}
