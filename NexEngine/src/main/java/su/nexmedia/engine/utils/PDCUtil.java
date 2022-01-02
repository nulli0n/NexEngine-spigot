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
import java.util.ArrayList;
import java.util.UUID;

public class PDCUtil {

    public static final PersistentDataType<byte[], double[]> DOUBLE_ARRAY = new DoubleArray();
    public static final PersistentDataType<byte[], String[]> STRING_ARRAY = new StringArray(Charset.forName("UTF-8"));
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

        if (value instanceof Boolean) {
            Boolean i = (Boolean) value;
            setData(holder, key, i.booleanValue() ? 1 : 0);
            return;
        }
        if (value instanceof Double) {
            Double i = (Double) value;
            container.set(key, PersistentDataType.DOUBLE, i.doubleValue());
        }
        else if (value instanceof Integer) {
            Integer i = (Integer) value;
            container.set(key, PersistentDataType.INTEGER, i.intValue());
        }
        else if (value instanceof String[]) {
            String[] i = (String[]) value;
            container.set(key, STRING_ARRAY, i);
        }
        else if (value instanceof double[]) {
            double[] i = (double[]) value;
            container.set(key, DOUBLE_ARRAY, i);
        }
        else {
            String i = value.toString();
            container.set(key, PersistentDataType.STRING, i);
        }

        if (holder instanceof BlockState) {
            BlockState state = (BlockState) holder;
            state.update();
        }
    }

    public static void removeData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {

        PersistentDataContainer container = holder.getPersistentDataContainer();
        container.remove(key);

        if (holder instanceof BlockState) {
            BlockState state = (BlockState) holder;
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
        Integer o = getData(holder, PersistentDataType.INTEGER, key);
        if (o == null)
            return 0;

        return o.intValue();
    }

    public static double getDoubleData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        Double o = getData(holder, PersistentDataType.DOUBLE, key);
        if (o == null)
            return 0;

        return o.doubleValue();
    }

    public static boolean getBooleanData(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        int i = getIntData(holder, key);

        return i == 0 ? false : true;
    }

    // ==================================================== //

    @Nullable
    public static <Z> Z getData(@NotNull ItemStack item, @NotNull PersistentDataType<?, Z> type, @NotNull NamespacedKey key) {

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return null;

        return getData(meta, type, key);
    }

    public static void setData(@NotNull ItemStack item, @NotNull NamespacedKey key, @NotNull Object value) {

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

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
        return meta == null ? false : getBooleanData(meta, key);
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
        public double @NotNull [] fromPrimitive(byte[] primitive, @NotNull PersistentDataAdapterContext context) {
            ByteBuffer bb = ByteBuffer.wrap(primitive);
            DoubleBuffer dbuf = bb.asDoubleBuffer(); // Make DoubleBuffer
            double[] a = new double[dbuf.remaining()]; // Make an array of the correct size
            dbuf.get(a);

            return a;
        }
    }

    public static class StringArray implements PersistentDataType<byte[], String[]> {

        private Charset charset;

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
        public byte @NotNull [] toPrimitive(String[] strings, PersistentDataAdapterContext itemTagAdapterContext) {
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
        public String @NotNull [] fromPrimitive(byte[] bytes, PersistentDataAdapterContext itemTagAdapterContext) {
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

            return list.toArray(new String[list.size()]);
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
        public byte @NotNull [] toPrimitive(UUID complex, PersistentDataAdapterContext context) {
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(complex.getMostSignificantBits());
            bb.putLong(complex.getLeastSignificantBits());
            return bb.array();
        }

        @Override
        public @NotNull UUID fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
            ByteBuffer bb = ByteBuffer.wrap(primitive);
            long firstLong = bb.getLong();
            long secondLong = bb.getLong();
            return new UUID(firstLong, secondLong);
        }
    }
}
