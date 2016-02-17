package debug;

import com.dank.analysis.visitor.stuff.CodecResolver;
import com.dank.asm.ClassPath;
import com.dank.util.FieldCallGraphVisitor;
import com.dank.util.MethodCallGraphVisitor;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.FlowVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

/**
 * Created by Greg on 12/24/2015.
 */
public class FieldDebug {

    File from;
    File deob;
    File remap;

    public static ClassPath classPath;
    public static MethodCallGraphVisitor mGraph;
    public static FieldCallGraphVisitor fGraph;
    public static CodecResolver resolver;
    private static Manifest manifest;
    private static int version = 106;

    public FieldDebug(String owner, String field_name, boolean fullMethod) {

        from = new File("./jars/" + version + "/gamepack.jar");
        deob = new File("./jars/" + version + "/deobfuscated.jar");
        remap = new File("./jars/" + version + "/remapped.jar");

        classPath = new ClassPath();
        manifest = classPath.addJar(from.getAbsolutePath());
        mGraph = new MethodCallGraphVisitor(classPath);

        List<MethodNode> full_method = new ArrayList<MethodNode>();

        for(ClassNode classNode : classPath.getClasses()) {
            for (MethodNode methodNode : classNode.methods) {
                FlowVisitor fv = new FlowVisitor();
                fv.accept(methodNode);
                for(BasicBlock bb : fv.blocks) {
                    for (AbstractInsnNode ain : bb.instructions) {
                        if (ain instanceof FieldInsnNode && ((FieldInsnNode) ain).owner.equals(owner) && ((FieldInsnNode) ain).name.equals(field_name)) {
                            System.out.println(methodNode.owner.name + "." + methodNode.name +methodNode.desc);
                            System.out.println(bb.tree().toString());
                            if(fullMethod) full_method.add(methodNode);
                        }
                    }
                }
            }
        }
        if(fullMethod && full_method.size() > 0) {
            FlowVisitor fv = new FlowVisitor();
            for(MethodNode methodNode : full_method) {
                fv.accept(methodNode);
                System.out.println(methodNode.owner.name + "." + methodNode.name + methodNode.desc);
                for(BasicBlock bb : fv.blocks) {
//                    System.out.println(bb.stack().toString());
                    System.out.println(bb.tree().toString());
                }
            }
        }
    }

    public static void main(String[] args) {
//        new FieldDebug("ez", "b", true);


        new FieldDebug("ap", "by", false);

    }
}
