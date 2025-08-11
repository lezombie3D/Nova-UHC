/*    */
package net.novaproject.novauhc.utils.jnbt.v2;

/*    */
/*    */ public class ByteArrayTag extends Tag {
    /*    */   private final byte[] value;

    /*    */
    /*    */
    public ByteArrayTag(byte[] value) {
        /*  7 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public byte[] getValue() {
        /* 11 */
        return this.value;
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 15 */
        StringBuilder hex = new StringBuilder();
        /*    */
        byte[] value;
        /* 17 */
        for (int length = (value = this.value).length, i = 0; i < length; i++) {
            /* 18 */
            byte b = value[i];
            /* 19 */
            String hexDigits = Integer.toHexString(b).toUpperCase();
            /* 20 */
            if (hexDigits.length() == 1)
                /* 21 */ hex.append("0");
            /* 22 */
            hex.append(hexDigits).append(" ");
            /*    */
        }
        /* 24 */
        return "TAG_Byte_Array(" + hex + ")";
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\ByteArrayTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */