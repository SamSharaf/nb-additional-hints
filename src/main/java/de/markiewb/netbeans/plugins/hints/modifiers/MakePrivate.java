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
package de.markiewb.netbeans.plugins.hints.modifiers;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.support.FixFactory;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "ERR_MakePrivate=Make Private",
    "DN_MakePrivate=Make Private",
    "DESC_MakePrivate=Makes a class, method or field private."})
public class MakePrivate {

    private static final EnumSet<Modifier> opositeModifiers = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);

    @Hint(displayName = "#DN_MakePrivate", description = "#DESC_MakePrivate", category = "suggestions",
            hintKind = Hint.Kind.INSPECTION, severity = Severity.HINT)
    @TriggerTreeKind({Tree.Kind.CLASS, Tree.Kind.METHOD, Tree.Kind.VARIABLE})
    public static ErrorDescription convert(HintContext ctx) {

        Element element = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (element == null) {
            return null;
        }

        ModifiersTree modifiers;

        switch (element.getKind()) {
            case FIELD:
                VariableTree vt = (VariableTree) ctx.getPath().getLeaf();
                modifiers = vt.getModifiers();
                break;
            case CLASS:
                if (isTopLevelClass(element)) {
                    return null;
                }
                ClassTree ct = (ClassTree) ctx.getPath().getLeaf();
                modifiers = ct.getModifiers();
                break;
            case METHOD:
                MethodTree mt = (MethodTree) ctx.getPath().getLeaf();
                modifiers = mt.getModifiers();
                break;
            default:
                return null;
        }

        if (modifiers.getFlags().contains(Modifier.PRIVATE)) {
            return null;
        }

        Fix fix = FixFactory.changeModifiersFix(ctx.getInfo(), new TreePath(ctx.getPath(), modifiers), EnumSet.of(Modifier.PRIVATE), opositeModifiers, Bundle.ERR_MakePrivate());
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_MakePrivate(), fix);
    }

    private static boolean isTopLevelClass(Element element) {

        if (element == null) {
            return false;
        }

        if (element.getKind() == ElementKind.CLASS) {
            TypeElement clazz = (TypeElement) element;
            if (clazz.getNestingKind() == NestingKind.TOP_LEVEL) {
                return true;
            }
        }

        return false;
    }
}
