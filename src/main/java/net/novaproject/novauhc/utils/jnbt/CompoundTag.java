/*    */
package net.novaproject.novauhc.utils.jnbt;
/*    */
/*    */

import java.util.Collections;
import java.util.Map;

/*    */
/*    */ public final class CompoundTag extends Tag {
    /*    */   private final Map<String, Tag> value;

    /*    */
    /*    */
    public CompoundTag(String name, Map<String, Tag> value) {
        /* 10 */
        super(name);
        /* 11 */
        this.value = Collections.unmodifiableMap(value);
        /*    */
    }

    /*    */
    /*    */
    public Map<String, Tag> getValue() {
        /* 15 */
        return this.value;
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 19 */
        String name = getName();
        /* 20 */
        String append = "";
        /* 21 */
        if (name != null && !name.equals(""))
            /* 22 */ append = "(\"" + getName() + "\")";
        /* 23 */
        StringBuilder bldr = new StringBuilder();
        /* 24 */
        bldr.append("TAG_Compound" + append + ": " + this.value.size() + " entries\r\n{\r\n");
        /* 25 */
        for (Map.Entry<String, Tag> entry : this.value.entrySet())
            /* 26 */
            bldr.append("   " + entry.getValue().toString().replaceAll("\r\n", "\r\n   ") + "\r\n");
        /* 27 */
        bldr.append("}");
        /* 28 */
        return bldr.toString();
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\CompoundTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */