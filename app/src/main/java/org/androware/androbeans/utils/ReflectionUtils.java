package org.androware.androbeans.utils;



import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jkirkley on 5/21/16.
 */
public class ReflectionUtils {

    public static Map<String, Class> name2primitiveClassMap = new HashMap<>();
    static {
        name2primitiveClassMap.put("int", int.class);
        name2primitiveClassMap.put("long", long.class);
        name2primitiveClassMap.put("byte", byte.class);
        name2primitiveClassMap.put("float", float.class);
        name2primitiveClassMap.put("double", double.class);
        name2primitiveClassMap.put("boolean", boolean.class);
        name2primitiveClassMap.put("short", short.class);
        name2primitiveClassMap.put("char", char.class);
    }

    public static Class getClass(String className) {
        try {
            if(name2primitiveClassMap.containsKey(className)) {
                return name2primitiveClassMap.get(className);
            }
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    public static boolean isAssignable(Class a, Class b) {
        if(a.isPrimitive() || b.isPrimitive()) {
            if(a.isPrimitive() && b.isPrimitive()) {
                return a == b;
            } else if(a.isPrimitive()) {
                return a == getStaticFieldValue(b, "TYPE");
            } else {
                return b == getStaticFieldValue(a, "TYPE");
            }
        }
        return a.isAssignableFrom(b);
    }


    public static Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public static Field getField(Class c, String fieldName) {
        try {
            return c.getField(fieldName);
        } catch (NoSuchFieldException e) {
        }
        return null;
    }

    public static Field getDeclaredField(Class c, String fieldName) {
        try {
            return c.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
        }
        return null;
    }

    public static Object getFieldValue(Object target, String fieldName) {
        return getFieldValue(getField(target.getClass(), fieldName), target);
    }

    public static Object getStaticFieldValue(Class targetClass, String fieldName) {
        Field field = getField(targetClass, fieldName);
        return getFieldValue(field, (Object)null);
    }

    public static Object getDeclaredFieldValue(Object target, String fieldName) {
        return getFieldValue(getDeclaredField(target.getClass(), fieldName), target);
    }

    public static Object getStaticDeclaredFieldValue(Class targetClass, String fieldName) {
        return getFieldValue(getDeclaredField(targetClass, fieldName), (Object)null);
    }

    public static Class getInnerClass(Class ofthis,  String innerClassName) {
        return getClass(ofthis.getName() + "$" + innerClassName);
    }

    public static Class getFieldType(Class targetClass, String fieldName) {
        try {

            Field field = null;
            field = targetClass.getDeclaredField(fieldName);

            return field.getType();

        } catch (NoSuchFieldException e) {
        }
        return null;
    }

    public static void setField(Class targetClass, String fieldName, Object target, Object value) {

        try {

            Field field = null;
            field = targetClass.getDeclaredField(fieldName);
            field.set(target, value);

        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public static void forceSetField(Class targetClass, String fieldName, Object target, Object value) {

        try {

            Field field = null;
            field = targetClass.getDeclaredField(fieldName);

            if (field != null) {
                field.setAccessible(true);
                field.set(target, value);
            }

        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }
    public static boolean hasMethod(Class c, String methodName, Class... paramTypes) {

        try {
            c.getMethod(methodName, paramTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }

    }


    public static Method getMethodFromArgs(Class c, String methodName, Object... args) {

        try {
            Class[] classes = new Class[args.length];
            int i = 0;
            for(Object arg: args) {
                classes[i++] = arg.getClass();
            }
            return c.getMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
        }
        return null;
    }


    public static Method getMethod(Class c, String methodName, Class... paramTypes) {

        try {
            return c.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
        }
        return null;
    }

    public static Object callMethod(Object target, Method method, Object... args) {

        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            // TODO handle this properly
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public static Object callMethod(Object target, String methodName, Object... args) {

        try {
            Method method = getMethodFromArgs(target.getClass(), methodName, args);
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            // TODO handle this properly
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public static Constructor getConstructor(Class c, Class... args) {

        try {
            Constructor constructor = c.getConstructor(args);
            return constructor;

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Object newInstance(Constructor constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return null;
    }

    public static Object newInstance(Class type) {

        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object newInstance(String className) {

        Class c = getClass(className);
        if (c != null) {
            return newInstance(c);
        }
        return null;
    }

    public static Class getGenericType(Class c, String fieldName, int indexOfType) {
        try {
            return getGenericType(c.getField(fieldName), indexOfType);
        } catch (NoSuchFieldException e) {
            // TODO handle exception properly - log it
        }
        return null;
    }

    public static Class getGenericType(Field f) {
        return getGenericType(f, 0);
    }

    public static Class getGenericType(Field f, int indexOfType) {
        if(f.getType().isArray()) {
            return f.getType().getComponentType();
        }
        ParameterizedType genericType = (ParameterizedType) f.getGenericType();
        Type t[] = genericType.getActualTypeArguments();
        return (Class) t[indexOfType];

    }

    public static <T> T[] toTypedArray(List<T> list, Class arrayClass) {
        if (list == null || list.size() == 0) return null;
        T[] array = (T[]) java.lang.reflect.Array.newInstance(arrayClass.getComponentType(), list.size());
        return list.toArray(array);
    }


    public static <T> T[] toTypedArray(List<T> list, Field field) {
        if (list == null || list.size() == 0) return null;
        Class clazz = getGenericType(field, 0);
        T[] array = (T[]) java.lang.reflect.Array.newInstance(clazz, list.size());
        return list.toArray(array);
    }

    public static <T> T[] toTypedArray(List<T> list, Class c, String fieldName) {
        if (list == null || list.size() == 0) return null;
        Class clazz = getGenericType(c, fieldName, 0);
        T[] array = (T[]) java.lang.reflect.Array.newInstance(clazz, list.size());
        return list.toArray(array);
    }
}
