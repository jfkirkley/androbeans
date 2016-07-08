package org.androware.androbeans.utils;


import java.lang.reflect.Constructor;
import java.util.List;


/**
 * Created by jkirkley on 6/9/16.
 */
public class ConstructorSpec  {

    public List<String> paramClassNames;
    public Object [] paramObjects;
    public String targetClassName;

    protected Class [] paramClasses;
    protected Class targetClass;

    public ConstructorSpec() {
    }

    public ConstructorSpec(Class targetClass) {
        this.targetClass = targetClass;
    }

    public ConstructorSpec(Class targetClass, Class [] paramClasses,  Object ... paramObjects) {
        this(targetClass);
        this.paramClasses = paramClasses;
        this.paramObjects = paramObjects;
    }

    public ConstructorSpec(Class targetClass, Class ... paramClasses) {
        this(targetClass, paramClasses, null);
    }

    public void __init__() {
        targetClass = ReflectionUtils.getClass(targetClassName);
        if(paramClassNames != null) {
            paramClasses = new Class[paramClassNames.size()];
            int i = 0;
            for (String className : paramClassNames) {
                paramClasses[i++] = ReflectionUtils.getClass(className);
            }
            if(paramObjects == null) {
                paramObjects = new Object[paramClasses.length];
            }
        }
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
