package com.fightura.fightura.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.model.rendering.PartFilterScheme;
import org.joml.Matrix4f;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.mixin.client.MixinLivingEntityRenderer;

public final class FighturaRenderLayer extends PatchedLayer<AbstractClientPlayer, AbstractClientPlayerPatch<AbstractClientPlayer>, PlayerModel<AbstractClientPlayer>, RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> {
    @Override
    protected void renderLayer(
            AbstractClientPlayerPatch<AbstractClientPlayer> entitypatch,
            AbstractClientPlayer player,
            @Nullable RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> vanillaLayer,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            OpenMatrix4f[] poses,
            float bob,
            float yRot,
            float xRot,
            float partialTicks
    ) {
        if (poses == null || poses.length == 0 || poses[0] == null) {
            return;
        }
        FighturaPoseCache.put(player.getUUID(), entitypatch.getArmature(), poses);

        Avatar avatar = AvatarManager.getAvatar(player);
        if (avatar == null || !avatar.loaded || avatar.renderer == null) {
            return;
        }

        EntityRenderer<? super AbstractClientPlayer> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        if (!(renderer instanceof LivingEntityRenderer<?, ?> livingRenderer)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        boolean bodyVisible = ((MixinLivingEntityRenderer) livingRenderer).invokeIsBodyVisible(player);
        boolean translucent = !bodyVisible && minecraft.player != null && !player.isInvisibleTo(minecraft.player);
        boolean glowing = !bodyVisible && minecraft.shouldEntityAppearGlowing(player);
        boolean invisible = !translucent && !bodyVisible && !glowing;
        PartFilterScheme filter = invisible ? PartFilterScheme.PIVOTS : PartFilterScheme.MODEL;
        float alpha = translucent ? 0.15F : 1.0F;
        float bodyYaw = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot);

        FighturaPoseCache.Snapshot snapshot = new FighturaPoseCache.Snapshot(entitypatch.getArmature(), poses);
        EntityRenderMode previousMode = avatar.renderMode;
        avatar.renderMode = EntityRenderMode.RENDER;

        Matrix4f basePose = new Matrix4f(poseStack.last().pose());

        poseStack.pushPose();
        try {
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.501F, 0.0F);

            FiguraMat4 poseMatrix = new FiguraMat4().set(
                    new Matrix4f(basePose).invert().mul(poseStack.last().pose())
            );
            avatar.renderEvent(partialTicks, poseMatrix);

            FighturaPipeline.enter(avatar.owner, snapshot);
            try {
                avatar.render(player, bodyYaw, partialTicks, alpha, poseStack, buffer, packedLight,
                        OverlayTexture.NO_OVERLAY, livingRenderer, filter, translucent, glowing);
            } finally {
                FighturaPipeline.exit();
            }

            avatar.postRenderEvent(partialTicks, poseMatrix);
        } finally {
            poseStack.popPose();
            avatar.renderMode = previousMode;
        }
    }
}
