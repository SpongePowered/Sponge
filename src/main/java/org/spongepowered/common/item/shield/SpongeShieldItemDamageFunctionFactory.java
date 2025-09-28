package org.spongepowered.common.item.shield;

import org.spongepowered.api.data.type.ShieldItemDamageFunction;

public final class SpongeShieldItemDamageFunctionFactory implements ShieldItemDamageFunction.Factory {

    @Override
    public ShieldItemDamageFunction<ShieldItemDamageFunction.MultiplyAdd> create(ShieldItemDamageFunction.MultiplyAdd config) {
        return (ShieldItemDamageFunction<ShieldItemDamageFunction.MultiplyAdd>) config;
    }

}
