package eu.darkbot.popcorn.def;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.manager.RepairManager;
import com.github.manolo8.darkbot.core.manager.EffectManager.Effect;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.extensions.RegisterFeature;
import com.github.manolo8.darkbot.modules.DisconnectModule;
import com.github.manolo8.darkbot.utils.I18n;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterFeature("Default plugin")
@Feature(name = "Anti push", description = "Turns off the bot if an enemy uses draw fire or is killed over X times by the same player", enabledByDefault = true)
public class AntiPush implements Behaviour, Configurable<AntiPush.Config> {
    private MapManager mapManager;
    private RepairManager repairManager;
    private List<Ship> ships;
    private Main main;
    private AntiPush.Config config;
    private final Map<Integer, List<Instant>> deathStats = new HashMap<>();
    private boolean wasDead = true;

    public void install(Main main) {
        this.main = main;
        this.mapManager = main.mapManager;
        this.ships = main.mapManager.entities.ships;
        this.repairManager = main.repairManager;
    }

    public void setConfig(AntiPush.Config config) {
        this.config = config;
    }

    public void tick() {
        this.tickDrawFire();
        this.tickDeathPause();
    }

    public void tickStopped() {
        if (this.config.DEATH_PAUSE_TIME != 0) {
            this.removeOldDeaths();
            if (this.repairManager.isDead() && !this.wasDead) {
                this.ships.stream().filter((s) -> s.playerInfo.username.equals(this.repairManager.getKillerName())).findFirst().ifPresent((killer) -> this.deathStats.computeIfAbsent(killer.id, (l) -> new ArrayList<>()).add(Instant.now()));
                this.wasDead = true;
            }
        }
    }

    private void tickDrawFire() {
        if (this.config.DRAWFIRE_PAUSE_TIME != 0)
            for (Ship ship : this.ships)
                if (ship.playerInfo.isEnemy() && ship.hasEffect(Effect.DRAW_FIRE) && this.mapManager.isTarget(ship)) {
                    System.out.println("Pausing bot" + (this.config.DRAWFIRE_PAUSE_TIME > 0 ? " for " + this.config.DRAWFIRE_PAUSE_TIME + " minutes" : "") + ", enemy used draw fire");
                    Long pauseMillis = this.config.DRAWFIRE_PAUSE_TIME > 0 ? (long) this.config.DRAWFIRE_PAUSE_TIME * 60L * 1000L : null;
                    this.main.setModule(new DisconnectModule(pauseMillis, I18n.get("module.disconnect.reason.draw_fire")));
                }
    }

    private void tickDeathPause() {
        if (this.config.DEATH_PAUSE_TIME != 0) {
            this.wasDead = false;
            this.deathStats.entrySet().stream().filter((e) -> e.getValue().size() >= this.config.MAX_DEATHS).findFirst().ifPresent((entry) -> {
                System.out.format("Pausing for %d minutes (Death pause feature): killed by %s %d times\n", this.config.DEATH_PAUSE_TIME, this.repairManager.getKillerName(), entry.getValue().size());
                this.main.setModule(new DisconnectModule(this.config.DEATH_PAUSE_TIME > 0 ? (long)(this.config.DEATH_PAUSE_TIME * 60L) * 1000L : null, I18n.get("module.disconnect.reason.death_pause", this.repairManager.getKillerName(), entry.getValue().size())));
                this.deathStats.remove(entry.getKey());
            });
        }
    }

    private void removeOldDeaths() {
        this.deathStats.values().forEach((time) -> time.removeIf((t) -> Duration.between(t, Instant.now()).toDays() >= 1L));
        this.deathStats.values().removeIf(List::isEmpty);
    }

    public static class Config {
        @Option(value = "Pause time on draw fire (minutes)", description = "Pause time, 0 to disable feature, -1 for infinite pause")
        @Num(min = -1, max = 300)
        public int DRAWFIRE_PAUSE_TIME = -1;
        @Option(value = "Max kills by same player", description = "The maximum times one player can kill you before bot pauses.")
        @Num(min = 1, max = 1000, step = 1)
        public int MAX_DEATHS = 7;
        @Option(value = "Pause time after kills reached (minutes)", description = "Time to pause after kills reached, 0 to disable feature, -1 for infinite pause")
        @Num(min = -1, max = 300)
        public int DEATH_PAUSE_TIME = -1;
    }
}
