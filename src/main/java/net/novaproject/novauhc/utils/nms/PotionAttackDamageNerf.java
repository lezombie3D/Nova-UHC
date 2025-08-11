package net.novaproject.novauhc.utils.nms;

import net.minecraft.server.v1_8_R3.AttributeModifier;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import net.minecraft.server.v1_8_R3.MobEffectAttackDamage;
import net.minecraft.server.v1_8_R3.MobEffectList;

public class PotionAttackDamageNerf extends MobEffectAttackDamage {
    /*    */
    public PotionAttackDamageNerf(int i, MinecraftKey minecraftKey, boolean b, int i1) {
        /* 10 */
        super(i, minecraftKey, b, i1);
        /*    */
    }

    /*    */
    /*    */
    public double a(int id, AttributeModifier modifier) {
        /* 14 */
        double result = super.a(id, modifier);
        /* 15 */
        if (this.id == MobEffectList.INCREASE_DAMAGE.id)
            /* 16 */ result /= 5.0D;
        /* 17 */
        return result;
        /*    */
    }
    /*    */
}