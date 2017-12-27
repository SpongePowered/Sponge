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

import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementManager;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.common.advancement.SpongeAdvancementBuilder;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancement;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancementList;
import org.spongepowered.common.registry.CustomRegistrationPhase;
import org.spongepowered.common.registry.type.AbstractPrefixCheckCatalogRegistryModule;

@CustomRegistrationPhase
public class AdvancementRegistryModule extends AbstractPrefixCheckCatalogRegistryModule<Advancement>
        implements AdditionalCatalogRegistryModule<Advancement> {

    public static boolean INSIDE_REGISTER_EVENT = false;

    public static AdvancementRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    AdvancementRegistryModule() {
        super("minecraft");
    }

    public void clear() {
        this.catalogTypeMap.clear();
    }

    @Override
    public void registerAdditionalCatalog(Advancement advancement) {
        super.register(advancement);
        ((IMixinAdvancement) advancement).setRegistered();
        if (INSIDE_REGISTER_EVENT) {
            final net.minecraft.advancements.Advancement mcAdv = (net.minecraft.advancements.Advancement) advancement;
            final IMixinAdvancementList advancementList = (IMixinAdvancementList) AdvancementManager.ADVANCEMENT_LIST;
            advancementList.getAdvancements().put(mcAdv.getId(), mcAdv);
            if (mcAdv.getParent() != SpongeAdvancementBuilder.DUMMY_ROOT_ADVANCEMENT) {
                advancementList.getNonRootsSet().add(mcAdv);
                final AdvancementList.Listener listener = advancementList.getListener();
                if (listener != null) {
                    listener.nonRootAdvancementAdded(mcAdv);
                }
            }
        }
    }

    private static final class Holder {
        static final AdvancementRegistryModule INSTANCE = new AdvancementRegistryModule();
    }
}
