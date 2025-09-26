package org.spongepowered.common.item;

import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.api.data.type.ShieldItemDamageFunction;
import org.spongepowered.common.util.Preconditions;

public class SpongeShieldItemDamageFunctionBuilder implements ShieldItemDamageFunction.Builder {
    private double minAttackDamage = 0;
    private double constantDamage = 0;
    private double fractionalDamage = 0;

    @Override
    public ShieldItemDamageFunction.Builder minAttackDamage(double minDamage) {
        Preconditions.checkArgument(minDamage >= 0, "minAttackDamage must be >= 0");
        this.minAttackDamage = minDamage;

        return this;
    }

    @Override
    public ShieldItemDamageFunction.Builder constantDamage(double constantDamage) {
        this.constantDamage = constantDamage;

        return this;
    }

    @Override
    public ShieldItemDamageFunction.Builder fractionalDamage(double fractionalDamage) {
        this.fractionalDamage = fractionalDamage;

        return this;
    }

    @Override
    public ShieldItemDamageFunction build() {
        return (ShieldItemDamageFunction) (Object) new BlocksAttacks.ItemDamageFunction(
            (float) minAttackDamage,
            (float) constantDamage,
            (float) fractionalDamage
        );
    }

    @Override
    public ShieldItemDamageFunction.Builder reset() {
        this.minAttackDamage = 0;
        this.constantDamage = 0;
        this.fractionalDamage = 0;

        return this;
    }

}
