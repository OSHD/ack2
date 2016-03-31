package com.dank;

import com.dank.analysis.Analyser;
import com.dank.analysis.impl.character.Character;
import com.dank.analysis.impl.character.npc.Npc;
import com.dank.analysis.impl.character.player.Player;
import com.dank.analysis.impl.character.player.config.PlayerConfig;
import com.dank.analysis.impl.character.visitor.Orientation2;
import com.dank.analysis.impl.client.Client;
import com.dank.analysis.impl.definition.item.ItemDefinition;
import com.dank.analysis.impl.definition.npc.NpcDefinition;
import com.dank.analysis.impl.definition.object.ObjectDefinition;
import com.dank.analysis.impl.focus.KeyFocusListener;
import com.dank.analysis.impl.landscape.*;
import com.dank.analysis.impl.misc.*;
import com.dank.analysis.impl.model.Model;
import com.dank.analysis.impl.node.*;
import com.dank.analysis.impl.node.graphics.Graphics;
import com.dank.analysis.impl.widget.Widget;
import com.dank.analysis.visitor.DummyParameterVisitor;
import com.dank.analysis.visitor.MultiplierVisitor;
import com.dank.analysis.visitor.MultiplyExpressionVisitor;
import com.dank.analysis.visitor.stuff.CodecResolver;
import com.dank.asm.ClassPath;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMember;
import com.dank.hook.RSMethod;
import com.dank.patch.GClass;
import com.dank.patch.GField;
import com.dank.patch.GMethod;
import com.dank.patch.Patch;
import com.dank.util.FieldCallGraphVisitor;
import com.dank.util.MethodCallGraphVisitor;
import com.dank.util.deob.OpPredicateRemover;
import com.dank.util.io.Fetcher;
import com.dank.util.multipliers.Multiplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dank.tests.HierarchyVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.OpaquePredicateVisitor;
import org.objectweb.asm.commons.util.CallVisitor;
import org.objectweb.asm.remap.RemappingAdapter;
import org.objectweb.asm.remap.RemappingData;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.jar.Manifest;

//import it.sauronsoftware.ftp4j.FTPClient;

public final class DankEngine implements Opcodes {

    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    private static int localRevision = 0;
    public static ClassPath classPath;
    public static MethodCallGraphVisitor mGraph;
    public static FieldCallGraphVisitor fGraph;
    public static CodecResolver resolver;
    private static Manifest manifest;

    private static int version = 110;
    public static boolean fetch = true;

    public static boolean auto = true;


    public static ClassNode lookupClass(String name) {
        return classPath.get(name);
    }

    public static MethodNode lookupMethod(String owner, String name, String desc) {

        ClassNode cn = lookupClass(owner);
        if (cn == null)
            return null;
        for (MethodNode mn : cn.methods) {
            if (!mn.name.equals(name) || !mn.desc.equals(desc))
                continue;
            return mn;
        }
        return null;
    }

    public static FieldNode lookupField(String owner, String name, String desc) { // field names are unique, no need for desc
        ClassNode cn = lookupClass(owner);
        if (cn == null)
            return null;
        for (FieldNode fn : cn.fields) {
            if (!fn.name.equals(name) || !fn.desc.equals(desc))
                continue;
            return fn;
        }
        return null;
    }

    public static void main(final String... args) throws Exception {


        PrintStream stream = new PrintStream(System.out, true, "UTF-8") {

            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("log.txt")));

            @Override
            public void println(String string) {
                super.println(string);
                try {
                    writer.write(string);
                    writer.newLine();
                    writer.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            public void close() {
                try {
                    writer.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        System.setOut(stream);

//        while (auto) {
//            VersionChecker checker = new VersionChecker();
//            checker.run();
//        }

        File from;
        File deob;
        File remap;
        if (fetch) {
            System.out.println("Fetching");

            from = Fetcher.getFile();
            System.out.println(from.getParent());
            deob = new File(from.getParent() + "/deobfuscated.jar");
            remap = new File(from.getParent() + "/remapped.jar");
        } else {
            from = new File("./jars/" + version + "/gamepack.jar");
            deob = new File("./jars/" + version + "/deobfuscated.jar");
            remap = new File("./jars/" + version + "/remapped.jar");
        }

        VersionChecker checker = new VersionChecker();
        checker.run();

        classPath = new ClassPath();

        manifest = classPath.addJar(from.getAbsolutePath());

        try {
            new CallVisitor(classPath);
        } catch (Exception e) {
            //I don't want to listen to your CNF bullshit
        }
        new OpaquePredicateVisitor(classPath);

        DankBuild.instance.set("build", String.valueOf(Integer.parseInt((String) DankBuild.instance.get("build")) + 1));
        classPath.dump(deob.getAbsolutePath());

        try {
            DankBuild.instance.save();
        } catch (Exception e) {
            e.printStackTrace();
        }

        OpPredicateRemover.run(classPath.getMap());
        // MultiRemover.run(classPath.getMap());

        final Analyser[] analysers = {new GStrings(), new GameCanvas(), new Parameters(), new Node(), new DualNode(), new Deque(),

                new HashTable(), new Renderable(), new Buffer(), new PacketBuffer(), new IsaacRandom(), new GPI(), new ScriptEvent(),
                new InterfaceNode(), new GraphicsStub(), new ItemTable(), new Varpbit(), new Sprite(), new RuneScript(),
                new AbstractFont(), new FontImpl(), new CacheTable(), new ImageProduct(),
                new Projectile(), new ItemDefinition(), new Widget(), new ObjectDefinition(), new PlayerConfig(),
                new ExchangeOffer(),
                new Player(),
                new Client(),
                new Character(),
                new NpcDefinition(),
                new Npc(),
                new EntityMarker(), new BoundaryDecorationStub(), new BoundaryStub(), new TileStub(), new GroundItem(),
                new ItemPile(), new DynamicObject(), new Landscape(), new LandscapeTile(), new Model(),
                new KeyFocusListener(), new GraphicsEngine(), new Graphics(), new Messages(), new MessageHandler(),
//                new AbstractFont(), new FontImpl(), new CacheTable(), new ImageProduct()
                /* new PacketVisitor() */
        };

        //load multiplier cache
        Multiplier.loadCache_(classPath.getClasses());

        final HierarchyVisitor hierarchyVisitor = new HierarchyVisitor(classPath);
        classPath.getClasses().forEach(hierarchyVisitor::accept);

        //calls = new CallGraphVisitor(classPath, hierarchyVisitor);
        //System.out.println("Graphed " + calls.size() + " Call(s)");
        mGraph = new MethodCallGraphVisitor(classPath);
        fGraph = new FieldCallGraphVisitor(classPath);

        final DummyParameterVisitor dpv = new DummyParameterVisitor();
        classPath.forEach((name, cn) -> cn.methods.forEach(mn -> {
            mn.graph().forEach(bb -> {
                bb.tree().accept(new MultiplierVisitor());
                bb.tree().accept(new MultiplyExpressionVisitor(bb));
            });
            dpv.accept(mn);
        }));

        // new OpaquePredicateVisitor(classPath);

        for (final Analyser analyser : analysers) {
            analyser.execute(classPath);
        }

        // System.out.println("Finding coders...");
        resolver = new CodecResolver();
        Analyzer a = new Analyzer<>(resolver);
        for (ClassNode cn : classPath.getClasses()) {
            for (MethodNode mn : cn.methods) {
                a.analyze(cn.name, mn);


                RSMethod add_entity = (RSMethod) Hook.LANDSCAPE.get("addTempEntity");
                if (add_entity == null) break;
                for (AbstractInsnNode ain : mn.instructions.toArray()) {
                    if (ain.opcode() == INVOKEVIRTUAL) {
                        org.objectweb.asm.tree.MethodInsnNode min = (org.objectweb.asm.tree.MethodInsnNode) ain;
                        if (add_entity.equals(min.owner, min.name, min.desc)) {
                            Orientation2.run(mn);
                            break;
                        }
                    }
                }

            }
        }
        // System.out.println("... Done");

        System.out.println();

        int found_hooks = 0;
        int found_classes = 0;
        int total_hooks = 0;
        int total_classes = 0;
        int warnings = 0;
        for (final Hook hook : Hook.values()) {
            String raw = hook.toString();
            System.out.println(raw);
            total_hooks += hook.hooks.size();
            total_classes += 1;
            found_hooks += hook.getIdentifiedSet().size();
            if (hook.getInternalName() != null) found_classes++;
            hook.verify();
            warnings += hook.getWarnings().size();
        }
        System.out.println("  Found " + found_hooks + "/" + total_hooks + " hooks, " + found_classes + "/" + total_classes + " classes!");

        System.out.flush();

        if (warnings > 0) {
            System.err.println(warnings + " Warnings:");
            for (Hook h : Hook.values()) {
                if (h.getWarnings().size() == 0) continue;
                System.err.println(h.getDefinedName() + " (" + h.getWarnings().size() + " warnings):");
                Collections.sort(h.getWarnings());
                for (String w : h.getWarnings()) {
                    System.err.println("\t" + w);
                }
                System.err.println(System.lineSeparator());
            }

        }

        RemappingAdapter adapter = new RemappingAdapter(classPath);
        for (final Hook hook : Hook.values()) {
            if (hook.getInternalName() != null) {
                adapter.remapClass(new RemappingData(hook.getInternalName(), hook.getDefinedName()));
            }
            for (final Map.Entry<String, RSMember> entry : hook.hooks.entrySet()) {
                if (entry.getValue() != null) {
                    if (entry.getValue() instanceof RSMethod) {
                        RSMethod method = (RSMethod) entry.getValue();
                        adapter.remapMethod(method.owner, method.desc, new RemappingData(method.name, entry.getKey()));
                        MethodNode superMethod = classPath.getMethodFromSuper(classPath.get(method.owner), method.name, method.desc, false);
                        if (superMethod != null) {
                            adapter.remapMethod(superMethod.owner.name, method.desc, new RemappingData(method.name, entry.getKey()));
                        }
                    } else {
                        RSField field = (RSField) entry.getValue();
                        adapter.remapField(field.owner, field.desc, new RemappingData(field.name, entry.getKey()));
                        FieldNode superField = classPath.getFieldFromSuper(classPath.get(field.owner), field.name, field.desc, true);
                        if (superField != null) {
                            adapter.remapField(superField.owner.name, field.desc, new RemappingData(field.name, entry.getKey()));
                        }
                    }
                }
            }
        }

        List<ClassNode> classNodeList = classPath.getSupers(Hook.NPC.resolve());

        classNodeList.stream().forEach(classNode -> System.out.println(classNode.name));

        adapter.manipulate();
        classPath.dump(remap.getAbsolutePath());

        //	write(new File("./hooks.ds2"), false);

        stream.close();

        Patch p = save();
        GsonBuilder b = new GsonBuilder();
        b.setPrettyPrinting();
        Gson gson = b.create();
        String json = gson.toJson(p);

        FileOutputStream out = new FileOutputStream("./hooks.gson");
        out.write(json.getBytes());
        out.flush();
        out.close();

        /**
         *
         *
         * F += V
         *
         * GETFIELD F LDC V IADD PUTFIELD F
         *
         *
         * PUTFIELD -> Value must be encoded. Value can be already in a encoded
         * state.
         *
         *
         * Enc(F) + V
         *
         *
         *
         *
         *
         *
         * fieldA = ((fieldB * (Ea * Db) * Eb) * Da
         *
         * int k =
         *
         * RNFT = Raw Numeric Field Type
         *
         * Once a RNFT is loaded onto the stack, what it does with it is the
         * question. We must acknoledge every operation
         *
         * Properties: Encoded: ( The value provided to these operations must be
         * in a encoded state, if not ruled out )
         *
         * - PUTFILED, PUTSTATIC : The value set must be encoded field = field -
         * IMUL, IADD, ISUB, FMUL, IADD, ISUB : The value provided must be
         * encoded field + (...), field - (...), field * (...)
         *
         *
         * Natural Properties: Operations performed with a RNFT that prove its
         * natural
         *
         * - Type conversions of RNFT: Impossible with a encoded type - IINC,
         * L2I, I2B, I2C, I2S, I2L, I2D, L2D
         *
         * - Logical: Impossible logic with a encoded type - IREM, ISHL, ISHR,
         * IUSHR, IAND, IOR, IXOR - LREM, LSHL, LSHR, LUSHR, LAND, LOR, LXOR
         *
         * - Arithmetic: Division is not possible with encoded types - IDIV,
         * LDIV
         *
         * - Control flow with RNFT: Impossible to encode the underlying 0
         * compare - IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE
         *
         * - Field Set: - PUTFIELD, PUTSTATIC : field = field
         *
         * - Specifications: - NEWARRAY, ANEWARRAY : Array size - LALOAD,
         * DALOAD, FALOAD, IALOAD, BALOAD, CALOAD, SALOAD, AALOAD: Array index -
         * LASTORE, DASTORE, FASTORE, IASTORE, BASTORE, CASTORE, SASTORE,
         * AASTORE: - If the index if a RNFT then that field is natural - If the
         * element store is a RNFT then that field is natural: Since elements in
         * arrays are not encoded [Observed]
         *
         * - Observed: - ISTORE, LSTORE : Locals are natural - IRETURN, LRETURN
         * : Returned values are natural - IF_ICMPEQ , ... , LCMP : The compared
         * value is not encoded ; if(field == value) - INVOKE, ... ,: If any
         * argument is a RNFT then its natural: since arguments are natural
         * (since locls are not encoded) AnyState: INEG, LNEG
         *
         *
         */

		/*
         * System.out.println(resolver.getDecoder("n","k","I") + " x " +
		 * resolver.getEncoder("n","k","I"));
		 * System.out.println(resolver.getDecoder("n","b","I"));
		 * System.out.println(resolver.getDecoder("n","a","I"));
		 */
    }

    private static void write(final File ds2, final boolean upload) {
        try (final DataOutputStream out = new DataOutputStream(new FileOutputStream(ds2))) {
            writeHeader(out);
            writeBody(out);
            out.flush();
            out.close();
            if (upload) {
//                final FTPClient ftp = new FTPClient();
//                ftp.connect("botwise.org", 21);
//                ftp.login("vector@botwise.org", "vectorhipster");
//                ftp.upload(ds2);
//                ftp.disconnect(true);
//                System.out.println("Uploaded ds2!");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeHeader(final DataOutputStream out) throws IOException {
        out.writeInt(0xCAFEBABE);
        out.writeInt(manifest.hashCode());
        out.writeInt(version);
        out.writeInt(Integer.parseInt(String.valueOf(DankBuild.instance.get("build"))));
    }

    private static int getAccess(RSMember member) {
        for (ClassNode cn : classPath.getClasses()) {
            if (member instanceof RSField) {
                for (FieldNode fn : cn.fields) {
                    if (fn.name.equals(member.name) && fn.desc.equals(member.desc)) {
                        return fn.access;
                    }
                }
            } else if (member instanceof RSMethod) {
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals(member.name) && mn.desc.equals(member.desc)) {
                        return mn.access;
                    }
                }
            }
        }
        return 0;
    }

    public static Number getPred(RSMethod m) {
        return OpPredicateRemover.getValueValue(m.owner, m.name, m.desc);
    }

    public static Number getDecoder(RSField f) {
//        return Multiplier.getMultiple(f.owner + "." + f.name);
        return DankEngine.resolver.getDecoder(f.owner, f.name, f.desc);
    }

    public static Patch save() {

        Patch p = new Patch();
        p.rev = version;
        p.crc = manifest.hashCode();
        for (final Hook container : Hook.values()) {

            GClass clazz = new GClass();
            clazz.name = container.getInternalName();

            /*
              ✓ innerAnimId...................ai.bo x -1125691628
             ✓ innerFrameId..................ai.bc x 1350891992
             */
            for (Map.Entry<String, RSMember> entry : container.hooks.entrySet()) {
                String name = entry.getKey();
                RSMember member = entry.getValue();
                if (member instanceof RSMethod) {
                    GMethod method = new GMethod();
                    method.name = member.name;
                    method.desc = member.desc;
                    method.owner = member.owner;
                    method.access = getAccess(member);
                    method.predicate = getPred((RSMethod) member);
                    if (clazz.methods == null) clazz.methods = new HashMap<>();
                    clazz.methods.put(name, method);
                } else if (member instanceof RSField) {
                    GField field = new GField();
                    field.owner = member.owner;
                    field.name = member.name;
                    field.desc = member.desc;
                    field.access = getAccess(member);
                    field.decoder = getDecoder((RSField) member); //MultiplierFinder.findMultiplier(field.owner, field.name);
                    if (clazz.fields == null) clazz.fields = new HashMap<>();
                    clazz.fields.put(name, field);
                }
            }


            if (p.classes == null) p.classes = new HashMap<>();

            p.classes.put(container.getDefinedName(), clazz);

        }

        return p;
    }

    private static void writeBody(final DataOutputStream out) throws IOException {
        out.writeShort(Hook.values().length);
        for (final Hook container : Hook.values()) {
            out.writeByte(0); // Type = class
            if (container.getInternalName() == null) {
                out.writeUTF("BROKEN");
                out.writeUTF(container.getDefinedName());
                out.writeShort(0); // hook count
            } else {
                out.writeUTF(container.getInternalName());
                out.writeUTF(container.getDefinedName());
                out.writeShort(container.hooks.size());
                for (final Map.Entry<String, RSMember> entry : container.hooks.entrySet()) {
                    final RSMember member = entry.getValue();
                    out.writeByte(1); // Type = member
                    if (member == null) {
                        out.writeBoolean(false);
                        out.writeUTF("BROKEN");
                        out.writeUTF("BROKEN");
                        out.writeUTF("BROKEN");
                        out.writeUTF(entry.getKey());
                        out.writeLong(0);

                        // whether to inject into client or not
                        out.writeBoolean(container.getDefinedName().equals("Client"));
                    } else {
                        out.writeBoolean(member instanceof RSMethod);
                        out.writeUTF(member.owner);
                        out.writeUTF(member.name);
                        out.writeUTF(member.desc);
                        out.writeUTF(entry.getKey());
                        if (member instanceof RSMethod) {
                            if (DummyParameterVisitor.VALUES.containsKey(member.key())) {
                                out.writeLong(DummyParameterVisitor.VALUES.get(member.key()));
                            } else {
                                out.writeLong(Long.MAX_VALUE);
                            }
                        } else {
//                            out.writeLong(MultiplierVisitor.getDecoder(member.key()));
                            if (member.multiplier == 0)
                                out.writeLong(Multiplier.getMultiple(member.key()));
                            else
                                out.writeLong(member.multiplier);
//                            out.writeLong(MultiplierFinder.findMultiplier(member.owner , member.name));
                        }

                        // whether to inject into client or not
                        out.writeBoolean(container.getDefinedName().equals("Client"));
                    }
                }
            }
        }
    }
}