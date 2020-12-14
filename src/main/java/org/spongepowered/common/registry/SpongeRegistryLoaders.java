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

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.ComponentArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.Vec2Argument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameters;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.data.type.CatType;
import org.spongepowered.api.data.type.CatTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.item.inventory.menu.handler.InventoryCallbackHandler;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.ban.BanType;
import org.spongepowered.api.service.ban.BanTypes;
import org.spongepowered.api.service.economy.account.AccountDeletionResultType;
import org.spongepowered.api.service.economy.account.AccountDeletionResultTypes;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.ban.SpongeBanType;
import org.spongepowered.common.command.brigadier.argument.StandardCatalogedArgumentParser;
import org.spongepowered.common.command.parameter.managed.standard.SpongeBigDecimalValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeBigIntegerValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeColorValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeDataContainerValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeDateTimeValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeDurationValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeGameProfileValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeIPAddressValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeNoneValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongePluginContainerValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeServerLocationValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeTargetBlockValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeTargetEntityValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeUserValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeWorldPropertiesValueParameter;
import org.spongepowered.common.data.type.SpongeBodyPart;
import org.spongepowered.common.data.type.SpongeCatType;
import org.spongepowered.common.economy.SpongeAccountDeletionResultType;
import org.spongepowered.common.inventory.menu.handler.SpongeClickType;
import org.spongepowered.common.item.SpongeItemStackSnapshot;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector2d;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class SpongeRegistryLoaders {

    // @formatter:off

    public static RegistryLoader<AccountDeletionResultType> accountDeletionResultType() {
        return RegistryLoader.of(l -> l .mapping(SpongeAccountDeletionResultType::new, m -> m.add(
                AccountDeletionResultTypes.ABSENT,
                AccountDeletionResultTypes.FAILED,
                AccountDeletionResultTypes.SUCCESS,
                AccountDeletionResultTypes.UNDELETABLE
        )));
    }

    public static RegistryLoader<BanType> banType() {
        return RegistryLoader.of(l -> {
            l.add(BanTypes.IP, k -> new SpongeBanType(k, Ban.IP.class));
            l.add(BanTypes.PROFILE, k -> new SpongeBanType(k, Ban.Profile.class));
        });
    }

    public static RegistryLoader<BodyPart> bodyPart() {
        return RegistryLoader.of(l -> l.mapping(SpongeBodyPart::new, m -> m.add(
                BodyParts.CHEST,
                BodyParts.HEAD,
                BodyParts.LEFT_ARM,
                BodyParts.LEFT_LEG,
                BodyParts.RIGHT_ARM,
                BodyParts.RIGHT_LEG
        )));
    }

    public static RegistryLoader<CatalogedValueParameter<?>> catalogedValueParameter() {
        return RegistryLoader.of(l -> {
            l.add(CatalogedValueParameters.BIG_DECIMAL, SpongeBigDecimalValueParameter::new);
            l.add(CatalogedValueParameters.BIG_INTEGER, SpongeBigIntegerValueParameter::new);
            l.add(CatalogedValueParameters.BLOCK_STATE, k -> StandardCatalogedArgumentParser.createConverter(k, BlockStateArgument.block(), (reader, cause, state) -> (BlockState) state.getState()));
            l.add(CatalogedValueParameters.BOOLEAN, k -> StandardCatalogedArgumentParser.createIdentity(k, BoolArgumentType.bool()));
            l.add(CatalogedValueParameters.COLOR, SpongeColorValueParameter::new); //Includes ColorArgumentParser.color(), but does more. TODO: what does 1.16 do?
            l.add(CatalogedValueParameters.DATA_CONTAINER, SpongeDataContainerValueParameter::new);
            l.add(CatalogedValueParameters.DATE_TIME, SpongeDateTimeValueParameter::new);
            l.add(CatalogedValueParameters.DOUBLE, k -> StandardCatalogedArgumentParser.createIdentity(k, DoubleArgumentType.doubleArg()));
            l.add(CatalogedValueParameters.DURATION, SpongeDurationValueParameter::new);
            l.add(CatalogedValueParameters.ENTITY, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.entity(), (reader, cause, selector) -> (Entity) selector.findSingleEntity(cause.getSource())));
            l.add(CatalogedValueParameters.GAME_PROFILE, SpongeGameProfileValueParameter::new);
            l.add(CatalogedValueParameters.INTEGER, k -> StandardCatalogedArgumentParser.createIdentity(k, IntegerArgumentType.integer()));
            l.add(CatalogedValueParameters.IP, SpongeIPAddressValueParameter::new);
            l.add(CatalogedValueParameters.ITEM_STACK_SNAPSHOT, k -> StandardCatalogedArgumentParser.createConverter(k, ItemArgument.item(), (reader, cause, converter) -> new SpongeItemStackSnapshot((ItemStack) (Object) converter.createItemStack(1, true))));
            l.add(CatalogedValueParameters.LOCATION_ALL, k -> new SpongeServerLocationValueParameter(k, true));
            l.add(CatalogedValueParameters.LOCATION_ONLINE_ONLY, k -> new SpongeServerLocationValueParameter(k, false));
            l.add(CatalogedValueParameters.LONG, k -> StandardCatalogedArgumentParser.createIdentity(k, LongArgumentType.longArg()));
            l.add(CatalogedValueParameters.MANY_ENTITIES, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.entities(), (reader, cause, selector) -> selector.findEntities(cause.getSource()).stream().map(x -> (Entity) x).collect(Collectors.toList())));
            l.add(CatalogedValueParameters.MANY_GAME_PROFILES, k -> StandardCatalogedArgumentParser.createConverter(k, GameProfileArgument.gameProfile(), (reader, cause, converter) -> converter.getNames(cause.getSource())));
            l.add(CatalogedValueParameters.MANY_PLAYERS, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.players(), (reader, cause, selector) -> selector.findPlayers(cause.getSource())));
            l.add(CatalogedValueParameters.NONE, SpongeNoneValueParameter::new);
            l.add(CatalogedValueParameters.PLAYER, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.player(), (reader, cause, selector) -> (Player) selector.findSinglePlayer(cause.getSource())));
            l.add(CatalogedValueParameters.PLUGIN, SpongePluginContainerValueParameter::new);
            l.add(CatalogedValueParameters.REMAINING_JOINED_STRINGS, k -> StandardCatalogedArgumentParser.createIdentity(k, StringArgumentType.greedyString()));
            l.add(CatalogedValueParameters.RESOURCE_KEY, k -> StandardCatalogedArgumentParser.createConverter(k, ResourceLocationArgument.id(), (reader, cause, resourceLocation) -> (ResourceKey) (Object) resourceLocation));
            l.add(CatalogedValueParameters.STRING, k -> StandardCatalogedArgumentParser.createIdentity(k, StringArgumentType.string()));
            l.add(CatalogedValueParameters.TARGET_BLOCK, SpongeTargetBlockValueParameter::new);
            l.add(CatalogedValueParameters.TARGET_ENTITY, k -> new SpongeTargetEntityValueParameter(k, false));
            l.add(CatalogedValueParameters.TARGET_PLAYER, k -> new SpongeTargetEntityValueParameter(k, true));
            l.add(CatalogedValueParameters.TEXT_FORMATTING_CODE, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.string(), (reader, cause, result) -> SpongeAdventure.legacyAmpersand(result)));
            l.add(CatalogedValueParameters.TEXT_FORMATTING_CODE_ALL, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.greedyString(), (reader, cause, result) -> SpongeAdventure.legacyAmpersand(result)));
            l.add(CatalogedValueParameters.TEXT_JSON, k -> StandardCatalogedArgumentParser.createConverter(k, ComponentArgument.textComponent(), (reader, cause, result) -> SpongeAdventure.asAdventure(result)));
            l.add(CatalogedValueParameters.TEXT_JSON_ALL, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.greedyString(), (reader, cause, result) -> SpongeAdventure.json(result)));
            l.add(CatalogedValueParameters.URL, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.string(),
                    (reader, cause, input) -> {
                        try {
                            return new URL(input);
                        } catch (final MalformedURLException ex) {
                            throw new SimpleCommandExceptionType(new StringTextComponent("Could not parse " + input + " as a URL"))
                                    .createWithContext(reader);
                        }
                    })
            );
            l.add(CatalogedValueParameters.USER, SpongeUserValueParameter::new);
            l.add(CatalogedValueParameters.UUID, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.string(),
                    (reader, cause, input) -> {
                        try {
                            return UUID.fromString(input);
                        } catch (final IllegalArgumentException ex) {
                            throw new SimpleCommandExceptionType(new StringTextComponent(ex.getMessage()))
                                    .createWithContext(reader);
                        }
                    })
            );
            l.add(CatalogedValueParameters.VECTOR2D, k -> StandardCatalogedArgumentParser.createConverter(k, Vec2Argument.vec2(),
                    (reader, cause, result) -> {
                        final Vector3d r = result.getPosition(cause.getSource());
                        return new Vector2d(r.x, r.z);
                    })
            );
            l.add(CatalogedValueParameters.VECTOR3D, k -> StandardCatalogedArgumentParser.createConverter(k, Vec3Argument.vec3(), (reader, cause, result) -> VecHelper.toVector3d(result.getPosition(cause.getSource()))));
            l.add(CatalogedValueParameters.WORLD_PROPERTIES_ALL, k -> new SpongeWorldPropertiesValueParameter(k, true));
            l.add(CatalogedValueParameters.WORLD_PROPERTIES_ONLINE_ONLY, k -> new SpongeWorldPropertiesValueParameter(k, false));
        });
    }

    public static RegistryLoader<CatType> catType() {
        return RegistryLoader.of(l -> {
            l.add(CatTypes.ALL_BLACK, k -> new SpongeCatType(k, 10));
            l.add(CatTypes.BLACK, k -> new SpongeCatType(k, 1));
            l.add(CatTypes.BRITISH_SHORTHAIR, k -> new SpongeCatType(k, 4));
            l.add(CatTypes.CALICO, k -> new SpongeCatType(k, 5));
            l.add(CatTypes.JELLIE, k -> new SpongeCatType(k, 9));
            l.add(CatTypes.PERSIAN, k -> new SpongeCatType(k, 6));
            l.add(CatTypes.RAGDOLL, k -> new SpongeCatType(k, 7));
            l.add(CatTypes.RED, k -> new SpongeCatType(k, 2));
            l.add(CatTypes.SIAMESE, k -> new SpongeCatType(k, 3));
            l.add(CatTypes.TABBY, k -> new SpongeCatType(k, 0));
            l.add(CatTypes.WHITE, k -> new SpongeCatType(k, 8));
        });
    }

    public static RegistryLoader<ClickType<? extends InventoryCallbackHandler>> clickType() {
        return RegistryLoader.of(l -> l .mapping(SpongeClickType::new, m -> m.add(
                ClickTypes.CLICK_LEFT
        )));
    }

    // @formatter:on
}
