package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.types.suppliers.*;
import com.github.manolo8.darkbot.config.utils.ItemUtils;
import com.github.manolo8.darkbot.core.manager.HookAdapter;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.tree.editors.CharacterEditor;
import com.github.manolo8.darkbot.gui.tree.utils.NpcTableModel;
import com.github.manolo8.darkbot.gui.tree.utils.TableHelpers;
import com.github.manolo8.darkbot.utils.SystemValues;
import eu.darkbot.api.config.annotations.Number;
import eu.darkbot.api.config.annotations.*;
import eu.darkbot.api.config.annotations.Visibility.Level;
import eu.darkbot.api.game.enums.PetGear;
import eu.darkbot.api.game.items.ItemCategory;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.shared.modules.LootCollectorModule;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag.*;

public class Config implements eu.darkbot.api.config.legacy.Config {

    // Defined map areas
    public Map<Integer, ZoneInfo> AVOIDED = new HashMap<>();
    public Map<Integer, ZoneInfo> PREFERRED = new HashMap<>();
    public Map<Integer, Set<SafetyInfo>> SAFETY = new HashMap<>();
    public transient Lazy<SafetyInfo> ADDED_SAFETY = new Lazy.NoCache<>();

    // Player list & resolver
    public Map<Integer, PlayerInfo> PLAYER_INFOS = new HashMap<>();
    public Set<PlayerTag> PLAYER_TAGS = new HashSet<>();
    public transient Lazy<Integer> PLAYER_UPDATED = new Lazy.NoCache<>();
    public Queue<UnresolvedPlayer> UNRESOLVED = new LinkedList<>();


    // Plugin custom configuration objects
    public Map<String, Object> CUSTOM_CONFIGS = new HashMap<>();
    public Map<String, PluginConfig> PLUGIN_INFOS = new HashMap<>();

    public transient boolean changed;
    public transient long changedAt;

    public @Option General GENERAL = new General();
    public static class General {
        @Option @Dropdown(options = ModuleSupplier.class)
        public String CURRENT_MODULE = LootCollectorModule.class.getCanonicalName();
        public @Option @Dropdown(options = StarManager.MapOptions.class) int WORKING_MAP = 26;
        public @Option ShipConfig OFFENSIVE = new ShipConfig(1, '8');
        public @Option ShipConfig ROAM = new ShipConfig(1, '9');
        public @Option ShipConfig RUN = new ShipConfig(2, '9');

        public @Option Safety SAFETY = new Safety();
        public static class Safety {
            public @Option PercentRange REPAIR_HP_RANGE = new PercentRange(0.4, 0.95);
            public @Option @Percentage double REPAIR_HP_NO_NPC = 0.5;
            public @Option @Percentage double REPAIR_TO_SHIELD = 1;
            public @Option ShipConfig REPAIR = new ShipConfig(1, '9');
            public @Option @Number(min = 1, max = 9999) @Number.Disabled(value = -1, def = 10) int MAX_DEATHS = 10;
            @Deprecated
            public long REVIVE_LOCATION = 1L;
            public @Option("config.general.safety.revive_location") @Dropdown ReviveLocation REVIVE = ReviveLocation.BASE;
            public @Option @Visibility(Level.INTERMEDIATE) @Number(min = 5, max = 60, step = 10) int WAIT_BEFORE_REVIVE = 5;
            public @Option @Visibility(Level.INTERMEDIATE) @Number(min = 3, max = 15 * 60, step = 10) int WAIT_AFTER_REVIVE = 90;
        }

        public @Option Running RUNNING = new Running();
        public static class Running {
            public @Option boolean RUN_FROM_ENEMIES = true;
            public @Option @Visibility(Level.INTERMEDIATE) @Number(max = 24 * 60 * 60, step = 300) int REMEMBER_ENEMIES_FOR = 300;
            public @Option boolean RUN_FROM_ENEMIES_SIGHT = false;
            public @Option boolean STOP_RUNNING_NO_SIGHT = true;
            public @Option @Visibility(Level.INTERMEDIATE) @Number(min = 500, max = 20000, step = 500) int MAX_SIGHT_DISTANCE = 4000;
            public @Option Character SHIP_ABILITY;
            public @Option @Visibility(Level.INTERMEDIATE) @Number(max = 20000, step = 500) int SHIP_ABILITY_MIN = 1500;
            public @Option @Visibility(Level.INTERMEDIATE) @Number(max = 20000, step = 500) int RUN_FURTHEST_PORT = 1500;
        }

