/*    */
package net.novaproject.novauhc.utils.jnbt.v2;

/*    */
/*    */ public class ByteTag extends Tag {
    /*    */   private final byte value;

    /*    */
    /*    */
    public ByteTag(byte value) {
        /*  7 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public Byte getValue() {
        /* 11 */
        return Byte.valueOf(this.value);
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 15 */
        return "TAG_Byte(" + this.value + ")";
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\ByteTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */