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
package org.spongepowered.test.particle;

import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Color;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Plugin("particletest")
public final class ParticleTest {

    private final PluginContainer plugin;

    @Inject
    public ParticleTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    private void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<ParticleType> particleType =
                Parameter.registryElement(TypeToken.get(ParticleType.class),
                        (ctx) -> Sponge.game(),
                        RegistryTypes.PARTICLE_TYPE,
                        "minecraft",
                        "sponge")
                        .key("particletype").build();
        final Command.Parameterized myCommand = Command.builder()
                .addParameter(particleType)
                .executor(context -> {
                    this.spawnParticles(context.cause().first(ServerPlayer.class).get(), context.requireOne(particleType));
                    return CommandResult.success();
                })
                .build();
        event.register(this.plugin, myCommand, "particletest");
    }

    private void spawnParticles(ServerPlayer serverPlayer, ParticleType type) {
        // TODO NOTE color is determined by velocity?
        final ParticleEffect effect = ParticleEffect.builder()
                .type(type)
                .option(ParticleOptions.BLOCK_STATE, BlockTypes.DIAMOND_BLOCK.get().defaultState())
                .option(ParticleOptions.COLOR, Color.LIME)
                .option(ParticleOptions.ITEM_STACK_SNAPSHOT, ItemStack.of(ItemTypes.GOLDEN_APPLE.get()).createSnapshot())
                .offset(Vector3d.from(0, 1, 1))
                .velocity(Vector3d.RIGHT.mul(0.5))
                .quantity(20)
                .build();

        try {
            final DataContainer dataContainer = effect.toContainer();
            final ByteArrayOutputStream serialized = new ByteArrayOutputStream();
            DataFormats.NBT.get().writeTo(serialized, dataContainer);
            final DataContainer deserialized = DataFormats.NBT.get().readFrom(new ByteArrayInputStream(serialized.toByteArray()));
            if (ParticleEffect.builder().build(deserialized).isPresent()) {
                serverPlayer.sendMessage(Component.text("Successfully serialized and rebuilt ParticleEffect"));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        serverPlayer.spawnParticles(effect, serverPlayer.position().add(-2, 1, -2));
    }
}
