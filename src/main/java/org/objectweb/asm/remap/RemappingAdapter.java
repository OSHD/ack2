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
package org.objectweb.asm.remap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;

import com.dank.asm.ClassPath;

/**
 * @author Kyle Friz
 * @since  Aug 8, 2015
 */
public class RemappingAdapter extends Remapper {

	private final ClassPath path;
	
	private final HashMap<String, RemappingData> classes = new HashMap<String, RemappingData>();
	private final HashMap<String, RemappingData> fields = new HashMap<String, RemappingData>();
	private final HashMap<String, RemappingData> methods = new HashMap<String, RemappingData>();

	public RemappingAdapter(ClassPath path) {
		this.path = path;
	}

	public void remapClass(RemappingData map) {
		classes.put(map.getName(), map);
	}
	
	public void remapField(String owner, String desc, RemappingData map) {
		fields.put(new String(owner + "" + map.getName() + "#" + desc), map);
	}
	
	public void remapMethod(String owner, String desc, RemappingData map) {
		methods.put(new String(owner + "" + map.getName() + "#" + desc), map);
	}
	
    @Override
    public String map(String owner) {
        if (classes.containsKey(owner))
            return classes.get(owner).getNewName();
        
        return owner;
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        String obfKey = owner + "" + name + "#" + desc;
        
        if (fields.containsKey(obfKey))
            return fields.get(obfKey).getNewName();
        else {
        	ClassNode node = path.get(owner);
        	if (node != null) {
        		List<ClassNode> supers = path.getSupers(node);
        		for (int i = supers.size() - 1; i >= 0; i--) {
        			ClassNode super_ = supers.get(i);
        			if (fields.containsKey(obfKey.replace(owner, super_.name)))
        				return fields.get(obfKey.replace(owner, super_.name)).getNewName();
        		}
        	}
        }
        return name;
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        String obfKey = owner + "" + name + "#" + desc;
        
        if (methods.containsKey(obfKey))
            return methods.get(obfKey).getNewName();
        else {
        	ClassNode node = path.get(owner);
        	if (node != null) {
        		List<ClassNode> supers = path.getSupers(node);
        		for (int i = supers.size() - 1; i >= 0; i--) {
        			ClassNode super_ = supers.get(i);
        			if (methods.containsKey(obfKey.replace(owner, super_.name)))
        				return methods.get(obfKey.replace(owner, super_.name)).getNewName();
        		}
        	}
        }
        return name;
    }
    
    public void manipulate() {
        List<ClassNode> refactored = new ArrayList<>();
        for (ClassNode cn : path.getClasses()) {
            ClassNode node = new ClassNode();
            cn.accept(new RemappingClassAdapter(node, this));
            
            refactored.add(node);
        }
        path.clear();
        for (ClassNode cn : refactored) {
        	path.addClass(cn);
        }
    }

	public void clear() {
		classes.clear();
		fields.clear();
		methods.clear();
	}
	
}