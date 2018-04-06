package vo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.khelekore.prtree.DistanceCalculator;
import org.khelekore.prtree.PointND;
import util.Configuration;

import java.io.Serializable;


/**
 * Created by qijianpeng on 12/02/2017.
 */
public class Point implements KryoSerializable
        , DistanceCalculator<Point>, PointND, Serializable, Cloneable{
    private static final long serialVersionUID = 10L;

    protected String id;
    protected Long timestamp;
    protected double[] attrs = new double[Configuration.DIMENSIONS];

    public double getX(){
        return attrs[0];
    }
    public double getY(){
        return attrs[1];
    }
    public void setX(double x){
        attrs[0] = x;
    }
    public void setY(double y){
        attrs[1] = y;
    }
    public boolean isQuery() {
        return isQuery;
    }

    public void setQuery(boolean query) {
        isQuery = query;
    }

    protected boolean isQuery = false;

    private boolean isBoundary = false;

    public Point(){}
    public Point(String id, double[] attrs, Long timestamp) {
        this.id = id;
        this.attrs = attrs;
        this.timestamp = timestamp;
    }
    public Point(Point point){
        this.id = point.getId();
        this.attrs = point.getAttrs();
        this.timestamp = point .getTimestamp();
        this.isQuery = point.isQuery;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double[] getAttrs() {
        return attrs;
    }

    public void setAttrs(double[] attrs) {
        this.attrs = attrs;
    }

    public void setAttr(Double value, int index){
        this.attrs[index] = value;
    }
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }


    /**
     * Returns the square of the distance between two points.
     * @param attr1
     * @param attr2
     * @return the square of the distance between the two
     * sets of specified coordinates.
     */
    public static double distanceSq(double[] attr1, double[] attr2){
        double sq = 0.0;
        int length = attr1.length;
        for(int i = length-1 ; i >= 0; i--){
            sq += (attr1[i]-attr2[i])*(attr1[i]-attr2[i]);
        }
        return sq;
    }
    /**
     * Returns the square of the distance between two points.
     * @param p1
     * @param p2
     * @return the square of the distance between the two
     * sets of specified coordinates.
     */
    public static double distanceSq(Point p1, Point p2){
        return distanceSq(p1.getAttrs(),p2.getAttrs());
    }

    /**
     * Returns the distance between two points.
     * @param attr1
     * @param attr2
     * @return the distance between the two
     * sets of specified coordinates.
     */
    public static double distance(Double[] attr1, Double[] attr2){
        double sq = 0.0;
        int length = attr1.length;
        for(int i = length-1 ; i >= 0; i--){
            sq += (attr1[i]-attr2[i])*(attr1[i]-attr2[i]);
        }
        return Math.sqrt(sq);
    }
    /**
     * Returns the distance from this <code>Point</code> to a
     * specified <code>Point</code>.
     * @param p
     * @return the distance between this <code>Point</code> and
     * the specified <code>Point</code>.
     */
    public double distance(Point p){
        double distance = 0.0;
        double[] attrs_p = p.getAttrs();
        for(int i = this.attrs.length-1; i >= 0; i--){
            distance += (this.attrs[i] - attrs_p[i])*(this.attrs[i] - attrs_p[i]);
        }
        return Math.sqrt(distance);
    }
    /**
     * Returns the distance from this <code>Point</code> to
     * a specified point.
     * @param attrs
     * @return the distance between this <code>Point</code>
     * and a specified point.
     */
    public double distance(Double[] attrs){
        double distance = 0.0;
        for(int i = this.attrs.length-1; i >= 0; i--){
            distance += (this.attrs[i] - attrs[i])*(this.attrs[i] - attrs[i]);
        }
        return Math.sqrt(distance);
    }

    /**
     * Creates a new object of the same class and with the
     * same contents as this object.
     * @return     a clone of this instance.
     * @exception  OutOfMemoryError            if there is not enough memory.
     * @see        java.lang.Cloneable
     */
    public Point clone() {
        try {
            Point clone = (Point) super.clone();
            double[] attrs = clone.getAttrs();
            double[] attrsClone = new double[attrs.length];

            for(int i = 0; i < attrs.length; i++){
                attrsClone[i] = new Double(attrs[i]);
            }
            clone.setAttrs(attrsClone);
            clone.id = this.id;
            clone.timestamp = this.timestamp;

            return clone;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new Error(e);
        }
    }

    /**
     * Determines whether or not two points are equal. Two instances of
     * <code>Point</code> are equal if the values of their
     * <code>id</code> and <code>timestamp</code> member fields, representing
     * their position in the spatio-temporal space, are the same.
     */
   /* public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point p = (Point) obj;
            return (getTimestamp().equals(p.getTimestamp())) && (this.getId() == p.getId());
        }
        return super.equals(obj);
    }
*/
   /* @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, nodeId);
    }*/

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i = 0; i < attrs.length; i++){
            sb.append(attrs[i]);
            if(i < attrs.length-1) sb.append(",");
        }
        sb.append("]");
        return "[id="+this.id+", time="+this.timestamp+"]";//+", attrs="+sb.toString()+"]";
    }

    public void write(Kryo kryo, Output output) {
        output.writeString(id);
        output.writeLong(timestamp.longValue());
        output.writeDoubles(attrs);
        output.writeBoolean(isBoundary);
        output.writeBoolean(isQuery);
    }

    public void read(Kryo kryo, Input input) {
        this.id = input.readString();
        this.timestamp = input.readLong();
        this.attrs = input.readDoubles(Configuration.DIMENSIONS);
        this.isBoundary = input.readBoolean();
        this.isQuery = input.readBoolean();
    }
    public static Point newInstance(String line){
        String[] attrs = line.split(Configuration.DATA_SEPARATOR);

        Point point = new Point(attrs[0], pointCoords(attrs, 2) ,Long.valueOf(attrs[1]));

        return point;
    }
    private static double[] pointCoords(String[] attrs, int start){
        double[] coords = new double[Configuration.DIMENSIONS];
        for (int i = start; i < start+ Configuration.DIMENSIONS; i++){
            coords[i - start] = Double.valueOf(attrs[i]);
        }
        return coords;
    }

    @Override
    public double distanceTo(Point r, PointND p) {
        double md = (r.attrs[0] - p.getOrd(0))*(r.attrs[0] - p.getOrd(0))
                +(r.attrs[1] - p.getOrd(1))*(r.attrs[1] - p.getOrd(1));
        return Math.sqrt (md);
    }

    @Override
    public int getDimensions() {
        return attrs.length;
    }

    @Override
    public double getOrd(int axis) {
        return attrs[axis];
    }

    public boolean isBoundary() {
        return isBoundary;
    }

    public void setBoundary(boolean boundary) {
        isBoundary = boundary;
    }
}
