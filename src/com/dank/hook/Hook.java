
package com.dank.hook;

import com.dank.DankEngine;
import com.dank.util.LockableHashMap;
import com.dank.util.deob.OpPredicateRemover;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;

/**
 * Project: RS3Injector
 * Time: 05:47
 * Date: 07-02-2015
 * Created by Dogerina.
 */

//
public enum Hook {



    GAME_STRINGS("GameStrings", null),

    GRAPHICS_ENGINE("Bitmap", null, "engineRastor::[I", "bitmap::Ljava/awt/Image;"),

    GAME_CANVAS("GameCanvas", null, "component::Ljava/awt/Component;"),

    PARAMETERS("Parameters", null, "key::Ljava/lang/String;"),

    NODE("Node", null, "key::J", "next::LNode;", "previous::LNode;", "unlink::()V"),

    DUAL_NODE("DualNode", NODE, "dualNext::LDualNode;", "dualPrevious::LDualNode;", "unlinkDual::()V"),

    ENTITY("Entity", DUAL_NODE, "modelHeight::I"),

    MODEL("Model", ENTITY, "verticesX::[I", "verticesY::[I", "verticesZ::[I", "XYZMag::I", "allowClickBounds::Z"),

    SPRITE("Sprite", null, "pixels::[I", "width::I", "height::I", "paddingX::I", "paddingY::I", "maxX::I", "maxY::I"),

    INTERFACE_NODE("InterfaceNode", NODE, "owner::I", "type::I"),

    NODETABLE("NodeTable", null, "buckets::[LNode;", "index::I", "size::I"),

    DEQUE("Deque", null, "head", "tail"),

    BUFFER("Buffer", NODE, "payload::[B", "caret::I", "crcTable::[I", "readInt::()I", "readUShort::()I", "readByte::()B", "applyRSA::(Ljava/math/BigInteger;Ljava/math/BigInteger;)V"),

    ISAAC_RANDOM("IsaacRandom", null, "val::()I", "isaac", "b", "c", "count"),

    PACKET_BUFFER("PacketBuffer", BUFFER, "bitMasks", "random", "bitCaret", "readHeader::(I)I", "writeHeader::(I)V", "readBits::(I)I"),

    GPI("GPI", null, "chatBuffer::LPacketBuffer;", "cachedAppearances::[LPacketBuffer;", "pendingFlagsIndices::[I", "pendingFlagsCount::I",
            "movementTypes::[B", "skipFlags::[B", "localPlayerCount::I", "localPlayerIndices::[I", "globalPlayerCount::I", "globalPlayerIndices::[I"),

    EXCHANGE_OFFER("ExchangeOffer", null, "status::B", "itemId::I", "price::I", "itemQuantity::I", "transferred::I", "spent::I"),

    VARPBIT("Varpbit", DUAL_NODE, "highBit::I", "lowBit::I", "varp::I"),

    CLAN_MATE("ClanMate", null, "displayName::Ljava/lang/String;", "rank::B", "world::I"),

    FRIENDED_PLAYER("FriendedPlayer", null, "displayName::Ljava/lang/String;", "previousName::Ljava/lang/String;", "world::I"),

    IGNORED_PLAYER("IgnoredPlayer", null, "displayName::Ljava/lang/String;", "previousName::Ljava/lang/String;"),

    ITEM_TABLE("ItemTable", NODE, "ids::[I", "quantities::[I"),

    LANDSCAPE("Landscape", null, "tiles::[[[LLandscapeTile;", "tempEntityMarkers::[LEntityMarker;", "addEntityMarker::null",
            "addBoundary::null", "addBoundaryDecoration::null", "addItemPile::null", "addTileDecoration::null",
            "render","addTempEntity","visibilityMap"),

    LANDSCAPE_TILE("LandscapeTile", null, "entityMarkers::[LEntityMarker;", "boundaryStub::LBoundaryStub;",
            "boundaryDecorationStub::LBoundaryDecorationStub;", "tileDecorationStub::LTileDecorationStub;",
            "itemPile::LItemPile;", "regionX::I", "regionY::I", "floorLevel::I"),

    BOUNDARY_STUB("BoundaryStub", null, "uid::I", "config::I", "strictX::I", "strictY::I", "entityA::LRenderable;", "entityB::LRenderable;",
            "orientationA::I", "orientationB::I", "height::I"),

    BOUNDARY_DECORATION_STUB("BoundaryDecorationStub", null, "uid::I", "config::I", "height::I", "insetX::I", "insetY::I",
            "orientationA::I", "orientationB::I", "entityA::LRenderable;", "entityB::LRenderable;", "strictX::I", "strictY::I"),

    TILE_STUB("TileDecorationStub", null, "uid::I", "config::I", "strictX::I", "strictY::I", "entity::LRenderable;", "height::I"),

    ITEM_PILE("ItemPile", null, "uid::I", "bottom::LRenderable;", "middle::LRenderable;", "top::LRenderable;",
            "counterHeight::I", "height::I", "strictX::I", "strictY::I"),

    ENTITY_MARKER("EntityMarker", null, "uid::I", "config::I", "strictX::I", "strictY::I", "entity::LRenderable;",
            "floorLevel::I", "height::I", "regionX::I", "regionY::I", "maxX::I", "maxY::I", "orientation::I"),

    GRAPHICS_STUB("GraphicsStub", ENTITY, "finished::Z", "id::I", "floorLevel::I", "regionX::I", "regionY::I", "height::I", "startCycle::I"),

    DYNAMIC_OBJECT("DynamicObject", ENTITY, "regionX::I", "regionY::I", "floorLevel::I", "id::I", "type::I", "orientation::I"),

    PROJECTILE("Projectile", ENTITY, "id::I", "strictX::I", "strictY::I", "startHeight::I", "loopCycle::I",
            "slope::I", "startDistance::I", "targetIndex::I", "endHeight::I"),

    CHARACTER("Character", ENTITY, "strictX::I", "strictY::I", "healthBarCycle::I", "hitpoints::I", "maxHitpoints::I",
            "animation::I", "targetIndex::I", "hitsplatCycles::[I", "hitsplatDamages::[I", "hitsplatTypes::[I",
            "orientation::I", "overheadText::Ljava/lang/String;", "queueSize::I", "npcBoundDim::I",
            "npcDegToTurn::I", "walkAnimation::I", "npcTurnAround::I", "npcTurnRight::I", "npcTurnLeft::I",
            "idleAnimation::I", "getNextAnimation::I", "unknown7::I", "modelHeight::I", "runAnimation::I",
            "animFrameId::I", "interAnimFrameId::I", "interAnimId::I"),

    NPC("Npc", CHARACTER, "definition::LNpcDefinition;"),

    PLAYER("Player", CHARACTER, "combatLevel::I", "name::Ljava/lang/String;", "prayerIcon::I", "skullIcon::I",
            "team::I", "config::LPlayerConfig;", "totalLevel::I","height::I"),

    GROUND_ITEM("GroundItem", ENTITY, "id::I", "quantity::I"),

    NPC_DEFINITION("NpcDefinition", DUAL_NODE, "varpIndex::I", "varp32Index::I", "transformIds::[I",
            "transform::()LNpcDefinition;", "combatLevel::I", "id::I", "colors::[S", "modifiedColors::[S",
            "name::Ljava/lang/String;", "actions::[Ljava/lang/String;"),

    OBJECT_DEFINITION("ObjectDefinition", DUAL_NODE, "name::Ljava/lang/String;", "actions::[Ljava/lang/String;",
            "transformIds::[I", "transform::()LObjectDefinition;", "id::I", "varpIndex::I", "varp32Index::I",
            "sizeX::I", "sizeY::I", "colors::[S", "modifiedColors::[S", "clipType::I", "modelClipped::Z",
            "clipped::Z", "mapFunction::I"),

    ITEM_DEFINITION("ItemDefinition", DUAL_NODE, "name::Ljava/lang/String;", "actions::[Ljava/lang/String;",
            "groundActions::[Ljava/lang/String;", "id::I", "notedId::I", "unnotedId::I", "storeValue::I",
            "stackable::I", "colors::[S", "modifiedColors::[S"),

    PLAYER_CONFIG("PlayerConfig", null, "appearance::[I", "appearanceColors::[I", "female::Z", "npcId::I"),

    GAME_ENGINE("GameEngine", null, "shell::LGameEngine;", "focused::Z"),

    WORLD("World", null, "world::I", "index::I", "mask::I", "location::I", "domain::Ljava/lang/String;",
            "activity::Ljava/lang/String;", "population::I"),

    SCRIPT_EVENT("ScriptEvent", NODE, "args::[I", "opbase::Ljava/lang/String;"),

    KEY_FOCUS_LISTENER("KeyFocusListener", null),

    RUNESCRIPT("RuneScript", DUAL_NODE, "intArgCount::I", "stringArgCount::I", "intStackCount::I",
            "stringStackCount::I", "opcodes::[[I", "intOperands::[I", "stringOperands::[Ljava/lang/String;"),

    WIDGET("Widget", null, "actions::[Ljava/lang/String;", "name::Ljava/lang/String;", "text::Ljava/lang/String;",
            "textColor::I", "alpha::I", "textureId::I", "spriteId::I", "selectedAction::Ljava/lang/String;",
            "config::I", "fontId::I", "textShadowed::Z", "borderThickness::I", "shadowColor::I",
            "flippedVertically::Z", "flippedHorizontally::Z", "modelType::I", "mouseEnterListener::[Ljava/lang/Object;",
            "mouseExitListener::[Ljava/lang/Object;", "renderListener::[Ljava/lang/Object;", "scrollListener::[Ljava/lang/Object;",
            "mouseHoverListener::[Ljava/lang/Object;", "configListenerArgs::[Ljava/lang/Object;",
            "configTriggers::[I", "tableListenerArgs::[Ljava/lang/Object;", "tableModTriggers::[I",
            "skillListenerArgs::[Ljava/lang/Object;", "skillTriggers::[I", "modelId::I", "insetX::I", "insetY::I",
            "viewportWidth::I", "viewportHeight::I", "relativeX::I", "relativeY::I", "width::I", "height::I",
            "hidden::Z", "index::I", "parentId::I", "id::I", "modelZoom::I", "rotationX::I", "rotationY::I",
            "rotationZ::I", "itemId::I", "itemQuantity::I", "boundsIndex::I", "loopCycle::I",
            "tableActions::Ljava/lang/String;", "varpOpcodes::[[I", "itemIds::[I", "itemQuantities::[I",
            "interactable::Z", "tooltip::Ljava/lang/String;", "type::I", "contentType::I", "buttonType::I",
            "rowPadding::I", "columnPadding::I", "xSprites::[I", "ySprites::[I",
            "parent::LWidget;", "children::[LWidget;"),

    CLIENT("Client", GAME_ENGINE, "myPlayerIndex::I", "audioEffectCount::I", "cameraX::I", "cameraY::I",
            "cameraZ::I", "cameraYaw::I", "cameraPitch::I", "floorLevel::I", "npcIndices::[I",
            "npcArray::[LNpc;", "playerArray::[LPlayer;", "myPlayer::LPlayer;",
            "regionBaseX::I", "regionBaseY::I", "tempVars::[I", "minimapSprite::LSprite;", "engineCycle::I",
            "menuActions::[Ljava/lang/String;", "menuNouns::[Ljava/lang/String;", "menuOpcodes::[I",
            "menuArg0::[I", "menuArg1::[I", "menuArg2::[I", "landscape::LLandscape;", "worldCount::I",
            "worlds::[LWorld;", "interfaceNodes::LHashTable;", "minimapRotation::I", "minimapScale::I",
            "viewRotation::I", "interfaces::[[LWidget;", "energy::I", "weight::I", "onCursorUids::[I",
            "onCursorCount::I", "cursorState::I", "playerActions::[Ljava/lang/String;", "itemSelectionStatus::I",
            "spellSelected::Z", "selectedItemName::Ljava/lang/String;", "selectedSpellName::Ljava/lang/String;",
            "currentSpellTargets::I", "menuActionPrefix::Ljava/lang/String;", "hintArrowX::I", "hintArrowY::I",
            "myWorld::I", "membersWorld::Z", "friendListSize::I", "clanChatOwner::Ljava/lang/String;",
            "clanChatName::Ljava/lang/String;", "clanChatRank::B", "clanChatSize::I", "ignoreListSize::I",
            "myRights::I", "connectionState::I", "destinationX::I", "destinationY::I", "engineVars::[I",
            "experiences::[I", "levels::[I", "currentLevels::[I", "fps::I", "groundItemDeque::[[[LDeque;",
            "friendedPlayers::[LFriendedPlayer;", "clanMates::[LClanMate;", "ignoredPlayers::[LIgnoredPlayer;",
            "graphicsObjectDeque::LDeque;", "projectileDeque::LDeque;", "interfaceNodes::LHashTable;",
            "itemTables::LHashTable;", "menuWidth::I", "menuHeight::I", "menuX::I", "menuY::I", "menuOpen::Z",
            "tileHeights::[[[I", "renderRules::[[[B", "screenCenterX::I", "screenCenterY::I", "selectedItemIndex::I",
            "xViewportBuffer::[I", "yViewportBuffer::[I", "worldSelectorDisplayed::Z",
            "localExchangeOffers::[LExchangeOffer;", "packet::LPacket;", "menuItemCount::I", "focused::Z",
            "screenZoom::I", "screenWidth::I", "screenHeight::I", "getObjectDefinition::(I)LObjectDefinition;",
            "getItemDefinition::(I)LItemDefinition;", "getNpcDefinition::(I)LNpcDefinition;", "addMenuRow",
            "setWorld::(I)V", "loadWorlds::()V", "getRuneScript::(I)LRuneScript;", "getVarpbit::(I)LVarpbit;",
            "getWidgetConfig::(I)I", "getItemSprite", "bootState::I", "cacheDirectory::Ljava/io/File;",
            "cacheLocation::Ljava/io/File;", "processFrames::()V", "processLogin::()V", "processLogic::()V",
            "colorsToFind::[S", "colorsToReplace::[[S", "colorsToFind1::[S", "colorsToReplace1::[[S", "getKeyFocusListener::LKeyFocusListener;");

