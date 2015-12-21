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
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Careers;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.util.RegistrationDependency;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency(ProfessionRegistryModule.class)
public class CareerRegistryModule implements AdditionalCatalogRegistryModule<Career> {

    public static CareerRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    public static final Comparator<SpongeCareer> CAREER_COMPARATOR = (o1, o2) -> Integer.compare(o1.type, o2.type);

    @RegisterCatalog(Careers.class)
    private final Map<String, Career> careerMap = new HashMap<>();

    @Override
    public void registerAdditionalCatalog(Career extraCatalog) {
        if (this.forgeSpongeMapping.containsKey(extraCatalog.getId().toLowerCase())) {
            // Basically, forge has alternate names for a minor few vanilla
            // careers and this avoids having duplicate "careers" registered.
            return;
        }
        if (!this.careerMap.containsKey(extraCatalog.getId())) {
            this.careerMap.put(extraCatalog.getId().toLowerCase(), extraCatalog);
        }
        ProfessionRegistryModule.getInstance().registerCareerForProfession(extraCatalog);
    }

    @Override
    public Optional<Career> getById(String id) {
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        return Optional.ofNullable(this.careerMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<Career> getAll() {
        return ImmutableList.copyOf(this.careerMap.values());
    }

    public Career registerCareer(Career career) {
        registerAdditionalCatalog(career);
        return career;
    }

    private final Map<String, String> forgeSpongeMapping = ImmutableMap.<String, String>builder()
        .put("leather", "leatherworker")
        .put("armor", "armorer")
        .put("tool", "tool_smith")
        .put("weapon", "weapon_smith")
        .build();

    @Override
    public void registerDefaults() {
        this.careerMap.put("farmer",
                registerCareer(new SpongeCareer(0, "farmer", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.farmer"))));
        this.careerMap.put("fisherman", registerCareer(
                new SpongeCareer(1, "fisherman", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.fisherman"))));
        this.careerMap.put("shepherd",
                registerCareer(new SpongeCareer(2, "shepherd", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.shepherd"))));
        this.careerMap.put("fletcher",
                registerCareer(new SpongeCareer(3, "fletcher", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.fletcher"))));

        this.careerMap.put("librarian", registerCareer(
                new SpongeCareer(0, "librarian", ProfessionRegistryModule.LIBRARIAN, new SpongeTranslation("entity.Villager.librarian"))));

        this.careerMap.put("cleric",
                registerCareer(new SpongeCareer(0, "cleric", ProfessionRegistryModule.PRIEST, new SpongeTranslation("entity.Villager.cleric"))));

        this.careerMap.put("armorer",
                registerCareer(new SpongeCareer(0, "armorer", ProfessionRegistryModule.BLACKSMITH, new SpongeTranslation("entity.Villager.armor"))));
        this.careerMap.put("weapon_smith", registerCareer(
                new SpongeCareer(1, "weapon_smith", ProfessionRegistryModule.BLACKSMITH, new SpongeTranslation("entity.Villager.weapon"))));
        this.careerMap.put("tool_smith", registerCareer(
                new SpongeCareer(2, "tool_smith", ProfessionRegistryModule.BLACKSMITH, new SpongeTranslation("entity.Villager.tool"))));

        this.careerMap.put("butcher",
                registerCareer(new SpongeCareer(0, "butcher", ProfessionRegistryModule.BUTCHER, new SpongeTranslation("entity.Villager.butcher"))));
        this.careerMap.put("leatherworker", registerCareer(
                new SpongeCareer(1, "leatherworker", ProfessionRegistryModule.BUTCHER, new SpongeTranslation("entity.Villager.leather"))));
    }

    private CareerRegistryModule() { }

    private static final class Holder {
        private static final CareerRegistryModule INSTANCE = new CareerRegistryModule();
    }
}
