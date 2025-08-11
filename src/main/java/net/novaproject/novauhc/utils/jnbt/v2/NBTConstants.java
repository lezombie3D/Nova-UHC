/*    */
package net.novaproject.novauhc.utils.jnbt.v2;
/*    */
/*    */

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/*    */
/*    */ public class NBTConstants {
    /*  6 */   public static final Charset CHARSET = StandardCharsets.UTF_8;
    /*    */
    /*    */   public static final int TYPE_END = 0;
    /*    */
    /*    */   public static final int TYPE_BYTE = 1;
    /*    */
    /*    */   public static final int TYPE_SHORT = 2;
    /*    */
    /*    */   public static final int TYPE_INT = 3;
    /*    */
    /*    */   public static final int TYPE_LONG = 4;
    /*    */
    /*    */   public static final int TYPE_FLOAT = 5;
    /*    */
    /*    */   public static final int TYPE_DOUBLE = 6;
    /*    */
    /*    */   public static final int TYPE_BYTE_ARRAY = 7;
    /*    */
    /*    */   public static final int TYPE_STRING = 8;
    /*    */
    /*    */   public static final int TYPE_LIST = 9;
    /*    */
    /*    */   public static final int TYPE_COMPOUND = 10;
    /*    */
    /*    */   public static final int TYPE_INT_ARRAY = 11;

    /*    */
    /*    */
    public static Class<? extends Tag> getClassFromType(int id) {
        /* 33 */
        switch (id) {
            /*    */
            case 0:
                /* 35 */
                return EndTag.class;
            /*    */
            case 1:
                /* 37 */
                return ByteTag.class;
            /*    */
            case 2:
                /* 39 */
                return ShortTag.class;
            /*    */
            case 3:
                /* 41 */
                return IntTag.class;
            /*    */
            case 4:
                /* 43 */
                return LongTag.class;
            /*    */
            case 5:
                /* 45 */
                return FloatTag.class;
            /*    */
            case 6:
                /* 47 */
                return DoubleTag.class;
            /*    */
            case 7:
                /* 49 */
                return ByteArrayTag.class;
            /*    */
            case 8:
                /* 51 */
                return StringTag.class;
            /*    */
            case 9:
                /* 53 */
                return ListTag.class;
            /*    */
            case 10:
                /* 55 */
                return CompoundTag.class;
            /*    */
            case 11:
                /* 57 */
                return IntArrayTag.class;
            /*    */
        }
        /* 59 */
        throw new IllegalArgumentException("Unknown tag type ID of " + id);
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\NBTConstants.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */