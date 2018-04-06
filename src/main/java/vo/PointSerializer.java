package vo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import util.Configuration;

import java.io.Serializable;

/**
 * Created by qijianpeng on 08/08/2017.
 * mail: jianpengqi@126.com
 */
public class PointSerializer extends com.esotericsoftware.kryo.Serializer<Point> implements Serializable {
    @Override
    public void write(Kryo kryo, Output output, Point point) {
        output.writeString(point.id);
        output.writeLong(point.timestamp);
        output.writeDoubles(point.attrs);
    }

    @Override
    public Point read(Kryo kryo, Input input, Class<Point> type) {
        Point point = new Point();
        point.id = input.readString();
        point.timestamp = input.readLong();
        point.attrs = input.readDoubles(Configuration.DIMENSIONS);
        return point;
    }
}
