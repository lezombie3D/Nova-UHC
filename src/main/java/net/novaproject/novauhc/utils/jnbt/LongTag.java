/*    */
package net.novaproject.novauhc.utils.jnbt;

/*    */
/*    */ public final class LongTag extends Tag {
    /*    */   private final long value;

    /*    */
    /*    */
    public LongTag(String name, long value) {
        /*  7 */
        super(name);
        /*  8 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public Long getValue() {
        /* 12 */
        return Long.valueOf(this.value);
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
        return "TAG_Long" + append + ": " + this.value;
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\LongTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */