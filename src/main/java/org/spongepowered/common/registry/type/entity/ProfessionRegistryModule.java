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
package org.spongepowered.common.registry.type.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.Professions;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ProfessionRegistryModule implements AlternateCatalogRegistryModule<Profession>, SpongeAdditionalCatalogRegistryModule<Profession> {

    public static final Profession FARMER = new SpongeProfession(0, "minecraft:farmer", "farmer");
    public static final Profession LIBRARIAN = new SpongeProfession(1, "minecraft:librarian", "librarian");
    public static final Profession PRIEST = new SpongeProfession(2, "minecraft:priest", "priest");
    public static final Profession BLACKSMITH = new SpongeProfession(3, "minecraft:blacksmith", "blacksmith");
    public static final Profession BUTCHER = new SpongeProfession(4, "minecraft:butcher", "butcher");
    public static final Profession NITWIT = new SpongeProfession(5, "minecraft:nitwit", "nitwit");

    public static ProfessionRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(Professions.class)
    private final Map<String, Profession> professionMap = new LinkedHashMap<>();

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(Profession extraCatalog) {
        final String catalogId = extraCatalog.getId().toLowerCase(Locale.ENGLISH);
        if (catalogId.equals("smith")) {
            return;
        }
        if (!this.professionMap.containsKey(catalogId)) {
            this.professionMap.put(catalogId, extraCatalog);
        }
    }

    @Override
    public Optional<Profession> getById(String id) {
        return Optional.ofNullable(this.professionMap.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<Profession> getAll() {
        return ImmutableList.copyOf(this.professionMap.values());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerCareerForProfession(Career career) {
        SpongeProfession profession = (SpongeProfession) checkNotNull(career).getProfession();
        List<SpongeCareer> careers = (List) profession.getUnderlyingCareers();
        boolean isRegistered = false;
        final SpongeCareer spongeCareer = (SpongeCareer) career;
        for (SpongeCareer professionCareer : careers) {
            if (spongeCareer.type == professionCareer.type) {
                isRegistered = true;
            }
        }
        if (!isRegistered) {
            if (!careers.contains(spongeCareer)) {
                careers.add(spongeCareer);
                Collections.sort(careers, CareerRegistryModule.CAREER_COMPARATOR);
            }
        }
    }

    @Override
    public void registerDefaults() {
        this.professionMap.put("minecraft:farmer", FARMER);
        this.professionMap.put("minecraft:librarian", LIBRARIAN);
        this.professionMap.put("minecraft:priest", PRIEST);
        this.professionMap.put("minecraft:blacksmith", BLACKSMITH);
        this.professionMap.put("minecraft:butcher", BUTCHER);
        this.professionMap.put("minecraft:nitwit", NITWIT);
    }

    ProfessionRegistryModule() { }

    @Override
    public Map<String, Profession> provideCatalogMap() {
        final HashMap<String, Profession> map = new HashMap<>();
        for (Map.Entry<String, Profession> entry : this.professionMap.entrySet()) {
            map.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return map;
    }

    private static final class Holder {
        static final ProfessionRegistryModule INSTANCE = new ProfessionRegistryModule();
    }
}
