/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.registry.type.keyboard;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.keyboard.InteractKeyEvent;
import org.spongepowered.api.keyboard.KeyBinding;
import org.spongepowered.api.keyboard.KeyBindings;
import org.spongepowered.api.keyboard.KeyCategories;
import org.spongepowered.api.keyboard.KeyCategory;
import org.spongepowered.api.keyboard.KeyContext;
import org.spongepowered.api.keyboard.KeyContexts;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.keyboard.SpongeKeyBinding;
import org.spongepowered.common.keyboard.SpongeKeyCategory;
import org.spongepowered.common.keyboard.SpongeKeyContext;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@RegistrationDependency({
        KeyContextRegistryModule.class,
        KeyCategoryRegistryModule.class
})
public class KeyBindingRegistryModule implements CatalogRegistryModule<KeyBinding>, SpongeAdditionalCatalogRegistryModule<KeyBinding> {

    public static KeyBindingRegistryModule get() {
        return Holder.INSTANCE;
    }

    private final Map<String, KeyBinding> keyBindings = new HashMap<>();
    private final Int2ObjectMap<KeyBinding> keyBindingsByInternalId = new Int2ObjectOpenHashMap<>();

    private int internalIdCounter = 50;

    public boolean isRegistered(KeyBinding keyBinding) {
        return this.keyBindings.containsValue(keyBinding);
    }

    public Optional<KeyBinding> getByInternalId(int internalId) {
        return Optional.ofNullable(this.keyBindingsByInternalId.get(internalId));
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

    @Override
    public void registerAdditionalCatalog(KeyBinding keyBinding) {
        checkNotNull(keyBinding, "keyBinding");
        checkArgument(!this.keyBindings.containsValue(keyBinding), "The key binding %s is already registered", keyBinding.getId());
        checkArgument(!this.keyBindings.containsKey(keyBinding.getId().toLowerCase(Locale.ENGLISH)),
                "The key binding id %s is already used", keyBinding.getId());
        checkArgument(KeyCategoryRegistryModule.get().isRegistered(keyBinding.getCategory()),
                "The key category %s of the key binding %s must be registered", keyBinding.getCategory().getId(), keyBinding.getId());
        checkArgument(KeyContextRegistryModule.get().isRegistered(keyBinding.getContext()),
                "The key context %s of the key binding %s must be registered", keyBinding.getContext().getId(), keyBinding.getId());

        final SpongeKeyBinding keyBinding0 = (SpongeKeyBinding) keyBinding;
        keyBinding0.setInternalId(this.internalIdCounter++);
        keyBinding0.getCategory().addBinding(keyBinding0);
        registerAdditionalBinding(keyBinding0);

        /* TODO: This doesn't work???
        if (keyBinding0.getEventConsumers() != null) {
            for (Map.Entry<Class<?>, Consumer<?>> entry : keyBinding0.getEventConsumers().entries()) {
                Sponge.getEventManager().registerListener(keyBinding0.getPlugin(), (Class) entry.getClass(),
                        new SpongeKeyBindingEventListener(entry.getValue()));
            }
        }
        */
    }

    @SuppressWarnings("unchecked")
    @Listener
    public void onInteractKey(InteractKeyEvent event) {
        final Multimap<Class<?>, Consumer<?>> listeners = ((SpongeKeyBinding) event.getKeyBinding()).getEventConsumers();
        if (listeners != null) {
            for (Map.Entry<Class<?>, Consumer<?>> entry : listeners.entries()) {
                if (entry.getKey().isInstance(event)) {
                    try {
                        ((Consumer) entry.getValue()).accept(event);
                    } catch (Exception e) {
                        SpongeImpl.getLogger().error("Failed to handle key interaction event for the plugin {}",
                                ((SpongeKeyBinding) event.getKeyBinding()).getPlugin().getId(), e);
                    }
                }
            }
        }
    }

    private void registerAdditionalBinding(SpongeKeyBinding keyBinding) {
        this.keyBindings.put(keyBinding.getId(), keyBinding);
        this.keyBindingsByInternalId.put(keyBinding.getInternalId(), keyBinding);
    }

    @Override
    public Optional<KeyBinding> getById(String id) {
        return Optional.ofNullable(this.keyBindings.get(checkNotNull(id, "id").toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<KeyBinding> getAll() {
        return ImmutableSet.copyOf(this.keyBindings.values());
    }

    private static SpongeKeyBinding buildDefaultBinding(String id, KeyCategory keyCategory, KeyContext keyContext,
            int internalId, String translationKey) {
        final Text displayName = Sponge.getRegistry().getTranslationById("key." + translationKey)
                .map(translation -> (Text) Text.of(translation))
                .orElseGet(() -> Text.of(id));
        final SpongeKeyBinding keyBinding = new SpongeKeyBinding(SpongeImpl.getMinecraftPlugin(),
                id, (SpongeKeyContext) keyContext, (SpongeKeyCategory) keyCategory, displayName, null, true);
        keyBinding.setInternalId(internalId);
        return keyBinding;
    }

    @Override
    public void registerDefaults() {
        final Map<String, SpongeKeyBinding> mappings = new HashMap<>();
        mappings.put("attack", buildDefaultBinding("attack",
                KeyCategories.GAMEPLAY, KeyContexts.IN_GAME, 0, "attack"));
        mappings.put("pick_block", buildDefaultBinding("pick_block",
                KeyCategories.GAMEPLAY, KeyContexts.IN_GAME, 1, "pickItem"));
        mappings.put("use_item", buildDefaultBinding("use_item",
                KeyCategories.GAMEPLAY, KeyContexts.IN_GAME, 2, "use"));
        registerAndClearMappings(KeyBindings.Gameplay.class, mappings);
        mappings.put("drop_item", buildDefaultBinding("drop_item",
                KeyCategories.INVENTORY, KeyContexts.IN_GAME, 3, "drop"));
        mappings.put("hotbar_1", buildDefaultBinding("hotbar_1",
                KeyCategories.INVENTORY, KeyContexts.INVENTORY, 4, "hotbar.1"));
        mappings.put("hotbar_2", buildDefaultBinding("hotbar_2",
                KeyCategories.INVENTORY, KeyContexts.INVENTORY, 5, "hotbar.2"));
        mappings.put("hotbar_3", buildDefaultBinding("hotbar_3",
                KeyCategories.INVENTORY, KeyContexts.INVENTORY, 6, "hotbar.3"));
        mappings.put("hotbar_4", buildDefaultBinding("hotbar_4",
                KeyCategories.INVENTORY, KeyContexts.INVENTORY, 7, "hotbar.4"));
        mappings.put("hotbar_5", buildDefaultBinding("hotbar_5",
                KeyCategories.INVENTORY, KeyContexts.INVENTORY, 8, "hotbar.5"));
        mappings.put("hotbar_6", buildDefaultBinding("hotbar_6",
                KeyCategories.INVENTORY, KeyContexts.INVENTORY, 9, "hotbar.6"));
        mappings.put("hotbar_7", buildDefaultBinding("hotbar_7",
                KeyCategories.INVENTORY, KeyContexts.INVENTORY, 10, "hotbar.7"));
        mappings.put("hotbar_8", buildDefaultBinding("hotbar_8",
                KeyCategories.INVENTORY, KeyContexts.INVENTORY, 11, "hotbar.8"));
        mappings.put("hotbar_9", buildDefaultBinding("hotbar_9",
                KeyCategories.INVENTORY, KeyContexts.INVENTORY, 12, "hotbar.9"));
        mappings.put("inventory", buildDefaultBinding("inventory",
                KeyCategories.INVENTORY, KeyContexts.UNIVERSAL, 13, "inventory"));
        mappings.put("swap_hand_items", buildDefaultBinding("swap_hand_items",
                KeyCategories.INVENTORY, KeyContexts.IN_GAME, 14, "swapHands"));
        registerAndClearMappings(KeyBindings.Inventory.class, mappings);
        mappings.put("fullscreen", buildDefaultBinding("fullscreen",
                KeyCategories.MISC, KeyContexts.UNIVERSAL, 15, "fullscreen"));
        mappings.put("screenshot", buildDefaultBinding("screenshot",
                KeyCategories.MISC, KeyContexts.UNIVERSAL, 16, "screenshot"));
        mappings.put("smooth_camera", buildDefaultBinding("smooth_camera",
                KeyCategories.MISC, KeyContexts.IN_GAME, 17, "smoothCamera"));
        mappings.put("spectator_outlines", buildDefaultBinding("spectator_outlines",
                KeyCategories.MISC, KeyContexts.UNIVERSAL, 18, "spectatorOutlines"));
        mappings.put("toggle_perspective", buildDefaultBinding("toggle_perspective",
                KeyCategories.MISC, KeyContexts.IN_GAME, 19, "togglePerspective"));
        registerAndClearMappings(KeyBindings.Misc.class, mappings);
        mappings.put("backward", buildDefaultBinding("backward",
                KeyCategories.MOVEMENT, KeyContexts.IN_GAME, 20, "back"));
        mappings.put("forward", buildDefaultBinding("forward",
                KeyCategories.MOVEMENT, KeyContexts.IN_GAME, 21, "forward"));
        mappings.put("jump", buildDefaultBinding("jump",
                KeyCategories.MOVEMENT, KeyContexts.IN_GAME, 22, "jump"));
        mappings.put("left", buildDefaultBinding("left",
                KeyCategories.MOVEMENT, KeyContexts.IN_GAME, 23, "left"));
        mappings.put("right", buildDefaultBinding("right",
                KeyCategories.MOVEMENT, KeyContexts.IN_GAME, 24, "right"));
        mappings.put("sneak", buildDefaultBinding("sneak",
                KeyCategories.MOVEMENT, KeyContexts.IN_GAME, 25, "sneak"));
        mappings.put("sprint", buildDefaultBinding("sprint",
                KeyCategories.MOVEMENT, KeyContexts.IN_GAME, 26, "sprint"));
        registerAndClearMappings(KeyBindings.Movement.class, mappings);
        mappings.put("chat", buildDefaultBinding("chat",
                KeyCategories.MULTIPLAYER, KeyContexts.IN_GAME, 27, "chat"));
        mappings.put("command", buildDefaultBinding("command",
                KeyCategories.MULTIPLAYER, KeyContexts.IN_GAME, 28, "command"));
        mappings.put("player_list", buildDefaultBinding("player_list",
                KeyCategories.MULTIPLAYER, KeyContexts.IN_GAME, 29, "playerList"));
        registerAndClearMappings(KeyBindings.Multiplayer.class, mappings);
    }

    private void registerAndClearMappings(Class<?> apiClass, Map<String, SpongeKeyBinding> keyBindings) {
        RegistryHelper.mapFields(apiClass, keyBindings);
        keyBindings.forEach((s, binding) -> registerAdditionalBinding(binding));
        keyBindings.clear();
    }

    private KeyBindingRegistryModule() {
    }

    private static final class Holder {

        static final KeyBindingRegistryModule INSTANCE = new KeyBindingRegistryModule();
    }
}
