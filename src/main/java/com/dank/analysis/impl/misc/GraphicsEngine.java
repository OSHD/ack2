package com.dank.analysis.impl.misc;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.tree.ClassNode;

import java.awt.*;
import java.lang.reflect.Modifier;

/**
 * Created by RSynapse on 1/24/2016.
 */
public class GraphicsEngine extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        if (Modifier.isAbstract(cn.access) && cn.fieldCount(Image.class) == 1) {
            return new ClassSpec(Hook.GRAPHICS_ENGINE, cn);
        }
        return null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        cn.fields.stream().filter(field -> field.desc.equals("Ljava/awt/Image;")).forEach(node -> {
            Hook.GRAPHICS_ENGINE.put(new RSField(node, "bitmap"));
        });
        cn.fields.stream().filter(field -> field.desc.equals("[I")
                && Modifier.isPublic(field.access) && !Modifier.isStatic(field.access)).forEach(node -> {
            Hook.GRAPHICS_ENGINE.put(new RSField(node, "engineRaster"));
        });
    }
}
