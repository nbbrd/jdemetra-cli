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
package be.nbb.cli.command.proc;

import be.nbb.cli.command.Command;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.function.Supplier;
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
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("be.nbb.cli.command.proc.CommandRegistration")
public final class CommandRegistrationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        roundEnv.getElementsAnnotatedWith(CommandRegistration.class).stream()
                .map(o -> getCodeGenerator(o).get())
                .forEach(this::write);

        return true;
    }

    private void write(JavaFile javaFile) {
        try (Writer w = processingEnv.getFiler().createSourceFile(javaFile.packageName + "." + javaFile.typeSpec.name).openWriter()) {
            javaFile.writeTo(w);
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not generate file: " + ex.getMessage());
        }
    }

    private static Supplier<JavaFile> getCodeGenerator(Element e) {
        if (e instanceof ExecutableElement) {
            return GeneratorFromMethod.of((ExecutableElement) e);
        }
        if (e instanceof VariableElement) {
            return GeneratorFromField.of((VariableElement) e);
        }
        throw new RuntimeException(e.toString());
    }

    @lombok.Value
    private static class GeneratorFromMethod implements Supplier<JavaFile> {

        String commandName;
        ClassName sourceType;
        String sourceMethod;

        static GeneratorFromMethod of(ExecutableElement e) {
            CommandRegistration registration = e.getAnnotation(CommandRegistration.class);
            String pkg = findPackage(e);
            String cls = e.getEnclosingElement().getSimpleName().toString();
            String methodName = e.getSimpleName().toString();
            return new GeneratorFromMethod(registration.name().trim().isEmpty() ? cls.toLowerCase() : registration.name(), ClassName.get(pkg, cls), methodName);
        }

        @Override
        public JavaFile get() {
            MethodSpec name = nameBuilder().addStatement("return $S", commandName).build();
            MethodSpec category = categoryBuilder().addStatement("return null").build();
            MethodSpec description = descriptionBuilder().addStatement("return null").build();
            MethodSpec exec = execBuilder().addStatement("return $T.$L(args)", sourceType, sourceMethod).build();

            TypeSpec command = commandBuilder(commandName + "Command")
                    .addMethod(name)
                    .addMethod(category)
                    .addMethod(description)
                    .addMethod(exec)
                    .build();

            return JavaFile.builder(sourceType.packageName(), command).build();
        }
    }

    @lombok.Value
    private static class GeneratorFromField implements Supplier<JavaFile> {

        String commandName;
        ClassName sourceType;
        String sourceField;

        static GeneratorFromField of(VariableElement e) {
            CommandRegistration registration = e.getAnnotation(CommandRegistration.class);
            String pkg = findPackage(e);
            String cls = e.getEnclosingElement().getSimpleName().toString();
            String sourceField = e.getSimpleName().toString();
            return new GeneratorFromField(registration.name().trim().isEmpty() ? cls.toLowerCase() : registration.name(), ClassName.get(pkg, cls), sourceField);
        }

        @Override
        public JavaFile get() {
            FieldSpec delegate = FieldSpec.builder(Command.class, "delegate")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("$T.$L", sourceType, sourceField)
                    .build();

            MethodSpec name = nameBuilder().addStatement("return delegate.getName()").build();
            MethodSpec category = categoryBuilder().addStatement("return delegate.getCategory()").build();
            MethodSpec description = descriptionBuilder().addStatement("return delegate.getDescription()").build();
            MethodSpec exec = execBuilder().addStatement("return delegate.exec(args)").build();

            TypeSpec command = commandBuilder(commandName + "Command")
                    .addField(delegate)
                    .addMethod(name)
                    .addMethod(category)
                    .addMethod(description)
                    .addMethod(exec)
                    .build();

            return JavaFile.builder(sourceType.packageName(), command).build();
        }
    }

    private static MethodSpec.Builder nameBuilder() {
        return MethodSpec.methodBuilder("getName")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class);
    }

    private static MethodSpec.Builder categoryBuilder() {
        return MethodSpec.methodBuilder("getCategory")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class);
    }

    private static MethodSpec.Builder descriptionBuilder() {
        return MethodSpec.methodBuilder("getDescription")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class);
    }

    private static MethodSpec.Builder execBuilder() {
        return MethodSpec.methodBuilder("exec")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String[].class, "args")
                .returns(int.class);
    }

    private static TypeSpec.Builder commandBuilder(String className) {
        return TypeSpec.classBuilder(className)
                .addAnnotation(newGeneratedAnnotation())
                .addAnnotation(newServiceProviderAnnotation())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(Command.class);
    }

    private static AnnotationSpec newGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class).addMember("value", "$S", CommandRegistrationProcessor.class).build();
    }

    private static AnnotationSpec newServiceProviderAnnotation() {
        return AnnotationSpec.builder(ServiceProvider.class).addMember("service", "$T.class", Command.class).build();
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
