package org.spongepowered.common.mixin.api.minecraft.world.item.component;

import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.api.data.type.ShieldItemDamageFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlocksAttacks.ItemDamageFunction.class)
public abstract class BlocksAttacks_ItemDamageFunction_API implements ShieldItemDamageFunction {

    @Shadow @Final private float threshold;
    @Shadow @Final private float base;
    @Shadow @Final private float factor;

    @Override
    public double minAttackDamage() {
        return this.threshold;
    }

    @Override
    public double constantDamage() {
        return this.base;
    }

    @Override
    public double fractionalDamage() {
        return this.factor;
    }

}