        public @Option @Visibility(Level.INTERMEDIATE) Roaming ROAMING = new Roaming();
        public static class Roaming {
            public @Option boolean KEEP = true;
            public @Option boolean SEQUENTIAL = false;
            public @Option boolean ONLY_KILL_PREFERRED = false;
            public @Option boolean ENEMY_CBS_INVISIBLE = false;
        }
    }

    public @Option Collect COLLECT = new Collect();
    public static class Collect {
        public @Option boolean STAY_AWAY_FROM_ENEMIES;
        public @Option boolean AUTO_CLOACK;
        public @Option Character AUTO_CLOACK_KEY;
        public @Option @Number(max = 10000, step = 50) int RADIUS = 400;
        public @Option boolean IGNORE_CONTESTED_BOXES = true;

        @Option
        @Table(decorator = TableHelpers.BoxInfoDecorator.class)
        public Map<String, BoxInfo> BOX_INFOS = new HashMap<>();
        public transient Lazy<String> ADDED_BOX = new Lazy.NoCache<>();
    }

    public @Option Loot LOOT = new Loot();
    public static class Loot {
        public @Option Sab SAB = new Sab();
        public @Option Rsb RSB = new Rsb();
        public static class Sab {
            public @Option boolean ENABLED = false;
            public @Option Character KEY = '2';
            public @Option @Percentage double PERCENT = 0.8;
            public @Option @Number(min = 500, max = 1_000_000, step = 1000) int NPC_AMOUNT = 12000;
            public @Option @Visibility(Level.ADVANCED) Condition CONDITION;
        }
        public static class Rsb {
            public @Option boolean ENABLED = false;
            public @Option Character KEY = '3';
            public @Deprecated int AMMO_REFRESH = 3500;
        }
        public @Option Character AMMO_KEY = '1';
        public @Option @Visibility(Level.INTERMEDIATE) Character SHIP_ABILITY;
        public @Option @Visibility(Level.INTERMEDIATE) @Number(min = 50_000, max = 5_000_000, step = 50_000) int SHIP_ABILITY_MIN = 150_000;
        public @Option @Visibility(Level.ADVANCED) @Number(max = 10, step = 1) int MAX_CIRCLE_ITERATIONS = 5;
        public @Option boolean RUN_CONFIG_IN_CIRCLE = true;

        public @Option boolean GROUP_NPCS = true;
        @Table(controls = {Table.Control.SEARCH, Table.Control.CUSTOM, Table.Control.ADD, Table.Control.REMOVE},
                customControls = TableHelpers.MapPickerBuilder.class,
                customModel = NpcTableModel.class,
                decorator = TableHelpers.NpcTableDecorator.class)
        public @Option Map<String, NpcInfo> NPC_INFOS = new HashMap<>();
        public transient Lazy<String> MODIFIED_NPC = new Lazy.NoCache<>();

        public @Option @Visibility(Level.INTERMEDIATE) @Number(min = 1000, max = 20000, step = 500) int NPC_DISTANCE_IGNORE = 3000;
    }

    public @Option PetSettings PET = new PetSettings();
    public static class PetSettings {
        public @Option boolean ENABLED = false;
        public @Option @Dropdown(options = PetGears.class) PetGear MODULE_ID = PetGear.PASSIVE;
        public @Option PercentRange FUEL_RANGE = new PercentRange(0.05, 0.1);
    }

    public @Option GroupSettings GROUP = new GroupSettings();
    public static class GroupSettings {
        public @Option boolean ACCEPT_INVITES = false;
        public @Option @Tag(Tag.Default.ALL) PlayerTag WHITELIST_TAG = null;
        public @Option @Tag(Tag.Default.NONE) PlayerTag INVITE_TAG = null;
        public @Option boolean OPEN_INVITES = false;
        public @Option boolean LEAVE_NO_WHITELISTED = false;
        //public @Option @Tag(TagDefault.NONE) PlayerTag KICK_TAG = null;
    }

