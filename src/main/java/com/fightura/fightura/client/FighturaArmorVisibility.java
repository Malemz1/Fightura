package com.fightura.fightura.client;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.api.vanilla_model.VanillaGroupPart;
import org.figuramc.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import yesman.epicfight.api.client.model.SkinnedMesh;

public final class FighturaArmorVisibility {
    private FighturaArmorVisibility() {
    }

    public static void apply(LivingEntity entity, EquipmentSlot slot, SkinnedMesh armorMesh) {
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar == null || avatar.luaRuntime == null) {
            return;
        }
        VanillaModelAPI vm = avatar.luaRuntime.vanilla_model;

        VanillaGroupPart group = groupFor(vm, slot);
        if (group != null) {
            Boolean visible = group.getVisible();
            if (visible != null) {
                boolean hidden = !visible;
                armorMesh.getAllParts().forEach(part -> part.setHidden(hidden));
                return;
            }
        }
        applyPerPiece(armorMesh, vm, slot);
    }

    private static VanillaGroupPart groupFor(VanillaModelAPI vm, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> vm.HELMET;
            case CHEST -> vm.CHESTPLATE;
            case LEGS -> vm.LEGGINGS;
            case FEET -> vm.BOOTS;
            default -> null;
        };
    }

    private static void applyPerPiece(SkinnedMesh armorMesh, VanillaModelAPI vm, EquipmentSlot slot) {
        switch (slot) {
            case HEAD -> {
                applyPart(armorMesh, "head", vm.HELMET_HEAD);
                applyPart(armorMesh, "hat", vm.HELMET_HAT);
            }
            case CHEST -> {
                applyPart(armorMesh, "torso", vm.CHESTPLATE_BODY);
                applyPart(armorMesh, "leftArm", vm.CHESTPLATE_LEFT_ARM);
                applyPart(armorMesh, "rightArm", vm.CHESTPLATE_RIGHT_ARM);
            }
            case LEGS -> {
                applyPart(armorMesh, "torso", vm.LEGGINGS_BODY);
                applyPart(armorMesh, "leftLeg", vm.LEGGINGS_LEFT_LEG);
                applyPart(armorMesh, "rightLeg", vm.LEGGINGS_RIGHT_LEG);
            }
            case FEET -> {
                applyPart(armorMesh, "leftLeg", vm.BOOTS_LEFT_LEG);
                applyPart(armorMesh, "rightLeg", vm.BOOTS_RIGHT_LEG);
            }
            default -> {
            }
        }
    }

    private static void applyPart(SkinnedMesh armorMesh, String partName, VanillaPart vanillaPart) {
        Boolean visible = vanillaPart.getVisible();
        if (visible != null && armorMesh.hasPart(partName)) {
            armorMesh.getPart(partName).setHidden(!visible);
        }
    }
}
