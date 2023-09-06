package io.eagle.util.jts;

import java.util.Objects;


public class JtsAttribute<T> {

    private T attribute = null;
    private boolean isPresent = false;

    public JtsAttribute() {
    }


    public JtsAttribute(T attribute) {
        this.attribute = attribute;
    }


    @Override
    public String toString() {
        return attribute + (this.isPresent ? "*" : "?");
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(attribute);
    }


    @Override
    public boolean equals(Object object) {
        if (object instanceof JtsAttribute) {
            JtsAttribute<?> that = (JtsAttribute<?>) object;
            return Objects.equals(this.attribute, that.attribute);
        }
        return false;
    }


    public boolean isPresent() {
        return isPresent;
    }


    public boolean isAbsent() {
        return !isPresent();
    }


    public T getAttribute() {
        return attribute;
    }


    public void setAttribute(T attribute) {
        this.isPresent = true;
        this.attribute = attribute;
    }


    public void clear() {
        this.isPresent = false;
        this.attribute = null;
    }

}
