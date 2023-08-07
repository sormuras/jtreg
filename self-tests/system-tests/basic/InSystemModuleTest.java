/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package basic;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class InSystemModuleTest {
    @Test
    @Tag("system")
    void test() throws Exception {
        // Prepare test files in build directory
        var temp = Files.createDirectories(Path.of("build/self-tests", "basic", "InSystemModuleTest"));
        var root = Files.createDirectories(temp.resolve("root"));
        Files.writeString(root.resolve("TEST.ROOT"), """
                JUnit.dirs=.
                """);
        var lang = Files.createDirectories(root.resolve("java.base/java/lang"));
        var math = Files.writeString(lang.resolve("MathTests.java"),
                //language=java
                """
                package java.lang;
                
                import static org.junit.jupiter.api.Assertions.*;
                import org.junit.jupiter.api.Test;
                
                public class MathTests {
                  @Test
                  public void accessPackagePrivateConstant() {
                    assertEquals(1.3407807929942597E154, Math.twoToTheDoubleScaleUp);
                  }
                }
                """);

        // System tests require a pre-built jtreg image
        var jtImageDirectory = Path.of("build/images/jtreg");
        var jtJarFile = jtImageDirectory.resolve( "lib/jtreg.jar");
        assertTrue(Files.isRegularFile(jtJarFile), "JAR file not found: " + jtJarFile);

        // Launch jtreg via Java's command-line program
        var jtReportsDir = Files.createDirectories(temp.resolve("jt-reports"));
        var jtWorkingDir = Files.createDirectories(temp.resolve("jt-working"));
        var command = List.of("java",
                "-jar", jtJarFile.toString(),
                "-v1",
                "-r:" + jtReportsDir,
                "-w:" + jtWorkingDir,
                math.toString());
        var process = new ProcessBuilder(command).redirectErrorStream(true).start();
        if (!process.waitFor(9, TimeUnit.SECONDS)) fail("Timeout!");
        var output = new String(process.getInputStream().readAllBytes());

        // Verify expected return values
        assertEquals(0, process.exitValue(), output);
        assertLinesMatch(
                """
                Passed: java.base/java/lang/MathTests.java
                Test results: passed: 1
                Report written to .+
                Results written to .+
                """.lines(),
                output.lines());
    }
}
