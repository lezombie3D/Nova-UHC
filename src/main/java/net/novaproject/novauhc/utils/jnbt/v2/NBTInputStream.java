/*     */
package net.novaproject.novauhc.utils.jnbt.v2;
/*     */
/*     */

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*     */
/*     */ public class NBTInputStream implements Closeable {
    /*     */   private final DataInputStream is;

    /*     */
    /*     */
    public NBTInputStream(InputStream is) throws IOException {
        /*  16 */
        this.is = new DataInputStream(is);
        /*     */
    }

    /*     */
    /*     */
    public NamedTag readNamedTag() throws IOException {
        /*  20 */
        return readNamedTag(0);
        /*     */
    }

    /*     */
    /*     */
    private NamedTag readNamedTag(int depth) throws IOException {
        /*     */
        String name;
        /*  25 */
        int type = this.is.readByte() & 0xFF;
        /*  26 */
        if (type != 0) {
            /*  27 */
            int nameLength = this.is.readShort() & 0xFFFF;
            /*  28 */
            byte[] nameBytes = new byte[nameLength];
            /*  29 */
            this.is.readFully(nameBytes);
            /*  30 */
            name = new String(nameBytes, NBTConstants.CHARSET);
            /*     */
        } else {
            /*  32 */
            name = "";
            /*     */
        }
        /*  34 */
        return new NamedTag(name, readTagPayload(type, depth));
        /*     */
    }

    /*     */
    /*     */
    private Tag readTagPayload(int type, int depth) throws IOException {
        /*     */
        int k;
        /*     */
        int childType;
        /*     */
        Map<String, Tag> tagMap;
        /*     */
        int length;
        /*     */
        byte[] bytes;
        /*     */
        int m;
        /*     */
        int[] data;
        /*     */
        List<Tag> tagList;
        /*     */
        int j;
        /*     */
        int i;
        /*  48 */
        switch (type) {
            /*     */
            case 0:
                /*  50 */
                if (depth == 0)
                    /*  51 */ throw new IOException("TAG_End found without a TAG_Compound/TAG_List tag preceding it.");
                /*  52 */
                return new EndTag();
            /*     */
            case 1:
                /*  54 */
                return new ByteTag(this.is.readByte());
            /*     */
            case 2:
                /*  56 */
                return new ShortTag(this.is.readShort());
            /*     */
            case 3:
                /*  58 */
                return new IntTag(this.is.readInt());
            /*     */
            case 4:
                /*  60 */
                return new LongTag(this.is.readLong());
            /*     */
            case 5:
                /*  62 */
                return new FloatTag(this.is.readFloat());
            /*     */
            case 6:
                /*  64 */
                return new DoubleTag(this.is.readDouble());
            /*     */
            case 7:
                /*  66 */
                k = this.is.readInt();
                /*  67 */
                bytes = new byte[k];
                /*  68 */
                this.is.readFully(bytes);
                /*  69 */
                return new ByteArrayTag(bytes);
            /*     */
            case 8:
                /*  71 */
                k = this.is.readShort();
                /*  72 */
                bytes = new byte[k];
                /*  73 */
                this.is.readFully(bytes);
                /*  74 */
                return new StringTag(new String(bytes, NBTConstants.CHARSET));
            /*     */
            case 9:
                /*  76 */
                childType = this.is.readByte();
                /*  77 */
                m = this.is.readInt();
                /*  78 */
                tagList = new ArrayList<>();
                /*  79 */
                for (i = 0; i < m; i++) {
                    /*  80 */
                    Tag tag = readTagPayload(childType, depth + 1);
                    /*  81 */
                    if (tag instanceof EndTag)
                        /*  82 */ throw new IOException("TAG_End not permitted in a list.");
                    /*  83 */
                    tagList.add(tag);
                    /*     */
                }
                /*  85 */
                return new ListTag(NBTUtils.getTypeClass(childType), tagList);
            /*     */
            case 10:
                /*  87 */
                tagMap = new HashMap<>();
                /*     */
                while (true) {
                    /*  89 */
                    NamedTag namedTag = readNamedTag(depth + 1);
                    /*  90 */
                    Tag tag2 = namedTag.getTag();
                    /*  91 */
                    if (tag2 instanceof EndTag)
                        /*     */ break;
                    /*  93 */
                    tagMap.put(namedTag.getName(), tag2);
                    /*     */
                }
                /*  95 */
                return new CompoundTag(tagMap);
            /*     */
            case 11:
                /*  97 */
                length = this.is.readInt();
                /*  98 */
                data = new int[length];
                /*  99 */
                for (j = 0; j < length; j++)
                    /* 100 */
                    data[j] = this.is.readInt();
                /* 101 */
                return new IntArrayTag(data);
            /*     */
        }
        /* 103 */
        throw new IOException("Invalid tag type: " + type + ".");
        /*     */
    }

    /*     */
    /*     */
    public void close() throws IOException {
        /* 107 */
        this.is.close();
        /*     */
    }
    /*     */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\NBTInputStream.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */