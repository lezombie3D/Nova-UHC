/*     */
package net.novaproject.novauhc.utils.jnbt.v2;
/*     */
/*     */

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/*     */
/*     */ public class ListTag
        /*     */ extends Tag {
    /*     */   private final Class<? extends Tag> type;
    /*     */   private final List<Tag> value;

    /*     */
    /*     */
    public ListTag(Class<? extends Tag> type, List<? extends Tag> value) {
        /*  15 */
        Preconditions.checkNotNull(value);
        /*  16 */
        this.type = type;
        /*  17 */
        this.value = Collections.unmodifiableList(value);
        /*     */
    }

    /*     */
    /*     */
    public Class<? extends Tag> getType() {
        /*  21 */
        return this.type;
        /*     */
    }

    /*     */
    /*     */
    public List<Tag> getValue() {
        /*  25 */
        return this.value;
        /*     */
    }

    /*     */
    /*     */
    public ListTag setValue(List<Tag> list) {
        /*  29 */
        return new ListTag(getType(), list);
        /*     */
    }

    /*     */
    /*     */
    @Nullable
    /*     */ public Tag getIfExists(int index) {
        /*     */
        try {
            /*  35 */
            return this.value.get(index);
            /*  36 */
        } catch (NoSuchElementException e) {
            /*  37 */
            return null;
            /*     */
        }
        /*     */
    }

    /*     */
    /*     */
    public byte[] getByteArray(int index) {
        /*  42 */
        Tag tag = getIfExists(index);
        /*  43 */
        if (tag instanceof ByteArrayTag)
            /*  44 */ return ((ByteArrayTag) tag).getValue();
        /*  45 */
        return new byte[0];
        /*     */
    }

    /*     */
    /*     */
    public byte getByte(int index) {
        /*  49 */
        Tag tag = getIfExists(index);
        /*  50 */
        if (tag instanceof ByteTag)
            /*  51 */ return ((ByteTag) tag).getValue().byteValue();
        /*  52 */
        return 0;
        /*     */
    }

    /*     */
    /*     */
    public double getDouble(int index) {
        /*  56 */
        Tag tag = getIfExists(index);
        /*  57 */
        if (tag instanceof DoubleTag)
            /*  58 */ return ((DoubleTag) tag).getValue().doubleValue();
        /*  59 */
        return 0.0D;
        /*     */
    }

    /*     */
    /*     */
    public double asDouble(int index) {
        /*  63 */
        Tag tag = getIfExists(index);
        /*  64 */
        if (tag instanceof ByteTag)
            /*  65 */ return ((ByteTag) tag).getValue().byteValue();
        /*  66 */
        if (tag instanceof ShortTag)
            /*  67 */ return ((ShortTag) tag).getValue().shortValue();
        /*  68 */
        if (tag instanceof IntTag)
            /*  69 */ return ((IntTag) tag).getValue().intValue();
        /*  70 */
        if (tag instanceof LongTag)
            /*  71 */ return ((LongTag) tag).getValue().longValue();
        /*  72 */
        if (tag instanceof FloatTag)
            /*  73 */ return ((FloatTag) tag).getValue().floatValue();
        /*  74 */
        if (tag instanceof DoubleTag)
            /*  75 */ return ((DoubleTag) tag).getValue().doubleValue();
        /*  76 */
        return 0.0D;
        /*     */
    }

    /*     */
    /*     */
    public float getFloat(int index) {
        /*  80 */
        Tag tag = getIfExists(index);
        /*  81 */
        if (tag instanceof FloatTag)
            /*  82 */ return ((FloatTag) tag).getValue().floatValue();
        /*  83 */
        return 0.0F;
        /*     */
    }

    /*     */
    /*     */
    public int[] getIntArray(int index) {
        /*  87 */
        Tag tag = getIfExists(index);
        /*  88 */
        if (tag instanceof IntArrayTag)
            /*  89 */ return ((IntArrayTag) tag).getValue();
        /*  90 */
        return new int[0];
        /*     */
    }

    /*     */
    /*     */
    public int getInt(int index) {
        /*  94 */
        Tag tag = getIfExists(index);
        /*  95 */
        if (tag instanceof IntTag)
            /*  96 */ return ((IntTag) tag).getValue().intValue();
        /*  97 */
        return 0;
        /*     */
    }

    /*     */
    /*     */
    public int asInt(int index) {
        /* 101 */
        Tag tag = getIfExists(index);
        /* 102 */
        if (tag instanceof ByteTag)
            /* 103 */ return ((ByteTag) tag).getValue().byteValue();
        /* 104 */
        if (tag instanceof ShortTag)
            /* 105 */ return ((ShortTag) tag).getValue().shortValue();
        /* 106 */
        if (tag instanceof IntTag)
            /* 107 */ return ((IntTag) tag).getValue().intValue();
        /* 108 */
        if (tag instanceof LongTag)
            /* 109 */ return ((LongTag) tag).getValue().intValue();
        /* 110 */
        if (tag instanceof FloatTag)
            /* 111 */ return ((FloatTag) tag).getValue().intValue();
        /* 112 */
        if (tag instanceof DoubleTag)
            /* 113 */ return ((DoubleTag) tag).getValue().intValue();
        /* 114 */
        return 0;
        /*     */
    }

    /*     */
    /*     */
    public List<Tag> getList(int index) {
        /* 118 */
        Tag tag = getIfExists(index);
        /* 119 */
        if (tag instanceof ListTag)
            /* 120 */ return ((ListTag) tag).getValue();
        /* 121 */
        return Collections.emptyList();
        /*     */
    }

    /*     */
    /*     */
    public ListTag getListTag(int index) {
        /* 125 */
        Tag tag = getIfExists(index);
        /* 126 */
        if (tag instanceof ListTag)
            /* 127 */ return (ListTag) tag;
        /* 128 */
        return new ListTag(StringTag.class, Collections.emptyList());
        /*     */
    }

    /*     */
    /*     */
    public <T extends Tag> List<T> getList(int index, Class<T> listType) {
        /* 132 */
        Tag tag = getIfExists(index);
        /* 133 */
        if (!(tag instanceof ListTag))
            /* 134 */ return Collections.emptyList();
        /* 135 */
        ListTag listTag = (ListTag) tag;
        /* 136 */
        if (listTag.getType().equals(listType))
            /* 137 */ return (List) listTag.getValue();
        /* 138 */
        return Collections.emptyList();
        /*     */
    }

    /*     */
    /*     */
    public long getLong(int index) {
        /* 142 */
        Tag tag = getIfExists(index);
        /* 143 */
        if (tag instanceof LongTag)
            /* 144 */ return ((LongTag) tag).getValue().longValue();
        /* 145 */
        return 0L;
        /*     */
    }

    /*     */
    /*     */
    public long asLong(int index) {
        /* 149 */
        Tag tag = getIfExists(index);
        /* 150 */
        if (tag instanceof ByteTag)
            /* 151 */ return ((ByteTag) tag).getValue().byteValue();
        /* 152 */
        if (tag instanceof ShortTag)
            /* 153 */ return ((ShortTag) tag).getValue().shortValue();
        /* 154 */
        if (tag instanceof IntTag)
            /* 155 */ return ((IntTag) tag).getValue().intValue();
        /* 156 */
        if (tag instanceof LongTag)
            /* 157 */ return ((LongTag) tag).getValue().longValue();
        /* 158 */
        if (tag instanceof FloatTag)
            /* 159 */ return ((FloatTag) tag).getValue().longValue();
        /* 160 */
        if (tag instanceof DoubleTag)
            /* 161 */ return ((DoubleTag) tag).getValue().longValue();
        /* 162 */
        return 0L;
        /*     */
    }

    /*     */
    /*     */
    public short getShort(int index) {
        /* 166 */
        Tag tag = getIfExists(index);
        /* 167 */
        if (tag instanceof ShortTag)
            /* 168 */ return ((ShortTag) tag).getValue().shortValue();
        /* 169 */
        return 0;
        /*     */
    }

    /*     */
    /*     */
    public String getString(int index) {
        /* 173 */
        Tag tag = getIfExists(index);
        /* 174 */
        if (tag instanceof StringTag)
            /* 175 */ return ((StringTag) tag).getValue();
        /* 176 */
        return "";
        /*     */
    }

    /*     */
    /*     */
    public String toString() {
        /* 180 */
        StringBuilder bldr = new StringBuilder();
        /* 181 */
        bldr.append("TAG_List").append(": ").append(this.value.size()).append(" entries of type ").append(NBTUtils.getTypeName(this.type)).append("\r\n{\r\n");
        /* 182 */
        for (Tag t : this.value)
            /* 183 */
            bldr.append("   ").append(t.toString().replaceAll("\r\n", "\r\n   ")).append("\r\n");
        /* 184 */
        bldr.append("}");
        /* 185 */
        return bldr.toString();
        /*     */
    }
    /*     */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\ListTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */