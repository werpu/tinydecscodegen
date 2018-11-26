package net.werpu.tools.supportive.reflectRefact.navigation;

import net.werpu.tools.supportive.fs.common.PsiElementContext;

import java.util.List;
import java.util.function.Function;

public class PsiElementQueryAdapter implements TreeQueryAdapter<PsiElementContext> {


    /**
     * find an element among the direct children
     */
    @Override
    public List<PsiElementContext> findChildren(PsiElementContext ctx, Function<PsiElementContext, Boolean> visitor) {
        return ctx.getChildren(visitor);
    }

    /**
     * recursive search deep down finding all elements
     */
    @Override
    public List<PsiElementContext> findElements(PsiElementContext ctx, Function<PsiElementContext, Boolean> visitor) {
        return ctx.findPsiElements2(visitor);
    }


    /**
     * recursive search up finding all matching elements
     */
    @Override
    public List<PsiElementContext> walkParents(PsiElementContext ctx, Function<PsiElementContext, Boolean> visitor) {
        return ctx.walkParents(visitor);
    }

    @Override
    public String getText(PsiElementContext ctx) {
        return ctx.getText();
    }

    @Override
    public String getIdentifier(PsiElementContext ctx) {
        return ctx.getName();
    }

    @Override
    public String toString(PsiElementContext ctx) {
        return ctx.getElement().toString();
    }


}
