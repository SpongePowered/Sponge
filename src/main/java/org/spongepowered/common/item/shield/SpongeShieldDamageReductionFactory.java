package org.spongepowered.common.item.shield;

import org.spongepowered.api.data.type.ShieldDamageReduction;

public final class SpongeShieldDamageReductionFactory implements ShieldDamageReduction.Factory {

    @Override
    public ShieldDamageReduction<ShieldDamageReduction.MultiplyAdd> create(ShieldDamageReduction.MultiplyAdd config) {
        return (ShieldDamageReduction<ShieldDamageReduction.MultiplyAdd>) config;
    }

}
