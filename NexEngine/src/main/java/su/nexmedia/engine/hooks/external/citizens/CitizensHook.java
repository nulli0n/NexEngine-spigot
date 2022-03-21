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
import su.nexmedia.engine.api.hook.AbstractHook;

import java.util.*;

public class CitizensHook extends AbstractHook<NexEngine> {

    private static Map<NexPlugin<?>, Set<TraitInfo>>        traits;
    private static Map<NexPlugin<?>, Set<CitizensListener>> listeners;

    public CitizensHook(@NotNull NexEngine plugin, @NotNull String pluginName) {
        super(plugin, pluginName);
    }

    @Override
    public boolean setup() {
        traits = new HashMap<>();
        listeners = new HashMap<>();

        this.registerListeners();
        return true;
    }

    @Override
    public void shutdown() {
        this.unregisterListeners();

        traits.forEach((plugin, traits) -> {
            traits.forEach(trait -> CitizensAPI.getTraitFactory().deregisterTrait(trait));
        });
        traits.clear();
        listeners.clear();
    }

    public static void addListener(@NotNull NexPlugin<?> plugin, @NotNull CitizensListener listener) {
        getListeners(plugin).add(listener);
    }

    @Deprecated
    public void removeListener(@NotNull CitizensListener listener) {
        getListeners(plugin).remove(listener);
    }

    @NotNull
    public static Set<CitizensListener> getListeners(@NotNull NexPlugin<?> plugin) {
        return listeners.computeIfAbsent(plugin, set -> new HashSet<>());
    }

    public static void unregisterListeners(@NotNull NexPlugin<?> plugin) {
        if (listeners.remove(plugin) != null) {
            plugin.info("[Citizens Hook] Unregistered listeners");
        }
    }

    public static void registerTrait(@NotNull NexPlugin<?> plugin, @NotNull Class<? extends Trait> trait) {
        TraitInfo traitInfo = TraitInfo.create(trait);
        registerTrait(plugin, traitInfo);
    }

    public static void registerTrait(@NotNull NexPlugin<?> plugin, @NotNull TraitInfo trait) {
        unregisterTrait(plugin, trait);
        if (traits.computeIfAbsent(plugin, set -> new HashSet<>()).add(trait)) {
            plugin.info("[Citizens Hook] Registered trait: " + trait.getTraitName());
            CitizensAPI.getTraitFactory().registerTrait(trait);
        }
    }

    public static void unregisterTrait(@NotNull NexPlugin<?> plugin, @NotNull TraitInfo trait) {
        if (traits.getOrDefault(plugin, Collections.emptySet()).remove(trait)) {
            plugin.info("[Citizens Hook] Unregistered trait: " + trait.getTraitName());
        }
        CitizensAPI.getTraitFactory().deregisterTrait(trait);
    }

    public static void unregisterTraits(@NotNull NexPlugin<?> plugin) {
        traits.getOrDefault(plugin, Collections.emptySet()).forEach(trait -> {
            plugin.info("[Citizens Hook] Unregistered trait: " + trait.getTraitName());
            CitizensAPI.getTraitFactory().deregisterTrait(trait);
        });
        traits.remove(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public static void onLeftClick(NPCLeftClickEvent e) {
        listeners.values().forEach(set -> set.forEach(listener -> listener.onLeftClick(e)));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public static void onRightClick(NPCRightClickEvent e) {
        listeners.values().forEach(set -> set.forEach(listener -> listener.onRightClick(e)));
    }
}
