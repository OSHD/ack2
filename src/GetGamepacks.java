import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Project: RS3Injector
 * Time: 17:36
 * Date: 11-02-2015
 * Created by Dogerina.
 */
public class GetGamepacks {

    public static void main(String... args) {
        for (int rev = 65; rev <= 103; rev++) {
            if (dl(k(rev), new File(verbose(rev)))) {
                System.out.printf("Downloaded %d!%n", rev);
            }
        }
    }

    private static boolean dl(String url, File target) {
        try (InputStream in = new URL(url).openStream()) {
            ReadableByteChannel channel = Channels.newChannel(in);
            FileOutputStream out = new FileOutputStream(target);
            out.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String verbose(int rev) {
        return rev + ".jar";
    }

    private static String k(int rev) {
        return String.format("http://revtek.x10.mx/resources/gamepacks/RS2007_%d.jar", rev);
    }
}
