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
        FiguraMat4 ancestorMatrix = findAncestorEffective(part.parent, context.owner());

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

    private static Mapping resolveMapping(FiguraModelPart part, UUID owner) {
        String byParent = FighturaJointMap.byParentType(part.parentType, owner);
        if (byParent != null) {
            return new Mapping(byParent, MatchKind.PARENT_TYPE);
        }
        String byName = FighturaJointMap.byName(part.name, owner);
        if (byName != null) {
            return new Mapping(byName, MatchKind.NAME);
        }
        return null;
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

    private static FiguraMat4 findAncestorEffective(FiguraModelPart cursor, UUID owner) {
        while (cursor != null) {
            if (resolveMapping(cursor, owner) != null) {
                return cursor.customization.getPositionMatrix();
            }
            cursor = cursor.parent;
        }
        return null;
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
