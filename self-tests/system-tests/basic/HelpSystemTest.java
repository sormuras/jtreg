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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class HelpSystemTest {
    @Test
    @Tag("system")
    void test() throws Exception {
        // System tests require a pre-built jtreg image
        var imageDirectory = Path.of("build/images/jtreg");
        var jar = imageDirectory.resolve( "lib/jtreg.jar");
        assertTrue(Files.isRegularFile(jar), "JAR file not found: " + jar);

        // Launch jtreg via Java's command-line program
        var command = List.of("java", "-jar", jar.toString(), "--help");
        var process = new ProcessBuilder(command).redirectErrorStream(true).start();
        if (!process.waitFor(9, TimeUnit.SECONDS)) fail("Timeout!");
        var output = new String(process.getInputStream().readAllBytes());

        // Verify expected return values
        assertEquals(0, process.exitValue(), output);
        assertLinesMatch(
                """
                Usage:
                         java -jar jtreg.jar options... tests...
                                
                Tests can be given as files or folders containing test files, or by means of
                test groups. Long lists of options and tests may be encapsulated in "at-files".
                
                >> MANY MORE HELP LINES >>
                """.lines(),
                output.lines());
    }
}
