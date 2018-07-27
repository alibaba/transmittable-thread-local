package com.alibaba.ttl.threadpool.agent.transformlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.alibaba.ttl.threadpool.agent.JavassistDynamicMutipleClassTransformlet;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class TtlClassloaderTransformlet implements JavassistDynamicMutipleClassTransformlet {

    private static final Logger logger = Logger.getLogger(TtlClassloaderTransformlet.class.getName());

    @Override
    public boolean needTransform(CtClass clazz, String className) {
        List<CtClass> superClassList = new ArrayList<CtClass>();
        addAllSuperClass(clazz, superClassList);

        //All the sub class of java.lang.ClassLoader will be enhanced
        //Here we enhance ClassLoader's sub classes，
        //But in fact, only 3rd part sub classes such as Tomcat WebAppBaseClassLoader can be enhanced
        //In jdk, bootstrap ext app, this 3 classloaders can't be enhanced
        //I found this hint by system.out.prinln all the class in premain
        //This hints that jdk's own 3 classloader can't be enhanced and it is already loader，
        //JDK's own classloaders is loaded before agent started
        for (Iterator<CtClass> iterator = superClassList.iterator(); iterator.hasNext();) {
            CtClass ctClass = (CtClass) iterator.next();
            if ("java.lang.ClassLoader".equals(ctClass.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doTransform(CtClass clazz) throws NotFoundException, CannotCompileException, IOException {
        final String className = clazz.getName();
        // enhance two loadClass method
        // if subclass doesn't have any load class method override, then ignore it
        // as method does not search the superclasses, so we only modify subclass
        String loadClass_methodName = "loadClass";
        final CtMethod[] loadClassMethods = clazz.getDeclaredMethods(loadClass_methodName);
        if (loadClassMethods != null) {
            for (CtMethod loadClassMethod : loadClassMethods) {
                final CtMethod new_loadClassMethod = CtNewMethod.copy(loadClassMethod, loadClass_methodName, clazz,
                        null);
                final String original_load_method_rename = "original$loadClass$method$renamed$by$ttl";
                loadClassMethod.setName(original_load_method_rename);// rename loadClass

                CtClass[] parameterTypes = loadClassMethod.getParameterTypes();
                String codeWeaveParameter = ((parameterTypes.length == 1) ? ("$1") : ("$1,$2"));

                final String code = "{\n" + 
                        "boolean isAgentClass = com.alibaba.ttl.classloader.TtlExtendLoader.isAgentLoadClass($1);\n"+ 
                        "if(isAgentClass){\n"+ 
                        "    boolean isExtendloadedClass = com.alibaba.ttl.classloader.TtlExtendLoader.isExtendLoadClass($1);\n"+ 
                        "    if(isExtendloadedClass){\n  " +
                        "        Class clazzTtlInner = com.alibaba.ttl.classloader.TtlExtendLoader.getClazz(this, $1);\n "+ 
                        "        if(clazzTtlInner==null){\n "+ 
                        "            byte[] clazzBytes = com.alibaba.ttl.classloader.TtlExtendLoader.getClazzBytes($1);\n"+ 
                        "            if(clazzBytes==null){ return " + original_load_method_rename + "(" + codeWeaveParameter + ");  }\n         "+
                        "                Class resultClass = this.defineClass($1, clazzBytes, 0, clazzBytes.length);\n   "+
                        "                com.alibaba.ttl.classloader.TtlExtendLoader.setClazzLoaderMap(this, $1, resultClass );  \n   "+
                        "                return resultClass;\n "+ 
                        "            }else{\n"+
                        "                return  clazzTtlInner;\n"+ 
                        "            }\n "+
                        "        }else{\n"+
                        "            return " + original_load_method_rename + "(" + codeWeaveParameter + "); \n "+
                        "        }\n"+
                        "    }else{  return " + original_load_method_rename + "(" + codeWeaveParameter + "); }\n" + 
                        "}\n";
                
                new_loadClassMethod.setBody(code);
                clazz.addMethod(new_loadClassMethod);
                logger.info(
                        "insert code around method " + new_loadClassMethod + " of class " + className + ": " + code);
            }

        }
    }

    private void addAllSuperClass(CtClass clazz, List<CtClass> superClassList) {
        CtClass supserClazz = null;
        try {
            supserClazz = clazz.getSuperclass();
        } catch (NotFoundException e) {
        }

        if (supserClazz != null) {
            superClassList.add(supserClazz);
            addAllSuperClass(supserClazz, superClassList);
        }
    }
}
