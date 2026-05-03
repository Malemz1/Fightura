package com.fightura.fightura.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.figuramc.figura.model.ParentType;

public final class FighturaJointMap {
    private static final Map<String, String> BUILT_IN;
    private static final Map<UUID, Map<String, String>> CUSTOM = new ConcurrentHashMap<>();

    static {
        Map<String, String> m = new LinkedHashMap<>();

        // Figura ParentType primary segments
        put(m, "Head", "Head");
        put(m, "Body", "Chest");
        put(m, "LeftArm", "Arm_L");
        put(m, "RightArm", "Arm_R");
        put(m, "LeftLeg", "Thigh_L");
        put(m, "RightLeg", "Thigh_R");

        // Common English bone names (head)
        put(m, "Neck", "Head");
        put(m, "Hat", "Head");

        // Common English bone names (torso)
        put(m, "Chest", "Chest");
        put(m, "Torso", "Chest");
        put(m, "UpperChest", "Chest");
        put(m, "Spine", "Spine");
        put(m, "Spine1", "Chest");
        put(m, "Spine2", "Chest");
        put(m, "Pelvis", "Pelvis");
        put(m, "Hips", "Pelvis");
        put(m, "Hip", "Pelvis");
        put(m, "Waist", "Spine");
        put(m, "Jacket", "Chest");

        // Common English bone names (left arm)
        put(m, "LeftShoulder", "Arm_L");
        put(m, "LeftUpperArm", "Arm_L");
        put(m, "LeftSleeve", "Arm_L");
        put(m, "LeftForeArm", "Hand_L");
        put(m, "LeftForearm", "Hand_L");
        put(m, "LeftLowerArm", "Hand_L");
        put(m, "LeftElbow", "Hand_L");
        put(m, "LArmLower", "Hand_L");
        put(m, "LeftHand", "Hand_L");

        // Common English bone names (right arm)
        put(m, "RightShoulder", "Arm_R");
        put(m, "RightUpperArm", "Arm_R");
        put(m, "RightSleeve", "Arm_R");
        put(m, "RightForeArm", "Hand_R");
        put(m, "RightForearm", "Hand_R");
        put(m, "RightLowerArm", "Hand_R");
        put(m, "RightElbow", "Hand_R");
        put(m, "RArmLower", "Hand_R");
        put(m, "RightHand", "Hand_R");

        // Common English bone names (left leg)
        put(m, "LeftUpperLeg", "Thigh_L");
        put(m, "LeftUpLeg", "Thigh_L");
        put(m, "LeftThigh", "Thigh_L");
        put(m, "LeftPants", "Thigh_L");
        put(m, "LeftLowerLeg", "Leg_L");
        put(m, "LeftShin", "Leg_L");
        put(m, "LeftKnee", "Leg_L");
        put(m, "LLegLower", "Leg_L");
        put(m, "LeftFoot", "Leg_L");

        // Common English bone names (right leg)
        put(m, "RightUpperLeg", "Thigh_R");
        put(m, "RightUpLeg", "Thigh_R");
        put(m, "RightThigh", "Thigh_R");
        put(m, "RightPants", "Thigh_R");
        put(m, "RightLowerLeg", "Leg_R");
        put(m, "RightShin", "Leg_R");
        put(m, "RightKnee", "Leg_R");
        put(m, "RLegLower", "Leg_R");
        put(m, "RightFoot", "Leg_R");

        // Mixamo standard (mixamorig:* prefix)
        put(m, "mixamorig:Head", "Head");
        put(m, "mixamorig:Neck", "Head");
        put(m, "mixamorig:Hips", "Pelvis");
        put(m, "mixamorig:Spine", "Spine");
        put(m, "mixamorig:Spine1", "Chest");
        put(m, "mixamorig:Spine2", "Chest");
        put(m, "mixamorig:LeftShoulder", "Arm_L");
        put(m, "mixamorig:LeftArm", "Arm_L");
        put(m, "mixamorig:LeftForeArm", "Hand_L");
        put(m, "mixamorig:LeftHand", "Hand_L");
        put(m, "mixamorig:RightShoulder", "Arm_R");
        put(m, "mixamorig:RightArm", "Arm_R");
        put(m, "mixamorig:RightForeArm", "Hand_R");
        put(m, "mixamorig:RightHand", "Hand_R");
        put(m, "mixamorig:LeftUpLeg", "Thigh_L");
        put(m, "mixamorig:LeftLeg", "Leg_L");
        put(m, "mixamorig:LeftFoot", "Leg_L");
        put(m, "mixamorig:RightUpLeg", "Thigh_R");
        put(m, "mixamorig:RightLeg", "Leg_R");
        put(m, "mixamorig:RightFoot", "Leg_R");

        // Blender Rigify (.L / .R suffix)
        put(m, "head", "Head");
        put(m, "neck", "Head");
        put(m, "spine", "Spine");
        put(m, "spine.001", "Spine");
        put(m, "spine.002", "Chest");
        put(m, "spine.003", "Chest");
        put(m, "shoulder.L", "Arm_L");
        put(m, "upper_arm.L", "Arm_L");
        put(m, "forearm.L", "Hand_L");
        put(m, "hand.L", "Hand_L");
        put(m, "shoulder.R", "Arm_R");
        put(m, "upper_arm.R", "Arm_R");
        put(m, "forearm.R", "Hand_R");
        put(m, "hand.R", "Hand_R");
        put(m, "thigh.L", "Thigh_L");
        put(m, "shin.L", "Leg_L");
        put(m, "foot.L", "Leg_L");
        put(m, "thigh.R", "Thigh_R");
        put(m, "shin.R", "Leg_R");
        put(m, "foot.R", "Leg_R");

        // VRM / VRoid (J_Bip_* prefix)
        put(m, "J_Bip_C_Head", "Head");
        put(m, "J_Bip_C_Neck", "Head");
        put(m, "J_Bip_C_Hips", "Pelvis");
        put(m, "J_Bip_C_Spine", "Spine");
        put(m, "J_Bip_C_Chest", "Chest");
        put(m, "J_Bip_C_UpperChest", "Chest");
        put(m, "J_Bip_L_Shoulder", "Arm_L");
        put(m, "J_Bip_L_UpperArm", "Arm_L");
        put(m, "J_Bip_L_LowerArm", "Hand_L");
        put(m, "J_Bip_L_Hand", "Hand_L");
        put(m, "J_Bip_R_Shoulder", "Arm_R");
        put(m, "J_Bip_R_UpperArm", "Arm_R");
        put(m, "J_Bip_R_LowerArm", "Hand_R");
        put(m, "J_Bip_R_Hand", "Hand_R");
        put(m, "J_Bip_L_UpperLeg", "Thigh_L");
        put(m, "J_Bip_L_LowerLeg", "Leg_L");
        put(m, "J_Bip_L_Foot", "Leg_L");
        put(m, "J_Bip_R_UpperLeg", "Thigh_R");
        put(m, "J_Bip_R_LowerLeg", "Leg_R");
        put(m, "J_Bip_R_Foot", "Leg_R");

        BUILT_IN = Collections.unmodifiableMap(m);
    }

