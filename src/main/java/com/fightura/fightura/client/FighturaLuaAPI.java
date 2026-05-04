package com.fightura.fightura.client;

import com.fightura.fightura.FighturaMod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.entries.FiguraAPI;
import org.figuramc.figura.entries.annotations.FiguraAPIPlugin;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.api.event.LuaEvent;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.EntityUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@LuaWhitelist
@FiguraAPIPlugin
@LuaTypeDoc(name = "FighturaAPI", value = "fightura")
public final class FighturaLuaAPI implements FiguraAPI {
    private final Avatar avatar;

    /** Fires when an Epic Fight attack animation begins. Args: motion name. */
    @LuaWhitelist
    public final LuaEvent attackStart = new LuaEvent();
    /** Fires when an Epic Fight attack animation ends. */
    @LuaWhitelist
    public final LuaEvent attackEnd = new LuaEvent();
    /** Fires when the entity enters a hurt state. */
    @LuaWhitelist
    public final LuaEvent hurtStart = new LuaEvent();
    /** Fires when the entity is knocked down. */
    @LuaWhitelist
    public final LuaEvent knockDown = new LuaEvent();
    /** Fires when player_mode changes (BATTLE / MINING / DEFAULT). Args: new mode, old mode. */
    @LuaWhitelist
    public final LuaEvent modeChange = new LuaEvent();
    /** Fires when current living motion changes. Args: new motion, old motion. */
    @LuaWhitelist
    public final LuaEvent motionChange = new LuaEvent();

    public FighturaLuaAPI() {
        this.avatar = null;
    }

    private FighturaLuaAPI(Avatar avatar) {
        this.avatar = avatar;
    }

    @Override
    public FiguraAPI build(Avatar avatar) {
        FighturaLuaAPI api = new FighturaLuaAPI(avatar);
        FighturaEventBus.register(avatar.owner, api);
        return api;
    }

    @Override
    public String getName() {
        return "fightura";
    }

    @Override
    public Collection<Class<?>> getWhitelistedClasses() {
        return List.of(FighturaLuaAPI.class);
    }

