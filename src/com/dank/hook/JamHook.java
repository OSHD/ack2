package com.dank.hook;

import com.dank.DankEngine;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;

/**
 * Project: DankWise
 * Date: 20-02-2015
 * Time: 19:51
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public enum JamHook {


    GAME_STRINGS("GameStrings", null, new String[0]),

    /*    NODE("Node", null, "key", "next", "previous", "unlink"),*/
    NODE("Node", null,
            new MemberSpec("key",      Type.getDescriptor(long.class)    , MemberSpec.TYPE_FIELD   ),
            new MemberSpec("next",     MemberSpec.futureDesc(Services.NODE) , MemberSpec.TYPE_FIELD   ),
            new MemberSpec("previous", MemberSpec.futureDesc(Services.NODE) , MemberSpec.TYPE_FIELD   ),
            new MemberSpec("unlink",   "()V"                             , MemberSpec.TYPE_METHOD  )
    ),

    /*DUAL_NODE("DualNode", NODE, "dualNext", "dualPrevious", "unlinkDual"),*/
    DUAL_NODE("DualNode", NODE,
            new MemberSpec("dualNext",     MemberSpec.futureDesc(Services.DUALNODE) , MemberSpec.TYPE_FIELD  ),
            new MemberSpec("dualPrevious", MemberSpec.futureDesc(Services.DUALNODE) , MemberSpec.TYPE_FIELD  ),
            new MemberSpec("unlinkDual",   "()V"                                    , MemberSpec.TYPE_METHOD )
    ),

    /*ENTITY("Renderable", DUAL_NODE, "modelHeight"),*/
    ENTITY("Entity", DUAL_NODE,
            new MemberSpec("modelHeight", Type.getDescriptor(int.class) , MemberSpec.TYPE_FIELD )
    ),

    /*MODEL("Model", ENTITY, "trianglesA", "trianglesB", "trianglesC", "verticesX", "verticesY", "verticesZ"),*/
    MODEL("Model", ENTITY,
            new MemberSpec("trianglesA",  Type.getDescriptor(int[].class) , MemberSpec.TYPE_FIELD ),
            new MemberSpec("trianglesB",  Type.getDescriptor(int[].class) , MemberSpec.TYPE_FIELD ),
            new MemberSpec("trianglesC",  Type.getDescriptor(int[].class) , MemberSpec.TYPE_FIELD ),
            new MemberSpec("verticesX",   Type.getDescriptor(int[].class) , MemberSpec.TYPE_FIELD ),
            new MemberSpec("verticesY",   Type.getDescriptor(int[].class) , MemberSpec.TYPE_FIELD ),
            new MemberSpec("verticesZ",   Type.getDescriptor(int[].class) , MemberSpec.TYPE_FIELD )
    ),

    /*SPRITE("Sprite", null, new String[0]),*/
    SPRITE("Sprite", null, new String[0]),

    /*INTERFACE_NODE("InterfaceNode", NODE, "owner", "type"),*/
    INTERFACE_NODE(Services.IFACENODE, NODE,
            new MemberSpec("owner",  Type.getDescriptor(int.class) , MemberSpec.TYPE_FIELD ),
            new MemberSpec("type",   Type.getDescriptor(int.class) , MemberSpec.TYPE_FIELD )
    ),

    /*NODETABLE("NodeTable", null, "buckets", "index", "size"),*/
    NODETABLE(Services.NODETABLE, NODE,
            new MemberSpec("buckets", "[" + MemberSpec.futureDesc(Services.NODE) , MemberSpec.TYPE_FIELD ),
            new MemberSpec("index",   Type.getDescriptor(int.class)           , MemberSpec.TYPE_FIELD ),
            new MemberSpec("size",    Type.getDescriptor(int.class)           , MemberSpec.TYPE_FIELD )
    ),

    /*DEQUE("Deque", null, "head", "tail"),*/
    DEQUE(Services.DEQUE, null,
            new MemberSpec("head", MemberSpec.futureDesc(Services.NODE) , MemberSpec.TYPE_FIELD ),
            new MemberSpec("tail", MemberSpec.futureDesc(Services.NODE) , MemberSpec.TYPE_FIELD )
    ),

    /*BUFFER("Buffer", NODE, "payload", "caret"),*/
    BUFFER(Services.BUFFER, NODE,
            new MemberSpec("payload", Type.getDescriptor(byte[].class) , MemberSpec.TYPE_FIELD ),
            new MemberSpec("caret",   Type.getDescriptor(int.class)    , MemberSpec.TYPE_FIELD )
    ),

    /*PACKET_BUFFER("PacketBuffer", BUFFER, new String[0]),*/
    PACKET_BUFFER(Services.PACBUFFER, BUFFER, new String[0]),

    /*LANDSCAPE("Landscape", null, "tiles", "tempEntityMarkers", "addEntityMarker", "addBoundary",
            "addBoundaryDecoration", "addItemPile", "addTileDecoration"),*/
    LANDSCAPE(Services.LANDSCAPE, null,
            new MemberSpec("tiles", Type.getDescriptor(byte[].class) , MemberSpec.TYPE_FIELD ),
            new MemberSpec("caret",   Type.getDescriptor(int.class)    , MemberSpec.TYPE_FIELD )
    ),

    LANDSCAPE_TILE("LandscapeTile", null, "entityMarkers", "boundaryStub", "boundaryDecorationStub", "tileDecorationStub",
            "itemPile", "regionX", "regionY", "floorLevel"),

    BOUNDARY_STUB("BoundaryStub", null, "uid", "config", "strictX", "strictY", "entityA", "entityB",
            "orientationA", "orientationB", "floorLevel"),

    BOUNDARY_DECORATION_STUB("BoundaryDecorationStub", null, "uid", "config", "floorLevel", "insetX", "insetY",
            "orientationA", "orientationB", "entityA", "entityB", "strictX", "strictY"),

    TILE_STUB("TileDecorationStub", null, "uid", "config", "strictX", "strictY", "entity", "height"),

    ITEM_PILE("ItemPile", null, "uid", "bottom", "middle", "top", "counterHeight", "height", "strictX", "strictY"),

    ENTITY_MARKER("EntityMarker", null, "uid", "config", "strictX", "strictY", "entity", "floorLevel", "height",
            "regionX", "regionY", "sizeX", "sizeY", "orientation"),

    GRAPHICS_STUB("GraphicsStub", ENTITY, "finished", "id", "floorLevel", "regionX", "regionY", "height", "startCycle"),

    DYNAMIC_OBJECT("DynamicObject", ENTITY, "regionX", "regionY", "floorLevel", "id", "type", "orientation"),

    PROJECTILE("Projectile", ENTITY, "id", "strictX", "strictY", "startHeight", "loopCycle", "slope",
            "startDistance", "targetIndex", "endHeight"),

    CHARACTER("Character", null, "strictX", "strictY", "healthBarCycle", "hitpoints", "maxHitpoints", "animation",
            "targetIndex", "hitsplatCycles", "hitsplatDamages", "hitsplatTypes", "orientation", "overheadText",
            "queueTraversed", "queueSize", "queueX", "queueY"),

    NPC("Npc", CHARACTER, "definition"),

    PLAYER("Player", CHARACTER, "combatLevel"),

    NPC_DEFINITION("NpcDefinition", DUAL_NODE, "varpIndex", "varp32Index", "transformIds", "transform", "combatLevel", "id"),

    GAME_ENGINE("GameEngine", null, new String[0]),

    WORLD("World", null, "world", "index", "mask", "location", "domain", "activity", "population"),

    SCRIPT_EVENT("ScriptEvent", NODE, "args", "name"),

    RUNESCRIPT("RuneScript", DUAL_NODE, "intArgCount", "stringArgCount", "intStackCount", "stringStackCount", "opcodes",
            "intOperands", "stringOperands"),

    WIDGET("Widget", null, "actions", "name", "text", "textColor", "alpha", "textureId", "spriteId", "selectedAction",
            "fontId", "textShadowed", "borderThickness", "shadowColor", "flippedVertically", "flippedHorizontally", "modelType",
            "mouseEnterListener", "mouseExitListener", "renderListener", "scrollListener", "mouseHoverListener",
            "configListenerArgs", "configTriggers", "tableListenerArgs", "tableModTriggers", "skillListenerArgs",
            "skillTriggers", "modelId", "insetX", "insetY", "viewportWidth", "viewportHeight", "relativeX", "relativeY",
            "width", "height", "hidden", "index", "parentId", "id", "modelZoom", "rotationX", "rotationY", "rotationZ",
            "itemId", "itemQuantity", "boundsIndex", "loopCycle", "tableActions", "varpOpcodes", "itemIds", "itemQuantities",
            "interactable", "tooltip", "type", "contentType", "buttonType", "rowPadding", "columnPadding", "xSprites", "ySprites"),

    CLIENT("Client", GAME_ENGINE, "myPlayerIndex", "audioEffectCount", "cameraX", "cameraY", "cameraZ", "cameraYaw", "cameraPitch",
            "floorLevel", "npcIndices", "npcArray", "playerIndices", "playerArray", "myPlayer", "regionBaseX", "regionBaseY",
            "tempVars", "minimapSprite", "engineCycle", "menuActions", "menuNouns", "menuOpcodes", "menuArg0", "menuArg1", "menuArg2",
            "landscape", "worldCount", "worlds", "interfaceNodes", "minimapRotation", "minimapScale", "viewRotation", "interfaces",
            "energy", "weight", "onCursorUids", "onCursorCount", "cursorState", "playerActions", "itemSelectionStatus", "spellSelected",
            "selectedItemName", "selectedSpellName", "currentSpellTargets", "menuActionPrefix", "hintArrowX", "hintArrowY", "myWorld",
            "membersWorld", "friendListSize", "clanChatOwner", "clanChatName", "clanChatRank", "clanChatSize", "ignoreListSize",
            "myRights", "connectionState", "destinationX", "destinationY", "engineVars", "experiences", "levels", "currentLevels",
            "focused", "fps", "groundItemDeque", "friends", "clanMates", "ignoredPlayers", "graphicsObjectDeque", "projectileDeque",
            "interfaceNodes", "interfaceSpriteCache", "itemTables", "landscapeFrame", "processingInterface", "loadingInterface",
            "menuWidth", "menuHeight", "menuX", "menuY", "menuOpen", "minimapSprite", "previousFloorLevel", "tileHeights",
            "renderRules", "screenCenterX", "screenCenterY", "screenWidth", "screenHeight", "selectedItemIndex",
            "viewportBufferWidth", "viewportBufferHeight", "xViewportBuffer", "yViewportBuffer", "worldSelectorDisplayed",
            "getObjectDefinition", "getItemDefinition", "getNpcDefinition", "addMenuRow", "setWorld", "loadWorlds",
            "getRuneScript", "getVarpbit", "getWidgetConfig");

    public final Map<String, RSMember> hooks;
    private final String definedName;
    private final JamHook superType;
    private String internalName;
    public final Map<String, MemberSpec> specs;

    private JamHook(final String definedName, final JamHook superType, final String... keys) {
        this.definedName = definedName;
        this.superType = superType;
        this.hooks = new HashMap<>();
        this.specs = new HashMap<>();
        for (final String key : keys) {
            hooks.put(key, null);
        }
    }

    private JamHook(final String definedName, final JamHook superType, final MemberSpec... keys) {
        this.definedName = definedName;
        this.superType = superType;
        this.hooks = new HashMap<>();
        this.specs = new HashMap<>();
        for (final MemberSpec key : keys) {
            key.resolveRef(this);
            hooks.put(key.name, null);
            specs.put(key.name,key);
        }
    }

    public static JamHook resolve(ClassNode cn) {
        for (final JamHook hc : JamHook.values()) {
            if (hc.getInternalName() != null && hc.getInternalName().equals(cn.name)) {
                return hc;
            }
        }
        return null;
    }

    public static JamHook forName(String defName) {
        for (final JamHook hc : JamHook.values()) {
            if (hc.getDefinedName() != null && hc.getDefinedName().equals(defName)) {
                return hc;
            }
        }
        return null;
    }

    public void verify() {
       /* for(Map.Entry<String,MemberSpec> spec0 : specs.entrySet()) {
            String member = spec0.getKey();
            MemberSpec spec = spec0.getValue();
            RSMember resolved = get(member);
            spec.resolve();
            if(resolved != null) {
                if(!resolved.verify(spec)) {
                    System.out.println("Eeek:" + spec.name + "," + spec.desc);
                }
            }
        }*/
    }

    public void put(RSMember member) {
        hooks.put(member.mnemonic, member);
    }

    public RSMember get(final String key) {
        return hooks.get(key);
    }

    public void clear(final String key) {
        hooks.put(key, null);
    }

    public String getDefinedName() {
        return definedName;
    }

    public JamHook getSuperType() {
        return superType;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(final String internalName) {
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
            StringBuilder specbuilder = new StringBuilder();
            specbuilder.append("  ✓ ").append(spec.getKey());
            while (specbuilder.length() < 34) {
                specbuilder.append('.');
            }

            MemberSpec mspec = specs.get(spec.getKey());
            List<String> problems = null;
            if(spec.val != null && mspec != null) {
                mspec.resolve();
                problems = spec.val.verify(mspec);
            }

            if (spec.getValue() != null && (problems == null || problems.isEmpty())) {
                if (spec.getValue().desc.contains(")")) {
                    specbuilder.append(spec.getValue().owner).append('.').append(spec.getValue().name).append(spec.getValue().desc).append("\n");
                } else {
                    specbuilder.append(spec.getValue().owner).append('.').append(spec.getValue().name).append("\n");
                }
            } else {
                String s = specbuilder.toString();
                s = s.replace("✓", "✕");
                specbuilder = new StringBuilder(s);
                specbuilder.append("BROKEN").append("\n");
                if(problems != null) {
                    for(String problem : problems) {
                        specbuilder.append("\t\t|-> ").append(problem).append("\n");
                    }
                }
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
}
