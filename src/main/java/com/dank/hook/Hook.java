
package com.dank.hook;

import com.dank.DankEngine;
import com.dank.util.LockableHashMap;
import com.dank.util.Wildcard;
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
public enum Hook {
	CLIENT_ERROR("ClientError", null, "message::Ljava/lang/String;", "source::Ljava/lang/Throwable;"),
	RUNNABLE_TASK("RunnableTask", null, "active::Z", "thread::Ljava/lang/Thread;", "eventQueue::Ljava/awt/EventQueue;"),
    NODE("Node", null, "key::J", "next::LNode;", "previous::LNode;", "unlink::()V", "isParent::()Z"),
    DEQUE("Deque", null, "head::LNode;", "tail::LNode;"),
    DUAL_NODE("DualNode", NODE, "dualNext::LDualNode;", "dualPrevious::LDualNode;", "unlinkDual::()V"),
    QUEUE("Queue", null, "head::LDualNode;", "remove::()LDualNode;", "getFirst::()LDualNode;", "reset::()V", "putFirst::(LDualNode;)V", "putLast::(LDualNode;)V"),
    BITMAP("Bitmap", null, "drawGraphics::(?)V", "width::I", "height::I", "pixels::[I", "image::Ljava/awt/Image;"),
    ENTITY("Entity", DUAL_NODE, "getAnimatedModel::(?)LModel;", "renderAtPoint::(IIIIIIIII)V", "modelHeight::I"),
    HUD("HUD", NODE, "owner::I", "type::I", "isMainHud::Z"),
    HASHTABLE("HashTable", null, "buckets::[LNode;", "index::I", "size::I", "head::LNode;", "tail::LNode;", "put::(LNode;J)V", "get::(J)LNode;", "next::()LNode;", "resetIndex::()LNode;", "clear::()V"),
    ISAAC_CIPHER("IsaacCipher", null, "count::I", "counter::I", "lastResult::I", "accumulator::I", "results::[I", "memory::[I", "next::()I", "initializeKeySet::()V", "decrypt::()V"),
    LOOKUP_TABLE("LookupTable", null, "lookupIdentifier::(?)I", "identityTable::[I"),
    REFERENCE_TABLE("ReferenceTable", null, "prepareChildBuffers::(I[I)Z", "unpackTable::([B)V", "filesCompleted::()Z",
    		"clearChildBuffer::(I)V", "clearChildBuffers::()V", "getChildBufferLengthAtIndex::(I?)I", "getChildBufferLength::(?)I",
    		"getFile::(I)[B", "getFile2::(II)[B", "getFile3::(II[I)[B", "getFile4::(II?)[B", "getFile5::(I)[B",
    		"hasFileLoaded::(Ljava/lang/String;Ljava/lang/String;?)Z", "getChildIndices::(I?)[I",
    		"getFileBytes::(Ljava/lang/String;Ljava/lang/String;?)[B", "getEntryIdentifier::(Ljava/lang/String;?)I", "getChildIdentifier::(ILjava/lang/String;?)I",
    		"hasFileBuffer::(I)Z", "hasEntryBuffer::(II)Z", "loadBuffer::(I?)V",
    		"childIdentifiers::[[I", "entryCrcs::[I", "entryIndices::[I", "entryChildCounts::[I", "entryIdentifiers::[I",
    		"childIndexCounts::[I", "entryIndexCount::I", "childBuffers::[[Ljava/lang/Object;", "entryBuffers::[Ljava/lang/Object;",
    		"childIdentityTables::[LLookupTable;", "entryIdentityTable::LLookupTable;", "childIndices::[[I", "discardUnpacked::I",
    		"discardEntryBuffers::Z", "encrypted::Z"),
    FILE_ON_DISK("FileOnDisk", null, "read::([BII)I", "write::([BII)V", "close::()V", "getLength::(?)J", "seek::(J)V", "length::J", "position::J", "file::Ljava/io/RandomAccessFile;"),
    SEEKABLE_FILE("SeekableFile", null, "getFileLength::(?)J", "setPosition::(J)V", "readBytes::([B?)V", "readSection::([BII)V", "writeBytes::([BII)V",
    		"close::(?)V", "finalizeWrite::()V", "finalizeRead::()V",
    		"fileLength::J", "position::J", "file::LFileOnDisk;"),
    CACHE_FILE("CacheFile", null, "writeFile::(I[BI)Z", "writeFileParts::(I[BIZ)Z", "readFile::(I)[B", 
    		"indexFile::LSeekableFile;", "dataFile::LSeekableFile;", "length::I", "cacheId::I"),
    REMOTE_FILE_TABLE("RemoteFileTable", REFERENCE_TABLE, "loadBuffer::(I?)V", "processChildRequests::()V",
    		"index::I", "cacheFile::LCacheFile;", "referenceFile::LCacheFile;", "requestingChildren::Z"),
    MEMCACHE("MemCache", null, "size::I", "remaining::I", "table::LHashTable;", "queue::LQueue;", "head::LDualNode;", "get::(J)LDualNode;", "remove::(J)V", "put::(LDualNode;J)V", "clear::()V"),
    BUFFER("Buffer", NODE, "payload::[B", "caret::I", "crcTable::[I", 
    		"applyRSA::(Ljava/math/BigInteger;Ljava/math/BigInteger;?)V", "decodeXTEA::([III)V", "readInt::(?)I", "writeInt::(I?)V",
    		"writeByte::(I?)V", "writeBytes::([BII)V", "readBytes::([BII?)V", "encodeXTEA::([I)V", "writeCrc::(I)I", "writeLong::(J)V", "writeLELong::(J)V",
    		"readLong::(?)J", "compareCrcs::()Z", "readString::()Ljava/lang/String;", "readCheckedString::()Ljava/lang/String;", "readJString::()Ljava/lang/String;",
    		"readJString2::()Ljava/lang/String;", "writeEncodedString::(Ljava/lang/CharSequence;)V", "writeString::(Ljava/lang/String;?)V", "writeJString::(Ljava/lang/String;)V",
    		"writeFlags::(I)V", "writeSmart::(I)V", "writeShort::(I?)V", "writeByteS::(I?)V", "writeByteA::(I?)V", "writeNegByte::(I?)V",
    		"writeSizeByte::(I?)V", "writeSizeShort::(I?)V", "writeLEShort::(I?)V", "writeShortA::(I?)V", "writeLEShortA::(I?)V",
    		"writeMediumInt::(I?)V", "writeMediumIntA::(I?)V", "writeSizeInt::(I?)V", "writeLEInt::(I?)V", "writeMEInt1::(I?)V", "writeMEInt2::(I?)V",
    		"readSmart::()I", "readSmallSmart::(?)I", "readUShort::(?)I", "readUByte::(?)I", "readMIUShort::()I", "readShortA::()I", "readShort::()I", "readLEShort::(?)I",
    		"readByteA::(?)B", "readNegByte::(?)B", "readByte::(?)B", "readByteS::(?)B", "readUByteA::(?)I", "readNegUByte::(?)I", "readUByteS::(?)I",
    		"readLEUShortA::(?)I", "readUShortA::(?)I", "readLEUShort::(?)I", "readShortShift::()I", "readMediumInt::(?)I", "readMEInt1::(?)I",
    		"readMEInt2::(?)I", "readLEInt::(?)I", "encodeXTEA2::([I)V", "decodeXTEA2::([III)V"
    		),
    PACKET_BUFFER("PacketBuffer", BUFFER, "cipher::LIsaacCipher;", "bitOffset::I", 
    		"readHeader::(?)I", "writeHeader::(I?)V", 
    		"startBitAccess::(?)V", "endBitAccess::(?)V",
    		"getBits::(I)I", "getBitsLeft::(I?)I",
    		"initCipher::([I?)V"),
    
    VARPBIT("Varpbit", DUAL_NODE, "highBit::I", "lowBit::I", "varp::I", "readValues::(LBuffer;I?)V", "unpackConfig::(LBuffer;)V"),
    EXCHANGE_OFFER("ExchangeOffer", null, "status::B", "itemId::I", "price::I", "itemQuantity::I", "transferred::I", "spent::I", "isCompleted::()I", "getStatus::(?)I"),
    MESSAGES("Message", DUAL_NODE, "message::Ljava/lang/String;", "sender::Ljava/lang/String;", "channel::Ljava/lang/String;",
            "type::I", "cycle::I", "index::I", "setMessage::(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;?)V"),
    MESSAGE_CHANNEL("MessageChannel", null, "messages::[LMessage;", "index::I", "getMessage::(I)LMessage;", "createMessage::(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)LMessage;", "getIndex::(?)I"),
    KEY_FOCUS_LISTENER("KeyFocusListener", null),
    MOUSE_LISTENER("MouseListener", null),
    ABSTRACT_MOUSE_WHEEL_LISTENER("AbstractMouseWheelListener", null, "popRotation::(?)I", "addMouseWheelListener::(Ljava/awt/Component;?)V", "removeMouseWheelListener::(Ljava/awt/Component;?)V"),
    MOUSE_WHEEL_LISTENER("MouseWheelListener", null, "rotation::I", "popRotation::(?)I", "addMouseWheelListener::(Ljava/awt/Component;?)V", "removeMouseWheelListener::(Ljava/awt/Component;?)V"),

    
    
