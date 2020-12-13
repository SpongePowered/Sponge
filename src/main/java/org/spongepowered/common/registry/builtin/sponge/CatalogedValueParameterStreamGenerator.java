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
package org.spongepowered.common.registry.builtin.sponge;

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
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.command.brigadier.argument.StandardCatalogedArgumentParser;
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
import org.spongepowered.common.item.SpongeItemStackSnapshot;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector2d;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CatalogedValueParameterStreamGenerator {

    private CatalogedValueParameterStreamGenerator() {
    }

    public static Stream<CatalogedValueParameter<?>> stream() {
        return Stream.of(
                new SpongeBigIntegerValueParameter(),
                new SpongeColorValueParameter(), // Includes ColorArgumentParser.color(), but does more. TODO: what does 1.16 do?
                new SpongeDataContainerValueParameter(),
                new SpongeDateTimeValueParameter(),
                StandardCatalogedArgumentParser.createIdentity("double", DoubleArgumentType.doubleArg()),
                new SpongeDurationValueParameter(),
                // This is for a single entity. We'll have a separate one for multiple.
                StandardCatalogedArgumentParser.createConverter("entity", EntityArgument.entity(), (reader, cause, selector) -> (Entity) selector.findSingleEntity(cause.getSource())),
                new SpongeGameProfileValueParameter(),
                StandardCatalogedArgumentParser.createIdentity("integer", IntegerArgumentType.integer()),
                new SpongeIPAddressValueParameter(),
                StandardCatalogedArgumentParser.createConverter("item_stack_snapshot", ItemArgument.item(), (reader, cause, converter) -> new SpongeItemStackSnapshot((ItemStack) (Object) converter.createItemStack(1, true))),
                new SpongeServerLocationValueParameter(true),
                new SpongeServerLocationValueParameter(false),
                StandardCatalogedArgumentParser.createIdentity("long", LongArgumentType.longArg()),
                StandardCatalogedArgumentParser.createConverter("many_entities", EntityArgument.entities(), (reader, cause, selector) -> selector.findEntities(cause.getSource()).stream().map(x -> (Entity) x).collect(Collectors.toList())),
                StandardCatalogedArgumentParser.createConverter("many_game_profiles", GameProfileArgument.gameProfile(), (reader, cause, converter) -> converter.getNames(cause.getSource())),
                StandardCatalogedArgumentParser.createConverter("many_players", EntityArgument.players(), (reader, cause, selector) -> selector.findPlayers(cause.getSource())),
                new SpongeNoneValueParameter(), StandardCatalogedArgumentParser.createConverter("player", EntityArgument.player(), (reader, cause, selector) -> (Player) selector.findSinglePlayer(cause.getSource())),
                new SpongePluginContainerValueParameter(),
                StandardCatalogedArgumentParser.createIdentity("remaining_joined_strings", StringArgumentType.greedyString()),
                StandardCatalogedArgumentParser.createConverter("resource_key", ResourceLocationArgument.id(), (reader, cause, resourceLocation) -> (ResourceKey) (Object) resourceLocation),
                StandardCatalogedArgumentParser.createIdentity("string", StringArgumentType.string()),
                new SpongeTargetBlockValueParameter(),
                new SpongeTargetEntityValueParameter(false),
                new SpongeTargetEntityValueParameter(true),
                StandardCatalogedArgumentParser.createConverter("text_formatting_code", StringArgumentType.string(), (reader, cause, result) -> SpongeAdventure.legacyAmpersand(result)),
                StandardCatalogedArgumentParser.createConverter("text_formatting_code_all", StringArgumentType.greedyString(), (reader, cause, result) -> SpongeAdventure.legacyAmpersand(result)),
                StandardCatalogedArgumentParser.createConverter("text_json", ComponentArgument.textComponent(), (reader, cause, result) -> SpongeAdventure.asAdventure(result)),
                StandardCatalogedArgumentParser.createConverter("text_json_all", StringArgumentType.greedyString(), (reader, cause, result) -> SpongeAdventure.json(result)),
                StandardCatalogedArgumentParser.createConverter("url", StringArgumentType.string(),
                        (reader, cause, input) -> {
                            try {
                                return new URL(input);
                            } catch (final MalformedURLException ex) {
                                throw new SimpleCommandExceptionType(new StringTextComponent("Could not parse " + input + " as a URL"))
                                        .createWithContext(reader);
                            }
                        }),
                new SpongeUserValueParameter(),
                StandardCatalogedArgumentParser.createConverter("uuid", StringArgumentType.string(),
                        (reader, cause, input) -> {
                            try {
                                return UUID.fromString(input);
                            } catch (final IllegalArgumentException ex) {
                                throw new SimpleCommandExceptionType(new StringTextComponent(ex.getMessage()))
                                        .createWithContext(reader);
                            }
                        }),
                StandardCatalogedArgumentParser.createConverter("vector2d", Vec2Argument.vec2(),
                        (reader, cause, result) -> {
                            final Vector3d r = result.getPosition(cause.getSource());
                            return new Vector2d(r.x, r.z);
                        }),
                StandardCatalogedArgumentParser.createConverter("vector3d", Vec3Argument.vec3(), (reader, cause, result) -> VecHelper.toVector3d(result.getPosition(cause.getSource()))),
                new SpongeWorldPropertiesValueParameter(true),
                new SpongeWorldPropertiesValueParameter(false)
            );
    }

}
