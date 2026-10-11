package com.takima.backskeleton.common.unit;

public class IncompatibleUnitsException extends RuntimeException {

    public IncompatibleUnitsException(Unit from, Unit to) {
        super("Cannot convert " + from + " (" + from.getFamily() + ") to " + to + " (" + to.getFamily() + ")");
    }
}
