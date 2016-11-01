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
package org.spongepowered.common.keyboard;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.keyboard.KeyContext;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.BitSet;
import java.util.List;
import java.util.function.Predicate;

public class SpongeKeyContext extends AbstractCatalogType implements KeyContext {

    final Predicate<Player> activePredicate;
    final Predicate<KeyContext> conflictPredicate;

    public SpongeKeyContext(PluginContainer plugin, String name, Predicate<Player> activePredicate,
            Predicate<KeyContext> conflictPredicate) {
        super(plugin, name);
        this.activePredicate = activePredicate;
        this.conflictPredicate = conflictPredicate;
    }

    @Override
    public boolean isActive(Player player) {
        return this.activePredicate.test(checkNotNull(player, "player"));
    }

    @Override
    public boolean conflicts(KeyContext keyContext) {
        return this.conflictPredicate.test(checkNotNull(keyContext, "keyContext"));
    }

    /**
     * We cannot send the conflict contexts to the client, that is impossible,
     * but we can send the conflicts between all the available/used conflict contexts.
     *
     * @param contexts The contexts
     * @return The conflict contexts data
     */
    public static Int2ObjectMap<BitSet> compileConflictContexts(Iterable<SpongeKeyContext> contexts) {
        final Int2ObjectMap<BitSet> conflictContexts = new Int2ObjectOpenHashMap<>();

        final List<SpongeKeyContext> contexts1 = Lists.newArrayList(contexts);
        for (SpongeKeyContext context : contexts) {
            BitSet bitSet = null;
            for (SpongeKeyContext context1 : contexts1) {
                // Don't compare to itself
                if (context1 == context) {
                    continue;
                }
                // At this point, the context internal id may not be -1
                checkArgument(context1.getInternalId() != -1);
                if (context.conflicts(context1)) {
                    if (bitSet == null) {
                        bitSet = new BitSet(1);
                    }
                    bitSet.set(context1.getInternalId());
                }
            }
            if (bitSet != null) {
                conflictContexts.put(context.getInternalId(), bitSet);
            }
        }

        return conflictContexts;
    }
}
