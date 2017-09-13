package com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.gan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class tools for reflection
 */
public final class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Get a Class of the String className (even if it is a primary type)
     *
     * @param className name of the class
     * @return Null if it is a ClassNotFoundException
     */
    public static Class<?> getClass(String className){

        Class<?> result=null;
        try {
            switch (className){
                case  "byte":
                    result =byte.class;
                    break;
                case  "short":
                    result =short.class;
                    break;
                case  "int":
                    result =int.class;
                    break;
                case  "long":
                    result =long.class;
                    break;
                case  "float":
                    result =float.class;
                    break;
                case  "double":
                    result =double.class;
                    break;
                case  "boolean":
                    result =boolean.class;
                    break;
                case  "char":
                    result =int.class;
                    break;
                default:
                    result = Class.forName(className);
                    break;
            }

        } catch (ClassNotFoundException e) {
            logger.error("getClass()> Error ClassNotFoundException  [{}]",className, e);
        }

        return result;
    }

    /**
     * Create a Class[] of all ArgumentsTypes
     * @param keywords  name of All arguments
     * @return Null if it is an empty arguments[]
     */
    public static Class<?>[] createArgumentTypes(String[] keywords) {

        Class[] types = null;
        if (keywords != null) {
            if (keywords.length > 0) {
                types = new Class[keywords.length];
                for (int i = 0; i < keywords.length; i++) {
                    types[i] = Utils.getClass((keywords[i]));
                }
            } else {
                types = new Class[]{};
            }
        }
        return types;
    }

}
