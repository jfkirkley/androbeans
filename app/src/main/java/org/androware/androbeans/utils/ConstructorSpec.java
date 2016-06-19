package org.androware.androbeans.utils;

import android.util.JsonReader;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Ref;
import java.util.List;
import java.util.Map;

import org.androware.androbeans.InstaBean;
import org.androware.androbeans.JSONinstaBean;

/**
 * Created by jkirkley on 6/9/16.
 */
public class ConstructorSpec extends JSONinstaBean {

    public List<String> paramClassNames;
    public Object [] paramObjects;
    public String targetClassName;

    protected Class [] paramClasses;
    protected Class targetClass;

    public ConstructorSpec(Class targetClass) {
        this.targetClass = targetClass;
    }

    public ConstructorSpec(Map map, Class type, InstaBean parent) {
        super(map, type, parent);
        init();
    }

    public ConstructorSpec(Class targetClass, Class [] paramClasses,  Object ... paramObjects) {
        this(targetClass);
        this.paramClasses = paramClasses;
        this.paramObjects = paramObjects;
    }

    public ConstructorSpec(Class targetClass, Class ... paramClasses) {
        this(targetClass, paramClasses, null);
    }

    public ConstructorSpec(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(reader, type, parent);
        init();
    }

    private void init() {
        targetClass = ReflectionUtils.getClass(targetClassName);
        if(paramClassNames != null) {
            paramClasses = new Class[paramClassNames.size()];
            int i = 0;
            for (String className : paramClassNames) {
                paramClasses[i++] = ReflectionUtils.getClass(className);
            }
        }
    }

    public ConstructorSpec(JsonReader reader, Class type) throws IOException {
        this(reader, type, null);
    }

    public Class[] getParamClasses() {
        return paramClasses;
    }

    public void setParamClasses(Class[] paramClasses) {
        this.paramClasses = paramClasses;
    }

    public Object[] getParamObjects() {
        return paramObjects;
    }

    public void setParamObjects(Object[] paramObjects) {
        this.paramObjects = paramObjects;
    }

    public Object build() {
        if(paramClasses != null ) {
            if(paramObjects != null) {
                Constructor constructor = ReflectionUtils.getConstructor(targetClass, paramClasses);
                return ReflectionUtils.newInstance(constructor, paramObjects);
            }
            throw new IllegalArgumentException("No param objects provided");

        } else {
            return ReflectionUtils.newInstance(targetClass);
        }
    }

    public Object build(Object ... paramObjects) {
        setParamObjects(paramObjects);
        Constructor constructor = ReflectionUtils.getConstructor(targetClass, paramClasses);
        return ReflectionUtils.newInstance(constructor, this.paramObjects);
    }
}
