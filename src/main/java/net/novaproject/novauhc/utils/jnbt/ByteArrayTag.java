/*    */
package net.novaproject.novauhc.utils.jnbt;

/*    */
/*    */ public final class ByteArrayTag extends Tag {
    /*    */   private final byte[] value;

    /*    */
    /*    */
    public ByteArrayTag(String name, byte[] value) {
        /*  7 */
        super(name);
        /*  8 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public byte[] getValue() {
        /* 12 */
        return this.value;
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 16 */
        StringBuilder hex = new StringBuilder();
        /* 17 */
        for (byte b : this.value) {
            /* 18 */
            String hexDigits = Integer.toHexString(b).toUpperCase();
            /* 19 */
            if (hexDigits.length() == 1)
                /* 20 */ hex.append("0");
            /* 21 */
            hex.append(hexDigits).append(" ");
            /*    */
        }
        /* 23 */
        String name = getName();
        /* 24 */
        String append = "";
        /* 25 */
        if (name != null && !name.equals(""))
            /* 26 */ append = "(\"" + getName() + "\")";
        /* 27 */
        return "TAG_Byte_Array" + append + ": " + hex;
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\ByteArrayTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */