/*    */
package net.novaproject.novauhc.utils.jnbt.v2;
/*    */
/*    */

import com.google.common.base.Preconditions;

/*    */
/*    */ public class NamedTag
        /*    */ {
    /*    */   private final String name;
    /*    */   private final Tag tag;

    /*    */
    /*    */
    public NamedTag(String name, Tag tag) {
        /* 11 */
        Preconditions.checkNotNull(name);
        /* 12 */
        Preconditions.checkNotNull(tag);
        /* 13 */
        this.name = name;
        /* 14 */
        this.tag = tag;
        /*    */
    }

    /*    */
    /*    */
    public String getName() {
        /* 18 */
        return this.name;
        /*    */
    }

    /*    */
    /*    */
    public Tag getTag() {
        /* 22 */
        return this.tag;
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\NamedTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */