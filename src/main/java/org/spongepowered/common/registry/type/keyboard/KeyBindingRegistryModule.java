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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.keyboard.KeyBinding;
import org.spongepowered.api.keyboard.KeyBindings;
import org.spongepowered.api.keyboard.KeyCategories;
import org.spongepowered.api.keyboard.KeyCategory;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.keyboard.SpongeKeyBinding;
import org.spongepowered.common.keyboard.SpongeKeyCategory;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency(KeyCategoryRegistryModule.class)
public class KeyBindingRegistryModule implements CatalogRegistryModule<KeyBinding>, SpongeAdditionalCatalogRegistryModule<KeyBinding> {

    public static KeyBindingRegistryModule getInstance() {
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
        checkArgument(KeyCategoryRegistryModule.getInstance().isRegistered(keyBinding.getCategory()),
                "The key category %s of the key binding %s must be registered", keyBinding.getCategory().getId(), keyBinding.getId());

        SpongeKeyBinding keyBinding0 = (SpongeKeyBinding) keyBinding;
        keyBinding0.setInternalId(this.internalIdCounter++);
        keyBinding0.getCategory().addBinding(keyBinding0);
        registerAdditionalBinding(keyBinding0);
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

    private static SpongeKeyBinding buildDefaultBinding(String name, KeyCategory keyCategory, int internalId, String translationKey) {
        Text displayName = Sponge.getRegistry().getTranslationById("key." + translationKey).map(translation -> (Text) Text.of(translation))
                .orElseGet(() -> Text.of(name));
        SpongeKeyBinding keyBinding = new SpongeKeyBinding("minecraft", name, (SpongeKeyCategory) keyCategory, displayName, true);
        keyBinding.setInternalId(internalId);
        return keyBinding;
    }

    @Override
    public void registerDefaults() {
        Map<String, SpongeKeyBinding> mappings = new HashMap<>();
        mappings.put("attack", buildDefaultBinding("attack", KeyCategories.GAMEPLAY, 0, "attack"));
        mappings.put("pick_block", buildDefaultBinding("pickBlock", KeyCategories.GAMEPLAY, 1, "pickItem"));
        mappings.put("use_item", buildDefaultBinding("useItem", KeyCategories.GAMEPLAY, 2, "use"));
        registerAndClearMappings(KeyBindings.Gameplay.class, mappings);
        mappings.put("drop_item", buildDefaultBinding("dropItem", KeyCategories.INVENTORY, 3, "drop"));
        mappings.put("hotbar_1", buildDefaultBinding("hotbar1", KeyCategories.INVENTORY, 4, "hotbar.1"));
        mappings.put("hotbar_2", buildDefaultBinding("hotbar2", KeyCategories.INVENTORY, 5, "hotbar.2"));
        mappings.put("hotbar_3", buildDefaultBinding("hotbar3", KeyCategories.INVENTORY, 6, "hotbar.3"));
        mappings.put("hotbar_4", buildDefaultBinding("hotbar4", KeyCategories.INVENTORY, 7, "hotbar.4"));
        mappings.put("hotbar_5", buildDefaultBinding("hotbar5", KeyCategories.INVENTORY, 8, "hotbar.5"));
        mappings.put("hotbar_6", buildDefaultBinding("hotbar6", KeyCategories.INVENTORY, 9, "hotbar.6"));
        mappings.put("hotbar_7", buildDefaultBinding("hotbar7", KeyCategories.INVENTORY, 10, "hotbar.7"));
        mappings.put("hotbar_8", buildDefaultBinding("hotbar8", KeyCategories.INVENTORY, 11, "hotbar.8"));
        mappings.put("hotbar_9", buildDefaultBinding("hotbar9", KeyCategories.INVENTORY, 12, "hotbar.9"));
        mappings.put("inventory", buildDefaultBinding("inventory", KeyCategories.INVENTORY, 13, "inventory"));
        mappings.put("swap_hand_items", buildDefaultBinding("swapHandItems", KeyCategories.INVENTORY, 14, "swapHands"));
        registerAndClearMappings(KeyBindings.Inventory.class, mappings);
        mappings.put("fullscreen", buildDefaultBinding("fullscreen", KeyCategories.MISC, 15, "fullscreen"));
        mappings.put("screenshot", buildDefaultBinding("screenshot", KeyCategories.MISC, 16, "screenshot"));
        mappings.put("smooth_camera", buildDefaultBinding("smoothCamera", KeyCategories.MISC, 17, "smoothCamera"));
        mappings.put("spectator_outlines", buildDefaultBinding("spectatorOutlines", KeyCategories.MISC, 18, "spectatorOutlines"));
        mappings.put("toggle_perspective", buildDefaultBinding("togglePerspective", KeyCategories.MISC, 19, "togglePerspective"));
        registerAndClearMappings(KeyBindings.Misc.class, mappings);
        mappings.put("backward", buildDefaultBinding("backward", KeyCategories.MOVEMENT, 20, "back"));
        mappings.put("forward", buildDefaultBinding("forward", KeyCategories.MOVEMENT, 21, "forward"));
        mappings.put("jump", buildDefaultBinding("jump", KeyCategories.MOVEMENT, 22, "jump"));
        mappings.put("left", buildDefaultBinding("left", KeyCategories.MOVEMENT, 23, "left"));
        mappings.put("right", buildDefaultBinding("right", KeyCategories.MOVEMENT, 24, "right"));
        mappings.put("sneak", buildDefaultBinding("sneak", KeyCategories.MOVEMENT, 25, "sneak"));
        mappings.put("sprint", buildDefaultBinding("sprint", KeyCategories.MOVEMENT, 26, "sprint"));
        registerAndClearMappings(KeyBindings.Movement.class, mappings);
        mappings.put("chat", buildDefaultBinding("chat", KeyCategories.MULTIPLAYER, 27, "chat"));
        mappings.put("command", buildDefaultBinding("command", KeyCategories.MULTIPLAYER, 28, "command"));
        mappings.put("player_list", buildDefaultBinding("playerList", KeyCategories.MULTIPLAYER, 29, "playerList"));
        registerAndClearMappings(KeyBindings.Multiplayer.class, mappings);
    }

    private void registerAndClearMappings(Class<?> apiClass, Map<String, SpongeKeyBinding> keyBindings) {
        RegistryHelper.mapFields(apiClass, keyBindings);
        keyBindings.forEach((s, binding) -> registerAdditionalBinding(binding));
        keyBindings.clear();
    }

    KeyBindingRegistryModule() {}

    private static final class Holder {

        static final KeyBindingRegistryModule INSTANCE = new KeyBindingRegistryModule();
    }
}
