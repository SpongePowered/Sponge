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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageScalings;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypeTemplate;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.DamageTypeTags;
import org.spongepowered.api.tag.TagTemplate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("damagetest")
public class DamageTest {

    private final PluginContainer plugin;

    private final ResourceKey EXHAUSTING_DAMAGE = ResourceKey.of("damagetest", "test");

    @Inject
    public DamageTest(final PluginContainer plugin) {
        this.plugin = plugin;
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
}
