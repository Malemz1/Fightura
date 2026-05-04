package com.fightura.fightura.client;

import java.util.Set;
import java.util.UUID;
import org.figuramc.figura.model.ParentType;

/**
 * Figura-aware façade over {@link BoneAliasTable}. Resolves a Figura
 * {@code parentType} or part name to an Epic Fight joint, with per-avatar
 * Lua overrides taking precedence over built-in aliases.
 */
public final class FighturaJointMap {
    private FighturaJointMap() {
    }

    public static String byParentType(ParentType parentType, UUID owner) {
        return parentType == null ? null : BoneAliasTable.resolve(parentType.name(), owner);
    }

    public static String byName(String name, UUID owner) {
        return BoneAliasTable.resolve(name, owner);
    }

    public static boolean isLuaMapped(UUID owner, String name) {
        return BoneAliasTable.isLuaMapped(owner, name);
    }

    public static void map(UUID owner, String alias, String joint) {
        BoneAliasTable.map(owner, alias, joint);
    }

    public static void clear(UUID owner, String alias) {
        BoneAliasTable.clear(owner, alias);
    }

    public static void clearAll(UUID owner) {
        BoneAliasTable.clearAll(owner);
    }

    public static void clearAll() {
        BoneAliasTable.clearAll();
    }

    public static Set<String> supportedAliases() {
        return BoneAliasTable.supportedAliases();
    }
}
