/*    */
package net.novaproject.novauhc.utils.jnbt;
/*    */
/*    */

import java.util.Collections;
import java.util.List;

/*    */
/*    */ public final class ListTag
        /*    */ extends Tag {
    /*    */   private final Class<? extends Tag> type;
    /*    */   private final List<Tag> value;

    /*    */
    /*    */
    public ListTag(String name, Class<? extends Tag> type, List<Tag> value) {
        /* 12 */
        super(name);
        /* 13 */
        this.type = type;
        /* 14 */
        this.value = Collections.unmodifiableList(value);
        /*    */
    }

    /*    */
    /*    */
    public Class<? extends Tag> getType() {
        /* 18 */
        return this.type;
        /*    */
    }

    /*    */
    /*    */
    public List<Tag> getValue() {
        /* 22 */
        return this.value;
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 26 */
        String name = getName();
        /* 27 */
        String append = "";
        /* 28 */
        if (name != null && !name.equals(""))
            /* 29 */ append = "(\"" + getName() + "\")";
        /* 30 */
        StringBuilder bldr = new StringBuilder();
        /* 31 */
        bldr.append("TAG_List" + append + ": " + this.value.size() + " entries of displayer " + NBTUtils.getTypeName(this.type) + "\r\n{\r\n");
        /* 32 */
        for (Tag t : this.value)
            /* 33 */
            bldr.append("   " + t.toString().replaceAll("\r\n", "\r\n   ") + "\r\n");
        /* 34 */
        bldr.append("}");
        /* 35 */
        return bldr.toString();
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\ListTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */