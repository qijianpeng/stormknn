package vo;

import org.khelekore.prtree.MBRConverter;

import java.io.Serializable;

/**
 * Created by qijianpeng on 01/03/2018.
 * mail: jianpengqi@126.com
 */
public class Point2DConverter implements MBRConverter<Point> {
    public int getDimensions () {
        return 2;
    }

    public double getMin (int axis, Point t) {
        return axis == 0 ? t.getAttrs()[0] : t.getAttrs()[1];
    }

    public double getMax (int axis, Point t) {
        return axis == 0 ? t.getAttrs()[0] : t.getAttrs()[1];
    }
}
