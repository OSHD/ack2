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
package com.dank.util.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.asm.InsnMatcher;
import com.dank.asm.InsnNodeUtils;

/**
 * @author Kyle Friz
 * @since  Oct 17, 2015
 */
public class Fetcher {

	public static File getFile() {
		ConfigCrawler crawler = new ConfigCrawler();
		try {
			crawler.crawl();
			
			JarInputStream input = crawler.stream();
			String hash = Integer.toString(input.getManifest().hashCode());
			int revision = -1;
			
			JarEntry entry;
			while ((entry = input.getNextJarEntry()) != null) {
				if (entry.getName().equals("client.class")) {
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					byte[] bytes = new byte[4096];
					int read;
			        while ((read = input.read(bytes ,0 , bytes.length)) > -1)         
			        	output.write(bytes, 0, read);
					
			        ClassReader reader = new ClassReader(output.toByteArray());
					ClassNode node = new ClassNode();
					reader.accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
					revision = getRevision(node);
				}
			}
			input.close();

			if (!Files.exists(Paths.get("").resolve("./jars/" + revision + "/" + hash + "/"))) {
				Files.createDirectories(Paths.get("").resolve("./jars/" + revision + "/"));
				crawler.download(Paths.get("").resolve("./jars/" + revision + "/").toString());
			}
			
			System.out.println("Fetched gamepack " + revision + "(" + hash + ") with " + crawler.size() + " parameter(s).");
			System.out.println();
			
			return Paths.get("").resolve("./jars/" + revision + "/gamepack.jar").toFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new File("");
	}
	
	public static int getRevision(ClassNode node) {
		for (Object mn : node.methods) {
			MethodNode method = (MethodNode) mn;
			if (!method.name.equals("init"))
				continue;
			
			InsnMatcher matcher = new InsnMatcher(method.instructions);
			for (Iterator<AbstractInsnNode[]> it = matcher.match("SIPUSH SIPUSH BIPUSH"); it.hasNext();) {
				AbstractInsnNode[] nodes = it.next();
				if (InsnNodeUtils.getNumericPushValue(nodes[0]) != 765)
					continue;
				
				if (InsnNodeUtils.getNumericPushValue(nodes[1]) != 503)
					continue;
				
				return (int) InsnNodeUtils.getNumericPushValue(nodes[2]);
			}
		}
		return -1;
	}
	
}
