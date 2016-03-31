package com.dank;

import com.dank.util.ProgressCallback;
import com.dank.util.io.Crawling;
import com.dank.util.io.VirtualBrowser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.prefs.Preferences;

/**
 * @author Septron
 * @since February 12, 2015
 */
public class VersionChecker extends Observable implements Runnable {

    @Override
    public void run() {
        long timeSinceLastCheck = System.currentTimeMillis();
        int version = 108;
        try {
            System.out.println("... Fetching live version");
            int old = Integer.parseInt((String) DankBuild.instance.get("curRevision"));
            int now = version = Crawling.getVersion(Crawling.ServerType.OS, 2, old, version);
            DankBuild.instance.set("curRevision", String.valueOf(now));
            DankBuild.instance.save();
            System.out.println("Version=" + now);
        } catch (Exception e) {
            System.err.println("Failed to get live version");
        }
        if (System.currentTimeMillis() > timeSinceLastCheck + 300) {
            int now = 0;
            try {
                now = Crawling.getVersion(Crawling.ServerType.OS, 2, version, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert now != 0;

            if (now >= version && DankEngine.fetch) { //change back to >
                System.out.println("RuneScape update found @ " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                System.out.println("Version has updated to " + now + "!");
                System.out.print("Started download...");
                final VirtualBrowser virtual = new VirtualBrowser();
                try (FileOutputStream out = new FileOutputStream(new File("./" + now + ".jar"))) {
                    out.write(virtual.getRaw(new URL(Crawling.getPackURL()), new ProgressCallback() {

                        private int last = 0;

                        @Override
                        public void update(int off, int len) {
                            final int percent = ((off * 100) / len);
                            if (percent >= last + 10) {
                                System.out.print(" " + percent + "% ");
                                last = percent;
                            }
                        }
                    }));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.hasChanged();
                super.notifyObservers();
            }
        }
    }

    public static void main(String[] args) {
    }

    private void writeToRegistry(String key, String value) {
        Preferences userPref = Preferences.userRoot();
        userPref.put(key, value);
        String s = userPref.get(key, "^@%");
        if (s.equals("^@%"))
            System.out.println("Failed to save settings...");
        else
            System.out.println("Settings saved.");
    }

    private String readRegistry(String key) {
        Preferences userPref = Preferences.userRoot();
        String s = userPref.get(key, "null");
        return s;
    }
}
