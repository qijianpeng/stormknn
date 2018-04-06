package util.router;



import gnu.trove.procedure.TIntCharProcedure;
import gnu.trove.procedure.TIntProcedure;
import util.SpatialIndex;
import util.rtree.Node;
import util.rtree.RTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.Configuration;
import util.Rectangle;
import vo.Point;

import java.util.*;

/**
 * Created by qijianpeng on 22/02/2017.
 */
public class Router {
    Map conf;
    public Router(Map stormConf) {
        conf = stormConf;
        INDEX_HIGHT = Integer.valueOf(conf.get(Configuration.NAME_INDEX_HIGHT).toString());
        BOUNDARY_LENGTH_SCALE = Double.valueOf(conf.get(Configuration.NAME_BOUNDARY_LENGTH_SCALE).toString());
    }
    public Router() { }
    class SaveToIntCharProcedure implements TIntCharProcedure {
        private Map<Integer, Character> ids = new HashMap<Integer, Character>();


        private Map<Integer, Character> getIds() {
            return ids;
        }
        public void clear(){
            ids.clear();
        }

        @Override
        public boolean execute(int a, char b) {
            ids.put(a, b);
            return true;
        }
    };
    private static final Logger LOG = LoggerFactory.getLogger(Router.class);
    private long SLIDE_TIME = 0;
    private int INDEX_HIGHT ;//Configuration.INDEX_HIGHT;
    private double BOUNDARY_LENGTH_SCALE ;// Configuration.BOUNDARY_LENGTH_SCALE;
    private double[][] area = Configuration.DEFAULT_AREA;
    private double MIN_CELL_WIDTH = Configuration.RADIUS/(2*Math.sqrt(Configuration.DIMENSIONS));
    private double boundaryLength = 0.0;
    SpatialIndex si = null;
    public void init(){//initialization.
        double minSide = minSideOfArea();
        long leafNum = 1 << (INDEX_HIGHT - 1);
        double leafLength = minSide / leafNum;
        boundaryLength = leafLength * BOUNDARY_LENGTH_SCALE;


        si = new RTree();
        Properties properties = new Properties();
        properties.setProperty("BOUNDARY_LENGTH", ""+boundaryLength);
        si.init(properties);

        int id = (1 << (INDEX_HIGHT - 1)) - 1;

        double x = area[0][0], y = area[1][0];
        double xInterval = (area[0][1] - area[0][0]) / leafNum;
        double yInterval = (area[1][1] - area[1][0]) / leafNum;

        for (int row = 0; row < leafNum; row ++){
            for (int col = 0; col < leafNum; col ++){
               Rectangle rec =  new Rectangle((float)( x  - boundaryLength) ,(float)( y  - boundaryLength),
                       (float)( x + xInterval + boundaryLength),
                       (float)(y + yInterval + boundaryLength));

                si.add(rec, id);
                x = x + xInterval;
                id ++;
            }
            x = area[0][0];// back to the start column.
            y = y + yInterval;
        }
        System.out.println();
    }//init

    SaveToIntCharProcedure stp =  new SaveToIntCharProcedure();
    public Map<Integer, Character> getIndexes(Point point){
        try {
            float x =  (float) point.getAttrs()[0];
            float y = (float) point.getAttrs()[1];
            si.nearest(new util.Point(x, y),stp,Float.MIN_VALUE);
            return new HashMap<Integer, Character>(stp.getIds());
        }finally {
            stp.getIds().clear();//clear buffer.
        }

    }//getIndexes


    public void clearBuffer() {
    }


    private double minSideOfArea(){

        double min = Double.MAX_VALUE;
        for(double[] minmax : area){
            double sideLength = minmax[1] - minmax[0];
            if(min > sideLength) min = sideLength;
        }
        return min;
    }
}
