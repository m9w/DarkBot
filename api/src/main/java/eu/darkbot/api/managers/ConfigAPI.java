package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.legacy.Config;
import eu.darkbot.api.config.types.BoxInfo;
import eu.darkbot.api.config.types.NpcInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

/**
 * Provides access to the bot user-managed configuration.
 */
public interface ConfigAPI extends API.Singleton {

    @Deprecated
    default Config getLegacy() {
        ConfigSetting<Config> cfg = getConfigRoot();
        return cfg.getValue();
    }

    /**
     * @param <T> the type to get it as
     * @return the root node of the bot configuration tree
     */
    <T> ConfigSetting<T> getConfigRoot();

    /**
     * Get the configuration for a specific path
     *
     * @param path configuration tree path, each segment is separated by a dot
     * @param <T> the type of the config node
     * @return the configuration on this path, null if not found
     */
    default @Nullable <T> ConfigSetting<T> getConfig(String path) {
        return getConfig(getConfigRoot(), path);
    }

    /**
     * Get the configuration for a specific path
     *
     * @param path configuration tree path, each segment is separated by a dot
     * @param <T> the type of the config node
     * @return the configuration on this path, null if not found
     * @throws IllegalArgumentException if the config path doesn't exist
     */
    default <T> @NotNull ConfigSetting<T> requireConfig(String path) {
        return requireConfig(getConfigRoot(), path);
    }

    /**
     * Get the configuration for a specific path
     *
     * @param path configuration tree path, each segment is separated by a dot
     * @param <T> the type of the config node
     * @return optional with the configuration on this path, empty optional if the path doesn't exist
     */
    default <T> Optional<ConfigSetting<T>> optionalConfig(String path) {
        return optionalConfig(getConfigRoot(), path);
    }

    /**
     * Get the current value of the configuration in a specific path
     *
     * @param path configuration tree path, each segment is separated by a dot
     * @param <T> the type of the config node
     * @return the configuration value on this path, null if not found
     * @throws ClassCastException if the type T does not properly match the type of this setting
     */
    default @Nullable <T> T getConfigValue(String path) {
        return getConfigValue(getConfigRoot(), path);
    }

    /**
     * Returns all child nodes that stem from the provided path
     *
     * @param path configuration tree path, each segment is separated by a dot
     * @return set of child nodes available, null for leaf or not found nodes
     */
    default Set<String> getChildren(String path) {
        return getChildren(getConfigRoot(), path);
    }

    /**
     * Get the configuration for a specific path in a specific config
     *
     * @param root configuration root to search on
     * @param path configuration tree path, each segment is separated by a dot
     * @param <T> the type of the config node
     * @return the configuration on this path, null if not found
     */
    default <T> @Nullable ConfigSetting<T> getConfig(ConfigSetting<?> root, String path) {
        try {
            return requireConfig(root, path);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Get the configuration for a specific path in a specific config
     *
     * @param root configuration root to search on
     * @param path configuration tree path, each segment is separated by a dot
     * @param <T> the type of the config node
     * @return the configuration on this path, null if not found
     * @throws IllegalArgumentException if the config path doesn't exist
     */
    default <T> @NotNull ConfigSetting<T> requireConfig(ConfigSetting<?> root, String path) {
        String[] paths = path.isEmpty() ? new String[]{} : path.split("\\.");
        for (String s : paths) {
            if (root instanceof ConfigSetting.Parent)
                root = ((ConfigSetting.Parent<?>) root).getChildren().get(s);
            else
                root = null;

            if (root == null)
                throw new IllegalArgumentException("Configuration not found: " + s + " in " + path);
        }
        //noinspection unchecked
        return (ConfigSetting<T>) root;
    }

    /**
     * Get the configuration for a specific path in a specific config
     *
     * @param root configuration root to search on
     * @param path configuration tree path, each segment is separated by a dot
     * @param <T> the type of the config node
     * @return optional with the configuration on this path, empty optional if the path doesn't exist
     */
    default <T> Optional<ConfigSetting<T>> optionalConfig(ConfigSetting<?> root, String path) {
        return Optional.ofNullable(getConfig(root, path));
    }

    /**
     * Get the current value of the configuration in a specific path, for a specific config
     *
     * @param root configuration root to search on
     * @param path configuration tree path, each segment is separated by a dot
     * @param <T> the type of the config node
     * @return the configuration value on this path, null if not found
     * @throws ClassCastException if the type T does not properly match the type of this setting
     */
    default <T> T getConfigValue(ConfigSetting<?> root, String path) {
        ConfigSetting<T> config = getConfig(root, path);
        return config != null ? config.getValue() : null;
    }

    /**
     * Returns all child nodes that stem from the provided path, for a specific config
     *
     * @param root configuration root to search on
     * @param path configuration tree path, each segment is separated by a dot
     * @return set of child nodes available, null for leaf nodes
     */
    default Set<String> getChildren(ConfigSetting<?> root, String path) {
        ConfigSetting<?> setting = getConfig(root, path);
        if (setting instanceof ConfigSetting.Parent)
            return ((ConfigSetting.Parent<?>) setting).getChildren().keySet();
        return null;
    }

    /**
     * Will get or create BoxInfo with given box name
     *
     * @param name of the box
     * @return BoxInfo of given box name
     */
    BoxInfo getOrCreateBoxInfo(String name);

    /**
     * Will get or create NpcInfo with given npc name
     *
     * @param name of the npc
     * @return NpcInfo of given npc name
     */
    NpcInfo getOrCreateNpcInfo(String name);
}
