import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.esotericsoftware.minlog.Log;
import org.apache.storm.shade.org.apache.curator.shaded.com.google.common.collect.ImmutableList;
import storm.bolt.WindowMap;
import util.router.util.TraceWindow;
import vo.Point;
import vo.PointSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by qijianpeng on 21/07/2017.
 */
public class TestKryo {

    public static void main(String[] args) throws CloneNotSupportedException {
     //   Log.TRACE();
      /*  Point p = new Point("1", new double[]{1.1,2.2}, 1L);
        Set<Point> set = new HashSet<>();
        set.add(p);
        p.setBoundary(true);
        GridTree gt = GridTree.getInstance();
        set.add(p);
        gt.finalIndexesNode(set);
        Node node = gt.getRoot();

      //  Node node = new Node();
        node.getPair().getNewSlide().add(p);
        node.setArea(new double[][]{{1.2, 2.3},{3.4, 4.5}});
        node.getPair().getCounts()[2] = 10;
        System.err.println(node.getPair().getNewSlide().toString());
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(true);
        kryo.register(Node.class);
        kryo.register(double[][].class);
        kryo.register(double[].class);
        kryo.register(HashSet.class);
        kryo.register(Point.class);
        kryo.register(ArrayList.class);
        kryo.register(LinkedList.class);
        kryo.register(State.class);
        kryo.register(Pair.class);
        *//*CollectionSerializer hashSetSerializer = new CollectionSerializer();
        hashSetSerializer.setElementsCanBeNull(false);
        hashSetSerializer.setElementClass(Point.class, kryo.getSerializer(Point.class));

        kryo.register(HashSet.class, hashSetSerializer);*//*
        Output output = null;
        try {
            output = new Output(new FileOutputStream("./file.bin"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        node.getTrace().push(1,222);
        kryo.writeObject(output, node);
        kryo.writeObject(output, ImmutableList.of(set));
        *//*kryo.writeObject(output, p);*//*
       *//* kryo.writeObject(output,set);*//*
        output.flush();
        output.close();

        Input input = null;
        try {
            input = new Input(new FileInputStream("./file.bin"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        kryo = new Kryo();
        kryo.setRegistrationRequired(true);
        kryo.register(Node.class);
        kryo.register(double[][].class);
        kryo.register(double[].class);
        kryo.register(HashSet.class);
        kryo.register(Point.class);
        kryo.register(ArrayList.class);
        kryo.register(LinkedList.class);
        kryo.register(List.class);
        kryo.register(State.class);
        kryo.register(Pair.class);
      //  kryo.register(HashSet.class, hashSetSerializer);

        Node res = kryo.readObject(input,Node.class);
        System.out.println(kryo.readObject(input, ImmutableList.class).toString());
      *//* Point pp = kryo.readObject(input, Point.class);*//*
      // Set res = kryo.readObject(input, HashSet.class);
       System.err.println(res.getSlidePoint().toString());
      // System.out.println(res.toString());

        input.close();*/


    }
}