/*     */
package net.novaproject.novauhc.utils.jnbt;
/*     */
/*     */

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/*     */
/*     */ public final class NBTOutputStream implements Closeable {
    /*     */   private final DataOutputStream os;

    /*     */
    /*     */
    public NBTOutputStream(OutputStream os) throws IOException {
        /*  14 */
        this.os = new DataOutputStream(new GZIPOutputStream(os));
        /*     */
    }

    /*     */
    /*     */
    public void writeTag(Tag tag) throws IOException {
        /*  18 */
        int type = NBTUtils.getTypeCode(tag.getClass());
        /*  19 */
        String name = tag.getName();
        /*  20 */
        byte[] nameBytes = name.getBytes(NBTConstants.CHARSET);
        /*  21 */
        this.os.writeByte(type);
        /*  22 */
        this.os.writeShort(nameBytes.length);
        /*  23 */
        this.os.write(nameBytes);
        /*  24 */
        if (type == 0)
            /*  25 */ throw new IOException("Named TAG_End not permitted.");
        /*  26 */
        writeTagPayload(tag);
        /*     */
    }

    /*     */
    /*     */
    private void writeTagPayload(Tag tag) throws IOException {
        /*  30 */
        int type = NBTUtils.getTypeCode(tag.getClass());
        /*  31 */
        switch (type) {
            /*     */
            case 0:
                /*  33 */
                writeEndTagPayload((EndTag) tag);
                /*     */
                return;
            /*     */
            case 1:
                /*  36 */
                writeByteTagPayload((ByteTag) tag);
                /*     */
                return;
            /*     */
            case 2:
                /*  39 */
                writeShortTagPayload((ShortTag) tag);
                /*     */
                return;
            /*     */
            case 3:
                /*  42 */
                writeIntTagPayload((IntTag) tag);
                /*     */
                return;
            /*     */
            case 4:
                /*  45 */
                writeLongTagPayload((LongTag) tag);
                /*     */
                return;
            /*     */
            case 5:
                /*  48 */
                writeFloatTagPayload((FloatTag) tag);
                /*     */
                return;
            /*     */
            case 6:
                /*  51 */
                writeDoubleTagPayload((DoubleTag) tag);
                /*     */
                return;
            /*     */
            case 7:
                /*  54 */
                writeByteArrayTagPayload((ByteArrayTag) tag);
                /*     */
                return;
            /*     */
            case 8:
                /*  57 */
                writeStringTagPayload((StringTag) tag);
                /*     */
                return;
            /*     */
            case 9:
                /*  60 */
                writeListTagPayload((ListTag) tag);
                /*     */
                return;
            /*     */
            case 10:
                /*  63 */
                writeCompoundTagPayload((CompoundTag) tag);
                /*     */
                return;
            /*     */
        }
        /*  66 */
        throw new IOException("Invalid tag displayer: " + type + ".");
        /*     */
    }

    /*     */
    /*     */
    private void writeByteTagPayload(ByteTag tag) throws IOException {
        /*  70 */
        this.os.writeByte(tag.getValue().byteValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeByteArrayTagPayload(ByteArrayTag tag) throws IOException {
        /*  74 */
        byte[] bytes = tag.getValue();
        /*  75 */
        this.os.writeInt(bytes.length);
        /*  76 */
        this.os.write(bytes);
        /*     */
    }

    /*     */
    /*     */
    private void writeCompoundTagPayload(CompoundTag tag) throws IOException {
        /*  80 */
        for (Tag childTag : tag.getValue().values())
            /*  81 */
            writeTag(childTag);
        /*  82 */
        this.os.writeByte(0);
        /*     */
    }

    /*     */
    /*     */
    private void writeListTagPayload(ListTag tag) throws IOException {
        /*  86 */
        Class<? extends Tag> clazz = tag.getType();
        /*  87 */
        List<Tag> tags = tag.getValue();
        /*  88 */
        int size = tags.size();
        /*  89 */
        this.os.writeByte(NBTUtils.getTypeCode(clazz));
        /*  90 */
        this.os.writeInt(size);
        /*  91 */
        for (int i = 0; i < size; i++)
            /*  92 */
            writeTagPayload(tags.get(i));
        /*     */
    }

    /*     */
    /*     */
    private void writeStringTagPayload(StringTag tag) throws IOException {
        /*  96 */
        byte[] bytes = tag.getValue().getBytes(NBTConstants.CHARSET);
        /*  97 */
        this.os.writeShort(bytes.length);
        /*  98 */
        this.os.write(bytes);
        /*     */
    }

    /*     */
    /*     */
    private void writeDoubleTagPayload(DoubleTag tag) throws IOException {
        /* 102 */
        this.os.writeDouble(tag.getValue().doubleValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeFloatTagPayload(FloatTag tag) throws IOException {
        /* 106 */
        this.os.writeFloat(tag.getValue().floatValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeLongTagPayload(LongTag tag) throws IOException {
        /* 110 */
        this.os.writeLong(tag.getValue().longValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeIntTagPayload(IntTag tag) throws IOException {
        /* 114 */
        this.os.writeInt(tag.getValue().intValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeShortTagPayload(ShortTag tag) throws IOException {
        /* 118 */
        this.os.writeShort(tag.getValue().shortValue());
        /*     */
    }

    /*     */
    /*     */
    private void writeEndTagPayload(EndTag tag) {
    }

    /*     */
    /*     */
    public void close() throws IOException {
        /* 124 */
        this.os.close();
        /*     */
    }
    /*     */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\NBTOutputStream.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */