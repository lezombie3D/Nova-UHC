/*    */
package net.novaproject.novauhc.utils.jnbt.v2;
/*    */
/*    */

import com.google.common.base.Preconditions;

/*    */
/*    */ public class IntArrayTag extends Tag {
    /*    */   private final int[] value;

    /*    */
    /*    */
    public IntArrayTag(int[] value) {
        /*  9 */
        Preconditions.checkNotNull(value);
        /* 10 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public int[] getValue() {
        /* 14 */
        return this.value;
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 18 */
        StringBuilder hex = new StringBuilder();
        /*    */
        int[] value;
        /* 20 */
        for (int length = (value = this.value).length, i = 0; i < length; i++) {
            /* 21 */
            int b = value[i];
            /* 22 */
            String hexDigits = Integer.toHexString(b).toUpperCase();
            /* 23 */
            if (hexDigits.length() == 1)
                /* 24 */ hex.append("0");
            /* 25 */
            hex.append(hexDigits).append(" ");
            /*    */
        }
        /* 27 */
        return "TAG_Int_Array(" + hex + ")";
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\IntArrayTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */