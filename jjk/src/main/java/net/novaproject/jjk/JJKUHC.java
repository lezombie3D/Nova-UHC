package net.novaproject.jjk;

import net.novaproject.jjk.role.Duo.Rika;
import net.novaproject.jjk.role.Duo.Yuta;
import net.novaproject.jjk.role.Exorcistes.*;
import net.novaproject.jjk.role.Fleaux.*;
import net.novaproject.jjk.role.Solitaire.Kashimo;
import net.novaproject.jjk.role.Solitaire.Sukuna;
import net.novaproject.jjk.role.Solitaire.Toji;
import net.novaproject.jjk.role.Zenin.*;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class JJKUHC extends ScenarioRole<JJKRole> {
    @Override
    public Camps[] getCamps() {
        return JJKCamp.values();
    }

    @Override
    public String getName() {
        return "Jujustu";
    }

    @Override
    public String getDescription(Player player) {
        return "";
    }



    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BEDROCK);
    }

    @Override
    public void setup() {
        super.setup();

        addRole(AoiTodo.class);
        addRole(Gojo.class);
        addRole(Yuji.class);
        addRole(Junpei.class);
        addRole(Kasumi.class);
        addRole(Nanami.class);
        addRole(Kiyotaka.class);
        addRole(Mai.class);
        addRole(Maki.class);
        addRole(Megumi.class);
        addRole(Mechamaru.class);
        addRole(MeiMei.class);
        addRole(Momo.class);
        addRole(Nobara.class);
        addRole(Noritoshi.class);
        addRole(Panda.class);
        addRole(Shoko.class);
        addRole(Toge.class);
        addRole(Yaga.class);
        addRole(Yoshinobu.class);

        addRole(Choso.class);
        addRole(Dagon.class);
        addRole(Eso.class);
        addRole(Geto.class);
        addRole(Hanami.class);
        addRole(Haruta.class);
        addRole(Jogo.class);
        addRole(Juzo.class);
        addRole(Kechizu.class);
        addRole(Mahito.class);
        addRole(Miguel.class);
        addRole(Uraume.class);

        addRole(Naobito.class);
        addRole(Naoya.class);
        addRole(Ogi.class);
        addRole(Jinichi.class);
        addRole(Ranta.class);
        addRole(Chojuro.class);

        addRole(Yuta.class);
        addRole(Rika.class);

        addRole(Sukuna.class);
        addRole(Toji.class);
        addRole(Kashimo.class);
    }
}