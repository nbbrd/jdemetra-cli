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
import be.nbb.cli.command.CommandReference;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
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

        CommandRegistration registration;
        ClassName sourceType;
        String sourceMethod;

        static GeneratorFromMethod of(ExecutableElement e) {
            CommandRegistration registration = e.getAnnotation(CommandRegistration.class);
            String pkg = findPackage(e);
            String cls = e.getEnclosingElement().getSimpleName().toString();
            String methodName = e.getSimpleName().toString();
            return new GeneratorFromMethod(registration, ClassName.get(pkg, cls), methodName);
        }

        @Override
        public JavaFile get() {
            MethodSpec command = commandBuilder().addStatement("return $T.$L(args)", sourceType, sourceMethod).build();
            TypeSpec result = commandRefBuilder(registration).addMethod(command).build();
            return JavaFile.builder(sourceType.packageName(), result).build();
        }
    }

    @lombok.Value
    private static class GeneratorFromField implements Supplier<JavaFile> {

        CommandRegistration registration;
        ClassName sourceType;
        String sourceField;

        static GeneratorFromField of(VariableElement e) {
            CommandRegistration registration = e.getAnnotation(CommandRegistration.class);
            String pkg = findPackage(e);
            String cls = e.getEnclosingElement().getSimpleName().toString();
            String sourceField = e.getSimpleName().toString();
            return new GeneratorFromField(registration, ClassName.get(pkg, cls), sourceField);
        }

        @Override
        public JavaFile get() {
            MethodSpec command = commandBuilder().addStatement("return $T.$L", sourceType, sourceField).build();
            TypeSpec result = commandRefBuilder(registration).addMethod(command).build();
            return JavaFile.builder(sourceType.packageName(), result).build();
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

    private static MethodSpec.Builder commandBuilder() {
        return MethodSpec.methodBuilder("getCommand")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(Command.class);
    }

    private static boolean isDescriptionKey(CommandRegistration reg) {
        return reg.description().startsWith("#");
    }

    private static String getDescriptionKey(CommandRegistration reg) {
        return reg.description().substring(1).replace(".", "_");
    }

    private static TypeSpec.Builder commandRefBuilder(CommandRegistration reg) {
        MethodSpec name = nameBuilder().addStatement("return $S", reg.name()).build();
        MethodSpec category = categoryBuilder().addStatement("return $S", reg.category()).build();
        MethodSpec description = isDescriptionKey(reg)
                ? descriptionBuilder().addStatement("return Bundle.$N()", getDescriptionKey(reg)).build()
                : descriptionBuilder().addStatement("return $S", reg.description()).build();

        return TypeSpec.classBuilder(reg.name() + "Command")
                .addAnnotation(newGeneratedAnnotation())
                .addAnnotation(newServiceProviderAnnotation())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(CommandReference.class)
                .addMethod(name)
                .addMethod(category)
                .addMethod(description);
    }

    private static AnnotationSpec newGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class).addMember("value", "$S", CommandRegistrationProcessor.class).build();
    }

    private static AnnotationSpec newServiceProviderAnnotation() {
        return AnnotationSpec.builder(ServiceProvider.class).addMember("service", "$T.class", CommandReference.class).build();
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
