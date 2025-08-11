/*    */
package net.novaproject.novauhc.utils.jnbt.v2;

/*    */
/*    */ public class DoubleTag extends Tag {
    /*    */   private final double value;

    /*    */
    /*    */
    public DoubleTag(double value) {
        /*  7 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public Double getValue() {
        /* 11 */
        return Double.valueOf(this.value);
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 15 */
        return "TAG_Double(" + this.value + ")";
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\DoubleTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */