/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * "Portions Copyrighted 2013 Benno Markiewicz"
 */
package de.markiewb.netbeans.plugins.hints.replaceplus;

import de.markiewb.netbeans.plugins.hints.replaceplus.ReplacePlusHint;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExClipboard;

/* TODO to make this test work:
 - add test dependency on Java Hints Test API (and JUnit 4)
 - to ensure that the newest Java language features supported by the IDE are available,
 regardless of which JDK you build the module with:
 -- for Ant-based modules, add "requires.nb.javac=true" into nbproject/project.properties
 -- for Maven-based modules, use dependency:copy in validate phase to create
 target/endorsed/org-netbeans-libs-javacapi-*.jar and add to endorseddirs
 in maven-compiler-plugin and maven-surefire-plugin configuration
 See: http://wiki.netbeans.org/JavaHintsTestMaven
 */
public class CopyJoinedStringToClipboardFixTest {

    @Test
    public void testFixWorkingOnlyLiterals() throws Exception {
        HintTest.create().
                input("package test;\n"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        String foo=\"A\"+\"B\"+\"C\";\n"
                + "    }\n"
                + "}\n"). 
                run(CopyJoinedStringToClipboardHint.class).
                findWarning("3:19-3:30:hint:" + Bundle.DN_CopyJoinedStringToClipboard()).
                applyFix(Bundle.LBL_CopyJoinedStringToClipboardFix()).
                assertCompilable().
                assertOutput("package test;\n"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        String foo=\"A\"+\"B\"+\"C\";\n"
                + "    }\n"
                + "}\n");

        assertEquals("ABC", getClipboardContent());
    }

    @Test
    public void testFixWorkingOnlyLiteralsWithLineBreaks() throws Exception {
        HintTest.create().
                input("package test;\n"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        String foo=\"A\\n\"+\"B\\r\"+\"C\";"
                + "    }\n"
                + "}\n").
                run(CopyJoinedStringToClipboardHint.class).
                findWarning("3:19-3:34:hint:" + Bundle.DN_CopyJoinedStringToClipboard()).
                applyFix(Bundle.LBL_CopyJoinedStringToClipboardFix()).
                assertCompilable().
                assertOutput("package test;\n"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        String foo=\"A\\n\"+\"B\\r\"+\"C\";"
                + "    }\n"
                + "}\n");
        final String expected = "A\nB\rC";
        final String actual = getClipboardContent();
        assertEquals(expected, actual);
    }
    
    /**
     * https://github.com/markiewb/nb-additional-hints/issues/1
     * @throws Exception 
     */
    @Test
    public void testFixWorkingQuotedStrings1() throws Exception {
	HintTest.create().
		input("package test;\n"
		+ "public class Test {\n"
		+ "    public static void main(String[] args) {\n"
		+ "	String b = \"Hello \" + \"\\\"World\\\"\";"
		+ "    }\n"
		+ "}\n").
		run(CopyJoinedStringToClipboardHint.class).
		findWarning("3:12-3:34:hint:" + Bundle.DN_CopyJoinedStringToClipboard()).
		applyFix(Bundle.LBL_CopyJoinedStringToClipboardFix()).
		assertCompilable().
		assertOutput("package test;\n"
		+ "public class Test {\n"
		+ "    public static void main(String[] args) {\n"
		+ "	String b = \"Hello \" + \"\\\"World\\\"\";"
		+ "    }\n"
		+ "}\n");
        assertEquals("Hello \"World\"", getClipboardContent());

    }
    
    /**
     * https://github.com/markiewb/nb-additional-hints/issues/8
     * @throws Exception 
     */
    @Test
    public void testFixWorkingQuotedStrings2() throws Exception {
	HintTest.create().
		input("package test;"
		+ "public class Test {"
		+ "    public static void main(String[] args) {"
		+ "	String b = \"Hello \" + \"\\\"Inner1\\\"+\\\"Inner2\\\"\" + \" World\";"
		+ "    }"
		+ "}").
		run(CopyJoinedStringToClipboardHint.class).
		findWarning("0:88-0:133:hint:" + Bundle.DN_CopyJoinedStringToClipboard()).
		applyFix(Bundle.LBL_CopyJoinedStringToClipboardFix()).
		assertCompilable().
		assertOutput("package test;"
		+ "public class Test {"
		+ "    public static void main(String[] args) {"
		+ "	String b = \"Hello \" + \"\\\"Inner1\\\"+\\\"Inner2\\\"\" + \" World\";"
		+ "    }"
		+ "}");
        assertEquals("Hello \"Inner1\"+\"Inner2\" World", getClipboardContent());

    }

    private String getClipboardContent() throws UnsupportedFlavorException, IOException {
        ExClipboard clipboard = Lookup.getDefault().
                lookup(ExClipboard.class);
        return ((String) clipboard.getData(DataFlavor.stringFlavor));
    }
}
