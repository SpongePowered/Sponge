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
package org.spongepowered.common.mixin.core.crash;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import org.spongepowered.api.service.crash.CrashReport;
import org.spongepowered.api.service.crash.CrashReportable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(net.minecraft.crash.CrashReport.class)
@Implements(@Interface(iface = CrashReport.class, prefix = "report$"))
public abstract class MixinCrashReport {

    @Shadow private String description;
    @Shadow private Throwable cause;
    @Shadow public abstract String getCompleteReport();
    @Shadow public abstract CrashReportCategory makeCategory(String name);
    private final Set<CrashReportable> addedReportables = Sets.newHashSet();

    public Throwable report$getCause() {
        return this.cause;
    }

    @Intrinsic
    public String report$getDescription() {
        return this.description;
    }

    public CrashReport.Category report$addCategory(String name) {
        return (CrashReport.Category) this.makeCategory(name);
    }

    public CrashReport report$addReportable(CrashReportable reportable) {
        checkNotNull(reportable, "reportable");
        if (this.addedReportables.contains(reportable)) {
            return (CrashReport) this;
        }
        reportable.fillCrashReport((CrashReport) this);
        this.addedReportables.add(reportable);
        return (CrashReport) this;
    }

    public void report$fire() throws RuntimeException {
        throw new ReportedException((net.minecraft.crash.CrashReport) (Object) this);
    }

    public String report$toText() {
        return this.getCompleteReport();
    }

}