    public final Map<String, RSMember> hooks;
    public final Map<String, String> hookToDesc;
    private final String definedName;
    private final Hook superType;
    private String internalName;
    private List<String> warnings = new ArrayList<>();

    private static Remapper MAPPER = HookMap.INSTANCE;

    Hook(final String definedName, final Hook superType, final String... keys) {
        this.definedName = definedName;
        this.superType   = superType;
        String[] keys0 = new String[keys.length];
        String[] desc0 = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            int idx = key.indexOf(':');
            if (idx < 0) {
                keys0[i] = key;
                desc0[i] = "Undefined";
                continue;
            }
            keys0[i] = key.substring(0, idx);
            desc0[i] = key.substring(key.lastIndexOf(':') + 1);
        }
        this.hooks = new LockableHashMap<>(keys0);
        this.hookToDesc = new HashMap<>();
        for (int i = 0; i < keys0.length; i++)
            hookToDesc.put(keys0[i], desc0[i]);
    }


    public static RSMember resolve(final String key) {
        for (final Hook hc : Hook.values()) {
            for (final RSMember mem : hc.getIdentifiedSet()) {
                if (mem.key().equals(key))
                    return mem;
            }
        }
        return null;
    }

    public static Hook resolve(ClassNode cn) {
        for (final Hook hc : Hook.values()) {
            if (hc.getInternalName() != null && hc.getInternalName().equals(cn.name)) {
                return hc;
            }
        }
        return null;
    }

    public static Hook forName(String defName) {
        for (final Hook hc : Hook.values()) {
            if (hc.getDefinedName() != null && hc.getDefinedName().equals(defName)) {
                return hc;
            }
        }
        return null;
    }

    private void warn(String msg) {
        warnings.add(definedName + ":" + msg);
    }

    public void put(RSMember member) {
        RSMember old = hooks.get(member.mnemonic);
        if(old != null && !old.equals(member)) {
            String h = member.isField() ? "Field" : "Method";
            warn("Overwrite of " + h + " '" + member.mnemonic + "' [" + old.key() + " ==> " + member.key() + "]");
        }
        if(!hooks.containsKey(member.mnemonic)) {
            String h = member.isField() ? "Field" : "Method";
            warn(h + " '" + member.mnemonic + "' is not defined.");
        }
        hooks.put(member.mnemonic, member);
    }

    private static String popPred(String methodDesc) {
        Type[] args = Type.getArgumentTypes(methodDesc);
        Type ret = Type.getReturnType(methodDesc);
        Type[] args0 = Arrays.copyOf(args,args.length-1);
        return Type.getMethodDescriptor(ret,args0);
    }

    public void verify() {
        for(RSMember mem : hooks.values()) {
            if(mem == null) continue;
            String desc0 = mapDesc(mem.desc);
            String desc  = hookToDesc.get(mem.mnemonic);
            if(desc == null) continue; // undefined member
            if(mem.isMethod()) {
                boolean has_pred = OpPredicateRemover.hasPred(mem.owner,mem.name,mem.desc);
                if(has_pred) desc0 = popPred(desc0);
            }
            if(!desc.equals(desc0)) {
                String h = mem.isField() ? "Field" : "Method";
                warn( h + " '" + mem.mnemonic + "' has a unexpected descriptor " + "(defined=" + desc + ",resolved=" + desc0 + ")");
            }
        }

        for(RSMember mem : hooks.values()) {
            if(mem == null) continue;
            for(RSMember mem0 : hooks.values()) {
                if(mem0 == null) continue;
                if(mem == mem0) continue;
                if(mem.equals(mem0)) {
                    String h = mem.isField() ? "Fields" : "Methods";
                    warn(h + " " + mem.mnemonic + " and " + mem0.mnemonic + " are equal.");
                }
            }
        }
    }

    public List<String> getWarnings() {
        return warnings;
    }

    private static String mapMethodDesc(String desc) {
        return MAPPER.mapMethodDesc(desc);
    }

    private static String mapDesc(String desc) {
        if(desc.charAt(0) == '(') return mapMethodDesc(desc);
        return MAPPER.mapDesc(desc); //Map object
    }

    private static String map(String typeName) {
        return MAPPER.map(typeName);
    }





    public RSMember get(final String key) {
        return hooks.get(key);
    }

    public RSMethod getMethod(String key) {
        return (RSMethod) get(key);
    }

    public void clear(final String key) {
        hooks.put(key, null);
    }

    public String getDefinedName() {
        return definedName;
    }

    public Hook getSuperType() {
        return superType;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(final String internalName) {
        if(this.internalName != null && !this.internalName.equals(internalName)) {
            warn("TypeName override (" + this.internalName + " ==> " + internalName + ")");
        }
        this.internalName = internalName;
    }

    public String getInternalDesc() {
        return 'L' + internalName + ';';
    }

    public ClassNode resolve() {
        return DankEngine.classPath.get(getInternalName());
    }

    public String getInternalArrayDesc(final int dims) {
        String s = "";
        for (int i = 0; i < dims; i++) {
            s += '[';
        }
        return s.concat('L' + internalName + ';');
    }

    public String getInternalArrayDesc() {
        return getInternalArrayDesc(1);
    }

    @Override
    public String toString() {
        if (internalName == null) return definedName + " := BROKEN\n";
        final StringBuilder sb = new StringBuilder(definedName + " := " + internalName);
        if (superType != null) {
            sb.append(" extends ").append(superType.definedName);
        }
        sb.append("\n");

        class Thing implements Comparable<Thing> {
            String key;
            RSMember val;

            Thing(String key, RSMember val) {
                this.key = key;
                this.val = val;
            }

            public String getKey() {
                return key;
            }

            public RSMember getValue() {
                return val;
            }

            @Override
            public int compareTo(Thing o) {
                return String.CASE_INSENSITIVE_ORDER.compare(key, o.key);
            }
        }

        List<Thing> members = new ArrayList<>(hooks.size());
        for (Map.Entry<String, RSMember> spec : hooks.entrySet()) {
            members.add(new Thing(spec.getKey(), spec.getValue()));
        }
        Collections.sort(members);

        for (final Thing spec : members) {
            int max = 0;
            for(Thing thing : members) {
                if(thing.getValue() == null) continue;
                if(thing.getValue().desc.contains(")")) continue;
                String k = thing.getValue().owner + "" + thing.getValue().name;
                if(k.length()>max) max = k.length();
            }
            max--;

            StringBuilder specbuilder = new StringBuilder();
            specbuilder.append("  " + '\u2713' + " ").append(spec.getKey());
            while (specbuilder.length() < 34) {
                specbuilder.append('.');
            }
            if (spec.getValue() != null) {
                if (spec.getValue().desc.contains(")")) {
                    specbuilder.append(spec.getValue().owner).append('.').append(spec.getValue().name).append(spec.getValue().desc);
                    RSMember m = spec.val;
                    String domain = OpPredicateRemover.getDomain(m.owner,m.name,m.desc);
                    if(domain!=null) {
                        specbuilder.append(" " + '\u03B5' + " ").append(domain);
                    }
                } else {
                    specbuilder.append(spec.getValue().owner).append('.').append(spec.getValue().name);
                    if (spec.getValue().desc.equals("I")) {
//                        final Number n = MultiplierVisitor.getDecoder(spec.getValue().owner.concat(".").concat(spec.getValue().name));
                       final Number n = DankEngine.resolver.getDecoder(spec.getValue().owner, spec.getValue().name, spec.getValue().desc);
//                       final Number n = MultiplierFinder.findMultiplier(spec.getValue().owner, spec.getValue().name);
//                       final Number n = Multiplier.getMultiple(spec.getValue().owner+ "."+ spec.getValue().name);
                        int k = spec.getValue().owner.length() + spec.val.name.length();
                        while (k++<max) specbuilder.append(' ');
                        specbuilder.append(" x ")/*.append(i)*/.append(n != null ? n : "DNE");
                    }

                }
                specbuilder.append("\n");
            } else {
                String s = specbuilder.toString();
                s = s.replace("\u2713", "\u2715");
                specbuilder = new StringBuilder(s);
                specbuilder.append("BROKEN").append("\n");
            }
            sb.append(specbuilder);
        }
        return sb.toString();
    }

    public Set<RSMember> getIdentifiedSet() {
        final Set<RSMember> set = new HashSet<>();
        for (final RSMember spec : hooks.values()) {
            if (spec == null) continue;
            set.add(spec);
        }
        return set;
    }

    public void clearAll() {
        hooks.clear();
    }

}