    GAME_STRINGS("GameStrings", null),
    GAME_CANVAS("GameCanvas", null, "component::Ljava/awt/Component;"),
    PARAMETERS("Parameters", null, "key::Ljava/lang/String;"),
    ANIMATION_SEQUENCE("AnimationSequence", null),
    STILL_MODEL("StillModel", ENTITY),
    MODEL("Model", ENTITY, "verticesX::[I", "verticesY::[I", "verticesZ::[I", "XYZMag::I", "allowClickBounds::Z"),
    SPRITE("Sprite", null, "pixels::[I", "width::I", "height::I", "paddingX::I", "paddingY::I", "maxX::I", "maxY::I",
    		"adjustRGB::(III)V", "drawImage2::(III)V", "drawImage::(II)V", "drawInverse::(II)V", "copyPixels::([I[IIIIIII)V", 
    		"shapeImageToPixels::([I[IIIIIIII)V", "createGraphics::()V", "copyPixelsAlpha::([I[IIIIIIIII)V", "rotate::(IIIIIID?)V",
    		"rotate2::(IIIIIIII[I[I)V", "trim::()V"),
    
    GPI("GPI", null, "chatBuffer::LBuffer;", "cachedAppearances::[LBuffer;", "pendingFlagsIndices::[I", "pendingFlagsCount::I",
            "movementTypes::[B", "skipFlags::[B", "localPlayerCount::I", "localPlayerIndices::[I", "globalPlayerCount::I", "globalPlayerIndices::[I"),

    
    CLAN_MATE("ClanMate", null, "displayName::Ljava/lang/String;", "rank::B", "world::I"),

    FRIENDED_PLAYER("FriendedPlayer", null, "displayName::Ljava/lang/String;", "previousName::Ljava/lang/String;", "world::I"),

    IGNORED_PLAYER("IgnoredPlayer", null, "displayName::Ljava/lang/String;", "previousName::Ljava/lang/String;"),

    ITEM_TABLE("ItemTable", NODE, "ids::[I", "quantities::[I"),

    GRAPHICS_STUB("GraphicsStub", ENTITY, "animationSequence::LAnimationSequence;",
    		"finished::Z", "id::I", "floorLevel::I", "regionX::I", "regionY::I", "height::I", "startCycle::I"),

    PROJECTILE("Projectile", ENTITY, "updatePosition::(IIII)V", "updateAnimation::(I)V", "getAnimatedModel::(?)LModel;",
    		"id::I", "strictX::I", "strictY::I", "startHeight::I", "loopCycle::I", "animationSequence::LAnimationSequence;",
            "slope::I", "startDistance::I", "targetIndex::I", "endHeight::I", "currStrictX::D", "currStrictY::D", "currZ::D"),

    CHARACTER("Character", ENTITY, "resetPathQueue::(?)V", "updateHitData::(III)V", "isVisible::(?)Z",
            "hitsplatCycles::[I", "hitsplatDamages::[I", "hitsplatTypes::[I",
    		"queueSize::I", "currentQueueIndex::I", "queueX::[I", "queueY::[I", "queueRun::[B",
    		"strictX::I", "strictY::I", 
    		"healthBarCycle::I", "hitpoints::I", "maxHitpoints::I",
            "animation::I", "targetIndex::I", 
            "orientation::I", "overheadText::Ljava/lang/String;", "npcBoundDim::I",
            "npcDegToTurn::I", "walkAnimation::I", "npcTurnAround::I", "npcTurnRight::I", "npcTurnLeft::I",
            "idleAnimation::I", "getNextAnimation::I", "standTurnAnimIndex::I", "modelHeight::I", "runAnimation::I",
            "animFrameId::I", "interAnimFrameId::I", "interAnimId::I"),

    NPC_DEFINITION("NpcDefinition", DUAL_NODE, "varpIndex::I", "varp32Index::I", "transformIds::[I",
            "combatLevel::I", "id::I", "colors::[S", "modifiedColors::[S",
            "name::Ljava/lang/String;", "actions::[Ljava/lang/String;", 
            "headModelIds::[I", "modelIds::[I",
            "childDefinitionExists::()Z", "transform::()LNpcDefinition;", 
            "getBasicModel::()LStillModel;", "getAnimatedModel::(LAnimationSequence;ILAnimationSequence;I)LModel;",
            "unpackBuffer::(LBuffer;I)V", "readBuffer::(LBuffer;?)V"),

    NPC("Npc", CHARACTER, "definition::LNpcDefinition;"),

    PLAYER("Player", CHARACTER, "updatePlayer::(LBuffer;)V", "getAnimatedModel::()LModel;", "setPosition::(IIB)V",
    		"updateMovement::(IIB)V", "getConfigId::()I",
    		"visible::Z", "model::LModel;",
    		"combatLevel::I", "name::Ljava/lang/String;", "prayerIcon::I", "skullIcon::I",
            "team::I", "config::LPlayerConfig;", "totalLevel::I", "height::I"),


    OBJECT_DEFINITION("ObjectDefinition", DUAL_NODE, "name::Ljava/lang/String;", "actions::[Ljava/lang/String;",
            "transformIds::[I", "transform::()LObjectDefinition;", "id::I", "varpIndex::I", "varp32Index::I",
            "sizeX::I", "sizeY::I", "colors::[S", "modifiedColors::[S", "clipType::I", "modelClipped::Z",
            "clipped::Z", "mapFunction::I"),

    ITEM_DEFINITION("ItemDefinition", DUAL_NODE, "name::Ljava/lang/String;", "actions::[Ljava/lang/String;",
            "groundActions::[Ljava/lang/String;", "id::I", "notedId::I", "unnotedId::I", "storeValue::I",
            "stackable::I", "colors::[S", "modifiedColors::[S"),

    PLAYER_CONFIG("PlayerConfig", null, "appearance::[I", "appearanceColors::[I", "female::Z", "npcId::I",
    		"baseModelId::J", "animatedModelId::J"),


    WORLD("World", null, "world::I", "index::I", "mask::I", "location::I", "domain::Ljava/lang/String;",
            "activity::Ljava/lang/String;", "population::I"),

    GRAPHICS("Graphics", DUAL_NODE, "raster::[I", "rasterWidth::I", "rasterHeight::I"),

    GROUND_ITEM("GroundItem", ENTITY, "id::I", "quantity::I"),

    PLAIN_TILE("PlainTile", null),
    SHAPED_TILE("ShapedTile", null),

    DYNAMIC_OBJECT("DynamicObject", ENTITY, "getRotatedModel::()LModel;",
    		"animationSequence::LAnimationSequence;",
    		"regionX::I", "regionY::I", "floorLevel::I", "id::I", "type::I", "orientation::I"),

    BOUNDARY_STUB("BoundaryStub", null, "uid::I", "config::I", "strictX::I", "strictY::I", "entityA::LEntity;", "entityB::LEntity;",
            "orientationA::I", "orientationB::I", "height::I"),