    private FighturaJointMap() {
    }

    public static String byParentType(ParentType parentType, UUID owner) {
        return parentType == null ? null : resolve(parentType.name(), owner);
    }

    public static String byName(String name, UUID owner) {
        return name == null ? null : resolve(name, owner);
    }

    public static void map(UUID owner, String alias, String joint) {
        if (owner == null || alias == null || joint == null) {
            return;
        }
        String key = alias.trim().toLowerCase(Locale.ROOT);
        String value = joint.trim();
        if (key.isEmpty() || value.isEmpty()) {
            return;
        }
        CUSTOM.computeIfAbsent(owner, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    public static void clear(UUID owner, String alias) {
        if (owner == null || alias == null) {
            return;
        }
        Map<String, String> map = CUSTOM.get(owner);
        if (map != null) {
            map.remove(alias.trim().toLowerCase(Locale.ROOT));
        }
    }

    public static void clearAll(UUID owner) {
        if (owner != null) {
            CUSTOM.remove(owner);
        }
    }

    public static void clearAll() {
        CUSTOM.clear();
    }

    public static Set<String> supportedAliases() {
        return BUILT_IN.keySet();
    }

    private static String resolve(String key, UUID owner) {
        String lowered = key.toLowerCase(Locale.ROOT);
        if (owner != null) {
            Map<String, String> custom = CUSTOM.get(owner);
            if (custom != null) {
                String mapped = custom.get(lowered);
                if (mapped != null) {
                    return mapped;
                }
            }
        }
        return BUILT_IN.get(lowered);
    }

    private static void put(Map<String, String> map, String alias, String joint) {
        map.put(alias.toLowerCase(Locale.ROOT), joint);
    }
}
