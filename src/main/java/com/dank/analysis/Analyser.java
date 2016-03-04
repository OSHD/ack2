package com.dank.analysis;

import org.objectweb.asm.tree.ClassNode;

import com.dank.DankEngine;
import com.dank.asm.ClassPath;
import com.dank.hook.Hook;
import com.dank.hook.HookSpecification;

/**
 * Project: RS3Injector
 * Time: 06:15
 * Date: 07-02-2015
 * Created by Dogerina.
 */
public abstract class Analyser {

    protected ClassSpec spec;

    public abstract ClassSpec specify(final ClassNode cn);
    public abstract void evaluate(final ClassNode cn);

    public final ClassPath getClassPath() {
        return DankEngine.classPath;
    }

    public final void execute(final ClassPath classPath) {
        classPath.getClasses()
                .stream()
                .filter(cn -> {
                    ClassSpec spec = specify(cn);
                    if (spec != null) {
                        this.spec = spec;
                        return true;
                    }
                    return false;
                })
                .forEach(this::evaluate);
    }

    protected final class ClassSpec implements HookSpecification {

        private final Hook container;
        private final String definedName, name;

        public ClassSpec(final Hook container, final String name) {
            this.container = container;
            this.definedName = container.getDefinedName();
            this.name = name;
            container.setInternalName(name);
        }

        public ClassSpec(final Hook container, final ClassNode node) {
            this(container, node.name);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDefinedName() {
            return definedName;
        }

        public Hook getContainer() {
            return container;
        }
    }
}
