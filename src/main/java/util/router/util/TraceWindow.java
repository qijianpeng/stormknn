package util.router.util;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import util.exception.ExpiredException;

import java.io.Serializable;
import java.util.stream.IntStream;

/**
 * Created by qijianpeng on 09/07/2017.
 */
public class TraceWindow implements IWindow, KryoSerializable, Serializable, Cloneable {
    private static final long serialVersionUID = 2L;
    /**
     * Trace information.
     */
    transient int[] _trace;
    /**
     * Trace time information.
     */
    transient long[] _traceTime;
    /**
     * An expired value in trace.
     */
    transient int expired;
    /**
     * current time pointer.
     */
    transient int first=0;
    /**
     * {@code current time - Window + 1} time pointer.
     */
    transient int last=0;
    /**
     * number of values in trace.
     */
    transient int _windowSize = 0;
    public TraceWindow(int capacity){
        _windowSize = capacity;
        _trace = new int[_windowSize];
        for(int i = 0; i < _windowSize; i++) _trace[i] = -1;
        _traceTime = new long[_windowSize];
    }
    public TraceWindow(){
        _windowSize = 10;
        _trace = new int[_windowSize];
        _traceTime = new long[_windowSize];
    }
    @Override
    public int getNewer() {
        return _trace[first];
    }

    @Override
    public int getOldest() {
        return _trace[last];
    }


    public int getExpired() {
        return expired;
    }

    @Override
    public void push(int o, long time) {
        if ( time <= _traceTime[first] - _windowSize){
            try {
                throw new ExpiredException("Timestamp expired.");
            } catch (ExpiredException e) {
                e.printStackTrace();
            }
        }
        if (_traceTime[first] <= time ){
            if (time - _traceTime[first]  > _windowSize)//领先超过一个窗口长度
                _traceTime[first] = time - _windowSize;
            long t = _traceTime[first];
            for (;;){
                if ( t == time )break;
                t++;
                int index = (int)(t % _windowSize);
                _traceTime[index] = t;
                _trace[index] = -1;
            }
            expired = _trace[last];
            first = (int)(time % _windowSize);
            _trace[first] = (int)o;
            _traceTime[first] = time;
        }
        last = (first + 1) % _windowSize;
    }

    public int get(long time){
        if (time < 0)return -1;
        return _trace[(int)(time % _windowSize)];

    }
    @Override
    public IntStream stream() {
        return IntStream.of(_trace);
    }


    public static void main(String[] args) {
        TraceWindow tw = new TraceWindow(5);
        IntStream.range(12, 25).forEach(i -> {
            tw.push(i, (long) i);
            System.out.println("newer: "+tw.getNewer());
        });
        tw.stream().forEach(e -> System.out.println(e));
        System.out.println("expired: "+tw.getExpired());


    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        stream().forEach(e ->{
            sb.append(e+",");
        });

        return sb.toString();
    }

    @Override
    public TraceWindow clone()  {
        TraceWindow tw = new TraceWindow();
        tw._trace = _trace.clone();
        tw._traceTime = _traceTime.clone();
        tw._windowSize = _windowSize;
        tw.expired = expired;
        tw.first = first;
        tw.last = last;
        return tw;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(_windowSize);
        output.writeInts( _trace);
        output.writeLongs(_traceTime);
        output.writeInt(expired);
        output.writeInt(first);
        output.writeInt(last);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        _windowSize = input.readInt();
        _trace =  input.readInts(_windowSize);
        _traceTime = input.readLongs(_windowSize);
        expired = input.readInt();
        first = input.readInt();
        last = input.readInt();

    }
}
