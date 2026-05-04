package com.fightura.fightura.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BoneAliasTableTest {

    @AfterEach
    void wipeOverrides() {
        BoneAliasTable.clearAll();
    }

    @Nested
    @DisplayName("Built-in aliases")
    class BuiltIns {
        @Test
        void primaryParentTypesResolve() {
            assertEquals("Head", BoneAliasTable.resolve("Head", null));
            assertEquals("Chest", BoneAliasTable.resolve("Body", null));
            assertEquals("Arm_L", BoneAliasTable.resolve("LeftArm", null));
            assertEquals("Arm_R", BoneAliasTable.resolve("RightArm", null));
            assertEquals("Thigh_L", BoneAliasTable.resolve("LeftLeg", null));
            assertEquals("Thigh_R", BoneAliasTable.resolve("RightLeg", null));
        }

        @Test
        void wearableParentTypesResolveToBodyJoints() {
            assertEquals("Chest", BoneAliasTable.resolve("Cape", null));
            assertEquals("Chest", BoneAliasTable.resolve("LeftElytra", null));
            assertEquals("Chest", BoneAliasTable.resolve("RightElytra", null));
            assertEquals("Hand_L", BoneAliasTable.resolve("LeftItemPivot", null));
            assertEquals("Hand_R", BoneAliasTable.resolve("RightItemPivot", null));
            assertEquals("Head", BoneAliasTable.resolve("HelmetItemPivot", null));
        }

        @Test
        void englishLowerArmAliases() {
            assertEquals("Hand_L", BoneAliasTable.resolve("LeftHand", null));
            assertEquals("Hand_L", BoneAliasTable.resolve("LeftForearm", null));
            assertEquals("Hand_L", BoneAliasTable.resolve("LeftElbow", null));
            assertEquals("Hand_L", BoneAliasTable.resolve("LArmLower", null));
            assertEquals("Hand_R", BoneAliasTable.resolve("RArmLower", null));
        }

        @Test
        void mixamoAliases() {
            assertEquals("Pelvis", BoneAliasTable.resolve("mixamorig:Hips", null));
            assertEquals("Arm_L", BoneAliasTable.resolve("mixamorig:LeftArm", null));
            assertEquals("Hand_L", BoneAliasTable.resolve("mixamorig:LeftForeArm", null));
            assertEquals("Thigh_L", BoneAliasTable.resolve("mixamorig:LeftUpLeg", null));
            assertEquals("Leg_L", BoneAliasTable.resolve("mixamorig:LeftLeg", null));
        }

        @Test
        void blenderRigifyAliases() {
            assertEquals("Arm_L", BoneAliasTable.resolve("upper_arm.L", null));
            assertEquals("Hand_L", BoneAliasTable.resolve("forearm.L", null));
            assertEquals("Thigh_L", BoneAliasTable.resolve("thigh.L", null));
            assertEquals("Leg_R", BoneAliasTable.resolve("shin.R", null));
            assertEquals("Chest", BoneAliasTable.resolve("spine.002", null));
        }

        @Test
        void vrmVroidAliases() {
            assertEquals("Pelvis", BoneAliasTable.resolve("J_Bip_C_Hips", null));
            assertEquals("Arm_L", BoneAliasTable.resolve("J_Bip_L_UpperArm", null));
            assertEquals("Hand_R", BoneAliasTable.resolve("J_Bip_R_LowerArm", null));
            assertEquals("Leg_L", BoneAliasTable.resolve("J_Bip_L_Foot", null));
        }

        @Test
        void resolutionIsCaseInsensitive() {
            assertEquals("Head", BoneAliasTable.resolve("HEAD", null));
            assertEquals("Head", BoneAliasTable.resolve("head", null));
            assertEquals("Head", BoneAliasTable.resolve("HeAd", null));
            assertEquals("Arm_L", BoneAliasTable.resolve("LEFTARM", null));
        }

        @Test
        void unknownNameReturnsNull() {
            assertNull(BoneAliasTable.resolve("NotABone", null));
            assertNull(BoneAliasTable.resolve("", null));
        }

        @Test
        void nullInputReturnsNull() {
            assertNull(BoneAliasTable.resolve(null, null));
        }
    }

    @Nested
    @DisplayName("Per-avatar overrides")
    class Overrides {
        @Test
        void mapAddsCustomAlias() {
            UUID owner = UUID.randomUUID();
            BoneAliasTable.map(owner, "MyTail", "Pelvis");
            assertEquals("Pelvis", BoneAliasTable.resolve("MyTail", owner));
        }

        @Test
        void overrideWinsOverBuiltIn() {
            UUID owner = UUID.randomUUID();
            BoneAliasTable.map(owner, "Head", "Chest");
            assertEquals("Chest", BoneAliasTable.resolve("Head", owner));
            // Built-in still applies for other avatars.
            assertEquals("Head", BoneAliasTable.resolve("Head", null));
            assertEquals("Head", BoneAliasTable.resolve("Head", UUID.randomUUID()));
        }

        @Test
        void overrideIsCaseInsensitive() {
            UUID owner = UUID.randomUUID();
            BoneAliasTable.map(owner, "MyHorn", "Head");
            assertEquals("Head", BoneAliasTable.resolve("MYHORN", owner));
            assertEquals("Head", BoneAliasTable.resolve("myhorn", owner));
        }

        @Test
        void clearRemovesSingleOverride() {
            UUID owner = UUID.randomUUID();
            BoneAliasTable.map(owner, "MyTail", "Pelvis");
            BoneAliasTable.map(owner, "MyHorn", "Head");
            BoneAliasTable.clear(owner, "MyTail");
            assertNull(BoneAliasTable.resolve("MyTail", owner));
            assertEquals("Head", BoneAliasTable.resolve("MyHorn", owner));
        }

        @Test
        void clearAllForOwnerRemovesAllItsOverrides() {
            UUID a = UUID.randomUUID();
            UUID b = UUID.randomUUID();
            BoneAliasTable.map(a, "MyTail", "Pelvis");
            BoneAliasTable.map(b, "MyTail", "Spine");
            BoneAliasTable.clearAll(a);
            assertNull(BoneAliasTable.resolve("MyTail", a));
            assertEquals("Spine", BoneAliasTable.resolve("MyTail", b));
        }

        @Test
        void clearingOverrideRevertsToBuiltIn() {
            UUID owner = UUID.randomUUID();
            BoneAliasTable.map(owner, "Head", "Chest");
            assertEquals("Chest", BoneAliasTable.resolve("Head", owner));
            BoneAliasTable.clear(owner, "Head");
            assertEquals("Head", BoneAliasTable.resolve("Head", owner));
        }

        @Test
        void emptyAliasOrJointIsIgnored() {
            UUID owner = UUID.randomUUID();
            BoneAliasTable.map(owner, "", "Head");
            BoneAliasTable.map(owner, "MyHorn", "");
            BoneAliasTable.map(owner, "  ", "  ");
            assertNull(BoneAliasTable.resolve("", owner));
            assertNull(BoneAliasTable.resolve("MyHorn", owner));
        }

        @Test
        void nullArgumentsAreIgnored() {
            BoneAliasTable.map(null, "x", "Head");
            BoneAliasTable.map(UUID.randomUUID(), null, "Head");
            BoneAliasTable.map(UUID.randomUUID(), "x", null);
            // No exception thrown is the assertion; resolve still works for built-ins.
            assertEquals("Head", BoneAliasTable.resolve("Head", null));
        }
    }

    @Nested
    @DisplayName("Supported alias listing")
    class SupportedAliases {
        @Test
        void includesPrimaryParentTypes() {
            assertTrue(BoneAliasTable.supportedAliases().contains("head"));
            assertTrue(BoneAliasTable.supportedAliases().contains("body"));
            assertTrue(BoneAliasTable.supportedAliases().contains("leftarm"));
        }

        @Test
        void includesMixamoMixinNames() {
            assertTrue(BoneAliasTable.supportedAliases().contains("mixamorig:hips"));
            assertTrue(BoneAliasTable.supportedAliases().contains("mixamorig:leftforearm"));
        }

        @Test
        void includesRigifyDottedNames() {
            assertTrue(BoneAliasTable.supportedAliases().contains("upper_arm.l"));
            assertTrue(BoneAliasTable.supportedAliases().contains("spine.002"));
        }
    }
}
