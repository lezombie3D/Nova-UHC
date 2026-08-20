package net.novaproject.ultimate;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.ultimate.modes.CibleModes;
import net.novaproject.ultimate.modes.CoupleModes;
import net.novaproject.ultimate.modes.LienModes;
import net.novaproject.ultimate.modes.PouvoirModes;
import net.novaproject.ultimate.soulbrotherplus.SoulBrotherPlus;
import net.novaproject.ultimate.superherosplus.SuperHerosPlus;
import net.novaproject.ultimate.australia.Australia;
import net.novaproject.ultimate.football.FootballEdition;
import net.novaproject.ultimate.smashbrothers.SmashBrothers;
import net.novaproject.ultimate.timedkings.TimedKings;
import net.novaproject.ultimate.beatthesanta.BeatTheSantaLang;
import net.novaproject.ultimate.fallenkigdom.FKLang;
import net.novaproject.ultimate.flowerpower.FlowerPowerLang;
import net.novaproject.ultimate.king.KingLang;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.mysteryteam.MysteryTeamLang;
import net.novaproject.ultimate.nuzlocke.NuzlockeLang;
import net.novaproject.ultimate.skydef.SkyDefLang;
import net.novaproject.ultimate.skyhigt.SkyHighLang;
import net.novaproject.ultimate.slavemarket.SlaveMarketLang;
import net.novaproject.ultimate.soulbrother.SoulBrotherLang;
import net.novaproject.ultimate.taupedefender.TaupeDefenderLang;
import net.novaproject.ultimate.taupegun.TaupeGunLang;
import net.novaproject.ultimate.taupegunapocalypse.TaupeGunApocalypseLang;
import net.novaproject.ultimate.truelove.TrueLoveLang;
import net.novaproject.ultimate.beatthesanta.BeatTheSanta;
import net.novaproject.ultimate.fallenkigdom.FallenKingdom;
import net.novaproject.ultimate.flowerpower.FlowerPower;
import net.novaproject.ultimate.gonefish.GoneFish;
import net.novaproject.ultimate.king.King;
import net.novaproject.ultimate.legend.Legend;
import net.novaproject.ultimate.legend.roles.abilities.*;
import net.novaproject.ultimate.mysteryteam.MysteryTeam;
import net.novaproject.ultimate.netheribus.NetheriBus;
import net.novaproject.ultimate.nuzlocke.Nuzlocke;
import net.novaproject.ultimate.nuzlocke.roles.bug.*;
import net.novaproject.ultimate.nuzlocke.roles.dark.*;
import net.novaproject.ultimate.nuzlocke.roles.dragon.*;
import net.novaproject.ultimate.nuzlocke.roles.electric.*;
import net.novaproject.ultimate.nuzlocke.roles.fairy.*;
import net.novaproject.ultimate.nuzlocke.roles.fighting.*;
import net.novaproject.ultimate.nuzlocke.roles.fire.*;
import net.novaproject.ultimate.nuzlocke.roles.flying.*;
import net.novaproject.ultimate.nuzlocke.roles.ghost.*;
import net.novaproject.ultimate.nuzlocke.roles.grass.*;
import net.novaproject.ultimate.nuzlocke.roles.ground.*;
import net.novaproject.ultimate.nuzlocke.roles.ice.*;
import net.novaproject.ultimate.nuzlocke.roles.normal.*;
import net.novaproject.ultimate.nuzlocke.roles.poison.*;
import net.novaproject.ultimate.nuzlocke.roles.psychic.*;
import net.novaproject.ultimate.nuzlocke.roles.rock.*;
import net.novaproject.ultimate.nuzlocke.roles.steel.*;
import net.novaproject.ultimate.nuzlocke.roles.water.*;
import net.novaproject.ultimate.random.RandomCraft;
import net.novaproject.ultimate.random.RandomDrop;
import net.novaproject.ultimate.skydef.SkyDef;
import net.novaproject.ultimate.skyhigt.SkyHigh;
import net.novaproject.ultimate.slavemarket.SlaveMarket;
import net.novaproject.ultimate.soulbrother.SoulBrother;
import net.novaproject.ultimate.superheros.SuperHeros;
import net.novaproject.ultimate.taupedefender.TaupeDefender;
import net.novaproject.ultimate.taupegun.TaupeGun;
import net.novaproject.ultimate.taupegunapocalypse.TaupeGunApocalypse;
import net.novaproject.ultimate.teamswapper.TeamSwapperClassic;
import net.novaproject.ultimate.teamswapper.TeamSwapperV3;
import net.novaproject.ultimate.teamswitch.Switch;
import net.novaproject.ultimate.truelove.TrueLove;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                registerLang();
                registerScenarios();
                registerLegendAbilities();
                registerNuzlockeAbilities();
            }
        }.runTaskLater(this, 20);
    }

    private void registerLang() {
        LangManager lm = LangManager.get();
        lm.register(BeatTheSantaLang.values());
        lm.register(FKLang.values());
        lm.register(FlowerPowerLang.values());
        lm.register(KingLang.values());
        lm.register(LegendLang.values());
        lm.register(MysteryTeamLang.values());
        lm.register(NuzlockeLang.values());
        lm.register(SkyDefLang.values());
        lm.register(SkyHighLang.values());
        lm.register(SlaveMarketLang.values());
        lm.register(SoulBrotherLang.values());
        lm.register(TaupeDefenderLang.values());
        lm.register(TaupeGunLang.values());
        lm.register(TaupeGunApocalypseLang.values());
        lm.register(TrueLoveLang.values());
        lm.importShipped(this);
        lm.requestReload();
    }

    private void registerScenarios() {
        ScenarioManager s = ScenarioManager.get();
        for (Scenario mode : CoupleModes.all()) s.addScenario(mode);
        for (Scenario mode : LienModes.all()) s.addScenario(mode);
        for (Scenario mode : CibleModes.all()) s.addScenario(mode);
        for (Scenario mode : PouvoirModes.all()) s.addScenario(mode);
        s.addScenario(new SoulBrotherPlus());
        s.addScenario(new SuperHerosPlus());
        s.addScenario(new TimedKings());
        s.addScenario(new SmashBrothers());
        s.addScenario(new FootballEdition());
        s.addScenario(new Australia());
        s.addScenario(new TaupeGun());
        s.addScenario(new TaupeGunApocalypse());
        s.addScenario(new TrueLove());
        s.addScenario(new FallenKingdom());
        s.addScenario(new SkyHigh());
        s.addScenario(new SuperHeros());
        s.addScenario(new SlaveMarket());
        s.addScenario(new King());
        s.addScenario(new RandomCraft());
        s.addScenario(new RandomDrop());
        s.addScenario(new NetheriBus());
        s.addScenario(new GoneFish());
        s.addScenario(new FlowerPower());
        s.addScenario(new BeatTheSanta());
        s.addScenario(new Legend());
        s.addScenario(new Nuzlocke());
        s.addScenario(new MysteryTeam());
        s.addScenario(new SoulBrother());
        s.addScenario(new SkyDef());
        s.addScenario(new TaupeDefender());
        s.addScenario(new Switch());
        s.addScenario(new TeamSwapperClassic());
        s.addScenario(new TeamSwapperV3());
    }

    private void registerLegendAbilities() {
        register(new ArcherBowPassive());
        register(new AssassinForcePassive());
        register(new CavalierHorseActive());
        register(new CorneMelodieAir());
        register(new CorneMelodieFeu());
        register(new CorneMelodieHeal());
        register(new CorneMelodieMetal());
        register(new CorneWeaknessPassive());
        register(new DragonFireballActive());
        register(new DragonFirePassive());
        register(new MagePotionPassive());
        register(new MarionnettistePuppetPassive());
        register(new MedecinHealPassive());
        register(new NainArmorActive());
        register(new NecroSummonActive());
        register(new OgrePassive());
        register(new PaladinBlessingActive());
        register(new PaladinLowHealthPassive());
        register(new PrincesseNoFallPassive());
        register(new PrisonnierChainActive());
        register(new PrisonnierSpeedPassive());
        register(new SoldatEquipmentPassive());
        register(new SuccubeAbsorptionActive());
        register(new SuccubeLifestealPassive());
        register(new TankResistancePassive());
        register(new ZeusEffectsActive());
        register(new ZeusLightningPassive());
    }

    private void registerNuzlockeAbilities() {

        register(new FireArrowBow());
        register(new FireCutcleanListener());
        register(new FireWoodPenaltyListener());
        register(new FireTrailEnv());

        register(new NormalSubstituteCommand());
        register(new NormalGappleHealListener());

        register(new GrassNearCommand());
        register(new GrassBurnVulnerableListener());
        register(new GrassArrowVulnerableListener());
        register(new GrassAppleDropEnv());

        register(new WaterMuddyBow());
        register(new WaterSpongeUse());

        register(new FightingDetectListener());
        register(new FightingCloseCombatMelee());
        register(new FightingBowMalusListener());
        register(new FightingResistancePiercerListener());

        register(new FlyingJumpToggleUse());
        register(new FlyingPunchBow());
        register(new FlyingFallListener());
        register(new FlyingKnockbackListener());

        register(new RockSturdyPassive());
        register(new RockFallDamageListener());
        register(new RockCobbleSmeltEnv());

        register(new DarkBlindnessBow());
        register(new DarkSoundCommand());
        register(new DarkNightBuffMeleeListener());

        register(new ElectricParalysisBow());
        register(new ElectricMotorDriveUse());
        register(new ElectricVulnerableListener());
        register(new ElectricStormEnv());

        register(new SteelMagnetMelee());
        register(new SteelDoubleOreListener());
        register(new SteelFireVulnerableListener());
        register(new SteelIronOreReliefListener());

        register(new IceFreezeDryUse());
        register(new IceBlizzardBow());
        register(new IceMeleeVulnerableListener());

        register(new PoisonImmunityListener());
        register(new PoisonSpikesMelee());
        register(new PoisonSpawnerEnvListener());

        register(new FairyWandUse());
        register(new FairySweetKissBow());
        register(new FairyFlowerEatListener());

        register(new GhostArrowDodgeListener());
        register(new GhostPveImmunityListener());

        register(new PsychicMindreadCommand());
        register(new PsychicBuffsListener());

        register(new DragonBreathBow());
        register(new DragonDanceListener());

        register(new BugCobwebBow());
        register(new BugGappleNerfListener());
        register(new BugSpiderImmunityEnv());

        register(new GroundDigUse());
        register(new GroundJumpPreventBow());
        register(new GroundWaterBlindNoFallListener());
    }

    private void register(Ability ability) {

        if (ability instanceof Listener listener) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}

