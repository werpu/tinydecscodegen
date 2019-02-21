package net.werpu.tools.supportive.reflectRefact.navigation.ideafixes;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.WalkingState;
import org.jetbrains.annotations.NotNull;

/**
 * @author cdr
 *
 * Sidenote I had to crossport this
 * class because of a gigantic f***up
 * from the idea guys who blocked an api
 * without documenting any replacment
 */
public abstract class PsiWalkingState extends WalkingState<PsiElement> {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.PsiWalkingState");
  private final PsiElementVisitor myVisitor;

  private static class PsiTreeGuide implements TreeGuide<PsiElement> {
    @Override
    public PsiElement getNextSibling(@NotNull PsiElement element) {
      return checkSanity(element, element.getNextSibling());
    }

    private static PsiElement checkSanity(PsiElement element, PsiElement sibling) {
      if (sibling == PsiUtilCore.NULL_PSI_ELEMENT) throw new PsiInvalidElementAccessException(element, "Sibling of "+element+" is NULL_PSI_ELEMENT");
      return sibling;
    }

    @Override
    public PsiElement getPrevSibling(@NotNull PsiElement element) {
      return checkSanity(element, element.getPrevSibling());
    }

    @Override
    public PsiElement getFirstChild(@NotNull PsiElement element) {
      return element.getFirstChild();
    }

    @Override
    public PsiElement getParent(@NotNull PsiElement element) {
      return element.getParent();
    }

    private static final PsiTreeGuide instance = new PsiTreeGuide();
  }

  protected PsiWalkingState(@NotNull PsiElementVisitor delegate) {
    this(delegate, PsiTreeGuide.instance);
  }
  protected PsiWalkingState(@NotNull PsiElementVisitor delegate, @NotNull TreeGuide<PsiElement> guide) {
    super(guide);
    myVisitor = delegate;
  }

  @Override
  public void visit(@NotNull PsiElement element) {
    element.accept(myVisitor);
  }

  @Override
  public void elementStarted(@NotNull PsiElement element) {

    super.elementStarted(element);
  }
}
