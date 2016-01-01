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
package org.spongepowered.gradle

import com.google.common.io.Files
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.SourceSet

import java.io.File
import java.io.ObjectInputStream.ValidationList
import java.nio.charset.Charset
import java.util.Map
import java.util.Map.Entry
 
class TaskSortAccessTransformers extends DefaultTask {
    
    TaskSortAccessTransformers() {
        TreeMap.metaClass {
            tree = { key -> 
                def value = delegate.get(key)
                if (!value) {
                    value = new TreeMap()
                    delegate.put(key, value)
                }
                value
            }
            set = { key -> 
                def value = delegate.get(key)
                if (!value) {
                    value = new TreeSet()
                    delegate.put(key, value)
                }
                value
            }
        }
    }
    
    void process(SourceSet sourceSet) {
        if (!sourceSet instanceof SourceSet) {
            throw new InvalidUserDataException("${sourceSet} is not a SourceSet")
        }
        sourceSet.resources.findAll { file ->
            file.file && file.name.toLowerCase().endsWith("_at.cfg")
        }.each this.&sortAtFile
    }
    
    void sortAtFile(File file) {
        // Section -> Access -> Package -> Class -> Entry
        Map<String, Map<String, Map<String, Map<String, String>>>> tree = new TreeMap()
        Map<String, Map<String, Map<String, String>>> section = tree.tree("")
        List<String> trash = []
        
        project.logger.lifecycle "Sorting AccessTransformer config {}", file
        
        for (String line : Files.readLines(file, Charset.defaultCharset())) {
            if (line?.isEmpty()) {
                continue
            }
            if (line.startsWith("#")) {
                if (line.length() > 2 && line.startsWith("# @")) {
                    continue
                }
                String sectionName = line.substring(1).trim()
                if (sectionName.length() < 1) {
                    continue
                }
                section = tree.tree(sectionName)
            } else {
                String[] parts = line.split("\\s+", 3)
                if (parts.length < 2) {
                    trash += line
                    continue
                }
                if (parts.length < 3) {
                    parts += ""
                }
                def (modifier, className, tail) = parts
                def packageName = ""
                def pos = className.lastIndexOf('.')
                if (pos > -1) {
                    packageName = className.substring(0, ++pos)
                    className = className.substring(pos)
                }
                
                section.tree(packageName).tree(modifier).set(className).add(tail)
            }
        }
        
        String outFile = "# @ ${file.name} sorted on ${new Date().dateTimeString}\n"

        for (junk in trash) {
            outFile <<= junk << "\n"
        }        
        
        for (s in tree.entrySet()) {
            if (s.value.size() > 0) {
                if (s.key.length() > 0) {
                    outFile <<= "\n# ${s.key}"
                }
                
                for (pkg in s.value.entrySet()) {
                    outFile <<= "\n"
                    for (acc in pkg.value.entrySet()) {
                        for (cls in acc.value.entrySet()) {
                            for (entry in cls.value) {
                                outFile <<= "${acc.key} ${pkg.key}${cls.key} ${entry}\n"
                            }
                        }
                    }
                }
            }
        }
        
        Files.write(outFile, file, Charset.defaultCharset())
    }
}
