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
package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.Professions;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProfessionRegistryModule implements AdditionalCatalogRegistryModule<Profession> {

    public static final Profession FARMER = new SpongeProfession(0, "farmer");
    public static final Profession LIBRARIAN = new SpongeProfession(1, "librarian");
    public static final Profession PRIEST = new SpongeProfession(2, "priest");
    public static final Profession BLACKSMITH = new SpongeProfession(3, "blacksmith");
    public static final Profession BUTCHER = new SpongeProfession(4, "butcher");

    public static ProfessionRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(Professions.class)
    private final Map<String, Profession> professionMap = new LinkedHashMap<>();

    @Override
    public void registerAdditionalCatalog(Profession extraCatalog) {
        if (extraCatalog.getId().toLowerCase().equals("smith")) {
            return;
        }
        this.professionMap.put(extraCatalog.getId().toLowerCase(), extraCatalog);
    }

    @Override
    public Optional<Profession> getById(String id) {
        return Optional.ofNullable(this.professionMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<Profession> getAll() {
        return ImmutableList.copyOf(this.professionMap.values());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerCareerForProfession(Career career) {
        SpongeProfession profession = (SpongeProfession) checkNotNull(career).getProfession();
        List<SpongeCareer> careers = (List<SpongeCareer>) (List) profession.getUnderlyingCareers();
        boolean isRegistered = false;
        for (SpongeCareer professionCareer : careers) {
            if (((SpongeCareer) career).type == professionCareer.type) {
                isRegistered = true;
            }
        }
        if (!isRegistered) {
            if (!careers.contains((SpongeCareer) career)) {
                careers.add((SpongeCareer) career);
            }
        }
        Collections.sort(careers, CareerRegistryModule.CAREER_COMPARATOR);

    }

    @Override
    public void registerDefaults() {
        this.professionMap.put("farmer", FARMER);
        this.professionMap.put("librarian", LIBRARIAN);
        this.professionMap.put("priest", PRIEST);
        this.professionMap.put("blacksmith", BLACKSMITH);
        this.professionMap.put("butcher", BUTCHER);
    }

    private ProfessionRegistryModule() { }

    private static final class Holder {
        private static final ProfessionRegistryModule INSTANCE = new ProfessionRegistryModule();
    }
}
