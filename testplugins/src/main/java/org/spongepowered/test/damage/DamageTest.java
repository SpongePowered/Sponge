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
package org.spongepowered.test.damage;

import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageScalings;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypeTemplate;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.DamageTypeTags;
import org.spongepowered.api.tag.TagTemplate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Map;

@Plugin("damagetest")
public class DamageTest implements LoadableModule {
    private static final ResourceKey EXHAUSTING_DAMAGE = ResourceKey.of("damagetest", "test");

    private final PluginContainer plugin;
    private final DamageListener listener = new DamageListener();

    @Inject
    public DamageTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.plugin, this.listener);
    }

    @Override
    public void disable(CommandContext ctx) {
        Sponge.eventManager().unregisterListeners(this.listener);
    }

    @Listener
    private void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(
                this.plugin,
                Command.builder()
                        .executor(context -> {
                            final DamageType value = DamageTypes.registry().value(EXHAUSTING_DAMAGE);
                            final DamageSource source = DamageSource.builder().type(value).build();
                            context.cause().first(ServerPlayer.class).get().damage(1, source);
                            return CommandResult.success();
                        })
                        .build(),
                "testdamage");
    }

    @Listener
    private void onDamageTypePack(final RegisterDataPackValueEvent<DamageTypeTemplate> event) {
        final DamageTypeTemplate template = DamageTypeTemplate.builder().name("test").scaling(DamageScalings.NEVER.get())
                .exhaustion(100d)
                .key(EXHAUSTING_DAMAGE)
                .build();
        event.register(template);
    }

    @Listener
    private void onDamageTypeTagPack(final RegisterDataPackValueEvent<TagTemplate<DamageType>> event) {
        final TagTemplate<DamageType> template = TagTemplate.builder(DataPacks.DAMAGE_TYPE_TAG).key(DamageTypeTags.BYPASSES_INVULNERABILITY.key())
                .addValue(RegistryKey.of(RegistryTypes.DAMAGE_TYPE, EXHAUSTING_DAMAGE)).build();
        event.register(template);
    }

    private static class DamageListener {
        @Listener
        private void onAttack(final AttackEntityEvent event, @Root DamageSource damageSource) {
            final Audience audience = Sponge.server();
            audience.sendMessage(Component.text("------------AttackEntityEvent------------"));
            audience.sendMessage(Component.text().content("entity: ").append(event.entity().displayName().get()).build());
            audience.sendMessage(Component.text("damage type: " + damageSource.type().key(RegistryTypes.DAMAGE_TYPE)));
            audience.sendMessage(Component.text("damage: " + event.originalDamage()));
            audience.sendMessage(Component.text("modifiers:"));
            for (final DamageFunction f : event.originalFunctions()) {
                final DamageModifier modifier = f.modifier();
                final Tuple<Double, Double> tuple = event.originalModifierDamage(modifier);
                audience.sendMessage(Component.text(" " + ResourceKey.resolve(modifier.group()).value() + "/" + modifier.type().key(RegistryTypes.DAMAGE_MODIFIER_TYPE).value() + ": " + tuple.first() + " -> " + tuple.second()));
            }
            audience.sendMessage(Component.text("final damage: " + event.originalFinalDamage()));
            audience.sendMessage(Component.text("-----------------------------------------"));
        }

        @Listener
        private void onDamage(final DamageEntityEvent event, @Root DamageSource damageSource) {
            final Audience audience = Sponge.server();
            audience.sendMessage(Component.text("------------DamageEntityEvent------------"));
            audience.sendMessage(Component.text().content("entity: ").append(event.entity().displayName().get()).build());
            audience.sendMessage(Component.text("damage type: " + damageSource.type().key(RegistryTypes.DAMAGE_TYPE)));
            audience.sendMessage(Component.text("damage: " + event.originalDamage()));
            audience.sendMessage(Component.text("modifiers:"));
            for (final DamageFunction f : event.originalFunctions()) {
                final DamageModifier modifier = f.modifier();
                final Tuple<Double, Double> tuple = event.originalModifierDamage(modifier);
                audience.sendMessage(Component.text(" " + ResourceKey.resolve(modifier.group()).value() + "/" + modifier.type().key(RegistryTypes.DAMAGE_MODIFIER_TYPE).value() + ": " + tuple.first() + " -> " + tuple.second()));
            }
            audience.sendMessage(Component.text("final damage: " + event.originalFinalDamage()));
            audience.sendMessage(Component.text("-----------------------------------------"));
        }
    }
}
