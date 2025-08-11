/*    */
package net.novaproject.novauhc.utils.jnbt.v2;
/*    */
/*    */

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/*    */
/*    */ public class ListTagBuilder
        /*    */ {
    /*    */   private final Class<? extends Tag> type;
    /*    */   private final List<Tag> entries;

    /*    */
    /*    */   ListTagBuilder(Class<? extends Tag> type) {
        /* 15 */
        Preconditions.checkNotNull(type);
        /* 16 */
        this.type = type;
        /* 17 */
        this.entries = new ArrayList<>();
        /*    */
    }

    /*    */
    /*    */
    public static ListTagBuilder create(Class<? extends Tag> type) {
        /* 40 */
        return new ListTagBuilder(type);
        /*    */
    }

    /*    */
    /*    */
    public static <T extends Tag> ListTagBuilder createWith(T... entries) {
        /* 44 */
        Preconditions.checkNotNull(entries);
        /* 45 */
        if (entries.length == 0)
            /* 46 */ throw new IllegalArgumentException("This method needs an array of at least one entry");
        /* 47 */
        Class<? extends Tag> type = entries[0].getClass();
        /* 48 */
        for (int i = 1; i < entries.length; i++) {
            /* 49 */
            if (!type.isInstance(entries[i]))
                /* 50 */ throw new IllegalArgumentException("An array of different tag types was provided");
            /*    */
        }
        /* 52 */
        ListTagBuilder builder = new ListTagBuilder(type);
        /* 53 */
        builder.addAll(Arrays.asList((Tag[]) entries));
        /* 54 */
        return builder;
        /*    */
    }

    /*    */
    /*    */
    public ListTagBuilder add(Tag value) {
        /* 21 */
        Preconditions.checkNotNull(value);
        /* 22 */
        if (!this.type.isInstance(value))
            /* 23 */
            throw new IllegalArgumentException(value.getClass().getCanonicalName() + " is not of expected type " + this.type.getCanonicalName());
        /* 24 */
        this.entries.add(value);
        /* 25 */
        return this;
        /*    */
    }

    /*    */
    /*    */
    public ListTagBuilder addAll(Collection<? extends Tag> value) {
        /* 29 */
        Preconditions.checkNotNull(value);
        /* 30 */
        for (Tag v : value)
            /* 31 */
            add(v);
        /* 32 */
        return this;
        /*    */
    }

    /*    */
    /*    */
    public ListTag build() {
        /* 36 */
        return new ListTag(this.type, new ArrayList<>(this.entries));
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\mathy\Download\\uhc-api-1.0-SNAPSHOT.jar!\fr\dome\\uhcap\\utils\jnbt\v2\ListTagBuilder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */