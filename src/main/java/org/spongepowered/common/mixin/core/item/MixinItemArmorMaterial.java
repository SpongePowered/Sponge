package org.spongepowered.common.mixin.core.item;

import net.minecraft.item.ItemArmor;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.data.type.ArmorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemArmor.ArmorMaterial.class)
public abstract class MixinItemArmorMaterial implements ArmorType {

    @Shadow private String name;

    private String capitalizedName;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(CallbackInfo ci) {
        this.capitalizedName = StringUtils.capitalize(this.name);
    }

    @Override
    public String getId() {
        return this.name;
    }

    @Override
    public String getName() {
        return this.capitalizedName;
    }
}