    BOUNDARY_DECORATION_STUB("BoundaryDecorationStub", null, "uid::I", "config::I", "height::I", "insetX::I", "insetY::I",
            "orientationA::I", "orientationB::I", "entityA::LEntity;", "entityB::LEntity;", "strictX::I", "strictY::I"),

    TILE_DECORATION_STUB("TileDecorationStub", null, "uid::I", "config::I", "strictX::I", "strictY::I", "entity::LEntity;", "height::I"),

    ITEM_PILE("ItemPile", null, "uid::I", "bottom::LEntity;", "middle::LEntity;", "top::LEntity;",
            "counterHeight::I", "height::I", "strictX::I", "strictY::I"),

    ENTITY_MARKER("EntityMarker", null, "uid::I", "config::I", "strictX::I", "strictY::I", "entity::LEntity;",
            "floorLevel::I", "height::I", "regionX::I", "regionY::I", "maxX::I", "maxY::I", "orientation::I"),

    LANDSCAPE_TILE("LandscapeTile", null, "entityMarkers::[LEntityMarker;", "boundaryStub::LBoundaryStub;",
            "boundaryDecorationStub::LBoundaryDecorationStub;", "tileDecorationStub::LTileDecorationStub;",
            "itemPile::LItemPile;", "regionX::I", "regionY::I", "floorLevel::I"),

    LANDSCAPE("Landscape", null, "tiles::[[[LLandscapeTile;", "tempEntityMarkers::[LEntityMarker;", "visibilityMap::[[[[Z", "heightmap::[[[I", "renderableTiles::[[[I", 
    		"tileShapePoints::[[I", "tileShapeIndices::[[I", "entityCachePosition::I", "currentHeightLevel::I", "zMapSize::I", "yMapSize::I", "xMapSize::I",
            "render::(IIIIII)V", "renderTile::(LLandscapeTile;Z)V", "removeObject::(LEntityMarker;)V", "setTileLogicHeight::(IIII)V", 
            "isMouseWithinTriangle::(IIIIIIII)Z", "applyBridgeMode::(II)V", "processBoundaries::()V", "clearEntityMarkerCache::()V", "initToNull::()V",
            "setHeightLevel::(I)V", "addTile::(IIIIIIIIIIIIIIIIIIII)V", "removeEntityMarker::(III)V", "drawMinimapTile::([IIIIII)V", "visibleInViewport::(III)Z",
            "updateViewportVisibility::(III)Z", "renderPlainTile::(LPlainTile;IIIIIII)V", "renderShapedTile::(LShapedTile;IIIIII)V", "getIDTagForXYZ::(IIII)I",
            "requestTrace::(III)V", "updateViewport::(IIIIII)Z", "snapBoundaryModels::(IIII)V", "shadeModels::(III)V", "shadeModel::(LStillModel;IIIII)V",
            "offsetModelVertexs::(LStillModel;III)V", "visibilityBlocked::(IIII)Z", "isRenderable::(IIII)Z",
    		"addEntityMarker::(IIIIIIIILEntity;IZII)Z", "addTileDecoration::(IIIILEntity;II)V", "addBoundaryDecoration::(IIIILEntity;LEntity;IIIIII)V", "addItemPile::(IIIILEntity;ILEntity;LEntity;)V", "addBoundary::(IIIILEntity;LEntity;IIII)V", "addEntity::(IIIIILEntity;IIIIII)Z", "addTempEntity::(IIIIILEntity;IIZ)Z", "addObject::(IIIIIILEntity;III)Z",
            "removeTileDecorationStub::(III)V", "removeBoundaryStub::(III)V", "removeBoundaryDecorationStub::(III)V", "removeItemPile::(III)V", 
            "getTileDecorationStub::(III)LTileDecorationStub;", "getBoundaryStub::(III)LBoundaryStub;", "getBoundaryDecorationStub::(III)LBoundaryDecorationStub;", "getEntityMarker::(III)LEntityMarker;",
            "getTileDecorationStubUID::(III)I", "getBoundaryStubUID::(III)I", "getBoundaryDecorationStubUID::(III)I", "getEntityMarkerUID::(III)I"),

    WIDGET("Widget", null, "actions::[Ljava/lang/String;", "name::Ljava/lang/String;", "text::Ljava/lang/String;",
            "textColor::I", "alpha::I", "textureId::I", "spriteId::I", "selectedAction::Ljava/lang/String;",
            "config::I", "fontId::I", "textShadowed::Z", "borderThickness::I", "shadowColor::I",
            "flippedVertically::Z", "flippedHorizontally::Z", "modelType::I", "mouseEnterListener::[Ljava/lang/Object;",
            "mouseExitListener::[Ljava/lang/Object;", "renderListener::[Ljava/lang/Object;", "scrollListener::[Ljava/lang/Object;",
            "mouseHoverListener::[Ljava/lang/Object;", "varListener::[Ljava/lang/Object;",
            "configTriggers::[I", "tableListener::[Ljava/lang/Object;", "tableTriggers::[I",
            "skillListener::[Ljava/lang/Object;", "skillTriggers::[I", "modelId::I", "insetX::I", "insetY::I",
            "viewportWidth::I", "viewportHeight::I", "relativeX::I", "relativeY::I", "width::I", "height::I",
            "hidden::Z", "index::I", "parentId::I", "id::I", "modelZoom::I", "rotationX::I", "rotationY::I",
            "rotationZ::I", "itemId::I", "itemQuantity::I", "boundsIndex::I", "loopCycle::I",
            "tableActions::Ljava/lang/String;", "varpOpcodes::[[I", "itemIds::[I", "itemQuantities::[I",
            "interactable::Z", "tooltip::Ljava/lang/String;", "type::I", "contentType::I", "buttonType::I",
            "rowPadding::I", "columnPadding::I", "xSprites::[I", "ySprites::[I",
            "parent::LWidget;", "children::[LWidget;", "xMargin::[I", "yMargin::[I", "wMargin::[I", "hMargin::[I",
            "xLayout::[I", "yLayout::[I", "hLayout::[I", "wLayout::[I", "enableEvents::Z", "textMode::I", "thickness::I",
            "rotation::I", "repeat::Z", "selectMode::I", "spriteId2::I", "fontId::I", "textSpacing::I", "horizontalMargin::I",
            "verticalMargin::I", "fill::Z", "perpendicular::Z", "textShadowed::Z", "v2::Z", "mousePressListener", "dragOverListener"
            , "mouseReleaseListener", "mouseEnterListener", "mouseExitListener", "compDragListener", "spellCastListener"
            , "cycleListener", "actionListener", "compDropListener", "mouseDragListener", "mouseHoverListener"
            , "spellSelectedListener", "scrollListener", "messageListener", "keyListener", "windowClosedListener"
            , "windowOpenedListener", "resizeListener"


    ),

    SCRIPT_EVENT("ScriptEvent", NODE, "mouseX::I", "mouseY::I", "keyCode::I", "keyChar::I",
    		"args::[Ljava/lang/Object;", "opbase::Ljava/lang/String;", "consumable::Z", "src::LWidget;", "target::LWidget;"),

    RUNESCRIPT("RuneScript", DUAL_NODE, "intArgCount::I", "stringArgCount::I", "intStackCount::I",
            "stringStackCount::I", "opcodes::[[I", "intOperands::[I", "stringOperands::[Ljava/lang/String;"),
    
    ABSTRACT_FONT("AbstractFont", GRAPHICS),
    FONT_IMPL("FontImpl", ABSTRACT_FONT),

    GAME_ENGINE("GameEngine", null, "getGameContainer::()Ljava/awt/Container;", "isHostValid::()Z", "displayError::(Ljava/lang/String;)V",
    		"render::()V", "renderGame::(?)V",
    		"gameEngineDumpProcessed::Z"),
    
    CLIENT("Client", GAME_ENGINE, "updateEntity::(LCharacter;IIIII)V", "updateEntities::(IIII)V", 
    		"newClientError::(Ljava/lang/Throwable;Ljava/lang/String;)LClientError;",
    		"threadSleep::(J)V", "inlinedThreadSleep::(J)V", "bootClient::()V", "buildComponentEvents::([LWidget;IIIIII?)V",
    		"getWidgetChild::(I)LWidget;", "layoutDimensions::(LWidget;IIZ)V", "layoutPositions::(LWidget;II)V",
    		"logError::(Ljava/lang/String;Ljava/lang/Throwable;)V", "addMessage::(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
    		"addMenuRow::(Ljava/lang/String;Ljava/lang/String;III?)V", "getItemSprite::(IIIIIZ)LSprite;", "repaintWidget::(LWidget;)V", 
    		"resetConnection::(I)V", "addHUD::(II?)LHUD;", "removeHUD::(LHUD;Z)V", "runScript::(LScriptEvent;?)V", 
    		"setWorld::(LWorld;)V", "getWidgetConfig::(LWidget;)I", "layoutContainer::([LWidget;LWidget;Z)V", 
    		"layoutContainer2::([LWidget;IIIZ)V", "layoutWindow::(IIIZ)V", "loadImage::(LReferenceTable;II)LSprite;", 
    		"processFrames::(?)V", "processLogic::(?)V", "renderComponent::([LWidget;IIIIIIII)V", "runScript::(LScriptEvent;?)V", 
    		"layoutComponent::(LWidget;)V", "loadWindow::(I)Z", "renderGame::(?)V", "buildPlayerMenu::(LPlayer;III)V",
    		"messageCacheLength::I", "bootStatus::Ljava/lang/String;", "XTEAKeys::[[I", "RSVMIntStack::[I",
    		"RSVMStringStack::[Ljava/lang/String;", "repaintWidgets::[Z", "b12_full::LFontImpl;",
    		"mouseX::I", "mouseY::I", "runeScriptCache::LMemCache;", "runeScriptFileSystem::LRemoteFileTable;",
    		"shell::LGameEngine;", "focused::Z", "clearScreenRequest::Z", "gameFrame::Ljava/awt/Frame;",
    		"myPlayerIndex::I", "audioEffectCount::I", "cameraX::I", "cameraY::I",
            "cameraZ::I", "cameraYaw::I", "cameraPitch::I", "floorLevel::I", "npcIndices::[I",
            "npcArray::[LNpc;", "playerArray::[LPlayer;", "myPlayer::LPlayer;",
            "regionBaseX::I", "regionBaseY::I", "tempVars::[I", "minimapSprite::LSprite;", "engineCycle::I",
            "menuActions::[Ljava/lang/String;", "menuNouns::[Ljava/lang/String;", "menuOpcodes::[I",
            "menuArg0::[I", "menuArg1::[I", "menuArg2::[I", "landscape::LLandscape;", "worldCount::I",
            "worlds::[LWorld;", "huds::LHashTable;", "minimapRotation::I", "minimapScale::I",
            "viewRotation::I", "interfaces::[[LWidget;", "energy::I", "weight::I", "onCursorUids::[I",
            "onCursorCount::I", "cursorState::I", "playerActions::[Ljava/lang/String;", "itemSelectionStatus::I",
            "spellSelected::Z", "selectedItemName::Ljava/lang/String;", "selectedSpellName::Ljava/lang/String;",
            "currentSpellTargets::I", "menuActionPrefix::Ljava/lang/String;", "hintArrowX::I", "hintArrowY::I",
            "myWorld::I", "membersWorld::Z", "friendListSize::I", "clanChatOwner::Ljava/lang/String;",
            "clanChatName::Ljava/lang/String;", "clanChatRank::B", "clanChatSize::I", "ignoreListSize::I",
            "myRights::I", "connectionState::I", "destinationX::I", "destinationY::I", "engineVars::[I",
            "experiences::[I", "levels::[I", "currentLevels::[I", "fps::I", "groundItemDeque::[[[LDeque;",
            "friendedPlayers::[LFriendedPlayer;", "clanMates::[LClanMate;", "ignoredPlayers::[LIgnoredPlayer;",
            "graphicsObjectDeque::LDeque;", "projectileDeque::LDeque;",
            "itemTables::LHashTable;", "menuWidth::I", "menuHeight::I", "menuX::I", "menuY::I", "menuOpen::Z",
            "tileHeights::[[[I", "renderRules::[[[B", "screenCenterX::I", "screenCenterY::I", "selectedItemIndex::I",
            "xViewportBuffer::[I", "yViewportBuffer::[I", "worldSelectorDisplayed::Z",
            "localExchangeOffers::[LExchangeOffer;", "packet::LPacketBuffer;", "menuItemCount::I", "focused::Z",
            "screenZoom::I", "screenWidth::I", "screenHeight::I", "getObjectDefinition::(I)LObjectDefinition;",
            "getItemDefinition::(I)LItemDefinition;", "getNpcDefinition::(I)LNpcDefinition;", 
            "loadWorlds::()V", "getRuneScript::(I)LRuneScript;", "getVarpbit::(I)I",
            "bootState::I", "cacheDirectory::Ljava/io/File;",
            "cacheLocation::Ljava/io/File;", "processLogin::()V", 
            "addNPCToMenu::(LNpcDefinition;III?)V",
            "colorsToFind::[S", "colorsToReplace::[[S", "colorsToFind1::[S", "colorsToReplace1::[[S",
            "getKeyFocusListener::LKeyFocusListener;", "canvas::Ljava/awt/Canvas;", "widgetsHeight::[I",
            "widgetsWidth::[I", "widgetPositionsX::[I", "widgetPositionsY::[I", "messageChannels::Ljava/util/Map;",
            "currentLoginName::Ljava/lang/String;", "hideRoofs::I", 
            "drawMenu", "chunkIds::[I", "loadedWindows::[Z",
            "fontCache::LMemCache;", "packetBitMasks");


    public final Map<String, RSMember> hooks;
    public final Map<String, String> hookToDesc;
    private final String definedName;
    private final Hook superType;
    private String internalName;
    private List<String> warnings = new ArrayList<>();

    private static Remapper MAPPER = HookMap.INSTANCE;

    Hook(final String definedName, final Hook superType, final String... keys) {
        this.definedName = definedName;
        this.superType = superType;
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
        if (old != null && !old.equals(member)) {
            String h = member.isField() ? "Field" : "Method";
            warn("Overwrite of " + h + " '" + member.mnemonic + "' [" + old.key() + " ==> " + member.key() + "]");
        }
        if (!hooks.containsKey(member.mnemonic)) {
            String h = member.isField() ? "Field" : "Method";
            warn(h + " '" + member.mnemonic + "' is not defined.");
        }
        hooks.put(member.mnemonic, member);
    }

    private static String popPred(String methodDesc) {
        Type[] args = Type.getArgumentTypes(methodDesc);
        Type ret = Type.getReturnType(methodDesc);
        Type[] args0 = Arrays.copyOf(args, args.length - 1);
        return Type.getMethodDescriptor(ret, args0);
    }

    public void verify() {
        for (RSMember mem : hooks.values()) {
            if (mem == null) continue;
            String desc0 = mapDesc(mem.desc);
            String desc = hookToDesc.get(mem.mnemonic);
            if (desc == null) continue; // undefined member
            if (mem.isMethod()) {
                boolean has_pred = OpPredicateRemover.hasPred(mem.owner, mem.name, mem.desc);
                if (has_pred) desc0 = popPred(desc0);
            }
            if (!new Wildcard(desc).matches(desc0)) {
                String h = mem.isField() ? "Field" : "Method";
                warn(h + " '" + mem.mnemonic + "' has a unexpected descriptor " + "(defined=" + desc + ",resolved=" + desc0 + ")");
            }
        }

        for (RSMember mem : hooks.values()) {
            if (mem == null) continue;
            for (RSMember mem0 : hooks.values()) {
                if (mem0 == null) continue;
                if (mem == mem0) continue;
                if (mem.equals(mem0)) {
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
        if (desc.charAt(0) == '(') return mapMethodDesc(desc);
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
        if (this.internalName != null && !this.internalName.equals(internalName)) {
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
            for (Thing thing : members) {
                if (thing.getValue() == null) continue;
                if (thing.getValue().desc.contains(")")) continue;
                String k = thing.getValue().owner + "" + thing.getValue().name;
                if (k.length() > max) max = k.length();
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
                    String domain = OpPredicateRemover.getDomain(m.owner, m.name, m.desc);
                    if (domain != null) {
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
                        while (k++ < max) specbuilder.append(' ');
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