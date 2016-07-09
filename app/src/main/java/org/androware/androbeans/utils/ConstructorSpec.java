package org.androware.androbeans.utils;


import java.lang.reflect.Constructor;
import java.util.List;

import static android.R.attr.name;


/**
 * Created by jkirkley on 6/9/16.
 */
public class ConstructorSpec  {

    public final static String PLUGIN_PARAM_PREFIX = "__plugin__";

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
            } else {

                // try to resolve refs
                // TODO need to factor this properly
                for( i = 0; i < paramObjects.length; ++i ) {
                    Object object = paramObjects[i];

                    if (!paramClasses[i].isAssignableFrom(object.getClass())) {

                        if (object instanceof String) {
                            String s = (String) object;

                            if(s.startsWith(PLUGIN_PARAM_PREFIX)) {
                                // this param will be filled in later
                                continue;
                            }
                            else if (s.indexOf('.') != -1) {
                                try {
                                    int resId = ResourceUtils.getResourceIdFromDotName(s);
                                    paramObjects[i] = resId;
                                } catch (IllegalArgumentException e) {
                                    // ignore
                                }
                            }
                        }
                        if (!paramClasses[i].isAssignableFrom(paramObjects[i].getClass()) ) {
                            throw new IllegalArgumentException("param " + i + " = " + object + " not of correct type.");
                        }
                    }
                }
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

    public void plugInValue(Object value, String name) {
        for(int i = 0; i < paramClasses.length; ++i) {
            if(paramClasses[i].isAssignableFrom(value.getClass()) ) {
                Object o = paramObjects[i];
                if(o instanceof String) {
                    String n = (String)o;
                    if(n.equals(PLUGIN_PARAM_PREFIX + name)) {
                        paramObjects[i] = value;
                    }
                }
            }
        }
    }
}
