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
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency(ProfessionRegistryModule.class)
public class CareerRegistryModule implements SpongeAdditionalCatalogRegistryModule<Career> {

    public final SpongeCareer LEATHERWORKER =
            new SpongeCareer(1, "minecraft:leatherworker", ProfessionRegistryModule.BUTCHER, new SpongeTranslation("entity.Villager.leather"));
    public final SpongeCareer BUTCHER =
            new SpongeCareer(0, "minecraft:butcher", ProfessionRegistryModule.BUTCHER, new SpongeTranslation("entity.Villager.butcher"));
    public final SpongeCareer TOOL_SMITH =
            new SpongeCareer(2, "minecraft:tool_smith", ProfessionRegistryModule.BLACKSMITH, new SpongeTranslation("entity.Villager.tool"));
    public final SpongeCareer WEAPON_SMITH =
            new SpongeCareer(1, "minecraft:weapon_smith", ProfessionRegistryModule.BLACKSMITH, new SpongeTranslation("entity.Villager.weapon"));
    public final SpongeCareer ARMORER =
            new SpongeCareer(0, "minecraft:armorer", ProfessionRegistryModule.BLACKSMITH, new SpongeTranslation("entity.Villager.armor"));
    public final SpongeCareer CLERIC =
            new SpongeCareer(0, "minecraft:cleric", ProfessionRegistryModule.PRIEST, new SpongeTranslation("entity.Villager.cleric"));
    public final SpongeCareer CARTOGRAPHER =
            new SpongeCareer(1, "minecraft:cartographer", ProfessionRegistryModule.LIBRARIAN, new SpongeTranslation("entity.Villager.cartographer"));
    public final SpongeCareer LIBRARIAN =
            new SpongeCareer(0, "minecraft:librarian", ProfessionRegistryModule.LIBRARIAN, new SpongeTranslation("entity.Villager.librarian"));
    public final SpongeCareer FLETCHER =
            new SpongeCareer(3, "minecraft:fletcher", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.fletcher"));
    public final SpongeCareer SHEPHERD =
            new SpongeCareer(2, "minecraft:shepherd", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.shepherd"));
    public final SpongeCareer FISHERMAN =
            new SpongeCareer(1, "minecraft:fisherman", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.fisherman"));
    public final SpongeCareer FARMER =
            new SpongeCareer(0, "minecraft:farmer", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.farmer"));
    public final SpongeCareer NITWIT =
            new SpongeCareer(0, "minecraft:nitwit", ProfessionRegistryModule.NITWIT, new SpongeTranslation("entity.Villager.nitwit"));

    public static CareerRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    public static final Comparator<SpongeCareer> CAREER_COMPARATOR = Comparator.comparingInt(o -> o.type);

    @RegisterCatalog(Careers.class)
    private final Map<String, Career> careerMap = new HashMap<>();

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(Career extraCatalog) {
        if (this.forgeSpongeMapping.containsKey(extraCatalog.getId().toLowerCase(Locale.ENGLISH))) {
            // Basically, forge has alternate names for a minor few vanilla
            // careers and this avoids having duplicate "careers" registered.
            return;
        }
        if (!this.careerMap.containsKey(extraCatalog.getId())) {
            this.careerMap.put(extraCatalog.getId().toLowerCase(Locale.ENGLISH), extraCatalog);
        }
        ProfessionRegistryModule.getInstance().registerCareerForProfession(extraCatalog);
    }

    @Override
    public Optional<Career> getById(String id) {
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        return Optional.ofNullable(this.careerMap.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
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
        this.careerMap.put("farmer", registerCareer(this.FARMER));
        this.careerMap.put("fisherman", registerCareer(this.FISHERMAN));
        this.careerMap.put("shepherd", registerCareer(this.SHEPHERD));
        this.careerMap.put("fletcher", registerCareer(this.FLETCHER));

        this.careerMap.put("librarian", registerCareer(this.LIBRARIAN));
        this.careerMap.put("cartographer", registerCareer(this.CARTOGRAPHER));

        this.careerMap.put("cleric", registerCareer(this.CLERIC));

        this.careerMap.put("armorer", registerCareer(this.ARMORER));
        this.careerMap.put("weapon_smith", registerCareer(this.WEAPON_SMITH));
        this.careerMap.put("tool_smith", registerCareer(this.TOOL_SMITH));

        this.careerMap.put("butcher", registerCareer(this.BUTCHER));
        this.careerMap.put("leatherworker", registerCareer(this.LEATHERWORKER));

        this.careerMap.put("nitwit", registerCareer(this.NITWIT));
    }

    CareerRegistryModule() { }

    private static final class Holder {
        static final CareerRegistryModule INSTANCE = new CareerRegistryModule();
    }
}
