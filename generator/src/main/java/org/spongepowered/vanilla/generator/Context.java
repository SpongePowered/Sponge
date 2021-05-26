/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.vanilla.generator;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.utils.SourceRoot;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class Context {

    static final String INDENT = "    ";
    static final String BASE_PACKAGE = "org.spongepowered.api";
    private final Path outputDirectory;
    private final RegistryAccess registries;
    private final String licenseHeader;
    private final SourceRoot sourceRoot;

    Context(final Path outputDirectory, final RegistryAccess registries, final String licenseHeader) {
        this.outputDirectory = outputDirectory;
        this.registries = registries;
        this.licenseHeader = licenseHeader;
        this.sourceRoot = new SourceRoot(outputDirectory);
        this.sourceRoot.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_16);
        final var config = new DefaultPrinterConfiguration();
        config.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS, true));
        final var printer = new DefaultPrettyPrinter(config);
        this.sourceRoot.setPrinter(printer::print);
    }

    public String gameVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    public RegistryAccess registries() {
        return this.registries;
    }

    public ClassName relativeClass(final String relativePackage, final String simpleName, final String... simpleNames) {
        final String pkg = relativePackage.isBlank() ? Context.BASE_PACKAGE : String.join(".", Context.BASE_PACKAGE, relativePackage);
        return ClassName.get(pkg, simpleName, simpleNames);
    }

    public CompilationUnit compilationUnit(final String relativePackage, final String simpleName) {
        final String pkg = relativePackage.isBlank() ? Context.BASE_PACKAGE : String.join(".", Context.BASE_PACKAGE, relativePackage);
        final CompilationUnit unit = this.sourceRoot.parse(pkg, simpleName + ".java");
        LexicalPreservingPrinter.setup(unit);
        return unit;
    }

  /**
   * Write the provided type to a file in the defined base package.
   *
   * @param spec type to write out to file
   * @throws IOException if thrown by javapoet
   */
  public void write(final TypeSpec spec) throws IOException {
      this.write("", spec);
  }

    /**
     * Write the provided type to a file in the defined base package.
     *
     * @param spec type to write out to file
     * @throws IOException if thrown by javapoet
     */
    public void write(final String relativePackage, final TypeSpec spec) throws IOException {
        // First write the file
        final String pkg = relativePackage.isBlank() ? Context.BASE_PACKAGE : String.join(".", Context.BASE_PACKAGE, relativePackage);
        final var file = JavaFile.builder(pkg, spec)
            .skipJavaLangImports(true)
            .indent(Context.INDENT)
            .build();

        Path destinationDir = this.outputDirectory;
        for (final String el : pkg.split("\\.")) {
            destinationDir = destinationDir.resolve(el);
        }
        Files.createDirectories(destinationDir);

        try (final var writer = Files.newBufferedWriter(destinationDir.resolve(file.typeSpec.name + ".java"), StandardCharsets.UTF_8)) {
            // write license header
            writer.write("/*");
            writer.write(this.licenseHeader);
            writer.write("*/");
            // then file
            file.writeTo(writer);
        }
    }

    /**
     * Save out all source files modified during this operation.
     */
    void complete() {
        this.sourceRoot.saveAll(StandardCharsets.UTF_8);
    }
}
