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

import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.MutableCatalogRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Comparator;

@RegistrationDependency(ProfessionRegistryModule.class)
public class CareerRegistryModule extends MutableCatalogRegistryModule<Career> implements SpongeAdditionalCatalogRegistryModule<Career> {

    public static CareerRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    public static final Comparator<SpongeCareer> CAREER_COMPARATOR = (o1, o2) -> Integer.compare(o1.type, o2.type);

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(Career extraCatalog) {
        tryRegister(extraCatalog);
    }

    @Override
    protected void register(Career type, String... aliases) {
        super.register(type, aliases);
        ProfessionRegistryModule.getInstance().registerCareerForProfession(type);
    }

    @Override
    protected boolean tryRegister(Career type, String... aliases) {
        boolean success = super.tryRegister(type, aliases);
        ProfessionRegistryModule.getInstance().registerCareerForProfession(type);
        return success;
    }

    public Career registerCareer(Career career) {
        registerAdditionalCatalog(career);
        return career;
    }

    @Override
    public void registerDefaults() {
        register(new SpongeCareer(0, "minecraft:farmer", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.farmer")),
                "farmer");
        register(new SpongeCareer(1, "minecraft:fisherman", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.fisherman")),
                "fisherman");
        register(new SpongeCareer(2, "minecraft:shepherd", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.shepherd")),
                "shepherd");
        register(new SpongeCareer(3, "minecraft:fletcher", ProfessionRegistryModule.FARMER, new SpongeTranslation("entity.Villager.fletcher")),
                "fletcher");

        register(new SpongeCareer(0, "minecraft:librarian", ProfessionRegistryModule.LIBRARIAN, new SpongeTranslation("entity.Villager.librarian")),
                "librarian");

        register(new SpongeCareer(0, "minecraft:cleric", ProfessionRegistryModule.PRIEST, new SpongeTranslation("entity.Villager.cleric")),
                "cleric");

        register(new SpongeCareer(0, "minecraft:armorer", ProfessionRegistryModule.BLACKSMITH, new SpongeTranslation("entity.Villager.armor")),
                "armorer", "armor");
        register(new SpongeCareer(1, "minecraft:weapon_smith", ProfessionRegistryModule.BLACKSMITH, new SpongeTranslation("entity.Villager.weapon")),
                "weapon_smith", "weapon");
        register(new SpongeCareer(2, "minecraft:tool_smith", ProfessionRegistryModule.BLACKSMITH, new SpongeTranslation("entity.Villager.tool")),
                "tool_smith", "tool");

        register(new SpongeCareer(0, "minecraft:butcher", ProfessionRegistryModule.BUTCHER, new SpongeTranslation("entity.Villager.butcher")),
                "butcher");
        register(new SpongeCareer(1, "minecraft:leatherworker", ProfessionRegistryModule.BUTCHER, new SpongeTranslation("entity.Villager.leather")),
                "leatherworker", "leather");
    }

    private CareerRegistryModule() {
    }

    private static final class Holder {

        private static final CareerRegistryModule INSTANCE = new CareerRegistryModule();

    }

}
