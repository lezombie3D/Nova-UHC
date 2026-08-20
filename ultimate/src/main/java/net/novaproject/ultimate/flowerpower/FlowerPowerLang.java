package net.novaproject.ultimate.flowerpower;

import net.novaproject.novauhc.lang.Lang;

import java.util.Map;

public enum FlowerPowerLang implements Lang {

    FLOWER_POWER_NAME("FlowerPower"),
    FLOWER_POWER_DESC("Les fleurs donnent des drops aléatoires aux joueurs."),
    VAR_GOLDEN_APPLE_AMOUNT_NAME("Quantité de Golden Apple"),
    VAR_GOLDEN_APPLE_AMOUNT_DESC("Nombre de Golden Apples droppées lors d'un drop rare."),
    VAR_INGOT_AMOUNT_NAME("Quantité de Lingots"),
    VAR_INGOT_AMOUNT_DESC("Nombre de lingots droppés lors d'un drop rare."),
    VAR_NORMAL_DROP_AMOUNT_NAME("Quantité Drop Normal"),
    VAR_NORMAL_DROP_AMOUNT_DESC("Nombre d'items droppés lors d'un drop commun."),
    VAR_MAX_ENCHANT_LEVEL_NAME("Niveau Max d'Enchantement"),
    VAR_MAX_ENCHANT_LEVEL_DESC("Niveau maximum des enchantements sur les livres enchantés droppés.");

    private final String fr;

    FlowerPowerLang(String fr) {
        this.fr = fr;
    }

    @Override
    public String getKey() {
        return "flowerpower." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return Map.of("fr_FR", fr);
    }
}

