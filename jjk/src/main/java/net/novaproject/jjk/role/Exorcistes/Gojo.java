package net.novaproject.jjk.role.Exorcistes;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.jjk.lang.JJKLang;
import net.novaproject.jjk.pouvoir.Blue;
import net.novaproject.jjk.utils.MessageUtils;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.utils.AbilityVariable;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Gojo extends JJKRole {

    @AbilityVariable(lang = JJKLang.class,nameKey = "BLUE_VAR_TITLE",type = VariableType.ABILITY)
    Ability blue;

    public Gojo() {
        setCamp(JJKCamp.EXORCISTES);
        this.blue = new Blue();
        getAbilities().add(blue);
    }

    @Override
    public String getName() {
        return "Gojo";
    }

    @Override
    public void sendDescription(Player p) {
        p.sendMessage("§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        p.sendMessage(" ");
        p.sendMessage("§e● §fInformations");
        p.sendMessage("§7Rôle : §aGojo");
        p.sendMessage("§7Objectif : §fGagner avec le camp §aExorcistes");
        p.sendMessage(" ");
        p.sendMessage("§e● §fVotre rôle §7(§fpasse la souris§7)");

        MessageUtils.sendHoverLine(
                p,
                "§7• §dÉnergie occulte : §51000❂",
                "§fÉnergie utilisée pour lancer vos techniques."
        );

        MessageUtils.sendHoverLine(
                p,
                "§7• §aPassif",
                "§fGojo maîtrise l'infini et possède une très grande puissance."
        );

        p.sendMessage(" ");
        p.sendMessage("§e● §fCapacités §7(§fpasse la souris§7)");

        MessageUtils.sendHoverLine(
                p,
                "§9• §fBlue",
                "§fAttire ou manipule l'espace autour de la cible."
        );

        p.sendMessage(" ");
        p.sendMessage("§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BONE);
    }



}