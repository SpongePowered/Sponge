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
package org.spongepowered.common.mixin.core.server.level;

import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerEntityHealthScaleBridge;
import org.spongepowered.common.mixin.core.world.entity.player.PlayerMixin;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin_HealthScale extends PlayerMixin implements ServerPlayerEntityHealthScaleBridge {

    // @formatter:off
    @Shadow private float lastSentHealth;
    @Shadow public ServerGamePacketListenerImpl connection;
    // @formatter:on

    private AttributeInstance impl$cachedMaxHealthAttribute = null;
    private Double impl$healthScale = null;

    @Inject(method = "doTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getArmorValue()I", ordinal = 1))
    private void updateHealthPriorToArmor(final CallbackInfo ci) {
        this.bridge$refreshScaledHealth();
    }

    @Redirect(method = "doTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getHealth()F"))
    public float impl$onGetHealth(final ServerPlayer serverPlayerEntity) {
        return this.bridge$getInternalScaledHealth();
    }

    @Override
    public boolean bridge$setHealthScale(final double scale) {
        if (scale < 1.0F || scale > Float.MAX_VALUE) {
            return false;
        }

        this.impl$healthScale = scale;
        this.lastSentHealth = -1.0F;

        ((SpongeDataHolderBridge) this).bridge$offer(Keys.HEALTH_SCALE, scale);

        this.bridge$refreshScaledHealth();
        return true;
    }

    @Override
    public void bridge$resetHealthScale() {
        this.impl$healthScale = null;
        this.lastSentHealth = -1.0F;

        ((SpongeDataHolderBridge) this).bridge$remove(Keys.HEALTH_SCALE);

        this.bridge$refreshScaledHealth();
    }

    @Override
    public void bridge$refreshScaledHealth() {
        // We need to use the dirty instances to signify that the player needs to have it updated, instead
        // of modifying the attribute instances themselves, we bypass other potentially detrimental logic
        // that would otherwise break the actual health scaling.
        final Set<AttributeInstance> dirtyInstances = this.shadow$getAttributes().getDirtyAttributes();
        this.bridge$injectScaledHealth(dirtyInstances);

        // Send the new information to the client.
        final FoodData foodData = this.shadow$getFoodData();
        this.connection.send(new ClientboundSetHealthPacket(this.bridge$getInternalScaledHealth(), foodData.getFoodLevel(), foodData.getSaturationLevel()));
        this.connection.send(new ClientboundUpdateAttributesPacket(this.shadow$getId(), dirtyInstances));

        // Reset the dirty instances since they've now been manually updated on the client.
        dirtyInstances.clear();

        // Clear the cached value, so it doesn't carry over to future calls to getInternalScaledHealth.
        this.impl$cachedMaxHealthAttribute = null;
    }

    @Override
    public void bridge$injectScaledHealth(final Collection<AttributeInstance> set) {
        // We need to remove the existing attribute instance for max health, since it's not always going to be the
        // same as SharedMonsterAttributes.MAX_HEALTH
        @Nullable AttributeInstance attribute = null;
        boolean foundMax = false; // Sometimes the max health isn't modified and no longer dirty
        for (final Iterator<AttributeInstance> iter = set.iterator(); iter.hasNext(); ) {
            final AttributeInstance dirtyInstance = iter.next();
            if ("attribute.name.generic.maxHealth".equals(dirtyInstance.getAttribute().value().getDescriptionId())) {
                foundMax = true;
                attribute = dirtyInstance;
                iter.remove();
                break;
            }
        }

        if (!foundMax) {
            // Means we didn't find the max health attribute and need to fetch the modifiers from
            // the cached map because it wasn't marked dirty for some reason
            attribute = this.shadow$getAttribute(Attributes.MAX_HEALTH);
        }

        if (this.bridge$isHealthScaled()) {
            final Collection<AttributeModifier> modifiers = attribute.getModifiers();

            this.impl$cachedMaxHealthAttribute = new AttributeInstance(Attributes.MAX_HEALTH, i -> {});
            this.impl$cachedMaxHealthAttribute.setBaseValue(this.impl$healthScale);

            if (!modifiers.isEmpty()) {
                modifiers.forEach(this.impl$cachedMaxHealthAttribute::addTransientModifier);
            }
        } else {
            this.impl$cachedMaxHealthAttribute = attribute;
        }

        set.add(this.impl$cachedMaxHealthAttribute);
    }

    @Override
    public Double bridge$getHealthScale() {
        return this.impl$healthScale;
    }

    @Override
    public float bridge$getInternalScaledHealth() {
        float maximumHealth = this.shadow$getMaxHealth();
        float healthScale = maximumHealth;

        // Attribute modifiers from mods can add onto health and multiply health, we need to replicate
        // what the mod may be trying to represent, regardless whether the health scale says to show
        // only x hearts.
        if (this.bridge$isHealthScaled()) {
            final AttributeInstance attribute;
            if (this.impl$cachedMaxHealthAttribute == null) {
                final Collection<AttributeModifier> modifiers = this.shadow$getAttribute(Attributes.MAX_HEALTH).getModifiers();
                attribute = new AttributeInstance(Attributes.MAX_HEALTH, i -> {});

                attribute.setBaseValue(this.impl$healthScale);

                if (!modifiers.isEmpty()) {
                    modifiers.forEach(attribute::addTransientModifier);
                }
            } else {
                attribute = this.impl$cachedMaxHealthAttribute;
            }

            healthScale = (float) attribute.getValue();
        }

        return (this.shadow$getHealth() / maximumHealth) * healthScale;
    }

    @Override
    public boolean bridge$isHealthScaled() {
        return this.impl$healthScale != null;
    }

}
