package org.spongepowered.common.mixin.core.advancement;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.common.SpongeImpl;

@Mixin(ICriterionTrigger.Listener.class)
public class MixinICriterionTriggerListener {

    @Shadow @Final private ICriterionInstance criterionInstance;

    @Inject(method = "grantCriterion", at = @At("HEAD"))
    private void onGrantCriterion(PlayerAdvancements playerAdvancements) {
        SpongeImpl.getCauseStackManager().pushCause(this.criterionInstance);
    }

    @Inject(method = "grantCriterion", at = @At("RETURN"))
    private void onGrantCriterionReturn(PlayerAdvancements playerAdvancements) {
        SpongeImpl.getCauseStackManager().popCause();
    }
}
