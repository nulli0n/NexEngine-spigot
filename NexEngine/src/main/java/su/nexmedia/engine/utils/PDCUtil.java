package su.nexmedia.engine.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class PDCUtil {

    public static final PersistentDataType<byte[], double[]> DOUBLE_ARRAY = new DoubleArray();
    public static final PersistentDataType<byte[], String[]> STRING_ARRAY = new StringArray(StandardCharsets.UTF_8);
    public static final PersistentDataType<byte[], UUID>     UUID         = new UUIDDataType();

    @Nullable
    public static <Z> Z getData(@NotNull PersistentDataHolder holder, @NotNull PersistentDataType<?, Z> type, @NotNull NamespacedKey key) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        if (container.has(key, type)) {
            return container.get(key, type);
        }
        return null;
    }

    public static void setData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull Object value) {
        PersistentDataContainer container = holder.getPersistentDataContainer();

        if (value instanceof Boolean i) {
            container.set(key, PersistentDataType.INTEGER, i ? 1 : 0);
        }
        else if (value instanceof Double i) {
            container.set(key, PersistentDataType.DOUBLE, i);
        }
        else if (value instanceof Integer i) {
            container.set(key, PersistentDataType.INTEGER, i);
        }
        else if (value instanceof Long i) {
            container.set(key, PersistentDataType.LONG, i);
        }
        else if (value instanceof String[] i) {
            container.set(key, STRING_ARRAY, i);
        }
        else if (value instanceof double[] i) {
            container.set(key, DOUBLE_ARRAY, i);
        }
        else if (value instanceof UUID i) {
            container.set(key, UUID, i);
        }
        else {
            String i = value.toString();
            container.set(key, PersistentDataType.STRING, i);
        }

        if (holder instanceof BlockState state) {
            state.update();
        }
    }

    public static void removeData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        container.remove(key);

        if (holder instanceof BlockState state) {
            state.update();
        }
    }

    @Nullable
    public static String getStringData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        return getData(holder, PersistentDataType.STRING, key);
    }

    @Nullable
    public static String[] getStringArrayData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        return getData(holder, STRING_ARRAY, key);
    }

    public static double[] getDoubleArrayData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        return getData(holder, DOUBLE_ARRAY, key);
    }

    public static int getIntData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        Integer value = getData(holder, PersistentDataType.INTEGER, key);
        return value == null ? 0 : value;
    }

    public static long getLongData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        Long value = getData(holder, PersistentDataType.LONG, key);
        return value == null ? 0 : value;
    }

    public static double getDoubleData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        Double value = getData(holder, PersistentDataType.DOUBLE, key);
        return value == null ? 0D : value;
    }

    public static boolean getBooleanData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        int value = getIntData(holder, key);
        return value == 1;
    }

    @Nullable
    public static UUID getUniqueIdData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        return getData(holder, UUID, key);
    }

    @Nullable
    public static <Z> Z getData(@NotNull ItemStack item, @NotNull PersistentDataType<?, Z> type, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        return getData(meta, type, key);
    }

    public static void setData(@NotNull ItemStack item, @NotNull NamespacedKey key, @NotNull Object value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        setData(meta, key, value);
        item.setItemMeta(meta);
    }

    public static void removeData(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        removeData(meta, key);
        item.setItemMeta(meta);
    }

    @Nullable
    public static String getStringData(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? null : getStringData(meta, key);
    }

    public static int getIntData(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? 0 : getIntData(meta, key);
    }
    public static long getLongData(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? 0 : getLongData(meta, key);
    }

    @Nullable
    public static String[] getStringArrayData(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? null : getStringArrayData(meta, key);
    }

    public static double[] getDoubleArrayData(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? null : getDoubleArrayData(meta, key);
    }

    public static double getDoubleData(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? 0 : getDoubleData(meta, key);
    }

    public static boolean getBooleanData(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && getBooleanData(meta, key);
    }

    @Nullable
    public static UUID getUniqueIdData(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? null : getUniqueIdData(meta, key);
    }

    public static class DoubleArray implements PersistentDataType<byte[], double[]> {

        @Override
        @NotNull
        public Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        @NotNull
        public Class<double[]> getComplexType() {
            return double[].class;
        }

        @Override
        public byte @NotNull [] toPrimitive(double[] complex, @NotNull PersistentDataAdapterContext context) {
            ByteBuffer bb = ByteBuffer.allocate(complex.length * 8);
            for (double d : complex) {
                bb.putDouble(d);
            }
            return bb.array();
        }

        @Override
        public double @NotNull [] fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            ByteBuffer bb = ByteBuffer.wrap(primitive);
            DoubleBuffer dbuf = bb.asDoubleBuffer(); // Make DoubleBuffer
            double[] a = new double[dbuf.remaining()]; // Make an array of the correct size
            dbuf.get(a);

            return a;
        }
    }

    public static class StringArray implements PersistentDataType<byte[], String[]> {

        private final Charset charset;

        public StringArray(Charset charset) {
            this.charset = charset;
        }

        @NotNull
        @Override
        public Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @NotNull
        @Override
        public Class<String[]> getComplexType() {
            return String[].class;
        }

        @Override
        public byte @NotNull [] toPrimitive(String[] strings, @NotNull PersistentDataAdapterContext itemTagAdapterContext) {
            byte[][] allStringBytes = new byte[strings.length][];
            int total = 0;
            for (int i = 0; i < allStringBytes.length; i++) {
                byte[] bytes = strings[i].getBytes(charset);
                allStringBytes[i] = bytes;
                total += bytes.length;
            }

            ByteBuffer buffer = ByteBuffer.allocate(total + allStringBytes.length * 4); // stores integers
            for (byte[] bytes : allStringBytes) {
                buffer.putInt(bytes.length);
                buffer.put(bytes);
            }

            return buffer.array();
        }

        @Override
        public String @NotNull [] fromPrimitive(byte @NotNull [] bytes, @NotNull PersistentDataAdapterContext itemTagAdapterContext) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            ArrayList<String> list = new ArrayList<>();

            while (buffer.remaining() > 0) {
                if (buffer.remaining() < 4)
                    break;
                int stringLength = buffer.getInt();
                if (buffer.remaining() < stringLength)
                    break;

                byte[] stringBytes = new byte[stringLength];
                buffer.get(stringBytes);

                list.add(new String(stringBytes, charset));
            }

            return list.toArray(new String[0]);
        }
    }

    public static class UUIDDataType implements PersistentDataType<byte[], UUID> {

        @NotNull
        @Override
        public Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @NotNull
        @Override
        public Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Override
        public byte @NotNull [] toPrimitive(UUID complex, @NotNull PersistentDataAdapterContext context) {
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(complex.getMostSignificantBits());
            bb.putLong(complex.getLeastSignificantBits());
            return bb.array();
        }

        @Override
        public @NotNull UUID fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            ByteBuffer bb = ByteBuffer.wrap(primitive);
            long firstLong = bb.getLong();
            long secondLong = bb.getLong();
            return new UUID(firstLong, secondLong);
        }
    }
}
