/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.objectweb.asm.commons;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.ParameterAnnotationNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * A {@link LocalVariablesSorter} for type mapping.
 * 
 * @author Eugene Kuleshov
 */
public class RemappingMethodAdapter extends LocalVariablesSorter {

	protected final Remapper remapper;

	protected RemappingMethodAdapter(final int access, final String desc, final MethodVisitor mv,
			final Remapper remapper) {
		super(access, desc, mv);
		this.remapper = remapper;
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		AnnotationVisitor av = super.visitAnnotationDefault();
		return av == null ? av : new RemappingAnnotationAdapter(av, remapper);
	}

	@Override
	public AnnotationVisitor visitAnnotation(AnnotationNode annotation, boolean visible) {
		AnnotationVisitor av = super.visitAnnotation(new AnnotationNode(remapper.mapDesc(annotation.desc)), visible);
		return av == null ? av : new RemappingAnnotationAdapter(av, remapper);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(TypeAnnotationNode type, boolean visible) {
		AnnotationVisitor av = super.visitTypeAnnotation(
				new TypeAnnotationNode(type.typeRef, type.typePath, remapper.mapDesc(type.desc)), visible);
		return av == null ? av : new RemappingAnnotationAdapter(av, remapper);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(ParameterAnnotationNode param, boolean visible) {
		AnnotationVisitor av = super.visitParameterAnnotation(
				new ParameterAnnotationNode(param.parameter, remapper.mapDesc(param.desc)), visible);
		return av == null ? av : new RemappingAnnotationAdapter(av, remapper);
	}

	@Override
	public void visitFrame(FrameNode frame) {
		super.visitFrame(
				new FrameNode(frame.type, frame.local.size(), remapEntries(frame.local.size(), frame.local.toArray()),
						frame.stack.size(), remapEntries(frame.stack.size(), frame.stack.toArray())));
	}

	private Object[] remapEntries(int n, Object[] entries) {
		for (int i = 0; i < n; i++) {
			if (entries[i] instanceof String) {
				Object[] newEntries = new Object[n];
				if (i > 0) {
					System.arraycopy(entries, 0, newEntries, 0, i);
				}
				do {
					Object t = entries[i];
					newEntries[i++] = t instanceof String ? remapper.mapType((String) t) : t;
				} while (i < n);
				return newEntries;
			}
		}
		return entries;
	}

	@Override
	public void visitFieldInsn(FieldInsnNode field) {
		super.visitFieldInsn(new FieldInsnNode(field.opcode(), remapper.mapType(field.owner),
				remapper.mapFieldName(field.owner, field.name, field.desc), remapper.mapDesc(field.desc)));
	}

	@Override
	public void visitMethodInsn(MethodInsnNode method) {
		if (mv != null) {
			mv.visitMethodInsn(new MethodInsnNode(method.opcode(), remapper.mapType(method.owner),
					remapper.mapMethodName(method.owner, method.name, method.desc), remapper.mapMethodDesc(method.desc),
					method.itf));
		}
	}

	@Override
	public void visitInvokeDynamicInsn(InvokeDynamicInsnNode invoke) {
		for (int i = 0; i < invoke.bsmArgs.length; i++) {
			invoke.bsmArgs[i] = remapper.mapValue(invoke.bsmArgs[i]);
		}
		super.visitInvokeDynamicInsn(
				new InvokeDynamicInsnNode(remapper.mapInvokeDynamicMethodName(invoke.name, invoke.desc),
						remapper.mapMethodDesc(invoke.desc), (Handle) remapper.mapValue(invoke.bsm), invoke.bsmArgs));
	}

	@Override
	public void visitTypeInsn(TypeInsnNode type) {
		super.visitTypeInsn(new TypeInsnNode(type.opcode(), remapper.mapType(type.desc)));
	}

	@Override
	public void visitLdcInsn(LdcInsnNode ldc) {
		super.visitLdcInsn(new LdcInsnNode(remapper.mapValue(ldc.cst)));
	}

	@Override
	public void visitMultiANewArrayInsn(MultiANewArrayInsnNode array) {
		super.visitMultiANewArrayInsn(new MultiANewArrayInsnNode(remapper.mapDesc(array.desc), array.dims));
	}

	@Override
	public AnnotationVisitor visitInsnAnnotation(TypeAnnotationNode annotation, boolean visible) {
		AnnotationVisitor av = super.visitInsnAnnotation(
				new TypeAnnotationNode(annotation.typeRef, annotation.typePath, remapper.mapDesc(annotation.desc)),
				visible);
		return av == null ? av : new RemappingAnnotationAdapter(av, remapper);
	}

	@Override
	public void visitTryCatchBlock(TryCatchBlockNode tryCatch) {
		super.visitTryCatchBlock(new TryCatchBlockNode(tryCatch.start, tryCatch.end, tryCatch.handler,
				tryCatch.type == null ? null : remapper.mapType(tryCatch.type)));
	}

	@Override
	public AnnotationVisitor visitTryCatchAnnotation(TypeAnnotationNode annotation, boolean visible) {
		AnnotationVisitor av = super.visitTryCatchAnnotation(
				new TypeAnnotationNode(annotation.typeRef, annotation.typePath, remapper.mapDesc(annotation.desc)),
				visible);
		return av == null ? av : new RemappingAnnotationAdapter(av, remapper);
	}

	@Override
	public void visitLocalVariable(LocalVariableNode local) {
		super.visitLocalVariable(new LocalVariableNode(local.name, remapper.mapDesc(local.desc),
				remapper.mapSignature(local.signature, true), local.start, local.end, local.index));
	}

	@Override
	public AnnotationVisitor visitLocalVariableAnnotation(LocalVariableAnnotationNode annotation, boolean visible) {
		AnnotationVisitor av = super.visitLocalVariableAnnotation(
				new LocalVariableAnnotationNode(annotation.typeRef, annotation.typePath,
						annotation.start.toArray(new LabelNode[0]), annotation.end.toArray(new LabelNode[0]),
						annotation.index.stream().mapToInt(i -> i).toArray(), remapper.mapDesc(annotation.desc)),
				visible);
		return av == null ? av : new RemappingAnnotationAdapter(av, remapper);
	}
}
