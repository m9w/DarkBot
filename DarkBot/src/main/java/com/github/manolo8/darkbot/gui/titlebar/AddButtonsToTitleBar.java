package com.github.manolo8.darkbot.gui.titlebar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AddButtonsToTitleBar {
    Class<? extends enumButtons> value();
    interface enumButtons {
        Runnable getAction();
        default String getIcon() {
            return "plugins";
        }
    }
}

/** Sample using:
 * @RegisterFeature(value = "feature", author = "You")
 * @Feature(name = "Title of sample module", description = "desc")
 * @AddButtonsToTitleBar(SampleModule.Buttons.class)
 * public class SampleModule implements Module {
 *
 *     ... Module methods ...
 *
 *     enum Buttons implements AddButtonsToTitleBar.enumButtons {
 *         name1("heart", () -> { System.out.println("Works1!"); }),
 *         name2_XYZ("plugins", () -> {  System.out.println("Works2!"); }),
 *         Name3("add", () -> { System.out.println("Works3!"); });
 *
 *         final String icon;
 *         final Runnable action;
 *
 *         Buttons(String icon, Runnable action) {
 *             this.icon = icon;
 *             this.action = action;
 *         }
 *         public Runnable getAction() { return action; }
 *         public String getIcon() { return icon; }
 *     }
 * }
 */
