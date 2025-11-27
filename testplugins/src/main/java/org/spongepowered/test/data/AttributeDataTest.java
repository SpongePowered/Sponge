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
package org.spongepowered.test.data;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.AttributeOperations;
import org.spongepowered.api.entity.attribute.ItemAttribute;
import org.spongepowered.api.entity.attribute.type.AttributeTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentConditions;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

@Plugin("attributedata")
public class AttributeDataTest implements LoadableModule {

    private final PluginContainer plugin;

    @Inject
    public AttributeDataTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(final CommandContext ctx) {
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Command.Builder builder = Command.builder();
        builder.addChild(Command.builder().executor(this::items).build(), "items");
        event.register(this.plugin, builder.build(), "attributedata");
    }

    private CommandResult items(final CommandContext commandContext) {
        final ServerPlayer player = commandContext.cause().first(ServerPlayer.class).orElse(null);
        if (player == null) {
            return CommandResult.error(Component.text("Must be run ingame by a player"));
        }

        final ItemStack stack1 = ItemStack.of(ItemTypes.DIAMOND_BOOTS);
        stack1.offer(Keys.CUSTOM_NAME, Component.text("Speed when on feet, jump boost when in hand"));
        stack1.offerSingle(Keys.ITEM_ATTRIBUTES, ItemAttribute.of(
            AttributeTypes.MOVEMENT_SPEED,
            AttributeModifier.of(ResourceKey.of(this.plugin, "boots_speed"), AttributeOperations.ADDITION, 1),
            EquipmentConditions.FEET
            ));
        stack1.offerSingle(Keys.ITEM_ATTRIBUTES, ItemAttribute.of(
            AttributeTypes.JUMP_STRENGTH,
            AttributeModifier.of(ResourceKey.of(this.plugin, "boots_jump"), AttributeOperations.ADDITION, 1),
            EquipmentConditions.HAND
            ));

        final ItemStack stack2 = ItemStack.of(ItemTypes.NETHERITE_SWORD);
        stack2.offer(Keys.CUSTOM_NAME, Component.text("Sword without damage"));
        stack2.getValue(Keys.ITEM_ATTRIBUTES)
            .map(Value::asMutable)
            .map(v -> v.transform(list -> list.stream()
                .filter(attribute -> attribute.type() != AttributeTypes.ATTACK_DAMAGE.get())
                .toList()))
            .ifPresent(stack2::offer);

        // In vanilla only one modifier with the same
        // key is applied within each attribute type.
        // This is to ensure that vanilla accepts
        // modifiers with the same key on the item.
        final ItemStack stack3 = ItemStack.of(ItemTypes.SUGAR);
        final ResourceKey sameKey = ResourceKey.of(this.plugin, "same_key");
        stack3.offer(Keys.CUSTOM_NAME, Component.text("Scale and one (out of 2) health boost"));
        stack3.offerSingle(Keys.ITEM_ATTRIBUTES, ItemAttribute.of(
            AttributeTypes.MAX_HEALTH,
            AttributeModifier.of(sameKey, AttributeOperations.ADDITION, 10),
            EquipmentConditions.MAINHAND
            ));
        stack3.offerSingle(Keys.ITEM_ATTRIBUTES, ItemAttribute.of(
            AttributeTypes.MAX_HEALTH,
            AttributeModifier.of(sameKey, AttributeOperations.ADDITION, 20),
            EquipmentConditions.MAINHAND
            )).ifNotSuccessful(RuntimeException::new);
        stack3.offerSingle(Keys.ITEM_ATTRIBUTES, ItemAttribute.of(
            AttributeTypes.SCALE,
            AttributeModifier.of(sameKey, AttributeOperations.MULTIPLY_TOTAL, 1),
            EquipmentConditions.MAINHAND
            ));

        player.inventory().offer(stack1, stack2, stack3);
        return CommandResult.success();
    }
}
