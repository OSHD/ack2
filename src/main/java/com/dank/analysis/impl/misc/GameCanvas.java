/**
 * Copyright (c) 2015 Kyle Friz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.dank.analysis.impl.misc;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * @author Kyle Friz
 * @since  Oct 21, 2015
 */
public class GameCanvas extends Analyser {

	/* (non-Javadoc)
	 * @see com.dank.analysis.Analyser#specify(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	public ClassSpec specify(ClassNode cn) {
		return cn.superName("java/awt/Canvas") ? new ClassSpec(Hook.GAME_CANVAS, cn) : null;
	}

	/* (non-Javadoc)
	 * @see com.dank.analysis.Analyser#evaluate(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	public void evaluate(ClassNode cn) {
		for (FieldNode field : cn.fields) {
			if (field.desc.equals("Ljava/awt/Component;")) {
				Hook.GAME_CANVAS.put(new RSField(field, "component"));
			}
		}
	}

}