    public @Option Miscellaneous MISCELLANEOUS = new Miscellaneous();
    public static class Miscellaneous {
        public @Option boolean REFRESH_AFTER_REVIVE = false;
        public @Option @Number(max = 60 * 12, step = 10) int REFRESH_TIME = 60;
        public @Option @Number(max = 60 * 12, step = 10) int PAUSE_FOR = 0;
        public @Option boolean RESET_REFRESH = true;
        public @Option @Visibility(Level.INTERMEDIATE) boolean UPDATE_STATS_WHILE_PAUSED = true;
        public @Option @Visibility(Level.INTERMEDIATE) @Percentage double DRONE_REPAIR_PERCENTAGE = 0.9;
        public @Option @Visibility(Level.INTERMEDIATE) boolean HONOR_LOST_EXACT = true;
        public @Option @Visibility(Level.INTERMEDIATE) boolean LOG_CHAT = false;
        public @Option @Visibility(Level.INTERMEDIATE) boolean LOG_DEATHS = false;
        public @Option @Visibility(Level.INTERMEDIATE) boolean AVOID_MINES = true;
        public @Option @Visibility(Level.INTERMEDIATE) boolean USERNAME_ON_TITLE = false;
        public @Option @Visibility(Level.ADVANCED) boolean AUTO_REFINE = false;
    }

    public @Option BotSettings BOT_SETTINGS = new BotSettings();
    public static class BotSettings {
        public @Option BotGui BOT_GUI = new BotGui();
        public static class BotGui {
            @Option @Dropdown(options = LanguageSupplier.class)
            public Locale LOCALE = new Locale(Locale.getDefault().getLanguage());

            public @Option @Visibility(Level.INTERMEDIATE) boolean CONFIRM_EXIT = true;
            public @Option @Visibility(Level.INTERMEDIATE) boolean SAVE_GUI_POS = false;
            public @Option @Visibility(Level.ADVANCED) @Number(min = 1, max = 20, step = 1) int BUTTON_SIZE = 4;

            public boolean ALWAYS_ON_TOP = true; // No @Option. Edited via button
            public WindowPosition MAIN_GUI_WINDOW = new WindowPosition();
            public WindowPosition CONFIG_GUI_WINDOW = new WindowPosition();

            public static class WindowPosition {
                // x and y refer to top left coordinates of window
                public int x = Integer.MIN_VALUE, y = Integer.MIN_VALUE;
                public int width = MainGui.DEFAULT_WIDTH, height = MainGui.DEFAULT_HEIGHT;
            }
        }

        public @Option @Visibility(Level.ADVANCED) APIConfig API_CONFIG = new APIConfig();
        public static class APIConfig {
            public @Option @Dropdown BrowserApi BROWSER_API = SystemValues.get(SystemValues.preferAPI);
            public @Option boolean FULLY_HIDE_API = true;
            public @Option boolean FORCE_GAME_LANGUAGE = false;
            public @Option boolean ENFORCE_HW_ACCEL = true;
            //public @Option boolean USE_3D = false; // After reload with forced HW_ACCEL in 3D, cpu usage is really high
            public @Option @Number(min = 1, max = 60) @Number.Disabled(value = 0, def = 30) int MAX_FPS = 0;

            @Option @Dropdown(multi = true)
            public Set<HookAdapter.Flag> DARK_HOOK_FLAGS = EnumSet.allOf(HookAdapter.Flag.class);

            public int width = 1280;
            public int height = 800;
        }

        public @Option MapDisplay MAP_DISPLAY = new MapDisplay();
        public static class MapDisplay {
            @Option @Dropdown(multi = true)
            public Set<DisplayFlag> TOGGLE = EnumSet.of(
                    HERO_NAME, HP_SHIELD_NUM, ZONES, STATS_AREA, BOOSTER_AREA, GROUP_NAMES, GROUP_AREA, SHOW_PET);
            public @Option @Visibility(Level.INTERMEDIATE) @Number(max = 300, step = 1) int TRAIL_LENGTH = 15;
            public @Option @Visibility(Level.INTERMEDIATE) boolean MAP_START_STOP = false;
            public @Option("colors") @Visibility(Level.ADVANCED) ColorScheme cs = new ColorScheme();
        }

