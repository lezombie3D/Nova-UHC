/*     */
package net.novaproject.novauhc.utils.jnbt.v2;
/*     */
/*     */

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*     */
/*     */ public class CompoundTag extends Tag {
    /*     */   private final Map<String, Tag> value;

    /*     */
    /*     */
    public CompoundTag(Map<String, Tag> value) {
        /*  12 */
        this.value = Collections.unmodifiableMap(value);
        /*     */
    }

    /*     */
    /*     */
    public boolean containsKey(String key) {
        /*  16 */
        return this.value.containsKey(key);
        /*     */
    }

    /*     */
    /*     */
    public Map<String, Tag> getValue() {
        /*  20 */
        return this.value;
        /*     */
    }

    /*     */
    /*     */
    public CompoundTag setValue(Map<String, Tag> value) {
        /*  24 */
        return new CompoundTag(value);
        /*     */
    }

    /*     */
    /*     */
    public CompoundTagBuilder createBuilder() {
        /*  28 */
        return new CompoundTagBuilder(new HashMap<>(this.value));
        /*     */
    }

    /*     */
    /*     */
    public byte[] getByteArray(String key) {
        /*  32 */
        Tag tag = this.value.get(key);
        /*  33 */
        if (tag instanceof ByteArrayTag)
            /*  34 */ return ((ByteArrayTag) tag).getValue();
        /*  35 */
        return new byte[0];
        /*     */
    }

    /*     */
    /*     */
    public byte getByte(String key) {
        /*  39 */
        Tag tag = this.value.get(key);
        /*  40 */
        if (tag instanceof ByteTag)
            /*  41 */ return ((ByteTag) tag).getValue().byteValue();
        /*  42 */
        return 0;
        /*     */
    }

    /*     */
    /*     */
    public double getDouble(String key) {
        /*  46 */
        Tag tag = this.value.get(key);
        /*  47 */
        if (tag instanceof DoubleTag)
            /*  48 */ return ((DoubleTag) tag).getValue().doubleValue();
        /*  49 */
        return 0.0D;
        /*     */
    }

    /*     */
    /*     */
    public double asDouble(String key) {
        /*  53 */
        Tag tag = this.value.get(key);
        /*  54 */
        if (tag instanceof ByteTag)
            /*  55 */ return ((ByteTag) tag).getValue().byteValue();
        /*  56 */
        if (tag instanceof ShortTag)
            /*  57 */ return ((ShortTag) tag).getValue().shortValue();
        /*  58 */
        if (tag instanceof IntTag)
            /*  59 */ return ((IntTag) tag).getValue().intValue();
        /*  60 */
        if (tag instanceof LongTag)
            /*  61 */ return ((LongTag) tag).getValue().longValue();
        /*  62 */
        if (tag instanceof FloatTag)
            /*  63 */ return ((FloatTag) tag).getValue().floatValue();
        /*  64 */
        if (tag instanceof DoubleTag)
            /*  65 */ return ((DoubleTag) tag).getValue().doubleValue();
        /*  66 */
        return 0.0D;
        /*     */
    }

    /*     */
    /*     */
    public float getFloat(String key) {
        /*  70 */
        Tag tag = this.value.get(key);
        /*  71 */
        if (tag instanceof FloatTag)
            /*  72 */ return ((FloatTag) tag).getValue().floatValue();
        /*  73 */
        return 0.0F;
        /*     */
    }

    /*     */
    /*     */
    public int[] getIntArray(String key) {
        /*  77 */
        Tag tag = this.value.get(key);
        /*  78 */
        if (tag instanceof IntArrayTag)
            /*  79 */ return ((IntArrayTag) tag).getValue();
        /*  80 */
        return new int[0];
        /*     */
    }

    /*     */
    /*     */
    public int getInt(String key) {
        /*  84 */
        Tag tag = this.value.get(key);
        /*  85 */
        if (tag instanceof IntTag)
            /*  86 */ return ((IntTag) tag).getValue().intValue();
        /*  87 */
        return 0;
        /*     */
    }

    /*     */
    /*     */
    public int asInt(String key) {
        /*  91 */
        Tag tag = this.value.get(key);
        /*  92 */
        if (tag instanceof ByteTag)
            /*  93 */ return ((ByteTag) tag).getValue().byteValue();
        /*  94 */
        if (tag instanceof ShortTag)
            /*  95 */ return ((ShortTag) tag).getValue().shortValue();
        /*  96 */
        if (tag instanceof IntTag)
            /*  97 */ return ((IntTag) tag).getValue().intValue();
        /*  98 */
        if (tag instanceof LongTag)
            /*  99 */ return ((LongTag) tag).getValue().intValue();
        /* 100 */
        if (tag instanceof FloatTag)
            /* 101 */ return ((FloatTag) tag).getValue().intValue();
        /* 102 */
        if (tag instanceof DoubleTag)
            /* 103 */ return ((DoubleTag) tag).getValue().intValue();
        /* 104 */
        return 0;
        /*     */
    }

    /*     */
    /*     */
    public List<Tag> getList(String key) {
        /* 108 */
        Tag tag = this.value.get(key);
        /* 109 */
        if (tag instanceof ListTag)
            /* 110 */ return ((ListTag) tag).getValue();
        /* 111 */
        return Collections.emptyList();
        /*     */
    }

    /*     */
    /*     */
    public ListTag getListTag(String key) {
        /* 115 */
        Tag tag = this.value.get(key);
        /* 116 */
        if (tag instanceof ListTag)
            /* 117 */ return (ListTag) tag;
        /* 118 */
        return new ListTag(StringTag.class, Collections.emptyList());
        /*     */
    }

    /*     */
    /*     */
    public <T extends Tag> List<T> getList(String key, Class<T> listType) {
        /* 122 */
        Tag tag = this.value.get(key);
        /* 123 */
        if (!(tag instanceof ListTag))
            /* 124 */ return Collections.emptyList();
        /* 125 */
        ListTag listTag = (ListTag) tag;
        /* 126 */
        if (listTag.getType().equals(listType))
            /* 127 */ return (List) listTag.getValue();
        /* 128 */
        return Collections.emptyList();
        /*     */
    }

    /*     */
    /*     */
    public long getLong(String key) {
        /* 132 */
        Tag tag = this.value.get(key);
        /* 133 */
        if (tag instanceof LongTag)
            /* 134 */ return ((LongTag) tag).getValue().longValue();
        /* 135 */
        return 0L;
        /*     */
    }

    /*     */
    /*     */
    public long asLong(String key) {
        /* 139 */
        Tag tag = this.value.get(key);
        /* 140 */
        if (tag instanceof ByteTag)
            /* 141 */ return ((ByteTag) tag).getValue().byteValue();
        /* 142 */
        if (tag instanceof ShortTag)
            /* 143 */ return ((ShortTag) tag).getValue().shortValue();
        /* 144 */
        if (tag instanceof IntTag)
            /* 145 */ return ((IntTag) tag).getValue().intValue();
        /* 146 */
        if (tag instanceof LongTag)
            /* 147 */ return ((LongTag) tag).getValue().longValue();
        /* 148 */
        if (tag instanceof FloatTag)
            /* 149 */ return ((FloatTag) tag).getValue().longValue();
        /* 150 */
        if (tag instanceof DoubleTag)
            /* 151 */ return ((DoubleTag) tag).getValue().longValue();
        /* 152 */
        return 0L;
        /*     */
    }

    /*     */
    /*     */
    public short getShort(String key) {
        /* 156 */
        Tag tag = this.value.get(key);
        /* 157 */
        if (tag instanceof ShortTag)
            /* 158 */ return ((ShortTag) tag).getValue().shortValue();
        /* 159 */
        return 0;
        /*     */
    }

    /*     */
    /*     */
    public String getString(String key) {
        /* 163 */
        Tag tag = this.value.get(key);
        /* 164 */
        if (tag instanceof StringTag)
            /* 165 */ return ((StringTag) tag).getValue();
        /* 166 */
        return "";
        /*     */
    }

    /*     */
    /*     */
    public String toString() {
        /* 170 */
        StringBuilder bldr = new StringBuilder();
        /* 171 */
        bldr.append("TAG_Compound").append(": ").append(this.value.size()).append(" entries\r\n{\r\n");
        /* 172 */
        for (Map.Entry<String, Tag> entry : this.value.entrySet())
            /* 173 */
            bldr.append("   ").append(entry.getValue().toString().replaceAll("\r\n", "\r\n   ")).append("\r\n");
        /* 174 */
        bldr.append("}");
        /* 175 */
        return bldr.toString();
        /*     */
    }
    /*     */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\CompoundTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */