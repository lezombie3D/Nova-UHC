/*    */
package net.novaproject.novauhc.utils.jnbt;

/*    */
/*    */ public final class ShortTag extends Tag {
    /*    */   private final short value;

    /*    */
    /*    */
    public ShortTag(String name, short value) {
        /*  7 */
        super(name);
        /*  8 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public Short getValue() {
        /* 12 */
        return Short.valueOf(this.value);
        /*    */
    }

    /*    */
    /*    */
    public String toString() {
        /* 16 */
        String name = getName();
        /* 17 */
        String append = "";
        /* 18 */
        if (name != null && !name.equals(""))
            /* 19 */ append = "(\"" + getName() + "\")";
        /* 20 */
        return "TAG_Short" + append + ": " + this.value;
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\ShortTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */