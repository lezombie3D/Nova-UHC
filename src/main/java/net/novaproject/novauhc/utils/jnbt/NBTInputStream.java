/*    */
package net.novaproject.novauhc.utils.jnbt;
/*    */
/*    */

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/*    */
/*    */ public final class NBTInputStream implements Closeable {
    /*    */   private final DataInputStream is;

    /*    */
    /*    */
    public NBTInputStream(InputStream is) throws IOException {
        /* 17 */
        this.is = new DataInputStream(new GZIPInputStream(is));
        /*    */
    }

    /*    */
    /*    */
    public Tag readTag() throws IOException {
        /* 21 */
        return readTag(0);
        /*    */
    }

    /*    */
    /*    */
    private Tag readTag(int depth) throws IOException {
        /*    */
        String name;
        /* 26 */
        int type = this.is.readByte() & 0xFF;
        /* 27 */
        if (type != 0) {
            /* 28 */
            int nameLength = this.is.readShort() & 0xFFFF;
            /* 29 */
            byte[] nameBytes = new byte[nameLength];
            /* 30 */
            this.is.readFully(nameBytes);
            /* 31 */
            name = new String(nameBytes, NBTConstants.CHARSET);
            /*    */
        } else {
            /* 33 */
            name = "";
            /*    */
        }
        /* 35 */
        return readTagPayload(type, name, depth);
        /*    */
    }

    /*    */
    /*    */
    private Tag readTagPayload(int type, String name, int depth) throws IOException {
        /*    */
        int length;
        /*    */
        byte[] bytes;
        /*    */
        int childType;
        /*    */
        List<Tag> tagList;
        /*    */
        int i;
        /*    */
        Map<String, Tag> tagMap;
        /* 45 */
        switch (type) {
            /*    */
            case 0:
                /* 47 */
                if (depth == 0)
                    /* 48 */ throw new IOException("TAG_End found without a TAG_Compound/TAG_List tag preceding it.");
                /* 49 */
                return new EndTag();
            /*    */
            case 1:
                /* 51 */
                return new ByteTag(name, this.is.readByte());
            /*    */
            case 2:
                /* 53 */
                return new ShortTag(name, this.is.readShort());
            /*    */
            case 3:
                /* 55 */
                return new IntTag(name, this.is.readInt());
            /*    */
            case 4:
                /* 57 */
                return new LongTag(name, this.is.readLong());
            /*    */
            case 5:
                /* 59 */
                return new FloatTag(name, this.is.readFloat());
            /*    */
            case 6:
                /* 61 */
                return new DoubleTag(name, this.is.readDouble());
            /*    */
            case 7:
                /* 63 */
                length = this.is.readInt();
                /* 64 */
                bytes = new byte[length];
                /* 65 */
                this.is.readFully(bytes);
                /* 66 */
                return new ByteArrayTag(name, bytes);
            /*    */
            case 8:
                /* 68 */
                length = this.is.readShort();
                /* 69 */
                bytes = new byte[length];
                /* 70 */
                this.is.readFully(bytes);
                /* 71 */
                return new StringTag(name, new String(bytes, NBTConstants.CHARSET));
            /*    */
            case 9:
                /* 73 */
                childType = this.is.readByte();
                /* 74 */
                length = this.is.readInt();
                /* 75 */
                tagList = new ArrayList<>();
                /* 76 */
                for (i = 0; i < length; i++) {
                    /* 77 */
                    Tag tag = readTagPayload(childType, "", depth + 1);
                    /* 78 */
                    if (tag instanceof EndTag)
                        /* 79 */ throw new IOException("TAG_End not permitted in a list.");
                    /* 80 */
                    tagList.add(tag);
                    /*    */
                }
                /* 82 */
                return new ListTag(name, NBTUtils.getTypeClass(childType), tagList);
            /*    */
            case 10:
                /* 84 */
                tagMap = new HashMap<>();
                /*    */
                while (true) {
                    /* 86 */
                    Tag tag = readTag(depth + 1);
                    /* 87 */
                    if (tag instanceof EndTag)
                        /*    */ break;
                    /* 89 */
                    tagMap.put(tag.getName(), tag);
                    /*    */
                }
                /* 91 */
                return new CompoundTag(name, tagMap);
            /*    */
        }
        /* 93 */
        throw new IOException("Invalid tag displayer: " + type + ".");
        /*    */
    }

    /*    */
    /*    */
    public void close() throws IOException {
        /* 97 */
        this.is.close();
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\NBTInputStream.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */