package com.fightura.fightura.mixin;

import com.fightura.fightura.client.FighturaPipeline;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.PartCustomization;
import org.figuramc.figura.model.rendering.ImmediateAvatarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ImmediateAvatarRenderer.class, remap = false)
public abstract class FighturaPartMatrixMixin {
    @Redirect(
            method = "renderPart",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/figuramc/figura/model/PartCustomization;recalculate()V"
            ),
            remap = false,
            require = 0
    )
    private void fightura$redirectRenderPartRecalculate(PartCustomization customization, FiguraModelPart part, int[] remainingComplexity, boolean prevPredicate) {
        if (!FighturaPipeline.overridePartMatrix(part, customization)) {
            customization.recalculate();
        }
    }

    @Redirect(
            method = "calculatePartMatrices",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/figuramc/figura/model/PartCustomization;recalculate()V"
            ),
            remap = false,
            require = 0
    )
    private void fightura$redirectCalculatePartMatricesRecalculate(PartCustomization customization, FiguraModelPart part) {
        if (!FighturaPipeline.overridePartMatrix(part, customization)) {
            customization.recalculate();
        }
    }
}
