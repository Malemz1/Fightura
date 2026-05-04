package com.fightura.fightura.client;

import java.util.UUID;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.PartCustomization;
import org.joml.Matrix4f;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

public final class FighturaPipeline {
    private static final ThreadLocal<RenderContext> CONTEXT = new ThreadLocal<>();

    private FighturaPipeline() {
    }

    public static void enter(UUID owner, FighturaPoseCache.Snapshot snapshot) {
        if (owner == null || snapshot == null) {
            CONTEXT.remove();
        } else {
            CONTEXT.set(new RenderContext(owner, snapshot));
        }
    }

    public static void exit() {
        CONTEXT.remove();
    }

    /**
     * Called from FighturaPartMatrixMixin instead of PartCustomization.recalculate().
     * Returns true if we set the matrix (skip Figura's own recalc), false to fall through.
     */
    public static boolean overridePartMatrix(FiguraModelPart part, PartCustomization customization) {
        RenderContext context = CONTEXT.get();
        if (context == null || part == null || customization == null) {
            return false;
        }

        Mapping mapping = resolveMapping(part, context.owner());
        if (mapping == null) {
            return false;
        }

        OpenMatrix4f jointAbsolute = context.snapshot().resolve(mapping.joint());
        if (jointAbsolute == null) {
            return false;
        }

        Matrix4f jointFigura = toFiguraSpace(jointAbsolute);
        FiguraMat4 localMatrix = computeLocalMatrix(part, customization, mapping.kind());
        FiguraMat4 ancestorMatrix = accumulateAncestorChain(part);

        FiguraMat4 result = FiguraMat4.of();
        if (ancestorMatrix != null) {
            result.set(ancestorMatrix);
            result.invert();
            FiguraMat4 jointMat = FiguraMat4.of();
            jointMat.set(jointFigura);
            result.rightMultiply(jointMat);
        } else {
            result.set(jointFigura);
        }
        result.rightMultiply(localMatrix);

        customization.setMatrix(result);
        return true;
    }

    /**
     * Names of bend / sub-segment bones that we auto-bridge by part name when
     * no parent_type is set. These are full-body / bend rig names that don't
     * overlap with Figura's {@code parentType} primary segments — bridging
     * them by name doesn't double-stack a joint transform that the parent
     * type system would already apply.
     */
    private static final java.util.Set<String> AUTO_BRIDGE_NAMES = java.util.Set.of(
            // forearms / hands
            "lefthand", "righthand",
            "leftforearm", "rightforearm",
            "leftlowerarm", "rightlowerarm",
            "leftelbow", "rightelbow",
            "larmlower", "rarmlower",
            // shins / feet
            "leftfoot", "rightfoot",
            "leftshin", "rightshin",
            "leftlowerleg", "rightlowerleg",
            "leftknee", "rightknee",
            "llegLower".toLowerCase(), "rlegLower".toLowerCase(),
            // mixamo lower-arm / lower-leg explicit
            "mixamorig:leftforearm", "mixamorig:rightforearm",
            "mixamorig:leftleg", "mixamorig:rightleg",
            // rigify lower segments
            "forearm.l", "forearm.r",
            "shin.l", "shin.r",
            // VRM lower segments
            "j_bip_l_lowerarm", "j_bip_r_lowerarm",
            "j_bip_l_lowerleg", "j_bip_r_lowerleg"
    );

    private static Mapping resolveMapping(FiguraModelPart part, UUID owner) {
        // Parent-type match: explicit avatar maker intent — always bridge.
        String byParent = FighturaJointMap.byParentType(part.parentType, owner);
        if (byParent != null) {
            return guardSameJoint(part, owner, byParent, MatchKind.PARENT_TYPE);
        }
        // Name match: only auto-bridge bend bones, or names explicitly
        // registered via fightura:mapBone(...) by the avatar's Lua script.
        String byName = FighturaJointMap.byName(part.name, owner);
        if (byName == null || part.name == null) {
            return null;
        }
        String key = part.name.toLowerCase(java.util.Locale.ROOT);
        boolean autoBridgeable = AUTO_BRIDGE_NAMES.contains(key)
                || FighturaJointMap.isLuaMapped(owner, part.name);
        if (!autoBridgeable) {
            return null;
        }
        return guardSameJoint(part, owner, byName, MatchKind.NAME);
    }

