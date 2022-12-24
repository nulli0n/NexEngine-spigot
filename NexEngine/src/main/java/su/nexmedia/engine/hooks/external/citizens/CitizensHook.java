package su.nexmedia.engine.hooks.external.citizens;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.manager.AbstractListener;

import java.util.*;

public final class CitizensHook extends AbstractListener<NexEngine> {

    private static final Map<NexPlugin<?>, Set<TraitInfo>>        TRAITS    = new HashMap<>();
    private static final Map<NexPlugin<?>, Set<CitizensListener>> LISTENERS = new HashMap<>();

    private static CitizensHook instance;

    private CitizensHook(NexEngine plugin) {
        super(plugin);
        this.registerListeners();
    }

    public static void setup() {
        if (instance == null) {
            instance = new CitizensHook(NexEngine.get());
        }
    }

    public static void shutdown() {
        if (instance != null) {
            instance.unregisterListeners();
            TRAITS.forEach((plugin, traits) -> {
                traits.forEach(trait -> CitizensAPI.getTraitFactory().deregisterTrait(trait));
            });
            TRAITS.clear();
            LISTENERS.clear();
        }
    }

    public static void addListener(@NotNull NexPlugin<?> plugin, @NotNull CitizensListener listener) {
        getListeners(plugin).add(listener);
        setup();
    }

    @NotNull
    public static Set<CitizensListener> getListeners(@NotNull NexPlugin<?> plugin) {
        return LISTENERS.computeIfAbsent(plugin, set -> new HashSet<>());
    }

    public static void unregisterListeners(@NotNull NexPlugin<?> plugin) {
        if (LISTENERS.remove(plugin) != null) {
            plugin.info("[Citizens Hook] Unregistered listeners");
        }
    }

    public static void registerTrait(@NotNull NexPlugin<?> plugin, @NotNull Class<? extends Trait> trait) {
        TraitInfo traitInfo = TraitInfo.create(trait);
        registerTrait(plugin, traitInfo);
    }

    public static void registerTrait(@NotNull NexPlugin<?> plugin, @NotNull TraitInfo trait) {
        unregisterTrait(plugin, trait);
        if (TRAITS.computeIfAbsent(plugin, set -> new HashSet<>()).add(trait)) {
            plugin.info("[Citizens Hook] Registered trait: " + trait.getTraitName());
            CitizensAPI.getTraitFactory().registerTrait(trait);
            setup();
        }
    }

    public static void unregisterTrait(@NotNull NexPlugin<?> plugin, @NotNull TraitInfo trait) {
        if (TRAITS.getOrDefault(plugin, Collections.emptySet()).remove(trait)) {
            plugin.info("[Citizens Hook] Unregistered trait: " + trait.getTraitName());
        }
        CitizensAPI.getTraitFactory().deregisterTrait(trait);
    }

    public static void unregisterTraits(@NotNull NexPlugin<?> plugin) {
        TRAITS.getOrDefault(plugin, Collections.emptySet()).forEach(trait -> {
            plugin.info("[Citizens Hook] Unregistered trait: " + trait.getTraitName());
            CitizensAPI.getTraitFactory().deregisterTrait(trait);
        });
        TRAITS.remove(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public static void onLeftClick(NPCLeftClickEvent e) {
        LISTENERS.values().forEach(set -> set.forEach(listener -> listener.onLeftClick(e)));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public static void onRightClick(NPCRightClickEvent e) {
        LISTENERS.values().forEach(set -> set.forEach(listener -> listener.onRightClick(e)));
    }
}
