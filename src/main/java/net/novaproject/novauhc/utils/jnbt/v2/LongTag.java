/*    */
package net.novaproject.novauhc.utils.jnbt.v2;

/*    */
/*    */ public class LongTag extends Tag {
    /*    */   private final long value;

    /*    */
    /*    */
    public LongTag(long value) {
        /*  7 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public Long getValue() {
        /* 11 */
        return Long.valueOf(this.value);
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 15 */
        return "TAG_Long(" + this.value + ")";
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\LongTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */