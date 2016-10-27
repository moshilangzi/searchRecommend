package com.comm.searchrecommend.utils;

import com.comm.searchrecommend.entity.ThreadShardEntity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by jasstion on 27/10/2016.
 */
public class ThreadLocalHelper {



    public static ThreadShardEntity getThreadShardEntity() throws Exception {


        ThreadShardEntity threadShardEntity=null;

        Thread otherThread = Thread.currentThread(); // get a reference to the otherThread somehow (this is just for demo)

        Field field = Thread.class.getDeclaredField("threadLocals");
        field.setAccessible(true);
        Object map = field.get(otherThread);

        Method method = Class.forName("java.lang.ThreadLocal$ThreadLocalMap").getDeclaredMethod("getEntry", ThreadLocal.class);
        method.setAccessible(true);
        WeakReference entry = (WeakReference) method.invoke(map, threadShardEntity);

        Field valueField = Class.forName("java.lang.ThreadLocal$ThreadLocalMap$Entry").getDeclaredField("value");
        valueField.setAccessible(true);
        ThreadShardEntity value = (ThreadShardEntity)valueField.get(entry);
        return threadShardEntity;
    }

public static void main(String[] args) throws Exception {
    UUID uuid = UUID.randomUUID();
    //每次服务请求对应的唯一id
    String uuidStr = uuid.toString();
    ThreadLocal<ThreadShardEntity> threadShardEntity = new ThreadLocal<ThreadShardEntity>();
    ThreadShardEntity threadShardEntity_=new ThreadShardEntity(uuidStr);
    threadShardEntity.set(threadShardEntity_);



    Thread otherThread = Thread.currentThread(); // get a reference to the otherThread somehow (this is just for demo)

    Field field = Thread.class.getDeclaredField("threadLocals");
    field.setAccessible(true);
    Object map = field.get(otherThread);

    Method method = Class.forName("java.lang.ThreadLocal$ThreadLocalMap").getDeclaredMethod("getEntry", ThreadLocal.class);
    method.setAccessible(true);
    WeakReference entry = (WeakReference) method.invoke(map, threadShardEntity);

    Field valueField = Class.forName("java.lang.ThreadLocal$ThreadLocalMap$Entry").getDeclaredField("value");
    valueField.setAccessible(true);
    ThreadShardEntity value = (ThreadShardEntity)valueField.get(entry);

    System.out.print(value.getSearchId());

}


}