        public @Option @Visibility(Level.INTERMEDIATE) CustomBackground CUSTOM_BACKGROUND = new CustomBackground();
        public static class CustomBackground {
            public @Option boolean ENABLED = false;
            public @Option boolean USE_GAME_BACKGROUND = false;
            public @Option @Percentage double OPACITY = 0.3f;
            public @Option ImageWrapper IMAGE = new ImageWrapper();
        }

        public @Option @Visibility(Level.INTERMEDIATE) Other OTHER = new Other();
        public static class Other {
            public @Option boolean DISABLE_MASTER_PASSWORD = false;
            public @Option @Number(min = 10, max = 300) int ZONE_RESOLUTION = 30;
            public @Option @Visibility(Level.ADVANCED) @Number(min = 10, max = 250) int MIN_TICK = 15;
            public @Option @Visibility(Level.ADVANCED) boolean DEV_STUFF = false;
        }
    }

    public /*@Option("Extra actions")*/ ExtraActions EXTRA = new ExtraActions();
    public static class ExtraActions {
        // Dummy testing condition
        public @Option Condition CONDITION;

        public @Option @Table Map<String, ActionInfo> ACTION_INFOS = new HashMap<>();
        public transient Lazy<String> MODIFIED_ACTIONS = new Lazy.NoCache<>();
    }

    public static class ShipConfig implements LegacyShipMode {
        public int CONFIG = 1;
        public Character FORMATION;

        public ShipConfig() {}
        public ShipConfig(int CONFIG, Character FORMATION) {
            this.CONFIG = CONFIG;
            this.FORMATION = FORMATION;
        }

        @Override
        public HeroAPI.Configuration getConfiguration() {
            return HeroAPI.Configuration.of(CONFIG);
        }

        @Override
        public SelectableItem.Formation getFormation() {
            return ItemUtils.findAssociatedItem(ItemCategory.DRONE_FORMATIONS, FORMATION)
                    .map(it -> SelectableItem.Formation.of(it.id))
                    .orElse(SelectableItem.Formation.STANDARD);
        }

        @Override
        public String toString() {
            return "Config: " + CONFIG + "   Formation: " + CharacterEditor.getDisplay(FORMATION);
        }

        @Override
        public @Nullable Character getLegacyFormation() {
            return FORMATION;
        }
    }

    public static class PercentRange implements eu.darkbot.api.config.types.PercentRange {
        public double min, max;

        public PercentRange() {}
        public PercentRange(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public double getMin() {
            return min;
        }

        @Override
        public double getMax() {
            return max;
        }

        @Override
        public String toString() {
            return Math.round(min * 100) + "%-" + Math.round(max * 100) + "%";
        }
    }


    @Override
    public Collection<? extends eu.darkbot.api.config.types.SafetyInfo> getSafeties(GameMap gameMap) {
        return SAFETY.getOrDefault(gameMap.getId(), Collections.emptySet());
    }

    @Override
    public eu.darkbot.api.config.types.ZoneInfo getPreferredZone(GameMap gameMap) {
        return PREFERRED.computeIfAbsent(gameMap.getId(), id -> new ZoneInfo(BOT_SETTINGS.OTHER.ZONE_RESOLUTION));
    }

    @Override
    public eu.darkbot.api.config.types.ZoneInfo getAvoidedZone(GameMap gameMap) {
        return AVOIDED.computeIfAbsent(gameMap.getId(), id -> new ZoneInfo(BOT_SETTINGS.OTHER.ZONE_RESOLUTION));
    }

    @Override
    public Map<Integer, ? extends eu.darkbot.api.config.types.PlayerInfo> getPlayerInfos() {
        return PLAYER_INFOS;
    }

    @Override
    public eu.darkbot.api.config.types.PlayerInfo getPlayerInfo(int id) {
        return PLAYER_INFOS.computeIfAbsent(id, i -> new PlayerInfo(null, i));
    }

    @Override
    public void refreshPlayerList() {
        PLAYER_UPDATED.send(null);
    }

}