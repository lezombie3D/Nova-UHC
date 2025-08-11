/*    */
package net.novaproject.novauhc.utils.jnbt.v2;
/*    */
/*    */

import com.google.common.base.Preconditions;

/*    */
/*    */ public class StringTag extends Tag {
    /*    */   private final String value;

    /*    */
    /*    */
    public StringTag(String value) {
        /*  9 */
        Preconditions.checkNotNull(value);
        /* 10 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public String getValue() {
        /* 14 */
        return this.value;
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 18 */
        return "TAG_String(" + this.value + ")";
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\StringTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */