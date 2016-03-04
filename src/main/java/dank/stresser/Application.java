package dank.stresser;

import com.dank.DankEngine;
import com.dank.analysis.Analyser;
import com.dank.analysis.impl.client.visitor.PacketVisitor;
import com.dank.analysis.visitor.MultiplierVisitor;
import com.dank.asm.ClassPath;
import com.dank.hook.Hook;
import org.objectweb.asm.commons.cfg.OpaquePredicateVisitor;
import org.objectweb.asm.commons.util.CallVisitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Septron
 * @since February 17, 2015
 */
public class Application {

    private static final Map<Integer, List<String>> breaked = new LinkedHashMap<>();

    public static String base = "./jars/";

    public static int min = 94;
    public static int max = 103;

    private static final Analyser[] analysers = {
          /*  new GStrings(), new Node(), new DualNode(), new Deque(), new HashTable(), new Renderable(), new Buffer(),
            new PacketBuffer(), new ScriptEvent(), new InterfaceNode(), new Widget(), new GraphicsStub(),
            new Projectile(), new Client(), new com.dank.analysis.impl.character.Character(), new NpcDefinition(), new Npc(), new EntityMarker(),
            new BoundaryDecorationStub(), new BoundaryStub(), new TileStub(), new ItemPile(), new DynamicObject(),
            new Player(), new Landscape(), new LandscapeTile(), new Model()*/

            new PacketVisitor()
    };

    public static void main(String... arguments) {
        System.out.println("With ♥ from Team Dank");
        Stopwatch stopwatch = new Stopwatch();
        for (int i = min; i < max + 1; i++) {
            ClassPath cp = new ClassPath();
            cp.addJar(base + i + ".jar");
            if (cp.getClasses().size() == 0) {
                System.err.println("Failed to load " + (base + i));
            } else {
                new CallVisitor(cp);
                new OpaquePredicateVisitor(cp);
                cp.forEach((name, cn) -> cn.methods.forEach(mn -> mn.graph().forEach(bb -> bb.tree().accept(new MultiplierVisitor()))));
                DankEngine.classPath = cp;
                stopwatch.start();
                for (final Analyser analyser : analysers) {
                    analyser.execute(cp);
                }
                stopwatch.stop();
                int found = 0;
                for (final Hook hook : Hook.values()) {
                    found += hook.getIdentifiedSet().size();
                }
                if (found >= 243) {
                    System.out.println("✓ Rev: " + i + " (" + stopwatch.ms() + " ms)");
                } else {
                    System.err.println("✕ Rev: " + i + " (" + stopwatch.ms() + " ms) - " + found + "/" + 243);
                    for (final Hook hook : Hook.values()) {
                        for (final String key : hook.hooks.keySet()) {
                            if (hook.get(key) == null) {
                                if (breaked.get(i) == null) {
                                    breaked.put(i, new ArrayList<>());
                                }
                                breaked.get(i).add(key);
                            }
                        }
                    }
                }
                for (final Hook hook : Hook.values()) {
                    hook.setInternalName(null);
                    hook.clearAll();
                }
                cp.getClasses().clear();
            }
        }
        List<String> keys = DSReader.keys("hooks.ds2");
        for (final Map.Entry<Integer, List<String>> breaks : breaked.entrySet()) {
            StringBuilder builder = new StringBuilder();
            builder.append("Rev ").append(breaks.getKey()).append(" breaks: ");
            for (String b : breaks.getValue()) {
                if (keys.contains(b))
                    builder.append(b).append(" ");
            }
            System.err.println(builder.toString());
            try {
                Thread.sleep(200);
            } catch (Exception ignored) {}
        }
        System.gc();
    }
}
