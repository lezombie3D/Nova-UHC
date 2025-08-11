/*    */
package net.novaproject.novauhc.utils.jnbt.v2;

/*    */
/*    */ public class ShortTag extends Tag {
    /*    */   private final short value;

    /*    */
    /*    */
    public ShortTag(short value) {
        /*  7 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public Short getValue() {
        /* 11 */
        return Short.valueOf(this.value);
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 15 */
        return "TAG_Short(" + this.value + ")";
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\ShortTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */