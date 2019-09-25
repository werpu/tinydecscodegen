/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * /
 */

package net.werpu.tools.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.transformations.i18n.I18NKeyModel;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.ElementNotResolvableException;
import net.werpu.tools.supportive.utils.IntellijUtils;
import net.werpu.tools.supportive.utils.SwingUtils;
import net.werpu.tools.toolWindows.I18NToolWindowListener;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

import static net.werpu.tools.actions_all.shared.VisibleAssertions.assertNotTs;
import static net.werpu.tools.supportive.reflectRefact.PsiAnnotationUtils.getPositionFilter;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.STRING_TEMPLATE_EXPR;

public class GoToI18NDeclarationFromSource extends AnAction {
    public GoToI18NDeclarationFromSource() {
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        if (true) {
            return;
        }
        //must be either typescript or html to be processable
        VisibleAssertions.templateVisible(anActionEvent);
        if (IntellijUtils.getFolderOrFile(anActionEvent) == null) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        try {
            IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
            //if typescript file the cursor at least must be in a string
            if (!assertNotTs(ctx)) {
                VisibleAssertions.cursorInTemplate(anActionEvent);
                //TODO cursor in substring literal and no blanks???
            }

            if (anActionEvent.getPresentation().isEnabledAndVisible() &&
                    isStringTemplate(ctx)) {
                new I18NKeyModel(new IntellijFileContext(anActionEvent));
                //in case of a non determinable key an ElementNotResolvableException is thrown
            } else {
                anActionEvent.getPresentation().setEnabledAndVisible(false);
            }
        } catch (ElementNotResolvableException ex) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
        }
    }

    private boolean isStringTemplate(IntellijFileContext ctx) {
        int cursorPos = SwingUtils.getCurrentCursorPos(ctx);
        Predicate<PsiElementContext> positionFilter = getPositionFilter(cursorPos);

        //search for an embedded string template in case of an embedded template
        //we can recycle a lot from the parsing of the i18n transformation model
        Optional<PsiElementContext> oCtx = ctx.$q(STRING_TEMPLATE_EXPR)
                .filter(positionFilter)
                .reduce((el1, el2) -> el2);
        return oCtx.isPresent();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final IntellijFileContext fileContext = new IntellijFileContext(e);
        I18NKeyModel model = new I18NKeyModel(fileContext);

        //The idea is to send a message to the tool window
        //and let the tool window handle the rest, because
        //it has everything implemented anyway and needs to adjust
        ApplicationManager.getApplication().getMessageBus().syncPublisher(I18NToolWindowListener.GO_TO_DECLRATION).goToDeclaration(fileContext.getVirtualFile(), model);
    }
}
