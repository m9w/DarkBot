package eu.darkbot.shared.utils;

import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.MovementAPI;
import eu.darkbot.api.managers.PetAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import java.util.Collection;

public class MapTraveler implements Listener {
    protected final EventBrokerAPI eventBroker;

    protected final PetAPI pet;
    protected final HeroAPI hero;
    protected final StarSystemAPI star;
    protected final MovementAPI movement;

    protected final Collection<? extends Portal> portals;

    protected final PortalJumper jumper;

    public Portal current;
    public GameMap target;

    protected int lastPortals;
    protected long shipTpWait = -1, mapChangeWait = -1;
    protected boolean done;

    public MapTraveler(PetAPI petApi,
                       HeroAPI heroApi,
                       StarSystemAPI starSystem,
                       MovementAPI movement,
                       PortalJumper jumper,
                       EntitiesAPI entities,
                       EventBrokerAPI eventBroker) {
        this.pet = petApi;
        this.hero = heroApi;
        this.star = starSystem;
        this.movement = movement;
        this.jumper = jumper;
        this.portals = entities.getPortals();
        this.eventBroker = eventBroker;
    }

    public void setTarget(GameMap target) {
        shipTpWait = mapChangeWait = -1;
        this.target = target;
        this.done = false;
    }

    public boolean isDone() {
        return done;
    }

    public void tick() {
        if (star.getCurrentMap() == target) {
            done = true;
            return;
        }

        if (hero.getLocationInfo().getLast().distanceTo(hero) > 5000)
            shipTpWait = System.currentTimeMillis() + 2000;

        if ((shipTpWait == -1) != (mapChangeWait == -1)) {
            if (System.currentTimeMillis() < Math.max(shipTpWait, mapChangeWait)) return;
        }
        if (current == null || !current.isValid() || lastPortals != portals.size()) {
            current = star.findNext(target);
            lastPortals = portals.size();
            jumper.reset();
        }

        if (current == null) {
            if (System.currentTimeMillis() - mapChangeWait > 3000)
                done = true; // No port found after 3 secs, just go back.
            return;
        }
        shipTpWait = mapChangeWait = -1;

        if (current.getLocationInfo().distanceTo(hero) > 1500) // Portal very close, no need to disable pet
            pet.setEnabled(false);
        hero.setRunMode();

        jumper.travelAndJump(current);
    }

    @EventHandler
    public void onMapChange(StarSystemAPI.MapChangeEvent event) {
        mapChangeWait = System.currentTimeMillis() + 2000;
        lastPortals = -1;
    }

}
