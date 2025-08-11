/*    */
package net.novaproject.novauhc.utils.jnbt.v2;

/*    */
/*    */ public class FloatTag extends Tag {
    /*    */   private final float value;

    /*    */
    /*    */
    public FloatTag(float value) {
        /*  7 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public Float getValue() {
        /* 11 */
        return Float.valueOf(this.value);
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 15 */
        return "TAG_Float(" + this.value + ")";
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\FloatTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */