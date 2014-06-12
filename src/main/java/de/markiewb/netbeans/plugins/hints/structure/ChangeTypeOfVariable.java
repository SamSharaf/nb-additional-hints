/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 * Portions Copyrighted 2014 benno.markiewicz@googlemail.com
 */
package de.markiewb.netbeans.plugins.hints.structure;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author markiewb
 */
@NbBundle.Messages({
    "DN_ChangeTypeOfVariable=Change type of variable",
    "DESC_ChangeTypeOfVariable=Changes type of variable <p>Provided by <a href=\"https://github.com/markiewb/nb-additional-hints\">nb-additional-hints</a> plugin</p>",})
public class ChangeTypeOfVariable {

    private static EnumSet<ElementKind> supportedKinds = EnumSet.of(ElementKind.LOCAL_VARIABLE, ElementKind.CLASS, ElementKind.INTERFACE);

    @Hint(displayName = "#DN_ChangeTypeOfVariable", description = "#DESC_ChangeTypeOfVariable", category = "suggestions", hintKind = Hint.Kind.ACTION, severity = Severity.HINT, enabled = true)
    @NbBundle.Messages({"# {0} - from","# {1} - to",
        "ERR_ChangeTypeOfVariable=Change <b>{0}</b> to <b>{1}</b>"})
    @TriggerTreeKind(Tree.Kind.IDENTIFIER)
    public static ErrorDescription toTernary(HintContext ctx) {
        TreePath path = ctx.getPath();

//        if (!Tree.Kind.VARIABLE.equals(path.getParentPath().getLeaf().getKind()) ){
//            return null;
//        }
        supportedKinds = EnumSet.of(ElementKind.LOCAL_VARIABLE, ElementKind.CLASS, ElementKind.INTERFACE);
        Element element = ctx.getInfo().getTrees().getElement(path);
        if (!supportedKinds.contains(element.getKind())) {
            return null;
        }

//        //do not tranform @SuppressWarnings("ABC") to @SuppressWarnings(this."ABC")
//        if (isWithinAnnotation(path)) {
//            return null;
//        }
        TypeMirror asType = element.asType();
        DeclaredType originaldt = (DeclaredType) asType;

        Types types = ctx.getInfo().getTypes();
//        TypeUtilities typeUtilities = ctx.getInfo().getTypeUtilities();
        List<Fix> fixes = new ArrayList<Fix>();
        List<? extends TypeMirror> directSupertypes = types.directSupertypes(asType);
        for (TypeMirror typeMirror : directSupertypes) {

            if (typeMirror.getKind() != TypeKind.DECLARED) {
                continue;
            }
            //FIXME group by depth by interfaces
            //FIXME support more super super super types
            DeclaredType dt = (DeclaredType) typeMirror;
            TypeElement e = (TypeElement) dt.asElement();
            String qn = e.getQualifiedName().toString();

//            Element asElement = types.asElement(typeMirror);
//            if (asElement.getKind()!=Typ){
//            types.getDeclaredType(null, typeArgs)
//            String typeName = typeMirror.toString()+asElement.getKind();
            String typeName = qn;

            //FIXME add option for this
            if (Object.class.getName().equals(typeName)) {
                continue;
            }

            TreePath myPath = path;
            final List<? extends TypeMirror> newTypeArguments = dt.getTypeArguments();
            final List<? extends TypeMirror> oldTypeArguments = originaldt.getTypeArguments();
            //Cornercase:
            if (newTypeArguments.isEmpty() && !oldTypeArguments.isEmpty()) {
                //HashMap<String, Integer> -> Cloneable
                //also replace typeparameters
                myPath = path.getParentPath();
            }
            //add type arguments like Map<String, Integer>
            StringBuilder sb = new StringBuilder();
//            sb.append(typeName.replace("<", "&lt;"));
            sb.append(typeName);
            if (!newTypeArguments.isEmpty()) {
                sb.append("&lt;").append(newTypeArguments).append(">");
            }
            String newTypeFormatted = sb.toString();
            String oldTypeFormatted = asType.toString().replace("<", "&lt;");
            Fix fix = org.netbeans.spi.java.hints.JavaFixUtilities.rewriteFix(ctx, Bundle.ERR_ChangeTypeOfVariable(oldTypeFormatted, newTypeFormatted), myPath, typeName);
            fixes.add(fix);
        }
        if (!fixes.isEmpty()) {
            Fix[] fixs = fixes.toArray(new Fix[fixes.size()]);

            return ErrorDescriptionFactory.forName(ctx, path, Bundle.DN_ChangeTypeOfVariable(), fixs);
        }
        return null;
    }

}
