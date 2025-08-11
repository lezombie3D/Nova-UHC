/*     */
package net.novaproject.novauhc.utils.jnbt.v2;
/*     */
/*     */

import com.google.common.base.Preconditions;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/*     */
/*     */ public class NBTOutputStream implements Closeable {
    /*     */   private final DataOutputStream os;

    /*     */
    /*     */
    public NBTOutputStream(OutputStream os) throws IOException {
        /*  15 */
        this.os = new DataOutputStream(os);
        /*     */
    }

    /*     */
    /*     */
    public void writeNamedTag(String name, Tag tag) throws IOException {
        /*  19 */
        Preconditions.checkNotNull(name);
        /*  20 */
        Preconditions.checkNotNull(tag);
        /*  21 */
        int type = NBTUtils.getTypeCode(tag.getClass());
        /*  22 */
        byte[] nameBytes = name.getBytes(NBTConstants.CHARSET);
        /*  23 */
        this.os.writeByte(type);
        /*  24 */
        this.os.writeShort(nameBytes.length);
        /*  25 */
        this.os.write(nameBytes);
        /*  26 */
        if (type == 0)
            /*  27 */ throw new IOException("Named TAG_End not permitted.");
        /*  28 */
        writeTagPayload(tag);
        /*     */
    }

    /*     */
    /*     */
    private void writeTagPayload(Tag tag) throws IOException {
        /*  32 */
        int type = NBTUtils.getTypeCode(tag.getClass());
        /*  33 */
        switch (type) {
            /*     */
            case 0:
                /*  35 */
                writeEndTagPayload((EndTag) tag);
                /*     */
                return;
            /*     */
            case 1:
                /*  38 */
                writeByteTagPayload((ByteTag) tag);
                /*     */
                return;
            /*     */
            case 2:
                /*  41 */
                writeShortTagPayload((ShortTag) tag);
                /*     */
                return;
            /*     */
            case 3:
                /*  44 */
                writeIntTagPayload((IntTag) tag);
                /*     */
                return;
            /*     */
            case 4:
                /*  47 */
                writeLongTagPayload((LongTag) tag);
                /*     */
                return;
            /*     */
            case 5:
                /*  50 */
                writeFloatTagPayload((FloatTag) tag);
                /*     */
                return;
            /*     */
            case 6:
                /*  53 */
                writeDoubleTagPayload((DoubleTag) tag);
                /*     */
                return;
            /*     */
            case 7:
                /*  56 */
                writeByteArrayTagPayload((ByteArrayTag) tag);
                /*     */
                return;
            /*     */
            case 8:
                /*  59 */
                writeStringTagPayload((StringTag) tag);
                /*     */
                return;
            /*     */
            case 9:
                /*  62 */
                writeListTagPayload((ListTag) tag);
                /*     */
                return;
            /*     */
            case 10:
                /*  65 */
                writeCompoundTagPayload((CompoundTag) tag);
                /*     */
                return;
            /*     */
            case 11:
                /*  68 */
                writeIntArrayTagPayload((IntArrayTag) tag);
                /*     */
                return;
            /*     */
        }
        /*  71 */
        throw new IOException("Invalid tag type: " + type + ".");
        /*     */
    }

    /*     */
    /*     */
    private void writeByteTagPayload(ByteTag tag) throws IOException {
        /*  75 */
        this.os.writeByte(tag.getValue().byteValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeByteArrayTagPayload(ByteArrayTag tag) throws IOException {
        /*  79 */
        byte[] bytes = tag.getValue();
        /*  80 */
        this.os.writeInt(bytes.length);
        /*  81 */
        this.os.write(bytes);
        /*     */
    }

    /*     */
    /*     */
    private void writeCompoundTagPayload(CompoundTag tag) throws IOException {
        /*  85 */
        for (Map.Entry<String, Tag> entry : tag.getValue().entrySet())
            /*  86 */
            writeNamedTag(entry.getKey(), entry.getValue());
        /*  87 */
        this.os.writeByte(0);
        /*     */
    }

    /*     */
    /*     */
    private void writeListTagPayload(ListTag tag) throws IOException {
        /*  91 */
        Class<? extends Tag> clazz = tag.getType();
        /*  92 */
        List<Tag> tags = tag.getValue();
        /*  93 */
        int size = tags.size();
        /*  94 */
        this.os.writeByte(NBTUtils.getTypeCode(clazz));
        /*  95 */
        this.os.writeInt(size);
        /*  96 */
        for (Tag tag2 : tags)
            /*  97 */
            writeTagPayload(tag2);
        /*     */
    }

    /*     */
    /*     */
    private void writeStringTagPayload(StringTag tag) throws IOException {
        /* 101 */
        byte[] bytes = tag.getValue().getBytes(NBTConstants.CHARSET);
        /* 102 */
        this.os.writeShort(bytes.length);
        /* 103 */
        this.os.write(bytes);
        /*     */
    }

    /*     */
    /*     */
    private void writeDoubleTagPayload(DoubleTag tag) throws IOException {
        /* 107 */
        this.os.writeDouble(tag.getValue().doubleValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeFloatTagPayload(FloatTag tag) throws IOException {
        /* 111 */
        this.os.writeFloat(tag.getValue().floatValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeLongTagPayload(LongTag tag) throws IOException {
        /* 115 */
        this.os.writeLong(tag.getValue().longValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeIntTagPayload(IntTag tag) throws IOException {
        /* 119 */
        this.os.writeInt(tag.getValue().intValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeShortTagPayload(ShortTag tag) throws IOException {
        /* 123 */
        this.os.writeShort(tag.getValue().shortValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeEndTagPayload(EndTag tag) {
    }

    /*     */
    /*     */
    private void writeIntArrayTagPayload(IntArrayTag tag) throws IOException {
        /* 129 */
        int[] data = tag.getValue();
        /* 130 */
        this.os.writeInt(data.length);
        /*     */
        int[] array;
        /* 132 */
        for (int length = (array = data).length, i = 0; i < length; i++) {
            /* 133 */
            int aData = array[i];
            /* 134 */
            this.os.writeInt(aData);
            /*     */
        }
        /*     */
    }

    /*     */
    /*     */
    public void close() throws IOException {
        /* 139 */
        this.os.close();
        /*     */
    }
    /*     */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\NBTOutputStream.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */