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
package org.spongepowered.common.registry.type.scoreboard;

import net.minecraft.scoreboard.IScoreCriteria;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

import java.util.HashMap;
import java.util.Map;

@RegisterCatalog(Criteria.class)
public final class CriteriaRegistryModule extends AbstractPrefixAlternateCatalogTypeRegistryModule<Criterion> {

    public static CriteriaRegistryModule getInstance() {
        return CriteriaRegistryModule.Holder.INSTANCE;
    }

    private final Map<String, Criterion> teamKillMappings = new HashMap<>();
    private final Map<String, Criterion> killedByTeamMappings = new HashMap<>();

    CriteriaRegistryModule() {
        super("minecraft", new String[]{"minecraft"}, id -> id.replace("_count", "s"));
    }

    @Override
    public void registerDefaults() {
        IScoreCriteria.INSTANCES.values().forEach(scoreCriterion -> {
            final Criterion criterion = (Criterion) scoreCriterion;

            this.register(criterion);

            criterion.getTeamColor().ifPresent(color -> {
                this.teamKillMappings.put(color.getId(), criterion);
                this.killedByTeamMappings.put(color.getId(), criterion);
            });
        });
    }

    private static final class Holder {
        static final CriteriaRegistryModule INSTANCE = new CriteriaRegistryModule();
    }

    public Map<String, Criterion> getTeamKillMappings() {
        return this.teamKillMappings;
    }

    public Map<String, Criterion> getKilledByTeamMappings() {
        return this.killedByTeamMappings;
    }

}
