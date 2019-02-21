package net.werpu.tools.inspections;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TinyDecPsiReference extends PsiReferenceBase<PsiElement> {

    public TinyDecPsiReference(@NotNull PsiElement element, TextRange rangeInElement, boolean soft) {
        super(element, rangeInElement, soft);
    }

    public TinyDecPsiReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);
    }

    public TinyDecPsiReference(@NotNull PsiElement element, boolean soft) {
        super(element, soft);
    }

    public TinyDecPsiReference(@NotNull PsiElement element) {
        super(element);
    }


    @Nullable
    @Override
    public PsiElement resolve() {
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
