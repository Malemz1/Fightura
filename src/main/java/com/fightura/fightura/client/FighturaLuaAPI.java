package com.fightura.fightura.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import yesman.epicfight.api.utils.math.OpenMatrix4f;
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
    @LuaMethodDoc("fightura.is_attacking")
    public boolean isAttacking() {
        LivingEntityPatch<?> patch = livingPatch();
        return patch != null && patch.getEntityState() != null && patch.getEntityState().attacking();
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

    private LivingEntityPatch<?> livingPatch() {
        if (avatar == null) {
            return null;
        }
        Entity entity = EntityUtils.getEntityByUUID(avatar.owner);
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }
        return EpicFightCapabilities.getEntityPatch(living, LivingEntityPatch.class);
    }

    private PlayerPatch<?> playerPatch() {
        if (avatar == null) {
            return null;
        }
        Entity entity = EntityUtils.getEntityByUUID(avatar.owner);
        if (!(entity instanceof Player player)) {
            return null;
        }
        return EpicFightCapabilities.getPlayerPatch(player);
    }
}
