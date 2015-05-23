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
package org.spongepowered.common.data.processor.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.DataTransactionBuilder.successReplaceData;

import com.google.common.base.Optional;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.entity.DamageableData;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.entity.SpongeDamageableData;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;

import java.util.UUID;

import javax.annotation.Nullable;

public class SpongeDamageableDataProcessor implements SpongeDataProcessor<DamageableData> {

    @Override
    public Optional<DamageableData> getFrom(DataHolder dataHolder) {
        if (!(checkNotNull(dataHolder) instanceof EntityLivingBase)) {
            return Optional.absent();
        }
        final EntityLivingBase attacker = ((EntityLivingBase) dataHolder).getLastAttacker();
        final double lastDamage = ((IMixinEntityLivingBase) dataHolder).getLastDamage();
        final int invulnTicks = ((EntityLivingBase) dataHolder).hurtResistantTime;
        final DamageableData data = create().setInvulnerabilityTicks(invulnTicks);
        if (attacker != null) {
            return Optional.of(data.setLastAttacker(((Living) attacker)).setLastDamage(lastDamage));
        } else {
            return Optional.of(data);
        }
    }

    @Override
    public Optional<DamageableData> fillData(DataHolder dataHolder, DamageableData manipulator, DataPriority priority) {
        if (!(checkNotNull(dataHolder) instanceof EntityLivingBase)) {
            return Optional.absent();
        }
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE:
                @Nullable
                final EntityLivingBase attacker = ((EntityLivingBase) dataHolder).getLastAttacker();
                final double lastDamage = ((IMixinEntityLivingBase) dataHolder).getLastDamage();
                final int invulnTicks = ((EntityLivingBase) dataHolder).hurtResistantTime;
                manipulator.setInvulnerabilityTicks(invulnTicks);
                if (attacker != null) {
                    manipulator.setLastAttacker((Living) attacker)
                            .setLastDamage(lastDamage);
                }
                return Optional.of(manipulator);
            default:
                return Optional.of(manipulator);
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, DamageableData manipulator, DataPriority priority) {
        if (!(checkNotNull(dataHolder) instanceof EntityLivingBase)) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case DATA_MANIPULATOR:
            case POST_MERGE:
                final DamageableData old = getFrom(dataHolder).get();
                ((EntityLivingBase) dataHolder).hurtResistantTime = manipulator.getInvulnerabilityTicks();
                ((EntityLivingBase) dataHolder).setLastAttacker(((EntityLivingBase) manipulator.getLastAttacker().orNull()));
                ((IMixinEntityLivingBase) dataHolder).setLastDamage(manipulator.getLastDamage().or(0D));
                return successReplaceData(old);
            default:
                return successNoData();
        }
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityLivingBase)) {
            return false;
        }
        ((EntityLivingBase) dataHolder).setLastAttacker(null);
        ((IMixinEntityLivingBase) dataHolder).setLastDamage(0.0D);
        ((EntityLivingBase) dataHolder).hurtResistantTime = 0;
        return true;
    }

    @Override
    public Optional<DamageableData> build(DataView container) throws InvalidDataException {
        final double lastDamage = DataUtil.getData(container, SpongeDamageableData.LAST_DAMAGE, Double.TYPE);
        final String lastAttacker = DataUtil.getData(container, SpongeDamageableData.LAST_ATTACKER, String.class);
        final UUID attackerUuid = UUID.fromString(lastAttacker); // We can't actually reconstruct this information
        return Optional.of(create().setLastDamage(lastDamage));
    }

    @Override
    public DamageableData create() {
        return new SpongeDamageableData();
    }

    @Override
    public Optional<DamageableData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityLivingBase)) {
            return Optional.absent();
        }
        return getFrom(dataHolder);
    }
}
