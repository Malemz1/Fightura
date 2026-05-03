package com.fightura.fightura.client;

import com.fightura.fightura.FighturaMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.api.client.forgeevent.PrepareModelEvent;
import yesman.epicfight.api.client.forgeevent.RenderEpicFightPlayerEvent;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.FirstPersonRenderer;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;

@OnlyIn(Dist.CLIENT)
public final class FighturaForgeEvents {
    private static PatchedEntityRenderer attachedPlayerRenderer;
    private static FirstPersonRenderer attachedFirstPersonRenderer;

    private FighturaForgeEvents() {
    }

    public static void register(IEventBus modBus) {
        FighturaMod.LOGGER.info("Fightura registering events");
        MinecraftForge.EVENT_BUS.register(FighturaForgeEvents.class);
        modBus.addListener(FighturaForgeEvents::onPatchedRenderersModified);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            FighturaPoseCache.clear();
            FighturaJointMap.clearAll();
        }
    }

    /** Hide Epic Fight's vanilla 1st-person hand when a Figura avatar is loaded for the camera entity. */
    @SubscribeEvent
    public static void onRenderEpicFightPlayer(RenderEpicFightPlayerEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCameraEntity() != event.getPlayerPatch().getOriginal()) {
            return;
        }
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        Avatar avatar = AvatarManager.getAvatar(event.getPlayerPatch().getOriginal());
        if (avatar != null && avatar.loaded) {
            event.setShouldRender(false);
        }
    }

    /** Sync Epic Fight HumanoidMesh part visibility from Figura's vanilla_model API. */
    @SubscribeEvent
    public static void onPrepareModel(PrepareModelEvent event) {
        if (!(event.getMesh() instanceof HumanoidMesh humanoidMesh)) {
            return;
        }
        LivingEntity entity = event.getEntityPatch().getOriginal();
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar == null || avatar.luaRuntime == null) {
            return;
        }
        VanillaModelAPI vm = avatar.luaRuntime.vanilla_model;
        applyVisible(humanoidMesh.head, vm.HEAD);
        applyVisible(humanoidMesh.torso, vm.BODY);
        applyVisible(humanoidMesh.leftArm, vm.LEFT_ARM);
        applyVisible(humanoidMesh.rightArm, vm.RIGHT_ARM);
        applyVisible(humanoidMesh.leftLeg, vm.LEFT_LEG);
        applyVisible(humanoidMesh.rightLeg, vm.RIGHT_LEG);
        applyVisible(humanoidMesh.hat, vm.HAT);
        applyVisible(humanoidMesh.jacket, vm.JACKET);
        applyVisible(humanoidMesh.leftSleeve, vm.LEFT_SLEEVE);
        applyVisible(humanoidMesh.rightSleeve, vm.RIGHT_SLEEVE);
        applyVisible(humanoidMesh.leftPants, vm.LEFT_PANTS);
        applyVisible(humanoidMesh.rightPants, vm.RIGHT_PANTS);
    }

    private static void onPatchedRenderersModified(PatchedRenderersEvent.Modify event) {
        PatchedEntityRenderer patched = event.get(EntityType.PLAYER);
        if (patched instanceof PPlayerRenderer playerRenderer && patched != attachedPlayerRenderer) {
            attachedPlayerRenderer = patched;
            FighturaMod.LOGGER.info("Fightura attached render layer to Epic Fight PLAYER renderer");
            playerRenderer.addCustomLayer(new FighturaRenderLayer());
        }

        if (ClientEngine.getInstance().renderEngine == null) {
            return;
        }
        FirstPersonRenderer firstPerson = ClientEngine.getInstance().renderEngine.getFirstPersonRenderer();
        if (firstPerson != null && firstPerson != attachedFirstPersonRenderer) {
            attachedFirstPersonRenderer = firstPerson;
            FighturaMod.LOGGER.info("Fightura attached render layer to Epic Fight first-person renderer");
            firstPerson.addCustomLayer(new FighturaFirstPersonLayer());
        }
    }

    private static void applyVisible(SkinnedMesh.SkinnedMeshPart part, VanillaPart vanillaPart) {
        Boolean visible = vanillaPart.getVisible();
        if (visible != null) {
            part.setHidden(!visible);
        }
    }
}
