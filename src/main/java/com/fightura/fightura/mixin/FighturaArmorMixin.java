package com.fightura.fightura.mixin;

import com.fightura.fightura.client.FighturaArmorVisibility;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;

@Mixin(value = WearableItemLayer.class, remap = false)
public abstract class FighturaArmorMixin {
    @Unique
    private static final ThreadLocal<EquipmentSlot> fightura$slot = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<LivingEntity> fightura$entity = new ThreadLocal<>();

    @Inject(
            method = "getArmorModel(Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ArmorItem;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Lyesman/epicfight/api/client/model/SkinnedMesh;",
            at = @At("HEAD"),
            remap = false
    )
    private void fightura$captureArmorContext(
            HumanoidArmorLayer<?, ?, ?> originalRenderer,
            HumanoidModel<?> originalModel,
            Model forgeHooksArmorModel,
            LivingEntity entity,
            ArmorItem armorItem,
            ItemStack itemstack,
            EquipmentSlot slot,
            CallbackInfoReturnable<SkinnedMesh> cir
    ) {
        fightura$slot.set(slot);
        fightura$entity.set(entity);
    }

    @Redirect(
            method = "renderLayer(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I[Lyesman/epicfight/api/utils/math/OpenMatrix4f;FFFF)V",
            at = @At(value = "INVOKE", target = "Lyesman/epicfight/api/client/model/SkinnedMesh;initialize()V"),
            remap = false
    )
    private void fightura$applyFiguraArmorVisibility(SkinnedMesh armorMesh) {
        armorMesh.initialize();
        EquipmentSlot slot = fightura$slot.get();
        LivingEntity entity = fightura$entity.get();
        if (slot != null && entity != null) {
            FighturaArmorVisibility.apply(entity, slot, armorMesh);
            fightura$slot.remove();
            fightura$entity.remove();
        }
    }
}
