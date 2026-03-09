package cn.zfz.pureorm.core;

import java.io.Serializable;

@FunctionalInterface
public interface LambadaColumn<T> extends Serializable {
    Object apply(T entity);
}
