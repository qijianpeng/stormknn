package util;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qijianpeng on 19/02/2017.
 */
public class XMLUtil {
    /**
     * Returns the configurations using a <code>Map<K,V> structure </code> from xml.
     * @return {@code Map<name:String, value:object>}
     * @throws Exception
     */
    public static Map<String, Object> readConfig(InputStream xml) throws Exception {
        Map<String, Object> cfg = new HashMap<String, Object>();
        SAXReader reader = new SAXReader();
        Document doc = reader.read(xml);//reader.read(xmlPath);
        Element root = doc.getRootElement();
        List<Element> list = root.elements();
        for(Element e : list){
            //reads
            String key = e.attributeValue("name");
            String value = e.getText();
            String type = e.attributeValue("type");
            if (null == type)System.err.println("name "+ key+" doesn't set type");
            //init
            Class Clz = Class.forName(type);
            Constructor constructor = Clz.getConstructor(String.class);
            Object o  = constructor.newInstance(value);
            //store
            cfg.put(key,o);
        }
        return cfg;
    }
}
