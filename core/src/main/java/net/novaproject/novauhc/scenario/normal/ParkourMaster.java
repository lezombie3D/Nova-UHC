package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.lang.ParkourMasterLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ParkourMaster extends Scenario {

    private final Map<UUID, ParkourChallenge> activeChallenges = new HashMap<>();
    private final List<ParkourReward> rewards = new ArrayList<>();
    private final Random random = new Random();
    private BukkitRunnable parkourTask;

    @ScenarioVariable(name = "Spawn Interval", description = "Intervalle (en secondes) entre l'apparition des parcours.", type = VariableType.INTEGER)
    private int spawnInterval = 300;

    @ScenarioVariable(name = "Min Checkpoints", description = "Nombre minimum de checkpoints par parcours.", type = VariableType.INTEGER)
    private int minCheckpoints = 3;

    @ScenarioVariable(name = "Max Checkpoints", description = "Nombre maximum de checkpoints par parcours.", type = VariableType.INTEGER)
    private int maxCheckpoints = 5;

    @ScenarioVariable(name = "Checkpoint Distance Min", description = "Distance minimale entre les checkpoints.", type = VariableType.INTEGER)
    private int checkpointDistanceMin = 5;

    @ScenarioVariable(name = "Checkpoint Distance Max", description = "Distance maximale entre les checkpoints.", type = VariableType.INTEGER)
    private int checkpointDistanceMax = 10;

    @ScenarioVariable(name = "Parkour Timeout", description = "Temps maximum (en secondes) pour compléter un parcours.", type = VariableType.INTEGER)
    private int parkourTimeout = 300;

    @ScenarioVariable(name = "Reward Golden Apple", description = "Quantité de pommes d'or offertes.", type = VariableType.INTEGER)
    private int rewardGoldenApple = 2;

    @ScenarioVariable(name = "Reward Arrow", description = "Quantité de flèches offertes.", type = VariableType.INTEGER)
    private int rewardArrow = 16;

    @ScenarioVariable(name = "Reward Ender Pearl", description = "Quantité de perles d'ender offertes.", type = VariableType.INTEGER)
    private int rewardEnderPearl = 4;

    @ScenarioVariable(name = "Reward Iron Ingot", description = "Quantité de lingots de fer offerts.", type = VariableType.INTEGER)
    private int rewardIronIngot = 8;

    @ScenarioVariable(name = "Reward Diamond", description = "Quantité de diamants offerts.", type = VariableType.INTEGER)
    private int rewardDiamond = 2;

    @ScenarioVariable(name = "Reward Enchanted Book", description = "Quantité de livres enchantés offerts.", type = VariableType.INTEGER)
    private int rewardEnchantedBook = 1;

    @Override
    public String getName() {
        return "ParkourMaster";
    }

    @Override
    public String getDescription() {
        return "Des parcours apparaissent aléatoirement. Les compléter donne des récompenses !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FEATHER);
    }

    @Override
    public String getPath() {
        return "parkourmaster";
    }

    @Override
    public ScenarioLang[] getLang() {
        return ParkourMasterLang.values();
    }

    @Override
    public void onGameStart() {
        initializeRewards();
        startParkourTask();
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        if (!isActive()) return;
        UUID uuid = player.getUniqueId();
        ParkourChallenge challenge = activeChallenges.get(uuid);
        if (challenge == null) return;
        Location loc = player.getLocation();
        if (challenge.isAtCheckpoint(loc)) {
            challenge.nextCheckpoint();
            if (challenge.isCompleted()) {
                completeParkour(player, challenge);
            } else {
                UHCPlayer uhc = UHCPlayerManager.get().getPlayer(player);
                Map<String, Object> placeholders = new HashMap<>();
                placeholders.put("%current%", String.valueOf(challenge.getCurrentCheckpoint()));
                placeholders.put("%total%", String.valueOf(challenge.getTotalCheckpoints()));
                ScenarioLangManager.send(uhc, ParkourMasterLang.CHECKPOINT_REACHED, placeholders);
                player.getWorld().playSound(loc, org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.5f);
            }
        }
        if (challenge.isPlayerOutOfBounds(loc)) {
            failParkour(player, challenge);
        }
    }

    private void startParkourTask() {
        if (parkourTask != null) parkourTask.cancel();
        parkourTask = new BukkitRunnable() {
            private int timer = 0;
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }
                timer++;
                if (timer >= spawnInterval) {
                    spawnRandomParkour();
                    timer = 0;
                }
                updateChallenges();
            }
        };
        parkourTask.runTaskTimer(Main.get(),0,20);
    }

    private void stopParkourTask() {
        if (parkourTask != null) parkourTask.cancel();
        parkourTask = null;
    }

    private void spawnRandomParkour() {
        List<UHCPlayer> players = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
        if (players.isEmpty()) return;
        Player player = players.get(random.nextInt(players.size())).getPlayer();
        if (activeChallenges.containsKey(player.getUniqueId())) return;
        Location loc = findSuitableSpawnLocation(player.getLocation());
        if (loc != null) {
            ParkourChallenge challenge = createParkourChallenge(loc);
            activeChallenges.put(player.getUniqueId(), challenge);
            player.sendMessage("§a[ParkourMaster] §fUn parcours est apparu près de vous ! Complétez-le pour une récompense !");
            Bukkit.broadcastMessage("§a[ParkourMaster] §fUn parcours est apparu près de " + player.getName() + " !");
            buildParkour(challenge);
        }
    }

    private Location findSuitableSpawnLocation(Location playerLoc) {
        World w = playerLoc.getWorld();
        for (int i = 0; i < 10; i++) {
            int x = playerLoc.getBlockX() + random.nextInt(100) - 50;
            int z = playerLoc.getBlockZ() + random.nextInt(100) - 50;
            int y = w.getHighestBlockYAt(x,z)+1;
            Location loc = new Location(w,x,y,z);
            if (loc.getBlock().getType()==Material.AIR && loc.clone().subtract(0,1,0).getBlock().getType().isSolid()) return loc;
        }
        return null;
    }

    private ParkourChallenge createParkourChallenge(Location start) {
        List<Location> checkpoints = new ArrayList<>();
        checkpoints.add(start.clone());
        int num = minCheckpoints + random.nextInt(maxCheckpoints - minCheckpoints + 1);
        Location current = start.clone();
        for (int i=0;i<num;i++) {
            int dist = checkpointDistanceMin + random.nextInt(checkpointDistanceMax - checkpointDistanceMin +1);
            double angle = random.nextDouble()*2*Math.PI;
            int dx = (int)(Math.cos(angle)*dist);
            int dz = (int)(Math.sin(angle)*dist);
            int dy = random.nextInt(5)-2;
            current = current.clone().add(dx,dy,dz);
            checkpoints.add(current.clone());
        }
        return new ParkourChallenge(checkpoints,System.currentTimeMillis()+(parkourTimeout*1000L));
    }

    private void buildParkour(ParkourChallenge challenge) {
        List<Location> checkpoints = challenge.getCheckpoints();
        for (int i=0;i<checkpoints.size();i++) {
            Location c = checkpoints.get(i);
            if (i==0) {c.getBlock().setType(Material.WOOL);c.getBlock().setData((byte)5);}
            else if (i==checkpoints.size()-1) {c.getBlock().setType(Material.WOOL);c.getBlock().setData((byte)14);}
            else {c.getBlock().setType(Material.WOOL);c.getBlock().setData((byte)4);}
            for (int x=-1;x<=1;x++) for (int z=-1;z<=1;z++) if (x!=0||z!=0) {
                Location l = c.clone().add(x,-1,z);
                if (l.getBlock().getType()==Material.AIR) l.getBlock().setType(Material.STONE);
            }
        }
    }

    private void completeParkour(Player p, ParkourChallenge c) {
        activeChallenges.remove(p.getUniqueId());
        ParkourReward r = rewards.get(random.nextInt(rewards.size()));
        giveReward(p,r);
        cleanupParkour(c);
        p.sendMessage("§a§l[ParkourMaster] §fParcours complété ! Récompense : "+r.description());
        Bukkit.broadcastMessage("§a[ParkourMaster] §f"+p.getName()+" a complété un parcours !");
        p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.LEVEL_UP,1.0f,1.0f);
    }

    private void failParkour(Player p, ParkourChallenge c) {
        activeChallenges.remove(p.getUniqueId());
        p.sendMessage("§c[ParkourMaster] §fVous avez échoué au parcours ! Réessayez la prochaine fois.");
        new BukkitRunnable(){public void run(){cleanupParkour(c);}}.runTaskLater(Main.get(),100);
    }

    private void cleanupParkour(ParkourChallenge c) {
        for (Location l:c.getCheckpoints()) {
            l.getBlock().setType(Material.AIR);
            for (int x=-1;x<=1;x++) for (int z=-1;z<=1;z++) {
                Location p = l.clone().add(x,-1,z);
                if (p.getBlock().getType()==Material.STONE) p.getBlock().setType(Material.AIR);
            }
        }
    }

    private void updateChallenges() {
        Iterator<Map.Entry<UUID,ParkourChallenge>> it = activeChallenges.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<UUID,ParkourChallenge> e = it.next();
            ParkourChallenge c = e.getValue();
            if (c.isExpired()) {
                Player p = Bukkit.getPlayer(e.getKey());
                if (p!=null) p.sendMessage("§c[ParkourMaster] §fLe parcours a expiré !");
                cleanupParkour(c);
                it.remove();
            }
        }
    }

    private void initializeRewards() {
        rewards.clear();
        rewards.add(new ParkourReward(rewardGoldenApple+" Pommes d'Or",new ItemStack(Material.GOLDEN_APPLE,rewardGoldenApple)));
        rewards.add(new ParkourReward(rewardArrow+" Flèches",new ItemStack(Material.ARROW,rewardArrow)));
        rewards.add(new ParkourReward(rewardEnderPearl+" Perles d'Ender",new ItemStack(Material.ENDER_PEARL,rewardEnderPearl)));
        rewards.add(new ParkourReward(rewardIronIngot+" Lingots de Fer",new ItemStack(Material.IRON_INGOT,rewardIronIngot)));
        rewards.add(new ParkourReward(rewardDiamond+" Diamants",new ItemStack(Material.DIAMOND,rewardDiamond)));
        rewards.add(new ParkourReward(rewardEnchantedBook+" Livre d'Enchantement",new ItemStack(Material.ENCHANTED_BOOK,rewardEnchantedBook)));
    }

    private void giveReward(Player p, ParkourReward r) {
        if (p.getInventory().firstEmpty()!=-1) p.getInventory().addItem(r.item());
        else {p.getWorld().dropItemNaturally(p.getLocation(),r.item());p.sendMessage("§a[ParkourMaster] §fInventaire plein ! Récompense jetée au sol.");}
    }

    private static class ParkourChallenge {
        private final List<Location> checkpoints;
        private final long expirationTime;
        private int currentCheckpoint = 0;
        public ParkourChallenge(List<Location> c,long t){checkpoints=c;expirationTime=t;}
        public List<Location> getCheckpoints(){return checkpoints;}
        public boolean isExpired(){return System.currentTimeMillis()>expirationTime;}
        public int getCurrentCheckpoint(){return currentCheckpoint+1;}
        public int getTotalCheckpoints(){return checkpoints.size();}
        public boolean isCompleted(){return currentCheckpoint>=checkpoints.size();}
        public boolean isAtCheckpoint(Location l){if(currentCheckpoint>=checkpoints.size())return false; return l.distance(checkpoints.get(currentCheckpoint))<=2.0;}
        public void nextCheckpoint(){currentCheckpoint++;}
        public boolean isPlayerOutOfBounds(Location l){if(currentCheckpoint>=checkpoints.size())return false; Location c=checkpoints.get(currentCheckpoint); return l.distance(c)>50.0 || l.getY()<c.getY()-10;}
    }

    private record ParkourReward(String description, ItemStack item){
        @Override
        public ItemStack item(){return item.clone();}
    }
}
