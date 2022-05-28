package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.BoxInfo;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.RegisterFeature;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;
import com.github.manolo8.darkbot.modules.MapModule;
import com.github.manolo8.darkbot.modules.utils.NpcAttacker;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@RegisterFeature(value = "Schifo", author = "Fabio")
@Feature(name = "BattlerayFarmer", description = "this module was made to farm Battlerays in 5-3")
public class BattlerayFarmer extends LootNCollectorModule implements Configurable<BattlerayFarmer.BattlerayConfig> {
    public List<Portal> portals;
    private Map MAP;
    private Main main;
    private HeroManager hero;
    private List<Npc> npcs;
    private List<Box> boxes;
    private NpcAttacker attack;
    private BattlerayFarmer.BattlerayConfig battlerayConfig;
    private BattlerayFarmer.State currentStatus;
    private boolean isRepaired = true;
    private Config config;

    public void install(Main main) {
        this.MAP = main.starManager.byName("5-3");
        this.main = main;
        this.npcs = main.mapManager.entities.npcs;
        this.portals = main.mapManager.entities.portals;
        this.attack = new NpcAttacker(main);
        this.hero = main.hero;
        this.currentStatus = BattlerayFarmer.State.IDLE;
        this.boxes = main.mapManager.entities.boxes;
        this.config = main.config;
        this.preSet();
    }

    public boolean canRefresh() {
        return false;
    }

    public void tickModule() {
        if (this.main.hero.map == null) return;
        if (this.main.hero.map != this.MAP) {
            this.main.setModule(new MapModule()).setTarget(this.MAP);
            this.main.hero.roamMode();
            return;
        }

        Npc BATTLERAY = this.npcs.stream().filter((npc) -> npc.playerInfo.username.equals("-=[ Battleray ]=-")).min(Comparator.comparingDouble((npc) -> npc.locationInfo.distance(this.hero))).orElse(null);
        Npc INTERCEPTOR = this.npcs.stream().filter((npc) -> npc.playerInfo.username.equals("-=[ Interceptor ]=-")).findFirst().orElse(null);
        Npc SABOTEUR = this.npcs.stream().filter((npc) -> npc.playerInfo.username.equals("-=[ Saboteur ]=-")).findFirst().orElse(null);
        Portal SAFE = this.portals.stream().filter((p) -> p.factionId == this.main.hero.playerInfo.factionId).min(Comparator.comparingDouble((p) -> p.locationInfo.distance(this.hero))).orElse(null);
        if (SAFE != null && this.getDistance(SAFE) < 200.0D) this.isRepaired = this.hero.health.hpPercent() >= this.main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.max;

        if (BATTLERAY != null && this.hero.health.hpPercent() > this.main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.min && this.isRepaired) {
            if (this.getDistance(BATTLERAY) > 1400.0D) this.currentStatus = BattlerayFarmer.State.DRIVE_BATTLERAY;
            else this.currentStatus = BattlerayFarmer.State.DRIVE_BATTLERAY_ATTACK;

            if (this.getDistance(BATTLERAY) > 2200.0D && INTERCEPTOR != null && this.battlerayConfig.killAlienWhenFly) {
                if (this.hero.shipInfo.speed < 200 && SABOTEUR != null && this.getDistance(SABOTEUR) < 1000.0D && this.battlerayConfig.killSaboteur) {
                    this.main.hero.attackMode();
                    this.main.hero.drive.move(BATTLERAY.locationInfo.now);
                    this.attack.target = SABOTEUR;
                    this.attack.doKillTargetTick();
                } else {
                    this.main.hero.roamMode();
                    this.main.hero.drive.move(BATTLERAY.locationInfo.now);
                    this.attack.target = INTERCEPTOR;
                    this.attack.doKillTargetTick();
                    if (this.attack.hasTarget() && this.attack.target.locationInfo.now.distance(this.hero.locationInfo.now) > 950.0D) this.attack.target = null;
                }
            } else if ((this.main.hero.target == null || !this.attack.hasTarget()) && this.getDistance(BATTLERAY) > 1000.0D) {
                if (this.getDistance(BATTLERAY) > 1600.0D) this.main.hero.roamMode();
                this.main.hero.drive.move(BATTLERAY.locationInfo.now);
            } else {
                if (INTERCEPTOR != null && BATTLERAY.ish) {
                    if (this.getDistance(INTERCEPTOR) > 700.0D) this.main.hero.drive.move(INTERCEPTOR.locationInfo.now);
                    this.currentStatus = BattlerayFarmer.State.ATTACK_INTERCEPTOR;
                    this.startAttack(INTERCEPTOR);
                    if (this.attack.hasTarget() && this.attack.target.locationInfo.now.distance(this.hero.locationInfo.now) > 950.0D) this.attack.target = null;
                    if (this.attack.hasTarget()) this.moveLogic();
                }

                if (INTERCEPTOR == null || !BATTLERAY.ish) {
                    if (this.getDistance(BATTLERAY) > 700.0D) this.main.hero.drive.move(BATTLERAY.locationInfo.now);
                    this.currentStatus = BattlerayFarmer.State.ATTACK_BATTLERAY;
                    this.startAttack(BATTLERAY);
                    if (this.attack.hasTarget()) this.moveLogic();
                }
            }
        } else if (this.hero.health.hpPercent() > this.main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.min && this.hero.health.hpPercent() >= this.main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.max && this.battlerayConfig.pickPalladium) {
            Box PALLADIUM = this.boxes.stream().filter((box) -> box.type.equals("ore_8")).min(Comparator.comparingDouble((box) -> this.hero.locationInfo.now.distance(box))).orElse(null);
            if (!this.main.hero.drive.isMoving() || PALLADIUM != null) {
                this.currentStatus = BattlerayFarmer.State.DRIVE_PALLADIUM;
                this.main.hero.roamMode();
                Random r = new Random();
                int leftX = 12900;
                int rightX = 32200;
                int upY = 18400;
                int downY = 25500;
                double randX = r.nextInt(rightX - leftX) + leftX;
                double randY = r.nextInt(downY - upY) + upY;
                if (PALLADIUM == null) this.hero.drive.move(randX, randY);
                else if (this.collectorModule.isNotWaiting()) {
                    this.collectorModule.findBox();
                    this.collectorModule.tryCollectNearestBox();
                }
            }
        } else {
            if (this.hero.health.hpPercent() > this.main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.min && this.hero.health.hpPercent() >= this.main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.max) this.currentStatus = BattlerayFarmer.State.DRIVE_SAFE;
            else this.currentStatus = BattlerayFarmer.State.DRIVE_SAFE_REPAIR;

            if (SAFE != null && this.getDistance(SAFE) > 200.0D) {
                this.main.hero.roamMode();
                this.hero.drive.move(SAFE);
            }

            if (!this.hero.drive.isMoving() && this.hero.health.shieldPercent() >= 0.85D && SAFE != null && this.getDistance(SAFE) < 200.0D) {
                this.hero.setMode(this.main.config.GENERAL.SAFETY.REPAIR);
                if ((double)(System.currentTimeMillis() - this.main.lastRefresh) > ((double)((long)this.config.MISCELLANEOUS.REFRESH_TIME) - 0.2D) * 60.0D * 1000.0D) Main.API.handleRefresh();
            }

            if (this.attack.target != null) {
                this.attack.target.removed();
                this.attack.target = null;
            }
        }
    }

