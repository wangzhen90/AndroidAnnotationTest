package com.wangzhen.annotation_processor;

import com.google.auto.service.AutoService;
import com.wangzhen.annotation_lib.BindView;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {
    private Elements elementUtil;
    private Map<String, ClassCreatorProxy> proxyMap = new HashMap<>();
    private ProcessingEnvironment processingEnv;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        System.out.println("-------BindViewProcessor init");
        elementUtil = processingEnvironment.getElementUtils();
        processingEnv = processingEnvironment;
    }
    /**
     * 指定 java 版本，这个可以用注解 @SupportedSourceVersion(SourceVersion.RELEASE_8) 来代替
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
    /**
     * 设置这个处理器是处理什么类型注解的,这个可以用注解 @SupportedAnnotationTypes(com.wangzhen.annotation_complier.StudentProcessor) 来代替
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class.getCanonicalName());
        return annotations;
    }
    /**
     * @param set              请求处理的注解类型
     * @param roundEnvironment
     * @return true:表示当前注解已经处理；false：可能需要后续的 processor 来处理
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("-------BindViewProcessor process");
        proxyMap.clear();
        // 1.获取所有有 BindView 注解的 Element，由于 BindView 是作用在 Field 上的，所以这些 Element 一定是 Field
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        // 2.遍历所有被注解的 Element
        for (Element element : elements) {
            VariableElement variableElement = (VariableElement) element;
            // 3.获得它所在的类
            TypeElement classElement = (TypeElement) variableElement.getEnclosingElement();
            String fullClassName = classElement.getQualifiedName().toString();
            ClassCreatorProxy classCreatorProxy = proxyMap.get(fullClassName);
            if (classCreatorProxy == null) {
                classCreatorProxy = new ClassCreatorProxy(elementUtil, classElement);
                proxyMap.put(fullClassName, classCreatorProxy);
            }
            // 4.获得它注解中的信息
            BindView bindAnnotation = variableElement.getAnnotation(BindView.class);
            int id = bindAnnotation.value();
            classCreatorProxy.putElement(id, variableElement);
            // 5.创建一个 java 文件
            createJavaFile();
        }
        return true;
    }

    private void createJavaFile(){
        for(String key:proxyMap.keySet()){
            ClassCreatorProxy proxyInfo = proxyMap.get(key);
            try {
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(proxyInfo.getProxyClassFullName(),proxyInfo.getTypeElement());
                Writer writer = jfo.openWriter();
                // 6.拼写 java 文件中的代码字符串
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
                System.out.println("-------BindViewProcessor create java file success");
            } catch (IOException e) {
                System.out.println("-------BindViewProcessor create java file failed");
                e.printStackTrace();
            }
        }
    }
}
