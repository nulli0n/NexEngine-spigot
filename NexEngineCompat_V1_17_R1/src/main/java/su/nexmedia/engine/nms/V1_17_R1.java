package su.nexmedia.engine.nms;

import com.google.common.collect.Multimap;
import io.netty.channel.Channel;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Random;


public class V1_17_R1 implements NMS {

    @Override
    @NotNull
    public Channel getChannel(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle().b.a().k;
    }

    @Override
    public void sendPacket(@NotNull Player player, @NotNull Object packet) {
        ((CraftPlayer) player).getHandle().b.sendPacket((Packet<?>) packet);
    }

    @Override
    public void sendAttackPacket(@NotNull Player p, int id) {
        CraftPlayer player = (CraftPlayer) p;
        Entity entity = player.getHandle();
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(entity, id);
        player.getHandle().b.sendPacket(packet);
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Block block) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerInteractManager manager = entityPlayer.d;
        return manager.breakBlock(position);
    }

    @Override
    @NotNull
    public List<ItemStack> getBlockDrops(@NotNull Block block, @NotNull Player player, @NotNull ItemStack tool) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());

        World nmsWorld = ((CraftWorld)player.getWorld()).getHandle();
        IBlockData nmsData = nmsWorld.getType(position);
        TileEntity nmsTile = nmsWorld.getTileEntity(position);
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(tool);

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        return net.minecraft.world.level.block.Block.getDrops(
            nmsData, nmsWorld.getMinecraftWorld(), position, nmsTile, entityPlayer, nmsItem)
            .stream().map(CraftItemStack::asBukkitCopy).toList();
    }

    @Override
    @NotNull
    public String toJSON(@NotNull ItemStack item) {
        NBTTagCompound c = new NBTTagCompound();
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        c = nmsItem.save(c);
        String js = c.toString();
        if (js.length() > 32767) {
            ItemStack item2 = new ItemStack(item.getType());
            return toJSON(item2);
        }

        return js;
    }

    @Override
    @Nullable
    public String toBase64(@NotNull ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        NBTTagList nbtTagListItems = new NBTTagList();
        NBTTagCompound nbtTagCompoundItem = new NBTTagCompound();

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        nmsItem.save(nbtTagCompoundItem);

        nbtTagListItems.add(nbtTagCompoundItem);

        try {
            NBTCompressedStreamTools.a(nbtTagCompoundItem, (DataOutput) dataOutput);
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

        NBTTagCompound nbtTagCompoundRoot;
        try {
            nbtTagCompoundRoot = NBTCompressedStreamTools.a((DataInput) new DataInputStream(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.a(nbtTagCompoundRoot); // .createStack(nbtTagCompoundRoot);
        ItemStack item = CraftItemStack.asBukkitCopy(nmsItem);

        return item;
    }

    @Override
    @NotNull
    public ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        // CraftItemStack craftItem = (CraftItemStack) item;
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

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

        if (item instanceof ItemArmor) {
            ItemArmor tool = (ItemArmor) item;
            attMap = tool.a(tool.b());
        }
        else if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool) item;
            attMap = tool.a(EnumItemSlot.a);
        }
        else if (item instanceof ItemSword) {
            ItemSword tool = (ItemSword) item;
            attMap = tool.a(EnumItemSlot.a);
        }
        else if (item instanceof ItemTrident) {
            ItemTrident tool = (ItemTrident) item;
            attMap = tool.a(EnumItemSlot.a);
        }

        return attMap;
    }

    private double getAttributeValue(@NotNull ItemStack item, @NotNull AttributeBase base) {
        Multimap<AttributeBase, AttributeModifier> attMap = this.getAttributes(item);
        if (attMap == null)
            return 0D;

        Collection<AttributeModifier> att = attMap.get(base);
        double damage = (att == null || att.isEmpty()) ? 0 : att.stream().findFirst().get().getAmount();

        return damage;// + 1;
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
        return this.isArmorSlot(itemStack, EnumItemSlot.f);
    }

    @Override
    public boolean isChestplate(@NotNull ItemStack itemStack) {
        return this.isArmorSlot(itemStack, EnumItemSlot.e);
    }

    @Override
    public boolean isLeggings(@NotNull ItemStack itemStack) {
        return this.isArmorSlot(itemStack, EnumItemSlot.d);
    }

    @Override
    public boolean isBoots(@NotNull ItemStack itemStack) {
        return this.isArmorSlot(itemStack, EnumItemSlot.c);
    }

    @Override
    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.f);
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.h);
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.i);
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.j);
    }
}
