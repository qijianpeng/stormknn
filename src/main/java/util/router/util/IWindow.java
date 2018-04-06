package util.router.util;

import java.util.stream.IntStream;

/**
 * Created by qijianpeng on 09/07/2017.
 */
public interface IWindow {
    public int getNewer();
    public int getOldest();
    public int getExpired();

    public void push(int t, long timeIndex);
    public int get(long time);
    public IntStream stream();

}