    private void preSet() {
        this.config.LOOT.NPC_INFOS.computeIfAbsent("-=[ Battleray ]=-", (n) -> new NpcInfo()).radius = 630.0D;
        this.config.LOOT.NPC_INFOS.computeIfAbsent("-=[ Interceptor ]=-", (n) -> new NpcInfo()).radius = 580.0D;
        this.config.LOOT.NPC_INFOS.computeIfAbsent("-=[ Saboteur ]=-", (n) -> new NpcInfo()).radius = 580.0D;
        this.config.COLLECT.BOX_INFOS.computeIfAbsent("ore_8", (n) -> new BoxInfo()).collect = true;
        this.config.COLLECT.BOX_INFOS.computeIfAbsent("ore_8", (n) -> new BoxInfo()).waitTime = 780;
        this.config.GROUP.OPEN_INVITES = true;
        this.config.GROUP.ACCEPT_INVITES = true;
    }

    private double getDistance(Entity entity) {
        return this.hero.locationInfo.now.distance(entity.locationInfo.now);
    }

    private void startAttack(Npc npc) {
        if (npc != null) {
            this.attack.target = npc;
            this.main.hero.attackMode(npc);
            this.attack.doKillTargetTick();
        }
    }

    public String status() {
        return this.currentStatus.message;
    }

    private void moveLogic() {
        Npc target = this.attack.target;
        if (target != null && target.locationInfo != null) {
            Location heroLoc = this.main.hero.locationInfo.now;
            Location targetLoc = target.locationInfo.destinationInTime(400L);
            double angle = targetLoc.angle(heroLoc);
            double radius = target.npcInfo.radius;
            double distance = radius;
            angle += Math.max((double)this.hero.shipInfo.speed * 0.625D + Double.min(200.0D, target.locationInfo.speed) * 0.625D - heroLoc.distance(Location.of(targetLoc, angle, radius)), 0.0D) / radius;
            Location direction = Location.of(targetLoc, angle, radius);
            while(!this.main.hero.drive.canMove(direction) && distance < 10000.0D) direction.toAngle(targetLoc, angle += 0.3D, distance += 2.0D);
            if (distance >= 10000.0D) direction.toAngle(targetLoc, angle, 500.0D);
            this.main.hero.drive.move(direction);
        }
    }

    public void setConfig(BattlerayFarmer.BattlerayConfig battlerayConfig) {
        this.battlerayConfig = battlerayConfig;
    }

    public static class BattlerayConfig {
        @Option(value = "Kill alien when fly", description = "Kill alien when fly")
        public boolean killAlienWhenFly = true;
        @Option(value = "Pick palladium", description = "Pick palladium while waiting Battleray")
        public boolean pickPalladium = false;
        @Option(value = "Kill saboteur", description = "Kill saboteur if it's slowing you down (need kill alien when fly on)")
        public boolean killSaboteur = false;
    }

    private enum State {
        IDLE("Idle"),
        DRIVE_BATTLERAY("Driving to Battleray"),
        DRIVE_BATTLERAY_ATTACK("Driving to Battleray, while attacking"),
        ATTACK_BATTLERAY("Killing Battleray"),
        ATTACK_INTERCEPTOR("Killing Interceptor"),
        DRIVE_SAFE("Driving to safe spot"),
        DRIVE_SAFE_REPAIR("Driving to safe spot for repair"),
        DRIVE_PALLADIUM("Searching Palladium");

        private final String message;
        State(String message) {
            this.message = message;
        }
    }
}
