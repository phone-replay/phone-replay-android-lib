package com.phonereplay.tasklogger.reflect;

import android.util.Log;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Overview<br>
 * A set of novel reflection tools,
 * Able to easily implement reflection and make the code highly readable.
 *
 * @author lody
 */
public class Reflect {

    public static final String TAG = "$$ Reflect";

    private final Object object;

    private final boolean isClass;

    private Reflect(Class<?> type) {
        this.object = type;
        this.isClass = true;
    }

    private Reflect(Object object) {
        this.object = object;
        this.isClass = false;
    }

    // ---------------------------------------------------------------------
    // Member
    // ---------------------------------------------------------------------

    /**
     * Encapsulate Class.forName(name)
     * <p/>
     * Can be called like this: <code>on(Class.forName(name))</code>
     *
     * @param name complete class name
     * @return tool class itself
     * @see #on(Class)
     */
    public static Reflect on(String name) throws ReflectException {
        return on(forName(name));
    }

    /**
     * Encapsulate Class.forName(name)
     * <p/>
     * Can be called like this: <code>on(Xxx.class)</code>
     *
     * @param clazz class
     * @return tool class itself
     * @see #on(Class)
     */
    public static Reflect on(Class<?> clazz) {
        return new Reflect(clazz);
    }

    // ---------------------------------------------------------------------
    // 构造器
    // ---------------------------------------------------------------------

    /**
     * Wrap an object
     * <p/>
     * You can use this method when you need to access the fields and methods of the instance
     * {@link Object}
     *
     * @param object The object that needs to be packaged
     * @return tool class itself
     */
    public static Reflect on(Object object) {
        return new Reflect(object);
    }

    /**
     * Convert objects with restricted access to unrestricted.
     * Generally,
     * Private fields and methods of a class cannot be obtained and called.
     * The reason is that Java will check whether it has access permissions before calling.
     * When this method is called,
     * Access permission checking mechanism will be turned off.
     *
     * @param accessible object with restricted access
     * @return object without access restrictions
     */
    public static <T extends AccessibleObject> T accessible(T accessible) {
        if (accessible == null) {
            return null;
        }

        if (accessible instanceof Member) {
            Member member = (Member) accessible;

            if (Modifier.isPublic(member.getModifiers()) &&
                    Modifier.isPublic(member.getDeclaringClass().getModifiers())) {

                return accessible;
            }
        }

        //The default is false, which means access permissions are checked during reflection.
        // When set to true, access permissions are not checked and private fields and methods can be accessed.
        if (!accessible.isAccessible()) {
            accessible.setAccessible(true);
        }

        return accessible;
    }

    // ---------------------------------------------------------------------
    // Excellent API that eliminates the clutter :)
    // ---------------------------------------------------------------------

    private static String property(String string) {
        int length = string.length();

        if (length == 0) {
            return "";
        } else if (length == 1) {
            return string.toLowerCase();
        } else {
            return string.substring(0, 1).toLowerCase() + string.substring(1);
        }
    }

