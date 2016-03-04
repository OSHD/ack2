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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;

/**
 * @author Kyle Friz
 * @since Feb 22, 2015
 */
public class ConfigCrawler {

	private Map<String, String> parameters = new HashMap<String, String>();

	public void crawl() throws IOException {
		URL url = new URL("http://oldschool.runescape.com/l=en/jav_config.ws");

		try (InputStream is = new BufferedInputStream(url.openStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("codebase"))
					parameters.put("codebase", line.substring(9));
				else if (line.startsWith("initial_jar"))
					parameters.put("initial_jar", line.substring(12));
				else if (line.startsWith("param")) {
					String[] args;
					if (line.contains("halign")) {
						args = line.substring(6).split("=ha");
						parameters.put(args[0], "ha" + args[1]);
					} else if (line.contains("services") || line.contains("slr.ws")) {
						args = line.substring(6).split("=http");
						parameters.put(args[0], "http" + args[1]);
					} else {
						args = line.substring(6).split("=");
						if (args.length == 1)
							parameters.put(args[0], "");
						else
							parameters.put(args[0], args[1]);
					}
				}
			}
			is.close();
			reader.close();
		}
	}

	/**
	 * Downloads and saves the gamepack. Also saves the parameters
	 * @param path 
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void download(String path) throws FileNotFoundException, IOException {
		URL url = new URL(parameters.get("codebase") + parameters.get("initial_jar"));
		try (InputStream is = new BufferedInputStream(url.openStream());
				OutputStream os = new BufferedOutputStream(new FileOutputStream(path + "/gamepack.jar"))) {

			int read;
			while ((read = is.read()) != -1) {
				os.write(read);
			}
			is.close();
			os.close();
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/parameters.txt"))) {
			for (String name : parameters.keySet()) {
				writer.write("map.put(\"" + name + "\", \"" + parameters.get(name) + "\");");
				writer.newLine();
			}
			writer.close();
		}
	}
	
	public JarInputStream stream() throws MalformedURLException, IOException {
		return new JarInputStream(new URL(parameters.get("codebase") + parameters.get("initial_jar")).openStream());
	}
	
	public int size() {
		return parameters.size();
	}

}
