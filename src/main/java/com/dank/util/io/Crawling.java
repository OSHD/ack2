package com.dank.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project: RS3Injector
 * Time: 05:42
 * Date: 07-02-2015
 * Created by Dogerina.
 */
public class Crawling {
    public static String getPackURL() {
        try {
            String server = "oldschool2.runescape.com";
            Pattern gamepack = Pattern.compile("archive=(.*)\'");
            VirtualBrowser virtual = new VirtualBrowser();
            String data = virtual.get(new URL("http://" + server));
            Matcher matcher;
            if ((matcher = gamepack.matcher(data)) != null && matcher.find()) {
                return ("http://" + server + "/" + matcher.group(1).trim());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static int getVersion(ServerType type, int world, int initial_major, int initial_minor) throws IOException {
        final String address = getServerAddress(world, type);
        System.out.println("Server Address: " + address);
        Socket socket;
        InputStream input;
        OutputStream output;
        int version = initial_major;
        while (true) {
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, 43594), 10000);
            input = socket.getInputStream();
            output = socket.getOutputStream();
            ByteBuffer handshake = RSHandshakeFactory.mkHandshake(world, version, initial_minor, type);
            output.write(handshake.array());
            output.flush();
            while (true) {
                if (input.available() > 0) {
                    int response = input.read();
                    if (response == (type == ServerType.OS ? 0 : 48)) {
                    	socket.close();
                        return version;
                    } else if (response == 6) {
                        System.out.println("Nope @ " + version);
                        version++;
                        socket.close();
                        break;
                    } else {
                    	socket.close();
                        throw new Error("wut @ " + response);
                    }
                }
            }
        }
    }

    public static String getServerAddress(int world, ServerType type) {
        if (type == ServerType.RS3) {
            return "world" + world + ".runescape.com";
        } else if (type == ServerType.OS) {
            return "oldschool" + world + ".runescape.com";
        } else throw new Error("Unknown Server Type: " + type);
    }

    public static enum ServerType {
        RS3, OS
    }

    public static final class RSHandshakeFactory {

        public static final Pattern PARAM_PATTERN = Pattern.compile("<param name=\"?([^\\s]+)\"?\\s+value=\"?([^>]*)\"?>",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        public static final int SESSION_PARAM_INDEX = 31;

        public static ByteBuffer mkHandshake(int world, int major, int minor, ServerType type) {
            if (type == ServerType.RS3) {
                String sessionID = getSessionID(world);
                if (sessionID == null) throw new InternalError("SessionID == null");
                ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 4 + 4 + 32 + 1);
                buffer.put((byte) 15);                   // handshake type ---------------- 1
                buffer.put((byte) ((4 + 4 + 1) + 32)); // remaining size ---------------- 1
                buffer.putInt(major);                  // major version ----------------- 4
                buffer.putInt(minor);                    // minor version ----------------- 4
                buffer.put(sessionID.getBytes());        // session ----------------------- 32
                buffer.put((byte) 0);                    // wut? -------------------------- 1
                return buffer;
            } else if (type == ServerType.OS) {
                ByteBuffer buffer = ByteBuffer.allocate(4 + 1);
                buffer.put((byte) 15); // handshake type ------- 1
                buffer.putInt(major);  // major version -------- 4
                return buffer;
            } else {
                throw new Error("Unhandled Handshake: " + type);
            }
        }

        public static String downloadPage(String address) {
            URL url;
            try {
                url = new URL(address);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                return null;
            }
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
                StringBuilder page = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    page.append(line);
                return page.toString();
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        public static String getSessionID(int world) {
            HashMap<String, String> params = new HashMap<>();
            try {
                final String pageSource = downloadPage("https://" + getServerAddress(world, ServerType.RS3) + "/g=runescape/,j0");
                final Matcher regexMatcher = PARAM_PATTERN.matcher(pageSource);
                while (regexMatcher.find()) {
                    if (!params.containsKey(regexMatcher.group(1))) {
                        params.put(regexMatcher.group(1).replaceAll("\"", ""),
                                regexMatcher.group(2).replaceAll("\"", ""));
                    }
                }
            } catch (Exception e) {
                System.out.println("failed : " + e.getMessage());
            }
            return params.size() >= SESSION_PARAM_INDEX ? params.get(String.valueOf(SESSION_PARAM_INDEX)) : null;
        }
    }
}
