package storm.bolt;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import util.Configuration;


import java.io.Serializable;
import java.util.*;


/**
 * Time-based window. Ascending order.
 * K 代表Time或其他升序的标识符.
 * V 代表Value.
 */
public class WindowMap<K extends Number, V > implements KryoSerializable, Serializable, Cloneable{

    transient TreeMap<K, V> windows= null;
    transient K expiredKey = null;
    transient V expired = null;
    transient K newerKey = null;
    transient V newer = null;
    transient int windowLength = 0;

    public WindowMap(WindowMap<K,V> windowMap){
        this.windows = (TreeMap)(windowMap.windows.clone());
        this.expiredKey = windowMap.expiredKey;
        this.expired = windowMap.expired;
        this.newerKey = windowMap.newerKey;
        this.newer = windowMap.newer;
        this.windowLength  = windowMap.windowLength;
    }

    @Override
    public WindowMap clone() throws CloneNotSupportedException {
        WindowMap wm = new WindowMap();
        wm.windows = (TreeMap)this.windows.clone();
        wm.expiredKey = expiredKey;
        wm.expired = expired;
        wm.newer = newer;
        wm.newerKey = newerKey;
        wm.windowLength = windowLength;
        return wm;
    }

    public void clear(){
        windows.clear();
        windowLength = 0;
        expired = null;
        expiredKey = null;
        newer = null;
        newerKey = null;
    }
    public K getExpiredKey() {
        return expiredKey;
    }

    public K getNewerKey() {
        return newerKey;
    }

    public WindowMap(int windowLength){
        this.windowLength = windowLength;
        windows = new TreeMap<>();

    }
    public WindowMap(){
        this.windowLength = 0;
        windows = new TreeMap<>();

    }


    public void push(K k, V v){
        if (k.longValue() < 0)return;
        if ( 0 !=  windows.size()) {
            Long interval = k.longValue() - windows.lastKey().longValue();
            if (interval >= windowLength) {//领先一个窗口以上, shifting.
                windows.clear();
                interval = (long)windowLength;
            }

            if (interval <= windowLength && interval >= 2) {//在窗口之间
                --interval;
                Number last = k.longValue() - interval - 1;
               for (long i = 1; i <= interval; i++){
                    Number key = last.longValue() + i;
                    windows.put((K) key, null);
                    if (windows.size() == windowLength + 1){
                        Map.Entry<K,V> entry = windows.pollFirstEntry();
                        expired = entry.getValue();
                        expiredKey = entry.getKey();
                    }
               }
            } else if (interval < 1 - windowLength) {//落后于窗口
                System.out.println("expired value:{" + k.toString()+" = " +v.toString()+"}" );
                return;
            }
        }

        windows.put(k, v);
       // System.err.println("\n window pushing ......done: "+ k.toString()+"="+v+"\n");
        Map.Entry<K,V> newerEntry = windows.lastEntry();
        newer = newerEntry.getValue();
        newerKey = newerEntry.getKey();
        if (windows.size() == windowLength + 1){
            Map.Entry<K,V> entry = windows.pollFirstEntry();
            expired = entry.getValue();
            expiredKey = entry.getKey();
        }
    }
    public V getExpired(){
        return expired;
    }
    public V getNewer(){
        return newer;
    }
    public V getValue(K k){
        return windows.get(k);
    }

    public WindowMap withWindow(int windowLength){
        this.windowLength = windowLength;
        return this;
    }

    public TreeMap<K, V> getWindows() {
        return windows;
    }

    public void fillTo(K slidestamp) {
        Number interval = slidestamp.longValue() - newerKey.longValue();
        for (long i = 1; i <= interval.longValue() ; i ++){
            Number k = newerKey.longValue() + 1;
            push((K)k, null);
        }
    }



    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, expiredKey);
        kryo.writeClassAndObject(output,expired);
        kryo.writeClassAndObject(output,newerKey);
        kryo.writeClassAndObject(output,newer);
        output.writeInt(windowLength);
        kryo.writeClassAndObject(output,windows);
    }


    @Override
    public void read(Kryo kryo, Input input) {
       this.expiredKey = (K)kryo.readClassAndObject(input);
       this.expired = (V)kryo.readClassAndObject(input);
       this.newerKey = (K)kryo.readClassAndObject(input);
       this.newer = (V) kryo.readClassAndObject(input);
       this.windowLength = input.readInt();
       this.windows = (TreeMap<K,V>)kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "WindowMap{" +
                "windows=" + windows +
                ", expiredKey=" + expiredKey +
                ", expired=" + expired +
                ", newerKey=" + newerKey +
                ", newer=" + newer +
                ", windowLength=" + windowLength +
                '}';
    }
}