    private static Mapping guardSameJoint(FiguraModelPart part, UUID owner, String joint, MatchKind kind) {
        FiguraModelPart cursor = part.parent;
        while (cursor != null) {
            String ancestorJoint = directJointFor(cursor, owner);
            if (ancestorJoint != null && ancestorJoint.equals(joint)) {
                return null;
            }
            cursor = cursor.parent;
        }
        return new Mapping(joint, kind);
    }

    private static String directJointFor(FiguraModelPart part, UUID owner) {
        String byParent = FighturaJointMap.byParentType(part.parentType, owner);
        if (byParent != null) {
            return byParent;
        }
        if (part.name == null) {
            return null;
        }
        String byName = FighturaJointMap.byName(part.name, owner);
        if (byName == null) {
            return null;
        }
        String key = part.name.toLowerCase(java.util.Locale.ROOT);
        if (!AUTO_BRIDGE_NAMES.contains(key) && !FighturaJointMap.isLuaMapped(owner, part.name)) {
            return null;
        }
        return byName;
    }

    private static FiguraMat4 computeLocalMatrix(FiguraModelPart part, PartCustomization customization, MatchKind kind) {
        if (kind == MatchKind.PARENT_TYPE) {
            // For parent-type matches, Figura has already mixed in vanilla model data.
            // Strip it so the local matrix is just the part's own transform.
            Boolean savedVanillaVisible = customization.vanillaVisible;
            part.resetVanillaTransforms();
            customization.vanillaVisible = savedVanillaVisible;
            customization.needsMatrixRecalculation = true;
            customization.recalculate();
            return customization.getPositionMatrix();
        }
        // Name match: compute local on a snapshot so the original is untouched.
        PartCustomization snapshot = new PartCustomization();
        customization.copyTo(snapshot);
        snapshot.needsMatrixRecalculation = true;
        snapshot.recalculate();
        return snapshot.getPositionMatrix();
    }

    /**
     * Accumulate the entire ancestor chain's positionMatrix, in the same
     * order Figura composes it via its customization stack. Returns null if
     * the part has no ancestors. The accumulation is root × ... × parent —
     * matching how Figura's PartCustomizationStack.modify() right-multiplies
     * each pushed customization.
     */
    private static FiguraMat4 accumulateAncestorChain(FiguraModelPart part) {
        if (part == null || part.parent == null) {
            return null;
        }
        java.util.Deque<FiguraModelPart> stack = new java.util.ArrayDeque<>();
        FiguraModelPart cursor = part.parent;
        while (cursor != null) {
            stack.push(cursor);
            cursor = cursor.parent;
        }
        FiguraMat4 result = FiguraMat4.of();
        boolean first = true;
        while (!stack.isEmpty()) {
            FiguraMat4 m = stack.pop().customization.getPositionMatrix();
            if (first) {
                result.set(m);
                first = false;
            } else {
                result.rightMultiply(m);
            }
        }
        return first ? null : result;
    }

    /**
     * Convert an Epic Fight matrix (block units) into Figura blockbench scale (×16 pixels).
     */
    private static Matrix4f toFiguraSpace(OpenMatrix4f epicFightMatrix) {
        Matrix4f mojang = OpenMatrix4f.exportToMojangMatrix(epicFightMatrix, null);
        return new Matrix4f().scale(16.0F).mul(mojang).scale(1.0F / 16.0F);
    }

    private record RenderContext(UUID owner, FighturaPoseCache.Snapshot snapshot) {
    }

    private record Mapping(String joint, MatchKind kind) {
    }

    private enum MatchKind { PARENT_TYPE, NAME }
}
