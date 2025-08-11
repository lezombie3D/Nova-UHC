/*    */
package net.novaproject.novauhc.utils.jnbt.v2;
/*    */
/*    */

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

/*    */
/*    */ public class CompoundTagBuilder {
    /*    */   private final Map<String, Tag> entries;

    /*    */
    /*    */   CompoundTagBuilder() {
        /* 11 */
        this.entries = new HashMap<>();
        /*    */
    }

    /*    */
    /*    */   CompoundTagBuilder(Map<String, Tag> value) {
        /* 15 */
        Preconditions.checkNotNull(value);
        /* 16 */
        this.entries = value;
        /*    */
    }

    /*    */
    /*    */
    public static CompoundTagBuilder create() {
        /* 74 */
        return new CompoundTagBuilder();
        /*    */
    }

    /*    */
    /*    */
    public CompoundTagBuilder put(String key, Tag value) {
        /* 20 */
        Preconditions.checkNotNull(key);
        /* 21 */
        Preconditions.checkNotNull(value);
        /* 22 */
        this.entries.put(key, value);
        /* 23 */
        return this;
        /*    */
    }

    /*    */
    /*    */
    public CompoundTagBuilder putByteArray(String key, byte[] value) {
        /* 27 */
        return put(key, new ByteArrayTag(value));
        /*    */
    }

    /*    */
    /*    */
    public CompoundTagBuilder putByte(String key, byte value) {
        /* 31 */
        return put(key, new ByteTag(value));
        /*    */
    }

    /*    */
    /*    */
    public CompoundTagBuilder putDouble(String key, double value) {
        /* 35 */
        return put(key, new DoubleTag(value));
        /*    */
    }

    /*    */
    /*    */
    public CompoundTagBuilder putFloat(String key, float value) {
        /* 39 */
        return put(key, new FloatTag(value));
        /*    */
    }

    /*    */
    /*    */
    public CompoundTagBuilder putIntArray(String key, int[] value) {
        /* 43 */
        return put(key, new IntArrayTag(value));
        /*    */
    }

    /*    */
    /*    */
    public CompoundTagBuilder putInt(String key, int value) {
        /* 47 */
        return put(key, new IntTag(value));
        /*    */
    }

    /*    */
    /*    */
    public CompoundTagBuilder putLong(String key, long value) {
        /* 51 */
        return put(key, new LongTag(value));
        /*    */
    }

    /*    */
    /*    */
    public CompoundTagBuilder putShort(String key, short value) {
        /* 55 */
        return put(key, new ShortTag(value));
        /*    */
    }

    /*    */
    /*    */
    public CompoundTagBuilder putString(String key, String value) {
        /* 59 */
        return put(key, new StringTag(value));
        /*    */
    }

    /*    */
    /*    */
    public CompoundTagBuilder putAll(Map<String, ? extends Tag> value) {
        /* 63 */
        Preconditions.checkNotNull(value);
        /* 64 */
        for (Map.Entry<String, ? extends Tag> entry : value.entrySet())
            /* 65 */
            put(entry.getKey(), entry.getValue());
        /* 66 */
        return this;
        /*    */
    }

    /*    */
    /*    */
    public CompoundTag build() {
        /* 70 */
        return new CompoundTag(new HashMap<>(this.entries));
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\CompoundTagBuilder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */