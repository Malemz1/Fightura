package com.fightura.fightura.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.model.rendering.PartFilterScheme;
import org.joml.Matrix4f;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

public final class FighturaFirstPersonLayer extends PatchedLayer<LocalPlayer, LocalPlayerPatch, PlayerModel<LocalPlayer>, RenderLayer<LocalPlayer, PlayerModel<LocalPlayer>>> {
    @Override
    protected void renderLayer(
            LocalPlayerPatch entitypatch,
            LocalPlayer player,
            @Nullable RenderLayer<LocalPlayer, PlayerModel<LocalPlayer>> vanillaLayer,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            OpenMatrix4f[] poses,
            float bob,
            float yRot,
            float xRot,
            float partialTicks
    ) {
        if (poses != null && poses.length > 0 && poses[0] != null) {
            FighturaPoseCache.put(player.getUUID(), entitypatch.getArmature(), poses);
        }

        Avatar avatar = AvatarManager.getAvatar(player);
        if (avatar == null || !avatar.loaded || avatar.renderer == null) {
            return;
        }

        EntityRenderer<? super LocalPlayer> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        if (!(renderer instanceof LivingEntityRenderer<?, ?> livingRenderer)) {
            return;
        }

        EntityRenderMode previousMode = avatar.renderMode;
        avatar.renderMode = EntityRenderMode.FIRST_PERSON;
        FiguraMat4 poseMatrix = new FiguraMat4().set(new Matrix4f(poseStack.last().pose()));
        try {
            avatar.renderEvent(partialTicks, poseMatrix);
            renderArm(avatar, player, livingRenderer, poseStack, buffer, packedLight, partialTicks, PartFilterScheme.LEFT_ARM);
            renderArm(avatar, player, livingRenderer, poseStack, buffer, packedLight, partialTicks, PartFilterScheme.RIGHT_ARM);
            avatar.postRenderEvent(partialTicks, poseMatrix);
        } finally {
            avatar.renderMode = previousMode;
        }
    }

    private static void renderArm(
            Avatar avatar,
            LocalPlayer player,
            LivingEntityRenderer<?, ?> renderer,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            float partialTicks,
            PartFilterScheme filter
    ) {
        avatar.render(player, player.getYRot(), partialTicks, 1.0F, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, renderer, filter, false, false);
    }
}
