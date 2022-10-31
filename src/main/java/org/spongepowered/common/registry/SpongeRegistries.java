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

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.economy.Currency;

public final class SpongeRegistries {

    public static void registerGlobalRegistries(final SpongeRegistryHolder holder) {
        VanillaRegistryLoader.load(holder);

        // TODO holders are never bound when not freezing
        holder.createRegistry(RegistryTypes.CURRENCY, (RegistryLoader<Currency>) null, true);
        holder.createRegistry(RegistryTypes.COMMAND_REGISTRAR_TYPE, SpongeRegistryLoaders.commandRegistrarType(), true);
        holder.createRegistry(RegistryTypes.PLACEHOLDER_PARSER, SpongeRegistryLoaders.placeholderParser(), true);
        holder.createRegistry(RegistryTypes.TELEPORT_HELPER_FILTER, SpongeRegistryLoaders.teleportHelperFilter(), true);

        holder.createAndFreezeRegistry(RegistryTypes.ACCOUNT_DELETION_RESULT_TYPE, SpongeRegistryLoaders.accountDeletionResultType());
        holder.createAndFreezeRegistry(RegistryTypes.BAN_TYPE, SpongeRegistryLoaders.banType());
        holder.createAndFreezeRegistry(SpongeRegistryTypes.TRACKER_TRANSACTION_TYPE, SpongeRegistryLoaders.blockTransactionTypes());
        holder.createAndFreezeRegistry(RegistryTypes.BODY_PART, SpongeRegistryLoaders.bodyPart());
        holder.createAndFreezeRegistry(RegistryTypes.CLICK_TYPE, SpongeRegistryLoaders.clickType());
        holder.createAndFreezeRegistry(RegistryTypes.CHUNK_REGENERATE_FLAG, SpongeRegistryLoaders.chunkRegenerateFlag());
        holder.createAndFreezeRegistry(RegistryTypes.CLIENT_COMPLETION_TYPE, SpongeRegistryLoaders.clientCompletionType());
        holder.createAndFreezeRegistry(RegistryTypes.COMMAND_COMPLETION_PROVIDER, SpongeRegistryLoaders.clientSuggestionProvider());
        holder.createAndFreezeRegistry(RegistryTypes.DAMAGE_TYPE, SpongeRegistryLoaders.damageType());
        holder.createAndFreezeRegistry(RegistryTypes.DAMAGE_MODIFIER_TYPE, SpongeRegistryLoaders.damageModifierType());
        holder.createAndFreezeRegistry(RegistryTypes.DISMOUNT_TYPE, SpongeRegistryLoaders.dismountType());
        holder.createAndFreezeRegistry(RegistryTypes.DISPLAY_SLOT, SpongeRegistryLoaders.displaySlot());
        holder.createAndFreezeRegistry(RegistryTypes.GOAL_EXECUTOR_TYPE, SpongeRegistryLoaders.goalExecutorType());
        holder.createAndFreezeRegistry(RegistryTypes.GOAL_TYPE, SpongeRegistryLoaders.goalType());
        holder.createAndFreezeRegistry(RegistryTypes.HORSE_COLOR, SpongeRegistryLoaders.horseColor());
        holder.createAndFreezeRegistry(RegistryTypes.HORSE_STYLE, SpongeRegistryLoaders.horseStyle());
        holder.createAndFreezeRegistry(RegistryTypes.LIGHT_TYPE, SpongeRegistryLoaders.lightType());
        holder.createAndFreezeRegistry(RegistryTypes.LLAMA_TYPE, SpongeRegistryLoaders.llamaType());
        holder.createAndFreezeRegistry(RegistryTypes.MATTER_TYPE, SpongeRegistryLoaders.matterType());
        holder.createAndFreezeRegistry(RegistryTypes.MOVEMENT_TYPE, SpongeRegistryLoaders.movementType());
        holder.createAndFreezeRegistry(RegistryTypes.MUSIC_DISC, SpongeRegistryLoaders.musicDisc());
        holder.createAndFreezeRegistry(RegistryTypes.NOTE_PITCH, SpongeRegistryLoaders.notePitch());
        holder.createAndFreezeRegistry(RegistryTypes.OPERATOR, SpongeRegistryLoaders.operator());
        holder.createAndFreezeRegistry(RegistryTypes.OPERATION, SpongeRegistryLoaders.operation());
        holder.createAndFreezeRegistry(RegistryTypes.ORIENTATION, SpongeRegistryLoaders.orientation());
        holder.createAndFreezeRegistry(RegistryTypes.PALETTE_TYPE, SpongeRegistryLoaders.paletteType());
        holder.createAndFreezeRegistry(RegistryTypes.PARROT_TYPE, SpongeRegistryLoaders.parrotType());
        holder.createAndFreezeRegistry(RegistryTypes.PARTICLE_OPTION, SpongeRegistryLoaders.particleOption());
        holder.createAndFreezeRegistry(RegistryTypes.PORTAL_TYPE, SpongeRegistryLoaders.portalType());
        holder.createAndFreezeRegistry(RegistryTypes.QUERY_TYPE, SpongeRegistryLoaders.queryType());
        holder.createAndFreezeRegistry(RegistryTypes.RABBIT_TYPE, SpongeRegistryLoaders.rabbitType());
        holder.createAndFreezeRegistry(RegistryTypes.RESOLVE_OPERATION, SpongeRegistryLoaders.resolveOperation());
        holder.createAndFreezeRegistry(RegistryTypes.SELECTOR_TYPE, SpongeRegistryLoaders.selectorType());
        holder.createAndFreezeRegistry(RegistryTypes.SELECTOR_SORT_ALGORITHM, SpongeRegistryLoaders.selectorSortAlgorithm());
        holder.createAndFreezeRegistry(RegistryTypes.SKIN_PART, SpongeRegistryLoaders.skinPart());
        holder.createAndFreezeRegistry(RegistryTypes.SPAWN_TYPE, SpongeRegistryLoaders.spawnType());
        holder.createAndFreezeRegistry(RegistryTypes.TICKET_TYPE, SpongeRegistryLoaders.ticketType());
        holder.createAndFreezeRegistry(RegistryTypes.TRANSACTION_TYPE, SpongeRegistryLoaders.transactionType());
        holder.createAndFreezeRegistry(SpongeRegistryTypes.VALIDATION_TYPE, SpongeRegistryLoaders.validationType());
        holder.createAndFreezeRegistry(RegistryTypes.WEATHER_TYPE, SpongeRegistryLoaders.weather());
        holder.createAndFreezeRegistry(RegistryTypes.DATA_FORMAT, SpongeRegistryLoaders.dataFormat());
        holder.createAndFreezeRegistry(RegistryTypes.MAP_COLOR_TYPE, SpongeRegistryLoaders.mapColorType());
        holder.createAndFreezeRegistry(RegistryTypes.MAP_DECORATION_ORIENTATION, SpongeRegistryLoaders.mapDecorationOrientation());
        holder.createAndFreezeRegistry(RegistryTypes.MAP_DECORATION_TYPE, SpongeRegistryLoaders.mapDecorationType());
        holder.createAndFreezeRegistry(RegistryTypes.MAP_SHADE, SpongeRegistryLoaders.mapShade());
        holder.createAndFreezeRegistry(RegistryTypes.FLAT_GENERATOR_CONFIG, SpongeRegistryLoaders.flatGeneratorConfig());
        holder.createAndFreezeRegistry(RegistryTypes.NOISE_CONFIG, SpongeRegistryLoaders.noiseConfig());
    }

    public static void registerServerRegistries(final RegistryHolder holder) {
    }

    public static void registerGlobalRegistries(final SpongeRegistryHolder holder, final RegistryAccess.Frozen registryAccess) {
        holder.createAndFreezeRegistry(RegistryTypes.COMMAND_TREE_NODE_TYPE, SpongeRegistryLoaders.clientCompletionKey(registryAccess));
        holder.createAndFreezeRegistry(RegistryTypes.REGISTRY_KEYED_VALUE_PARAMETER, SpongeRegistryLoaders.valueParameter(registryAccess));
    }
}
