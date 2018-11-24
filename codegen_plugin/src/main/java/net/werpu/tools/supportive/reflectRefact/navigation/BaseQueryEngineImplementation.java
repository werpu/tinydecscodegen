package net.werpu.tools.supportive.reflectRefact.navigation;

import java.util.stream.Stream;

public class BaseQueryEngineImplementation<T> {

    protected static TreeQueryEngine queryEngine;


    protected static<T> Stream<T> execQuery(Stream<T> subItem, Object[] commands) {


        return queryEngine.exec(subItem, commands, true);
    }
}