    @Override
    public Collection<Class<?>> getDocsClasses() {
        return List.of(FighturaLuaAPI.class);
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.is_available")
    public boolean isAvailable() {
        return livingPatch() != null;
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.is_epicfight_mode")
    public boolean isEpicFightMode() {
        PlayerPatch<?> patch = playerPatch();
        return patch != null && patch.isEpicFightMode();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.is_vanilla_mode")
    public boolean isVanillaMode() {
        PlayerPatch<?> patch = playerPatch();
        return patch != null && patch.isVanillaMode();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_player_mode")
    public String getPlayerMode() {
        PlayerPatch<?> patch = playerPatch();
        return patch == null ? null : patch.getPlayerMode().name();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_stamina")
    public double getStamina() {
        PlayerPatch<?> patch = playerPatch();
        return patch == null ? 0.0D : patch.getStamina();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_max_stamina")
    public double getMaxStamina() {
        PlayerPatch<?> patch = playerPatch();
        return patch == null ? 0.0D : patch.getMaxStamina();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_charging_amount")
    public int getChargingAmount() {
        PlayerPatch<?> patch = playerPatch();
        return patch == null ? 0 : patch.getChargingAmount();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_current_motion")
    public String getCurrentMotion() {
        LivingEntityPatch<?> patch = livingPatch();
        return patch == null ? null : motionName(patch.getCurrentLivingMotion());
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_current_composite_motion")
    public String getCurrentCompositeMotion() {
        ClientAnimator animator = clientAnimator();
        return animator == null ? null : motionName(animator.currentCompositeMotion());
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_base_animation")
    public String getBaseAnimation() {
        ClientAnimator animator = clientAnimator();
        return animator == null ? null : animationId(animator.baseLayer.animationPlayer);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {String.class},
                    argumentNames = {"priority"}
            ),
            value = "fightura.get_animation"
    )
    public String getAnimation(String priority) {
        ClientAnimator animator = clientAnimator();
        if (animator == null || priority == null) {
            return null;
        }
        String normalized = priority.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty() || "BASE".equals(normalized)) {
            return animationId(animator.baseLayer.animationPlayer);
        }
        try {
            Layer.Priority layerPriority = Layer.Priority.valueOf(normalized);
            return animationId(animator.getCompositeLayer(layerPriority).animationPlayer);
        } catch (IllegalArgumentException exception) {
            FighturaMod.LOGGER.debug("Unknown Fightura layer priority requested: {}", priority);
            return null;
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.is_attacking")
    public boolean isAttacking() {
        EntityState state = entityState();
        return state != null && state.attacking();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.is_movement_locked")
    public boolean isMovementLocked() {
        EntityState state = entityState();
        return state != null && state.movementLocked();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.is_turning_locked")
    public boolean isTurningLocked() {
        EntityState state = entityState();
        return state != null && state.turningLocked();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.is_inaction")
    public boolean isInaction() {
        EntityState state = entityState();
        return state != null && state.inaction();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.can_basic_attack")
    public boolean canBasicAttack() {
        EntityState state = entityState();
        return state != null && state.canBasicAttack();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.can_use_skill")
    public boolean canUseSkill() {
        EntityState state = entityState();
        return state != null && state.canUseSkill();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.can_use_item")
    public boolean canUseItem() {
        EntityState state = entityState();
        return state != null && state.canUseItem();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.is_hurt")
    public boolean isHurt() {
        EntityState state = entityState();
        return state != null && state.hurt();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.is_knockdown")
    public boolean isKnockDown() {
        EntityState state = entityState();
        return state != null && state.knockDown();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_state_level")
    public int getStateLevel() {
        EntityState state = entityState();
        return state == null ? 0 : state.getLevel();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_target_uuid")
    public String getTargetUUID() {
        LivingEntity target = target();
        return target == null ? null : target.getUUID().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_target_name")
    public String getTargetName() {
        LivingEntity target = target();
        return target == null ? null : target.getName().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.is_target_locked_on")
    public boolean isTargetLockedOn() {
        LivingEntityPatch<?> patch = livingPatch();
        return patch instanceof LocalPlayerPatch local && local.isTargetLockedOn();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_entity_uuid")
    public String getEntityUUID() {
        Entity entity = entity();
        return entity == null ? null : entity.getUUID().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_entity_name")
    public String getEntityName() {
        Entity entity = entity();
        return entity == null ? null : entity.getName().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.has_pose")
    public boolean hasPose() {
        return avatar != null && FighturaPoseCache.get(avatar.owner) != null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {String.class},
                    argumentNames = {"joint"}
            ),
            value = "fightura.get_joint_rotation"
    )
    public FiguraVec3 getJointRotation(String jointName) {
        OpenMatrix4f matrix = jointMatrix(jointName);
        if (matrix == null) {
            return null;
        }
        Vector3f euler = matrix.toQuaternion().getEulerAnglesZYX(new Vector3f());
        return FiguraVec3.of(euler.x, euler.y, euler.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {String.class},
                    argumentNames = {"joint"}
            ),
            value = "fightura.get_joint_matrix"
    )
    public FiguraMat4 getJointMatrix(String jointName) {
        OpenMatrix4f matrix = jointMatrix(jointName);
        if (matrix == null) {
            return null;
        }
        Matrix4f mojang = OpenMatrix4f.exportToMojangMatrix(matrix, null);
        return FiguraMat4.of().set(mojang);
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_joints")
    public List<String> getJoints() {
        if (avatar == null) {
            return List.of();
        }
        FighturaPoseCache.Snapshot snap = FighturaPoseCache.get(avatar.owner);
        if (snap == null || snap.armature() == null || snap.armature().rootJoint == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        snap.armature().rootJoint.getAllJoints().forEach(joint -> {
            if (joint.getName() != null) {
                names.add(joint.getName());
            }
        });
        return names;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {String.class, String.class},
                    argumentNames = {"alias", "joint"}
            ),
            value = "fightura.map_bone"
    )
    public void mapBone(String alias, String joint) {
        if (avatar != null) {
            FighturaJointMap.map(avatar.owner, alias, joint);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {String.class},
                    argumentNames = {"alias"}
            ),
            value = "fightura.clear_bone"
    )
    public void clearBone(String alias) {
        if (avatar != null) {
            FighturaJointMap.clear(avatar.owner, alias);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.clear_bones")
    public void clearBones() {
        if (avatar != null) {
            FighturaJointMap.clearAll(avatar.owner);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("fightura.get_supported_bones")
    public List<String> getSupportedBones() {
        return new ArrayList<>(FighturaJointMap.supportedAliases());
    }

    private OpenMatrix4f jointMatrix(String jointName) {
        return avatar == null ? null : FighturaPoseCache.resolveJoint(avatar.owner, jointName);
    }

    private Entity entity() {
        return avatar == null ? null : EntityUtils.getEntityByUUID(avatar.owner);
    }

    private LivingEntityPatch<?> livingPatch() {
        Entity entity = entity();
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }
        return EpicFightCapabilities.getEntityPatch(living, LivingEntityPatch.class);
    }

    private PlayerPatch<?> playerPatch() {
        Entity entity = entity();
        return entity instanceof Player player ? EpicFightCapabilities.getPlayerPatch(player) : null;
    }

    private ClientAnimator clientAnimator() {
        LivingEntityPatch<?> patch = livingPatch();
        return patch == null ? null : patch.getClientAnimator();
    }

    private EntityState entityState() {
        LivingEntityPatch<?> patch = livingPatch();
        return patch == null ? null : patch.getEntityState();
    }

    private LivingEntity target() {
        LivingEntityPatch<?> patch = livingPatch();
        return patch == null ? null : patch.getTarget();
    }

    private static String motionName(LivingMotion motion) {
        return motion == null ? null : motion.toString();
    }

    private static String animationId(AnimationPlayer animationPlayer) {
        if (animationPlayer == null || animationPlayer.isEmpty()) {
            return null;
        }
        AssetAccessor<? extends StaticAnimation> animation = animationPlayer.getRealAnimation();
        if (animation == null || animation.isEmpty()) {
            return null;
        }
        ResourceLocation id = animation.registryName();
        return id == null ? null : id.toString();
    }
}
