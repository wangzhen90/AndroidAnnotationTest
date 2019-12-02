package com.wangzhen.annotation_processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Description:
 *
 * @author wangzhen
 * @version 1.0
 */
public class ClassCreatorProxy {
    private String bindingClassName;
    private String packageName;
    private TypeElement typeElement;
    private Map<Integer, VariableElement> variableElementMap = new HashMap<>();

    public ClassCreatorProxy(Elements elements, TypeElement classElement) {
        this.typeElement = classElement;
        // Returns the package of an element.
        PackageElement packageElement = elements.getPackageOf(typeElement);
        packageName = packageElement.getQualifiedName().toString();
        bindingClassName = typeElement.getSimpleName() + "_ViewBinding";
    }

    public void putElement(int id, VariableElement element) {
        variableElementMap.put(id, element);
    }

    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n\n")
            .append("import com.wangzhen.annotation_lib.*;").append("\n\n")
            .append("public class ").append(bindingClassName).append(" {\n");
        generateMethods(builder);
        builder.append('\n');
        builder.append("}\n");
        return builder.toString();
    }

    private void generateMethods(StringBuilder builder) {
        //
        builder.append("public void bind(" + typeElement.getQualifiedName() + " host ) {\n");
        for (int id : variableElementMap.keySet()) {
            VariableElement element = variableElementMap.get(id);
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            builder.append("host." + name).append(" = ");
            builder.append("(" + type + ")(((android.app.Activity)host).findViewById( " + id + "));\n");
        }
        builder.append("  }\n");
    }

    public String getProxyClassFullName() {
        return packageName + "." + bindingClassName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public TypeSpec generateJavaCodeByPoet(){

        TypeSpec bindingClass = TypeSpec.classBuilder(bindingClassName)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(generateMethodByPoet())
            .build();
        return bindingClass;
    }

    private MethodSpec generateMethodByPoet(){
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(ClassName.bestGuess(typeElement.getQualifiedName().toString()),"host");

        for (int id : variableElementMap.keySet()) {
            VariableElement element = variableElementMap.get(id);
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            String codeString = "host." + name + " = " + "(" + type + ")(((android.app.Activity)host).findViewById( " + id + "));";
            methodBuilder.addCode(codeString);
        }
        return methodBuilder.build();
    }
}
