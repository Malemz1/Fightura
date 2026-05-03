package com.fightura.fightura.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

public final class FighturaPoseCache {
    private static final Map<UUID, Snapshot> SNAPSHOTS = new ConcurrentHashMap<>();

    private FighturaPoseCache() {
    }

    public static void put(UUID owner, Armature armature, OpenMatrix4f[] poses) {
        if (owner == null || armature == null || poses == null) {
            return;
        }
        SNAPSHOTS.put(owner, new Snapshot(armature, poses));
    }

    public static Snapshot get(UUID owner) {
        return owner == null ? null : SNAPSHOTS.get(owner);
    }

    public static void remove(UUID owner) {
        if (owner != null) {
            SNAPSHOTS.remove(owner);
        }
    }

    public static void clear() {
        SNAPSHOTS.clear();
    }

    public static OpenMatrix4f resolveJoint(UUID owner, String jointName) {
        Snapshot snap = get(owner);
        return snap == null ? null : snap.resolve(jointName);
    }

    public record Snapshot(Armature armature, OpenMatrix4f[] poses) {
        public OpenMatrix4f resolve(String jointName) {
            if (armature == null || poses == null || poses.length == 0 || jointName == null) {
                return null;
            }
            Joint joint = armature.searchJointByName(jointName);
            if (joint == null || joint == Joint.EMPTY) {
                return null;
            }
            int id = joint.getId();
            if (id < 0 || id >= poses.length || poses[id] == null) {
                return null;
            }
            return OpenMatrix4f.mul(poses[id], joint.getToOrigin(), null);
        }
    }
}
