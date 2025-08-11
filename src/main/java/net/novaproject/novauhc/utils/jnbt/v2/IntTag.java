/*    */
package net.novaproject.novauhc.utils.jnbt.v2;

/*    */
/*    */ public class IntTag extends Tag {
    /*    */   private final int value;

    /*    */
    /*    */
    public IntTag(int value) {
        /*  7 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public Integer getValue() {
        /* 11 */
        return Integer.valueOf(this.value);
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 15 */
        return "TAG_Int(" + this.value + ")";
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\IntTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */