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

import net.minecraft.tags.StaticTagHelper;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.common.accessor.tags.BlockTagsAccessor;
import org.spongepowered.common.accessor.tags.EntityTypeTagsAccessor;
import org.spongepowered.common.accessor.tags.FluidTagsAccessor;
import org.spongepowered.common.accessor.tags.ItemTagsAccessor;

public final class SpongeRegistries {

    public static void registerGlobalRegistries(final SpongeRegistryHolder holder) {
        VanillaRegistryLoader.load(holder);

        holder.createRegistry(RegistryTypes.ACCOUNT_DELETION_RESULT_TYPE, SpongeRegistryLoaders.accountDeletionResultType());
        holder.createRegistry(RegistryTypes.BAN_TYPE, SpongeRegistryLoaders.banType());
        holder.createRegistry(SpongeRegistryTypes.TRANSACTION_TYPE, SpongeRegistryLoaders.blockTransactionTypes());
        holder.createRegistry(RegistryTypes.BODY_PART, SpongeRegistryLoaders.bodyPart());
        holder.createRegistry(RegistryTypes.REGISTRY_KEYED_VALUE_PARAMETER, SpongeRegistryLoaders.valueParameter());
        holder.createRegistry(RegistryTypes.CLICK_TYPE, SpongeRegistryLoaders.clickType());
        holder.createRegistry(RegistryTypes.CAT_TYPE, SpongeRegistryLoaders.catType());
        holder.createRegistry(RegistryTypes.COMMAND_TREE_NODE_TYPE, SpongeRegistryLoaders.clientCompletionKey());
        holder.createRegistry(RegistryTypes.CLIENT_COMPLETION_TYPE, SpongeRegistryLoaders.clientCompletionType());
        holder.createRegistry(RegistryTypes.COMMAND_COMPLETION_PROVIDER, SpongeRegistryLoaders.clientSuggestionProvider());
        holder.createRegistry(RegistryTypes.COMMAND_REGISTRAR_TYPE, SpongeRegistryLoaders.commandRegistrarType(), true);
        holder.createRegistry(RegistryTypes.CURRENCY, (RegistryLoader<Currency>) null, true);
        holder.createRegistry(RegistryTypes.DAMAGE_TYPE, SpongeRegistryLoaders.damageType());
        holder.createRegistry(RegistryTypes.DAMAGE_MODIFIER_TYPE, SpongeRegistryLoaders.damageModifierType());
        holder.createRegistry(RegistryTypes.DISMOUNT_TYPE, SpongeRegistryLoaders.dismountType());
        holder.createRegistry(RegistryTypes.DISPLAY_SLOT, SpongeRegistryLoaders.displaySlot());
        holder.createRegistry(RegistryTypes.GOAL_EXECUTOR_TYPE, SpongeRegistryLoaders.goalExecutorType());
        holder.createRegistry(RegistryTypes.GOAL_TYPE, SpongeRegistryLoaders.goalType());
        holder.createRegistry(RegistryTypes.HORSE_COLOR, SpongeRegistryLoaders.horseColor());
        holder.createRegistry(RegistryTypes.HORSE_STYLE, SpongeRegistryLoaders.horseStyle());
        holder.createRegistry(RegistryTypes.LLAMA_TYPE, SpongeRegistryLoaders.llamaType());
        holder.createRegistry(RegistryTypes.MATTER_TYPE, SpongeRegistryLoaders.matterType());
        holder.createRegistry(RegistryTypes.MOVEMENT_TYPE, SpongeRegistryLoaders.movementType());
        holder.createRegistry(RegistryTypes.MUSIC_DISC, SpongeRegistryLoaders.musicDisc());
        holder.createRegistry(RegistryTypes.NOTE_PITCH, SpongeRegistryLoaders.notePitch());
        holder.createRegistry(RegistryTypes.OPERATOR, SpongeRegistryLoaders.operator());
        holder.createRegistry(RegistryTypes.OPERATION, SpongeRegistryLoaders.operation());
        holder.createRegistry(RegistryTypes.PALETTE_TYPE, SpongeRegistryLoaders.paletteType());
        holder.createRegistry(RegistryTypes.PARROT_TYPE, SpongeRegistryLoaders.parrotType());
        holder.createRegistry(RegistryTypes.PARTICLE_OPTION, SpongeRegistryLoaders.particleOption());
        holder.createRegistry(RegistryTypes.PLACEHOLDER_PARSER, SpongeRegistryLoaders.placeholderParser(), true);
        holder.createRegistry(RegistryTypes.PORTAL_TYPE, SpongeRegistryLoaders.portalType());
        holder.createRegistry(RegistryTypes.QUERY_TYPE, SpongeRegistryLoaders.queryType());
        holder.createRegistry(RegistryTypes.RABBIT_TYPE, SpongeRegistryLoaders.rabbitType());
        holder.createRegistry(RegistryTypes.RESOLVE_OPERATION, SpongeRegistryLoaders.resolveOperation());
        holder.createRegistry(RegistryTypes.SELECTOR_TYPE, SpongeRegistryLoaders.selectorType());
        holder.createRegistry(RegistryTypes.SELECTOR_SORT_ALGORITHM, SpongeRegistryLoaders.selectorSortAlgorithm());
        holder.createRegistry(RegistryTypes.SKIN_PART, SpongeRegistryLoaders.skinPart());
        holder.createRegistry(RegistryTypes.SPAWN_TYPE, SpongeRegistryLoaders.spawnType());
        holder.createRegistry(RegistryTypes.TELEPORT_HELPER_FILTER, SpongeRegistryLoaders.teleportHelperFilter(), true);
        holder.createRegistry(RegistryTypes.TICKET_TYPE, SpongeRegistryLoaders.ticketType().values());
        holder.createRegistry(SpongeRegistryTypes.VALIDATION_TYPE, SpongeRegistryLoaders.validationType());
        holder.createRegistry(RegistryTypes.WEATHER_TYPE, SpongeRegistryLoaders.weather());
        holder.createRegistry(RegistryTypes.DATA_FORMAT, SpongeRegistryLoaders.dataFormat());
        holder.createRegistry(RegistryTypes.MAP_COLOR_TYPE, SpongeRegistryLoaders.mapColorType());
        holder.createRegistry(RegistryTypes.MAP_DECORATION_ORIENTATION, SpongeRegistryLoaders.mapDecorationOrientation());
        holder.createRegistry(RegistryTypes.MAP_DECORATION_TYPE, SpongeRegistryLoaders.mapDecorationType());
        holder.createRegistry(RegistryTypes.MAP_SHADE, SpongeRegistryLoaders.mapShade());
        holder.createRegistry(RegistryTypes.TAG_TYPES, SpongeRegistryLoaders.tagTypes());
        holder.wrapTagHelperAsRegistry(RegistryTypes.BLOCK_TYPE_TAGS, (StaticTagHelper<BlockType>) (Object) BlockTagsAccessor.accessor$HELPER());
        holder.wrapTagHelperAsRegistry(RegistryTypes.ITEM_TYPE_TAGS, (StaticTagHelper<ItemType>) (Object) ItemTagsAccessor.accessor$HELPER());
        holder.wrapTagHelperAsRegistry(RegistryTypes.ENTITY_TYPE_TAGS, (StaticTagHelper<EntityType<?>>) (Object) EntityTypeTagsAccessor.accessor$HELPER());
        holder.wrapTagHelperAsRegistry(RegistryTypes.FLUID_TYPE_TAGS, (StaticTagHelper<FluidType>) (Object) FluidTagsAccessor.accessor$HELPER());
    }

    public static void registerServerRegistries(final SpongeRegistryHolder holder) {
    }
}
