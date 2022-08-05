package eu.darkbot.popcorn.def;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.BasePoint;
import com.github.manolo8.darkbot.core.itf.InstructionProvider;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.objects.OreTradeGui;
import com.github.manolo8.darkbot.core.objects.OreTradeGui.Ore;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.extensions.RegisterFeature;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;
import com.github.manolo8.darkbot.modules.MapModule;
import java.util.List;

@RegisterFeature("Default plugin")
@Feature(name = "Palladium Module", description = "Loot & collect, but when full cargo is full travels to 5-2 to sell")
public class PalladiumModule extends LootNCollectorModule implements InstructionProvider {
    private Map SELL_MAP;
    private Main main;
    private StatsManager statsManager;
    private List<BasePoint> bases;
    private OreTradeGui oreTrade;
    private long sellClick;

    public String instructions() {
        return "Recommended settings:\nGeneral -> Working map to 5-3\nCollect -> Set ore_8 wait to 750-800ms (depends on ping)\nNpc killer -> Pirate NPCs -> Kill & Enable Passive (in the Extra Column)\nAvoid zones -> Set in all areas of 5-3 except palladium field & paths to portals\nPreferred zones -> Set Preferred zone in palladium field\nGeneral -> Roaming & Preferred area -> enable only kill npcs in preferred area\nSafety places -> Set Portals to jump: Never.\nNpc killer -> Battleray -> Set low priority (100) so Interceptors are shot first";
    }

    public void install(Main main) {
        super.install(main);
        this.SELL_MAP = main.starManager.byName("5-2");
        this.main = main;
        this.statsManager = main.statsManager;
        this.bases = main.mapManager.entities.basePoints;
        this.oreTrade = main.guiManager.oreTrade;
    }

    public void tick() {
        if (this.statsManager.deposit >= this.statsManager.depositTotal && this.statsManager.depositTotal != 0) this.sell();
        else if (System.currentTimeMillis() - 500L > this.sellClick && this.oreTrade.showTrade(false, (BasePoint)null)) super.tick();
    }

    private void sell() {
        this.pet.setEnabled(false);
        if (this.hero.map != this.SELL_MAP) {
            this.main.setModule(new MapModule()).setTarget(this.SELL_MAP);
        } else {
            this.bases.stream().filter((b) -> b.locationInfo.isLoaded()).findFirst().ifPresent((base) -> {
                if (this.drive.movingTo().distance(base.locationInfo.now) > 200.0D) {
                    double angle = base.locationInfo.now.angle(this.hero.locationInfo.now) + Math.random() * 0.2D - 0.1D;
                    this.drive.move(Location.of(base.locationInfo.now, angle, 100.0D + 100.0D * Math.random()));
                } else if (!this.hero.locationInfo.isMoving() && this.oreTrade.showTrade(true, base) && System.currentTimeMillis() - 60000L > this.sellClick) {
                    this.oreTrade.sellOre(Ore.PALLADIUM);
                    this.sellClick = System.currentTimeMillis();
                }
            });
        }
    }
}
