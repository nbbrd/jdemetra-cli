/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package be.nbb.cli.util.proc;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.cli.command.Command;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("be.nbb.cli.util.proc.CommandRegistration")
public final class CommandRegistrationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        roundEnv.getElementsAnnotatedWith(CommandRegistration.class).stream()
                .map(o -> CommandProviderInfo.of((ExecutableElement) o))
                .forEach(this::write);

        return true;
    }

    private void write(CommandProviderInfo info) {
        JavaFile javaFile = generateCode(info);
        try (Writer w = processingEnv.getFiler().createSourceFile(javaFile.packageName + "." + javaFile.typeSpec.name).openWriter()) {
            javaFile.writeTo(w);
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not generate file: " + ex.getMessage());
        }
    }

    @lombok.Value
    private static class CommandProviderInfo {

        String command;
        ClassName clazz;
        String method;

        static CommandProviderInfo of(ExecutableElement e) {
            CommandRegistration registration = e.getAnnotation(CommandRegistration.class);
            String pkg = findPackage(e);
            String cls = e.getEnclosingElement().getSimpleName().toString();
            String methodName = e.getSimpleName().toString();
            return new CommandProviderInfo(registration.name().trim().isEmpty() ? cls.toLowerCase() : registration.name(), ClassName.get(pkg, cls), methodName);
        }
    }

    private static JavaFile generateCode(CommandProviderInfo info) {
        MethodSpec getNameMethod = MethodSpec.methodBuilder("getName")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $S", info.getCommand())
                .build();

        MethodSpec execMethod = MethodSpec.methodBuilder("exec")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String[].class, "args")
                .addStatement("$T.$L(args)", info.getClazz(), info.getMethod())
                .build();

        TypeSpec providerClass = TypeSpec.classBuilder(info.getCommand() + "Provider")
                .addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value", "$S", CommandRegistrationProcessor.class).build())
                .addAnnotation(AnnotationSpec.builder(ServiceProvider.class).addMember("service", "$T.class", Command.class).build())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(Command.class)
                .addMethod(getNameMethod)
                .addMethod(execMethod)
                .build();

        return JavaFile.builder(info.getClazz().packageName(), providerClass).build();
    }

    private static String findPackage(Element e) {
        switch (e.getKind()) {
            case PACKAGE:
                return ((PackageElement) e).getQualifiedName().toString();
            default:
                return findPackage(e.getEnclosingElement());
        }
    }
}
