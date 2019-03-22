package net.werpu.tools.supportive.fs.common;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;


/**
 * Model representation of an L18N Tree
 * it either is a key->string value pair or a key -> L18nSubtree
 * key value pair
 */

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class L18NElement {

    @EqualsAndHashCode.Exclude
    private final L18NElement parent;

    private final String key;

    @EqualsAndHashCode.Exclude
    private final String stringValue;

    @EqualsAndHashCode.Exclude
    List<L18NElement> subElements = new ArrayList<>();





}
