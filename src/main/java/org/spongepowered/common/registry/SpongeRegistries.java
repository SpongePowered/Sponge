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
package org.spongepowered.common.registry;

import net.minecraft.advancements.FrameType;
import net.minecraft.entity.passive.fish.TropicalFishEntity;
import net.minecraft.tileentity.BannerPattern;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.AdvancementType;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.StringDataFormat;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.TropicalFishShape;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.item.inventory.equipment.EquipmentGroup;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.registry.Registries;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.SpongeDataRegistration;
import org.spongepowered.common.data.persistence.HoconDataFormat;
import org.spongepowered.common.data.persistence.JsonDataFormat;
import org.spongepowered.common.data.persistence.NBTDataFormat;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionType;
import org.spongepowered.common.registry.builtin.sponge.PaletteTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.EquipmentGroupStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.EquipmentTypeStreamGenerator;

import java.util.Arrays;
import java.util.stream.Stream;

public final class SpongeRegistries {

    public static final RegistryType<TransactionType<?>> BLOCK_TRANSACTION_TYPE = SpongeRegistries.spongeKey("block_transaction_type");

    public static final RegistryType<CommandRegistrar<?>> COMMAND_REGISTRAR = SpongeRegistries.spongeKey("command_registrar");

    public static final RegistryType<SpawnType> SPAWN_TYPE = SpongeRegistries.spongeKey("spawn_type");

    private static <V> RegistryType<V> spongeKey(final String key) {
        return RegistryType.of(RegistryRoots.SPONGE, ResourceKey.sponge(key));
    }

