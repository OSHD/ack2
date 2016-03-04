package dank.stresser;

/**
 * @author Septron
 * @since January 20, 2015
 */
public class Stopwatch {

    private boolean running = false;

    private long start = 0;
    private long stop = 0;

    public void start() {
        start = System.currentTimeMillis();
        running = true;
    }

    public void stop() {
        stop = System.currentTimeMillis();
        running = false;
    }

    public long ms() {
        if (running)
            return System.currentTimeMillis() - start;
        else
            return stop - start;
    }

    public String formatted() {
        long time = ms() / 1000;
        return String.format("%02d:%02d:%02d", time / 3600, (time % 3600) / 60, time % 60);
    }
}
