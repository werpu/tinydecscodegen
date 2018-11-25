package net.werpu.tools.supportive.reflectRefact.navigation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * enum to isolate parts
 * of the grammar for better code and
 * readability
 */
@Getter
@AllArgsConstructor
public enum UnaryCommand {

    CHILD_ELEM,
    P_FIRST,
    P_PARENTS,
    P_PARENT,
    P_LAST

}