package io.eagle.util.jts;

import io.eagle.util.DataType;


@FunctionalInterface
public interface TypeChecker {

    void checkType(DataType dataType);
}