    private static Reflect on(Constructor<?> constructor, Object... args) throws ReflectException {
        try {
            return on(accessible(constructor).newInstance(args));
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private static Reflect on(Method method, Object object, Object... args) throws ReflectException {
        try {
            accessible(method);

            if (method.getReturnType() == void.class) {
                method.invoke(object, args);
                return on(object);
            } else {
                return on(method.invoke(object, args));
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * Inner class, taking an object out of its packaging
     */
    private static Object unwrap(Object object) {
        if (object instanceof Reflect) {
            return ((Reflect) object).get();
        }

        return object;
    }

    /**
     * Inner class, given a series of parameters, returns their types
     *
     * @see Object#getClass()
     */
    private static Class<?>[] types(Object... values) {
        if (values == null) {
            //空
            return new Class[0];
        }

        Class<?>[] result = new Class[values.length];

        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            result[i] = value == null ? NULL.class : value.getClass();
        }

        return result;
    }

    /**
     * 加载一个类
     *
     * @see Class#forName(String)
     */
    private static Class<?> forName(String name) throws ReflectException {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * Get the type of wrapped object,
     * If it is a basic type, such as int, float, boolean, etc.
     * Then it will be converted to the corresponding object type.
     */
    public static Class<?> wrapper(Class<?> type) {
        if (type == null) {
            return null;
        } else if (type.isPrimitive()) {
            if (boolean.class == type) {
                return Boolean.class;
            } else if (int.class == type) {
                return Integer.class;
            } else if (long.class == type) {
                return Long.class;
            } else if (short.class == type) {
                return Short.class;
            } else if (byte.class == type) {
                return Byte.class;
            } else if (double.class == type) {
                return Double.class;
            } else if (float.class == type) {
                return Float.class;
            } else if (char.class == type) {
                return Character.class;
            } else if (void.class == type) {
                return Void.class;
            }
        }

        return type;
    }

    public static void showMethod(Method method) {
        showMethod(method.getName(), method.getParameterTypes());
    }

    public static void showMethod(String name, Class<?>[] types) {
        StringBuilder msg = new StringBuilder(name + "( ");
        for (Class<?> t : types) {
            msg.append(t.getName() + ", ");
        }
        if (types.length != 0)
            msg.delete(msg.length() - 2, msg.length());
        msg.append(")");
        Log.d(TAG, msg.toString());
    }

    /**
     * 得到当前包装的对象
     */
    public <T> T get() {
        //The benefits of generics are immediately apparent
        return (T) object;
    }

    /**
     * Modify the value of a field
     * <p/>
     * Equivalent to {@link Field#set(Object, Object)}. If the wrapped object is a
     * {@link Class}, then the modification will be a static field,
     * If the wrapped object is a {@link Object}, then an instance field is modified.
     *
     * @param name  field name
     * @param value field value
     * @return tool class after completion
     */
    public Reflect set(String name, Object value) throws ReflectException {
        try {
            Field field = field0(name);
            field.set(object, unwrap(value));
            return this;
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public Reflect delete(String name) throws ReflectException {
        try {
            Field field = field0(name);
            field.set(object, null);
            return this;
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 得到字段对值
     *
     * @param name 字段名
     * @return The field value
     * @see #field(String)
     */
    public <T> T get(String name) throws ReflectException {
        return field(name).get();
    }

    /**
     * Get fields
     *
     * @param name field name
     * @return field
     */
    public Reflect field(String name) throws ReflectException {
        try {
            Field field = field0(name);
            return on(field.get(object));
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private Field field0(String name) throws ReflectException {
        Class<?> type = type();

        // Try to treat it as a public field
        try {
            return type.getField(name);
        }

        // Try to handle it privately
        catch (NoSuchFieldException e) {
            do {
                try {
                    return accessible(type.getDeclaredField(name));
                } catch (NoSuchFieldException ignore) {
                }

                type = type.getSuperclass();
            }
            while (type != null);

            throw new ReflectException(e);
        }
    }

    /**
     * Map all objects of an object into a Map, and the key is the field name.
     *
     * @return map containing all fields
     */
    public Map<String, Reflect> fields() {
        Map<String, Reflect> result = new LinkedHashMap<String, Reflect>();
        Class<?> type = type();

        do {
            for (Field field : type.getDeclaredFields()) {
                if (!isClass ^ Modifier.isStatic(field.getModifiers())) {
                    String name = field.getName();

                    if (!result.containsKey(name))
                        result.put(name, field(name));
                }
            }

            type = type.getSuperclass();
        }
        while (type != null);

        return result;
    }

    // ---------------------------------------------------------------------
    // ObjectAPI
    // ---------------------------------------------------------------------

    /**
     * Given the method name, call the parameterless method
     * <p/>
     * Equivalent to <code>call(name, new Object[0])</code>
     *
     * @param name method name
     * @return tool class itself
     * @see #call(String, Object...)
     */
    public Reflect call(String name) throws ReflectException {
        return call(name, new Object[0]);
    }

    /**
     * Calls a method given a method name and parameters.
     * <p/>
     * Encapsulated from {@link Method#invoke(Object, Object...)}, which can accept basic types
     *
     * @param name method name
     * @param args method parameters
     * @return tool class itself
     */
    public Reflect call(String name, Object... args) throws ReflectException {
        Class<?>[] types = types(args);

        // try to call method
        try {
            Method method = exactMethod(name, types);
            return on(method, object, args);
        }

        //If there is no method matching the parameters,
        // Match the method closest to the method name.
        catch (NoSuchMethodException e) {
            try {
                Log.d(TAG, "no exact method found, try to find the similar one!");
                Method method = similarMethod(name, types);
                showMethod(method);
                return on(method, object, args);
            } catch (NoSuchMethodException e1) {
                Log.e(TAG, "no similar found!");
                throw new ReflectException(e1);
            }
        }
    }

    /**
     * Get the method based on the method name and method parameters
     */
    private Method exactMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> type = type();

        // 先尝试直接调用
        try {
            showMethod(name, types);
            return type.getMethod(name, types);
        }

        //Maybe this is a private method
        catch (NoSuchMethodException e) {
            do {
                try {
                    return type.getDeclaredMethod(name, types);
                } catch (NoSuchMethodException ignore) {
                }

                type = type.getSuperclass();
            }
            while (type != null);

            throw new NoSuchMethodException();
        }
    }

    /**
     * Given a method name and parameters, match the closest method
     */
    private Method similarMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> type = type();

        //对于公有方法:
        for (Method method : type.getMethods()) {
            if (isSimilarSignature(method, name, types)) {
                return method;
            }
        }

        //对于私有方法：
        do {
            for (Method method : type.getDeclaredMethods()) {
                if (isSimilarSignature(method, name, types)) {
                    return method;
                }
            }

            type = type.getSuperclass();
        }
        while (type != null);

        throw new NoSuchMethodException("No similar method " + name + " with params " + Arrays.toString(types) + " could be found on type " + type() + ".");
    }

    private List<?> convertObjectToList(Object obj) {
        List<?> list = new ArrayList<>();
        if (obj.getClass().isArray()) {
            list = Arrays.asList((Object[]) obj);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>) obj);
        }
        return list;
    }

    // ---------------------------------------------------------------------
    // internal tool methods
    // ---------------------------------------------------------------------

    /**
     * Confirm again whether the method signature matches the actual one,
     * Convert basic types into corresponding object types,
     * If int is converted to Int
     */
    private boolean isSimilarSignature(Method possiblyMatchingMethod, String desiredMethodName, Class<?>[] desiredParamTypes) {
        return possiblyMatchingMethod.getName().equals(desiredMethodName) && match(possiblyMatchingMethod.getParameterTypes(), desiredParamTypes);
    }

    /**
     * Call a parameterless constructor
     * <p/>
     * Equivalent to <code>create(new Object[0])</code>
     *
     * @return tool class itself
     * @see #create(Object...)
     */
    public Reflect create() throws ReflectException {
        return create(new Object[0]);
    }

    /**
     * Call a parameterized constructor
     *
     * @param args constructor parameters
     * @return tool class itself
     */
    public Reflect create(Object... args) throws ReflectException {
        Class<?>[] types = types(args);


        try {
            Constructor<?> constructor = type().getDeclaredConstructor(types);
            return on(constructor, args);
        }

        //In this case, the constructor is often private, mostly used in factory methods, and the constructor is deliberately hidden.
        catch (NoSuchMethodException e) {
            //private阻止不了反射的脚步:)
            for (Constructor<?> constructor : type().getDeclaredConstructors()) {
                if (match(constructor.getParameterTypes(), types)) {
                    return on(constructor, args);
                }
            }

            throw new ReflectException(e);
        }
    }

    /**
     * Create a proxy for the wrapped object.
     *
     * @param proxyType proxy type
     * @return The delegate of the wrapping object.
     */
    @SuppressWarnings("unchecked")
    public <P> P as(Class<P> proxyType) {
        final boolean isMap = (object instanceof Map);
        final InvocationHandler handler = new InvocationHandler() {
            @SuppressWarnings("null")
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();

                try {
                    return on(object).call(name, args).get();
                } catch (ReflectException e) {
                    if (isMap) {
                        Map<String, Object> map = (Map<String, Object>) object;
                        int length = (args == null ? 0 : args.length);

                        if (length == 0 && name.startsWith("get")) {
                            return map.get(property(name.substring(3)));
                        } else if (length == 0 && name.startsWith("is")) {
                            return map.get(property(name.substring(2)));
                        } else if (length == 1 && name.startsWith("set")) {
                            map.put(property(name.substring(3)), args[0]);
                            return null;
                        }
                    }

                    throw e;
                }
            }
        };

        return (P) Proxy.newProxyInstance(proxyType.getClassLoader(), new Class[]{proxyType}, handler);
    }

    private boolean match(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
        if (declaredTypes.length == actualTypes.length) {
            for (int i = 0; i < actualTypes.length; i++) {
                if (actualTypes[i] == NULL.class)
                    continue;

                if (wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i])))
                    continue;

                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Reflect) {
            return object.equals(((Reflect) obj).get());
        }

        return false;
    }

    @Override
    public String toString() {
        return object.toString();
    }

    /**
     * Get the type of wrapped object
     *
     * @see Object#getClass()
     */
    public Class<?> type() {
        if (isClass) {
            return (Class<?>) object;
        } else {
            return object.getClass();
        }
    }

    /**
     * defines a null type
     */
    private static class NULL {
    }

}
