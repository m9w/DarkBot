package eu.darkbot.popcorn.def;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.FlashResManager;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.RegisterFeature;
import com.github.manolo8.darkbot.modules.TemporalModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RegisterFeature
@Feature(name = "Captcha picker", description = "Picks up captcha boxes when they appear", enabledByDefault = true)
public class CaptchaPicker extends TemporalModule implements Behaviour {
    private static final Pattern SPECIAL_REGEX = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");
    private static final Set<String> ALL_CAPTCHA_TYPES = Arrays.stream(Captcha.values()).map((c) -> c.box).collect(Collectors.toSet());
    private Main main;
    private HeroManager hero;
    private Drive drive;
    private FlashResManager flashResManager;
    private final Consumer<String> logConsumer = this::onLogReceived;
    private final List<String> pastLogMessages = new ArrayList<>();
    private CaptchaPicker.Captcha captchaType;
    private List<Box> boxes;
    private List<Box> toCollect;
    private long waiting;
    private long maxActiveTime;

    public void install(Main main) {
        super.install(main);
        this.main = main;
        this.hero = main.hero;
        this.drive = main.hero.drive;
        this.flashResManager = main.featureRegistry.getFeature(FlashResManager.class).orElseThrow(IllegalStateException::new);
        this.boxes = main.mapManager.entities.boxes;
        this.toCollect = null;
        main.facadeManager.log.logs.add(this.logConsumer);
    }

    public void uninstall() {
        this.main.facadeManager.log.logs.remove2(this.logConsumer);
    }

    private void onLogReceived(String log) {
        if (this.flashResManager.getTranslation(CaptchaPicker.Captcha.SOME_RED.key) == null) this.pastLogMessages.add(log);
        else {
            CaptchaPicker.Captcha[] var2 = CaptchaPicker.Captcha.values();
            for (Captcha captcha : var2)
                if (captcha.matches(log, this.flashResManager))
                    this.setCurrentCaptcha(captcha);
        }
    }

    public boolean canRefresh() {
        return false;
    }

    public String status() {
        return "Solving captcha: Collecting " + (this.captchaType == null ? "(waiting for log...)" : (this.captchaType.hasAmount ? this.captchaType.amount : "all") + " " + this.captchaType.box + " box(es)");
    }

    public void tickBehaviour() {
        if (!this.pastLogMessages.isEmpty() && this.flashResManager.getTranslation(CaptchaPicker.Captcha.SOME_RED.key) != null) {
            this.pastLogMessages.forEach(this::onLogReceived);
            this.pastLogMessages.clear();
        }

        if (this.main.module != this && this.hasAnyCaptchaBox()) {
            this.maxActiveTime = System.currentTimeMillis() + 30000L;
            this.main.setModule(this);
        }
    }

    public void tick() {
        if (!this.isWaiting()) {
            if (!this.hasAnyCaptchaBox()) {
                this.goBack();
            }

            this.drive.stop(false);
            if (System.currentTimeMillis() > this.maxActiveTime) {
                System.out.println("Triggering refresh: Timed out trying to solve captcha");
                this.goBack();
                Main.API.handleRefresh();
            }

            if (this.toCollect == null) {
                if (this.captchaType == null) return;
                Stream<Box> boxStream = this.boxes.stream().filter(this.captchaType::matches).sorted(Comparator.comparingDouble((box) -> this.hero.locationInfo.now.distance(box)));
                if (this.captchaType.hasAmount) boxStream = boxStream.limit(this.captchaType.amount);
                this.toCollect = boxStream.collect(Collectors.toList());
            }

            this.toCollect.stream().filter((b) -> !b.isCollected()).findFirst().ifPresent(this::collectBox);
        }
    }

    public void tickStopped() {
        this.maxActiveTime = System.currentTimeMillis() + 30000L;
    }

    private boolean hasAnyCaptchaBox() {
        Stream<String> var10000 = this.boxes.stream().map((b) -> b.type);
        return var10000.anyMatch(ALL_CAPTCHA_TYPES::contains);
    }

    private void collectBox(Box box) {
        box.clickable.setRadius(800);
        this.drive.clickCenter(true, box.locationInfo.now);
        box.setCollected();
        this.waiting = System.currentTimeMillis() + (long)Math.min(1000, box.getRetries() * 100) + this.hero.timeTo(this.hero.locationInfo.distance(box)) + 500L;
    }

    public boolean isWaiting() {
        return System.currentTimeMillis() < this.waiting;
    }

    private void setCurrentCaptcha(CaptchaPicker.Captcha captcha) {
        this.waiting = System.currentTimeMillis() + 500L;
        this.captchaType = captcha;
        this.toCollect = null;
    }

    protected void goBack() {
        this.toCollect = null;
        super.goBack();
    }

    private static String escapeRegex(String str) {
        return SPECIAL_REGEX.matcher(str).replaceAll("\\\\$0");
    }

    private enum Captcha {
        SOME_BLACK("POISON_PUSAT_BOX_BLACK", "captcha_choose_some_black"),
        ALL_BLACK("POISON_PUSAT_BOX_BLACK", "captcha_choose_all_black"),
        SOME_RED("BONUS_BOX_RED", "captcha_choose_some_red"),
        ALL_RED("BONUS_BOX_RED", "captcha_choose_all_red");

        private final String box;
        private final String key;
        private Pattern pattern;
        private boolean hasAmount;
        private int amount;

        Captcha(String name, String key) {
            this.box = name;
            this.key = key;
        }

        public boolean matches(String log, FlashResManager resManager) {
            if (this.pattern == null) {
                if (resManager == null) return false;
                String translation = resManager.getTranslation(this.key);
                if (translation == null || translation.isEmpty()) return false;
                this.hasAmount = translation.contains("%AMOUNT%");
                this.pattern = Pattern.compile(CaptchaPicker.escapeRegex(translation).replace("%AMOUNT%", "(?<amount>[0-9]+)").replace("%TIME%", "(?<time>[0-9]+)"));
            }
            Matcher m = this.pattern.matcher(log);
            boolean matched = m.matches();
            if (this.hasAmount && matched) this.amount = Integer.parseInt(m.group("amount"));
            return matched;
        }

        public boolean matches(Box box) {
            return this.box.equals(box.type);
        }

        public String toString() {
            return this.name() + (this.hasAmount ? " " + this.amount : "");
        }
    }
}
