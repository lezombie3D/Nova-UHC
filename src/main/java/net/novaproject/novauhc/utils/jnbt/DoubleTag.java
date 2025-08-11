/*    */
package net.novaproject.novauhc.utils.jnbt;

/*    */
/*    */ public final class DoubleTag extends Tag {
    /*    */   private final double value;

    /*    */
    /*    */
    public DoubleTag(String name, double value) {
        /*  7 */
        super(name);
        /*  8 */
        this.value = value;
        /*    */
    }

    /*    */
    /*    */
    public Double getValue() {
        /* 12 */
        return Double.valueOf(this.value);
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
        return "TAG_Double" + append + ": " + this.value;
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\DoubleTag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */