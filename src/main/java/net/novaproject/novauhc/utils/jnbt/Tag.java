/*    */
package net.novaproject.novauhc.utils.jnbt;

/*    */
/*    */ public abstract class Tag {
    /*    */   private final String name;

    /*    */
    /*    */
    public Tag(String name) {
        /*  7 */
        this.name = name;
        /*    */
    }

    /*    */
    /*    */
    public final String getName() {
        /* 11 */
        return this.name;
        /*    */
    }

    /*    */
    /*    */
    public abstract Object getValue();
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\Tag.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */