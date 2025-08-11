/*    */
package net.novaproject.novauhc.utils.jnbt;

/*    */
/*    */ public final class NBTUtils {
    /*    */
    public static String getTypeName(Class<? extends Tag> clazz) {
        /*  5 */
        if (clazz.equals(ByteArrayTag.class))
            /*  6 */ return "TAG_Byte_Array";
        /*  7 */
        if (clazz.equals(ByteTag.class))
            /*  8 */ return "TAG_Byte";
        /*  9 */
        if (clazz.equals(CompoundTag.class))
            /* 10 */ return "TAG_Compound";
        /* 11 */
        if (clazz.equals(DoubleTag.class))
            /* 12 */ return "TAG_Double";
        /* 13 */
        if (clazz.equals(EndTag.class))
            /* 14 */ return "TAG_End";
        /* 15 */
        if (clazz.equals(FloatTag.class))
            /* 16 */ return "TAG_Float";
        /* 17 */
        if (clazz.equals(IntTag.class))
            /* 18 */ return "TAG_Int";
        /* 19 */
        if (clazz.equals(ListTag.class))
            /* 20 */ return "TAG_List";
        /* 21 */
        if (clazz.equals(LongTag.class))
            /* 22 */ return "TAG_Long";
        /* 23 */
        if (clazz.equals(ShortTag.class))
            /* 24 */ return "TAG_Short";
        /* 25 */
        if (clazz.equals(StringTag.class))
            /* 26 */ return "TAG_String";
        /* 27 */
        throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
        /*    */
    }

    /*    */
    /*    */
    public static int getTypeCode(Class<? extends Tag> clazz) {
        /* 31 */
        if (clazz.equals(ByteArrayTag.class))
            /* 32 */ return 7;
        /* 33 */
        if (clazz.equals(ByteTag.class))
            /* 34 */ return 1;
        /* 35 */
        if (clazz.equals(CompoundTag.class))
            /* 36 */ return 10;
        /* 37 */
        if (clazz.equals(DoubleTag.class))
            /* 38 */ return 6;
        /* 39 */
        if (clazz.equals(EndTag.class))
            /* 40 */ return 0;
        /* 41 */
        if (clazz.equals(FloatTag.class))
            /* 42 */ return 5;
        /* 43 */
        if (clazz.equals(IntTag.class))
            /* 44 */ return 3;
        /* 45 */
        if (clazz.equals(ListTag.class))
            /* 46 */ return 9;
        /* 47 */
        if (clazz.equals(LongTag.class))
            /* 48 */ return 4;
        /* 49 */
        if (clazz.equals(ShortTag.class))
            /* 50 */ return 2;
        /* 51 */
        if (clazz.equals(StringTag.class))
            /* 52 */ return 8;
        /* 53 */
        throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
        /*    */
    }

    /*    */
    /*    */
    public static Class<? extends Tag> getTypeClass(int type) {
        /* 57 */
        switch (type) {
            /*    */
            case 0:
                /* 59 */
                return EndTag.class;
            /*    */
            case 1:
                /* 61 */
                return ByteTag.class;
            /*    */
            case 2:
                /* 63 */
                return ShortTag.class;
            /*    */
            case 3:
                /* 65 */
                return IntTag.class;
            /*    */
            case 4:
                /* 67 */
                return LongTag.class;
            /*    */
            case 5:
                /* 69 */
                return FloatTag.class;
            /*    */
            case 6:
                /* 71 */
                return DoubleTag.class;
            /*    */
            case 7:
                /* 73 */
                return ByteArrayTag.class;
            /*    */
            case 8:
                /* 75 */
                return StringTag.class;
            /*    */
            case 9:
                /* 77 */
                return ListTag.class;
            /*    */
            case 10:
                /* 79 */
                return CompoundTag.class;
            /*    */
        }
        /* 81 */
        throw new IllegalArgumentException("Invalid tag displayer : " + type + ".");
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\NBTUtils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */