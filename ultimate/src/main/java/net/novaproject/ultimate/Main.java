package net.novaproject.ultimate;

import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.ultimate.beatthesanta.BeatTheSanta;
import net.novaproject.ultimate.fallenkigdom.FallenKingdom;
import net.novaproject.ultimate.flowerpower.FlowerPower;
import net.novaproject.ultimate.gonefish.GoneFIsh;
import net.novaproject.ultimate.king.King;
import net.novaproject.ultimate.legend.Legend;
import net.novaproject.ultimate.netheribus.NetheriBus;
import net.novaproject.ultimate.random.RandomCraft;
import net.novaproject.ultimate.random.RandomDrop;
import net.novaproject.ultimate.skydef.SkyDef;
import net.novaproject.ultimate.skyhigt.SkyHigh;
import net.novaproject.ultimate.slavemarket.SlaveMarket;
import net.novaproject.ultimate.superheros.SuperHeros;
import net.novaproject.ultimate.taupegun.TaupeGun;
import net.novaproject.ultimate.teamatfirstseigth.TeamAtFirstSeigth;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ScenarioManager s = ScenarioManager.get();
                s.addScenario(new TaupeGun());
                s.addScenario(new TeamAtFirstSeigth());
                s.addScenario(new FallenKingdom());
                s.addScenario(new SkyHigh());
                s.addScenario(new SuperHeros());
                s.addScenario(new SlaveMarket());
                s.addScenario(new King());
                s.addScenario(new RandomCraft());
                s.addScenario(new RandomDrop());
                s.addScenario(new NetheriBus());
                s.addScenario(new GoneFIsh());
                s.addScenario(new FlowerPower());
                s.addScenario(new BeatTheSanta());
                s.addScenario(new Legend());
                s.addScenario(new MysteryTeam());
                s.addScenario(new SoulBrother());
                s.addScenario(new SkyDef());
            }
        }.runTaskLater(this, 20);

    }
}
