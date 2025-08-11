/*    */
package net.novaproject.novauhc.utils.jnbt.v2;
/*    */
/*    */

import java.util.Map;

/*    */
/*    */ public class NBTUtils {
    /*    */
    public static String getTypeName(Class<? extends Tag> clazz) {
        /*  7 */
        if (clazz.equals(ByteArrayTag.class))
            /*  8 */ return "TAG_Byte_Array";
        /*  9 */
        if (clazz.equals(ByteTag.class))
            /* 10 */ return "TAG_Byte";
        /* 11 */
        if (clazz.equals(CompoundTag.class))
            /* 12 */ return "TAG_Compound";
        /* 13 */
        if (clazz.equals(DoubleTag.class))
            /* 14 */ return "TAG_Double";
        /* 15 */
        if (clazz.equals(EndTag.class))
            /* 16 */ return "TAG_End";
        /* 17 */
        if (clazz.equals(FloatTag.class))
            /* 18 */ return "TAG_Float";
        /* 19 */
        if (clazz.equals(IntTag.class))
            /* 20 */ return "TAG_Int";
        /* 21 */
        if (clazz.equals(ListTag.class))
            /* 22 */ return "TAG_List";
        /* 23 */
        if (clazz.equals(LongTag.class))
            /* 24 */ return "TAG_Long";
        /* 25 */
        if (clazz.equals(ShortTag.class))
            /* 26 */ return "TAG_Short";
        /* 27 */
        if (clazz.equals(StringTag.class))
            /* 28 */ return "TAG_String";
        /* 29 */
        if (clazz.equals(IntArrayTag.class))
            /* 30 */ return "TAG_Int_Array";
        /* 31 */
        throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
        /*    */
    }

    /*    */
    /*    */
    public static int getTypeCode(Class<? extends Tag> clazz) {
        /* 35 */
        if (clazz.equals(ByteArrayTag.class))
            /* 36 */ return 7;
        /* 37 */
        if (clazz.equals(ByteTag.class))
            /* 38 */ return 1;
        /* 39 */
        if (clazz.equals(CompoundTag.class))
            /* 40 */ return 10;
        /* 41 */
        if (clazz.equals(DoubleTag.class))
            /* 42 */ return 6;
        /* 43 */
        if (clazz.equals(EndTag.class))
            /* 44 */ return 0;
        /* 45 */
        if (clazz.equals(FloatTag.class))
            /* 46 */ return 5;
        /* 47 */
        if (clazz.equals(IntTag.class))
            /* 48 */ return 3;
        /* 49 */
        if (clazz.equals(ListTag.class))
            /* 50 */ return 9;
        /* 51 */
        if (clazz.equals(LongTag.class))
            /* 52 */ return 4;
        /* 53 */
        if (clazz.equals(ShortTag.class))
            /* 54 */ return 2;
        /* 55 */
        if (clazz.equals(StringTag.class))
            /* 56 */ return 8;
        /* 57 */
        if (clazz.equals(IntArrayTag.class))
            /* 58 */ return 11;
        /* 59 */
        throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
        /*    */
    }

    /*    */
    /*    */
    public static Class<? extends Tag> getTypeClass(int type) {
        /* 63 */
        switch (type) {
            /*    */
            case 0:
                /* 65 */
                return EndTag.class;
            /*    */
            case 1:
                /* 67 */
                return ByteTag.class;
            /*    */
            case 2:
                /* 69 */
                return ShortTag.class;
            /*    */
            case 3:
                /* 71 */
                return IntTag.class;
            /*    */
            case 4:
                /* 73 */
                return LongTag.class;
            /*    */
            case 5:
                /* 75 */
                return FloatTag.class;
            /*    */
            case 6:
                /* 77 */
                return DoubleTag.class;
            /*    */
            case 7:
                /* 79 */
                return ByteArrayTag.class;
            /*    */
            case 8:
                /* 81 */
                return StringTag.class;
            /*    */
            case 9:
                /* 83 */
                return ListTag.class;
            /*    */
            case 10:
                /* 85 */
                return CompoundTag.class;
            /*    */
            case 11:
                /* 87 */
                return IntArrayTag.class;
            /*    */
        }
        /* 89 */
        throw new IllegalArgumentException("Invalid tag type : " + type + ".");
        /*    */
    }

    /*    */
    /*    */
    public static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws InvalidFormatException {
        /* 93 */
        if (!items.containsKey(key))
            /* 94 */ throw new InvalidFormatException("Missing a \"" + key + "\" tag");
        /* 95 */
        Tag tag = items.get(key);
        /* 96 */
        if (!expected.isInstance(tag))
            /* 97 */
            throw new InvalidFormatException(key + " tag is not of tag type " + expected.getName());
        /* 98 */
        return expected.cast(tag);
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\NBTUtils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */