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
package org.spongepowered.common.registry.type.advancement;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.advancements.AdvancementListBridge;
import org.spongepowered.common.mixin.core.advancements.AdvancementManagerAccessor;
import org.spongepowered.common.registry.CustomRegistrationPhase;
import org.spongepowered.common.registry.type.AbstractPrefixCheckCatalogRegistryModule;

import java.util.Optional;

@CustomRegistrationPhase
public class AdvancementTreeRegistryModule extends AbstractPrefixCheckCatalogRegistryModule<AdvancementTree>
        implements AdditionalCatalogRegistryModule<AdvancementTree> {

    public static AdvancementTreeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    AdvancementTreeRegistryModule() {
        super("minecraft");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerAdditionalCatalog(final AdvancementTree advancementTree) {
        checkState(SpongeImplHooks.isMainThread());
        final Advancement advancement = (Advancement) advancementTree.getRootAdvancement();
        final AdvancementListBridge advancementList = (AdvancementListBridge) AdvancementManagerAccessor.accessor$getAdvancementList();
        advancementList.bridge$getRootsSet().add(advancement);
        final AdvancementList.Listener listener = advancementList.bridge$getListener();
        if (listener != null) {
            listener.rootAdvancementAdded(advancement);
        }
    }

    void clear() {
        this.catalogTypeMap.clear();
    }

    void registerSilently(final Advancement rootAdvancement) {
        final Optional<AdvancementTree> optTree = ((org.spongepowered.api.advancement.Advancement) rootAdvancement).getTree();
        if (optTree.isPresent()) {
            super.register(optTree.get());
        } else {
            SpongeImpl.getLogger().warn("Attempted to register a root advancement {} without a advancement tree?", rootAdvancement.getId());
        }
    }

    void remove(final Advancement rootAdvancement) {
        final Optional<AdvancementTree> optTree = ((org.spongepowered.api.advancement.Advancement) rootAdvancement).getTree();
        optTree.ifPresent(advancementTree -> this.catalogTypeMap.remove(advancementTree.getId()));
    }

    private static final class Holder {
        static final AdvancementTreeRegistryModule INSTANCE = new AdvancementTreeRegistryModule();
    }
}
