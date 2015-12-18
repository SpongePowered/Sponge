package org.spongepowered.common.mixin.core.item;

import net.minecraft.item.Item;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.data.type.ToolType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.ToolMaterial.class)
public abstract class MixinItemToolMaterial implements ToolType {

    private String name;
    private String capitalizedName;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(CallbackInfo ci) {
        // This is a giant workaround due to being unable to refer to synthetic
        // methods provided by Enum and the base enum itself does not
        // have a field for the name.
        String toString = this.toString();
        if (toString.equalsIgnoreCase("emerald")) {
            toString = "diamond";
        }
        this.name = toString.toLowerCase();
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
