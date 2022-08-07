package com.github.manolo8.darkbot.extensions.features;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigHandler;
import com.github.manolo8.darkbot.extensions.plugins.*;
import com.github.manolo8.darkbot.utils.I18n;
import eu.darkbot.api.extensions.FeatureInfo;
import eu.darkbot.api.extensions.PluginInfo;
import eu.darkbot.api.extensions.RegisterFeature;
import eu.darkbot.api.managers.ExtensionsAPI;
import eu.darkbot.api.utils.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections8.Reflections;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class FeatureRegistry implements PluginListener, ExtensionsAPI {

    private final Main main;
    private final PluginHandler pluginHandler;
    private final Map<String, FeatureDefinition<?>> FEATURES_BY_ID = new LinkedHashMap<>();
    private final FeatureInstanceLoader featureLoader;
    private final ConfigHandler configHandler;

    private FeatureRegisterHandler registryHandler;

    public FeatureRegistry(Main main,
                           FeatureInstanceLoader featureLoader,
                           PluginHandler pluginHandler,
                           ConfigHandler configHandler) {
        this.main = main;
        this.pluginHandler = pluginHandler;
        this.featureLoader = featureLoader;
        this.configHandler = configHandler;

        pluginHandler.addListener(this);
    }

    @Inject
    public void setFeatureRegisterHandler(FeatureRegisterHandler registryHandler) {
        this.registryHandler = registryHandler;
    }

    @Override
    public void beforeLoad() {
        FEATURES_BY_ID.values()
                .stream()
                .map(FeatureDefinition::getInstance)
                .filter(Objects::nonNull)
                .forEach(featureLoader::unloadFeature);
        FEATURES_BY_ID.clear();
        registryHandler.getNativeFeatures().forEach(this::registerNativeFeature);
        registryHandler.update();
    }

    @Override
    public void afterLoad() {
        new Reflections().getTypesAnnotatedWith(RegisterFeature.class).forEach(feature -> registerPluginFeature(tryBuildFakePlugin(feature.getAnnotation(RegisterFeature.class)), feature));
        pluginHandler.LOADED_PLUGINS.stream().filter(plugin -> plugin.getFile() != null).forEach(pl -> getFeaturesList(pl).forEach(feature -> registerPluginFeature(pl, feature)));
        pluginHandler.LOADED_PLUGINS.sort((p1, p2) -> Boolean.compare(isDisabled(p1), isDisabled(p2)));
        registryHandler.update();
    }

    private List<String> getFeaturesList(Plugin plugin) {
        if (plugin.getDefinition().features.length != 0) return Arrays.asList(plugin.getDefinition().features);
        URLClassLoader loader = new URLClassLoader(new URL[]{plugin.getJar()});
        List<String> features = new LinkedList<>();
        try (JarFile jarFile = new JarFile(plugin.getFile())) {
            Enumeration<JarEntry> e = jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry jarEntry = e.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    String className = jarEntry.getName().replace("/", ".").replace(".class", "");
                    if(loader.loadClass(className).isAnnotationPresent(RegisterFeature.class))
                        features.add(className);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return features;
    }

    private boolean isDisabled(Plugin plugin) {
        return Arrays.stream(plugin.getFeatureIds())
                .map(this::getFeatureDefinition)
                .filter(Objects::nonNull)
                .noneMatch(FeatureDefinition::isEnabled);
    }

    public void updateConfig() {
        for (FeatureDefinition<?> fd : FEATURES_BY_ID.values()) {
            fd.sendUpdate(); // Update the checkbox in plugins tab
            configHandler.updateFeatureConfig(fd); // Update the value available in the feature definition
            featureLoader.updateConfig(fd);        // Update the value in the instance (eg: call feature.setConfig)
        }
    }

    private void registerNativeFeature(Class<?> clazz) {
        FEATURES_BY_ID.put(clazz.getCanonicalName(), new FeatureDefinition<>(null, clazz, fd -> null));
    }

    private void registerPluginFeature(Plugin plugin, Class<?> feature) {
        FeatureDefinition<?> fd = new FeatureDefinition<>(plugin, feature, configHandler::getFeatureConfig);
        fd.addStatusListener(def -> {
            registryHandler.update();
            if (main.getGui() != null)
                main.getGui().updateConfigTreeListeners();
        });
        fd.getIssues().addListener(iss -> registryHandler.update());
        FEATURES_BY_ID.put(feature.getCanonicalName(), fd);
    }

    private void registerPluginFeature(Plugin plugin, String clazzName) {
        try {
            registerPluginFeature(plugin, pluginHandler.PLUGIN_CLASS_LOADER.loadClass(clazzName));
        } catch (Throwable e) {
            plugin.getIssues().addWarning("bot.issue.feature.failed_to_load",
                    I18n.get("bot.issue.feature.failed_to_load.desc", clazzName, e.toString()));
        }
    }

    private <T> Optional<T> getFeature(String id) {
        synchronized (pluginHandler) {
            FeatureDefinition<T> feature = getFeatureDefinition(id);
            try {
                if (feature == null || !feature.canLoad()) return Optional.empty();

                T instance = feature.getInstance();
                if (instance != null) return Optional.of(instance);

                feature.setInstance(instance = featureLoader.loadFeature(feature));
                return Optional.of(instance);
            } catch (Throwable e) {
                feature.getIssues().addFailure("bot.issue.feature.failed_to_load", IssueHandler.createDescription(e));
                e.printStackTrace();
                return Optional.empty();
            }
        }
    }

    public <T> Optional<T> getFeature(Class<T> feature) {
        return getFeature(feature.getCanonicalName(), feature);
    }

    public <T> Optional<T> getFeature(String id, Class<T> type) {
        return getFeature(id);
    }

    public <T> Optional<T> getFeature(FeatureDefinition<T> fd) {
        return getFeature(fd.getId());
    }

    public Collection<FeatureDefinition<?>> getFeatures() {
        return FEATURES_BY_ID.values();
    }

    public <T> Stream<FeatureDefinition<T>> getFeatures(Class<T> type) {
        //noinspection unchecked
        return FEATURES_BY_ID
                .values()
                .stream()
                .filter(FeatureDefinition::canLoad)
                .filter(fd -> type.isAssignableFrom(fd.getClazz()))
                .map(fd -> (FeatureDefinition<T>) fd);
    }

    public Stream<FeatureDefinition<?>> getFeatures(Plugin plugin) {
        return FEATURES_BY_ID
                .values()
                .stream()
                .filter(fd -> fd.getPlugin() == plugin);
    }

    public <T> @Nullable FeatureDefinition<T> getFeatureDefinition(T feature) {
        return getFeatureDefinition(feature.getClass().getCanonicalName());
    }

    public <T> @Nullable FeatureDefinition<T> getFeatureDefinition(Class<T> feature) {
        return getFeatureDefinition(feature.getCanonicalName());
    }

    public <T> @Nullable FeatureDefinition<T> getFeatureDefinition(String id) {
        synchronized (pluginHandler) {
            //noinspection unchecked
            return (FeatureDefinition<T>) FEATURES_BY_ID.get(id);
        }
    }

    @Override
    public @NotNull Collection<? extends PluginInfo> getPluginInfos() {
        return pluginHandler.LOADED_PLUGINS;
    }

    @Override
    public <T> FeatureInfo<T> getFeatureInfo(@NotNull Class<T> feature) {
        return getFeatureDefinition(feature);
    }

    public <T> FeatureInfo<T> getFeatureInfo(@NotNull String featureId) {
        return getFeatureDefinition(featureId);
    }

    @Override
    public ClassLoader getClassLoader(@NotNull PluginInfo pluginInfo) {
        return pluginHandler.PLUGIN_CLASS_LOADER;
    }

    Plugin tryBuildFakePlugin(RegisterFeature feature){
        if(feature.value().length() == 0) return null;
        Plugin plugin = pluginHandler.LOADED_PLUGINS.stream().filter(p -> "org.marker.fake_plugin".equals(p.getBasePackage()))
                .filter(p -> feature.value().equals(p.getName())).findFirst().orElse(null);
        if(plugin != null) return plugin;
        PluginDefinition pluginDefinition = new PluginDefinition();
        pluginDefinition.name = feature.value();
        pluginDefinition.author = feature.author();
        pluginDefinition.basePackage = "org.marker.fake_plugin";
        pluginDefinition.version = main.getVersion();
        pluginHandler.LOADED_PLUGINS.add(plugin = new Plugin(null, null));
        plugin.setDefinition(pluginDefinition);
        return plugin;
    }
}