    public static void registerGlobalRegistries(final SpongeRegistryHolder registries) {
        VanillaRegistryLoader.load(registries);
        registries.createRegistry(Registries.ACCOUNT_DELETION_RESULT_TYPE, SpongeRegistryLoaders.accountDeletionResultType().values());
        registries.createRegistry(Registries.BAN_TYPE, SpongeRegistryLoaders.banType().values());
        registries.createRegistry(SpongeRegistries.BLOCK_TRANSACTION_TYPE, SpongeRegistryLoaders.blockTransactionTypes().values());
        registries.createRegistry(Registries.BODY_PART, SpongeRegistryLoaders.bodyPart().values());
        registries.createRegistry(Registries.CATALOGED_VALUE_PARAMETER, SpongeRegistryLoaders.catalogedValueParameter().values());
        registries.createRegistry(Registries.CLICK_TYPE, SpongeRegistryLoaders.clickType().values());
        registries.createRegistry(Registries.CLIENT_COMPLETION_KEY, SpongeRegistryLoaders.clientCompletionKey().values());
        registries.createRegistry(Registries.CLIENT_COMPLETION_TYPE, SpongeRegistryLoaders.clientCompletionType().values());
        registries.createRegistry(SpongeRegistries.COMMAND_REGISTRAR, SpongeRegistryLoaders.commandRegistrar().values());
        registries.createRegistry(Registries.CURRENCY, null, true);
        registries.createRegistry(Registries.DAMAGE_TYPE, SpongeRegistryLoaders.damageType().values());
        registries.createRegistry(Registries.DAMAGE_MODIFIER_TYPE, SpongeRegistryLoaders.damageModifierType().values());
        registries.createRegistry(Registries.DISMOUNT_TYPE, SpongeRegistryLoaders.dismountType().values());
        registries.createRegistry(Registries.DISPLAY_SLOT, SpongeRegistryLoaders.displaySlot().values());
        registries.createRegistry(Registries.EVENT_CONTEXT_KEY, SpongeRegistryLoaders.eventContextKey().values());
        registries.createRegistry(Registries.GOAL_EXECUTOR_TYPE, SpongeRegistryLoaders.goalExecutorType().values());
        registries.createRegistry(Registries.GOAL_TYPE, SpongeRegistryLoaders.goalType().values());
        registries.createRegistry(Registries.HORSE_COLOR, SpongeRegistryLoaders.horseColor().values());
        registries.createRegistry(Registries.HORSE_STYLE, SpongeRegistryLoaders.horseStyle().values());
        registries.createRegistry(Registries.KEY, SpongeRegistryLoaders.key().values());
        registries.createRegistry(Registries.LLAMA_TYPE, SpongeRegistryLoaders.llamaType().values());
        registries.createRegistry(Registries.MATTER_TYPE, SpongeRegistryLoaders.matterType().values());
        registries.createRegistry(Registries.MOVEMENT_TYPE, SpongeRegistryLoaders.movementType().values());
        registries.createRegistry(Registries.MUSIC_DISC, SpongeRegistryLoaders.musicDisc().values());
        registries.createRegistry(Registries.NOTE_PITCH, SpongeRegistryLoaders.notePitch().values());
        registries.createRegistry(Registries.OPERATION, SpongeRegistryLoaders.operation().values());
        registries.createRegistry(Registries.PARROT_TYPE, SpongeRegistryLoaders.parrotType().values());
        registries.createRegistry(Registries.PARTICLE_OPTION, SpongeRegistryLoaders.particleOption().values());
        registries.createRegistry(Registries.PLACEHOLDER_PARSER, SpongeRegistryLoaders.placeholderParser().values());
        registries.createRegistry(Registries.PORTAL_TYPE, SpongeRegistryLoaders.portalType().values());
        registries.createRegistry(Registries.QUERY_TYPE, SpongeRegistryLoaders.queryType().values());
        registries.createRegistry(Registries.RABBIT_TYPE, SpongeRegistryLoaders.rabbitType().values());
        registries.createRegistry(Registries.SELECTOR_TYPE, SpongeRegistryLoaders.selectorType().values());
        registries.createRegistry(Registries.SELECTOR_SORT_ALGORITHM, SpongeRegistryLoaders.selectorSortAlgorithm().values());
        registries.createRegistry(Registries.SKIN_PART, SpongeRegistryLoaders.skinPart().values());
        registries.createRegistry(Registries.SPAWN_TYPE, SpongeRegistryLoaders.spawnType().values());
        registries.createRegistry(Registries.TELEPORT_HELPER_FILTER, SpongeRegistryLoaders.teleportHelperFilter().values());
        registries.createRegistry(Registries.WEATHER, SpongeRegistryLoaders.weather().values());
        registries.createRegistry(Registries.WORLD_ARCHETYPE, SpongeRegistryLoaders.worldArchetype().values());

        // ----------------------------------------------------------------------------------------------------
        this
                .generateRegistry(AdvancementType.class, ResourceKey.minecraft("advancement_type"), Arrays.stream(FrameType.values()), true, false)
                .generateRegistry(BannerPatternShape.class, ResourceKey.minecraft("banner_pattern_shape"), Arrays.stream(BannerPattern.values()), true, false)
                .generateCallbackRegistry(DataRegistration.class, ResourceKey.sponge("data_registration"), Stream.empty(), (key, value) -> ((SpongeDataManager) Sponge.getGame().getDataManager()).registerCustomDataRegistration((SpongeDataRegistration) value), false, true)
                .generateRegistry(EquipmentGroup.class, ResourceKey.minecraft("equipment_group"), EquipmentGroupStreamGenerator.stream(), true, false)
                .generateRegistry(EquipmentType.class, ResourceKey.minecraft("equipment_type"), EquipmentTypeStreamGenerator.stream(), true, false)
                .generateRegistry(PaletteType.class, ResourceKey.sponge("palette"), PaletteTypeStreamGenerator.stream(), true, true)
                .generateRegistry(TropicalFishShape.class, ResourceKey.minecraft("tropical_fish_shape"), Arrays.stream(TropicalFishEntity.Type.values()), true, false)
                .generateRegistry(StringDataFormat.class, ResourceKey.sponge("string_data_format"), Stream.of(new JsonDataFormat(ResourceKey.sponge("json")), new HoconDataFormat(ResourceKey.sponge("hocon"))), true, false)
                .generateRegistry(DataFormat.class, ResourceKey.sponge("data_format"), Stream.of(new NBTDataFormat(ResourceKey.sponge("nbt"))), true, false)
        ;
    }
}
