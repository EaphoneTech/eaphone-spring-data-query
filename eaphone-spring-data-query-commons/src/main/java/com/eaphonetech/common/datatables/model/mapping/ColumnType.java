package com.eaphonetech.common.datatables.model.mapping;

import java.text.ParseException;
import java.util.Date;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public abstract class ColumnType {
    private static final String CODE_STRING = "string";
    private static final String CODE_INTEGER = "integer";
    private static final String CODE_DOUBLE = "double";
    private static final String CODE_DATE = "date";
    private static final String CODE_BOOLEAN = "boolean";

    public static final ColumnType STRING = new StringColumnType();
    public static final ColumnType INTEGER = new IntegerColumnType();
    public static final ColumnType DOUBLE = new DoubleColumnType();
    public static final ColumnType DATE = new DateColumnType();
    public static final ColumnType BOOLEAN = new BooleanColumnType();

    private String code;
    private boolean comparable;

    protected ColumnType(String code, boolean isComparable) {
        this.code = code;
        this.comparable = isComparable;
    }

    @Override
    public String toString() {
        return code;
    }

    private static boolean is(Class<?> target, Class<?>... checkTypes) {
        for (Class<?> checkClass : checkTypes) {
            if (target.isAssignableFrom(checkClass)) {
                return true;
            }
        }
        return false;
    }

    public static ColumnType parse(Class<?> c) {
        // default value is STRING
        if (c == null) {
            return STRING;
        }

        if (c.isPrimitive()) {
            // boolean, byte, char, short, int, long, float, and double.
            if (is(c, char.class)) {
                // TODO: whether type 'char' should be STRING or INTEGER?
                return STRING;
            } else if (is(c, byte.class, short.class, int.class, long.class)) {
                return INTEGER;
            } else if (is(c, float.class, double.class)) {
                return DOUBLE;
            } else if (is(c, boolean.class)) {
                return BOOLEAN;
            } else {
                // unexpected primitive type
                throw new IllegalArgumentException(String.format("unexpected type '%s'", c.toString()));
            }
        } else if (c.isArray()) {
            // TODO how to check array?
            // c.getComponentType() is type of array item;
            log.warn("Array type '{}' is not supported yet", c);
        } else {
            if (is(c, Character.class, String.class)) {
                return STRING;
            } else if (is(c, Byte.class, Short.class, Integer.class, Long.class)) {
                return INTEGER;
            } else if (is(c, Float.class, Double.class)) {
                return DOUBLE;
            } else if (is(c, Boolean.class)) {
                return BOOLEAN;
            } else if (is(c, Date.class)) {
                return DATE;
            }
        }
        return STRING;
    }

    public abstract Object tryConvert(Object o);

    static final class StringColumnType extends ColumnType {
        StringColumnType() {
            super(CODE_STRING, true);
        }

        @Override
        public Object tryConvert(Object o) {
            return o == null ? null : o.toString();
        }
    }

    static final class DateColumnType extends ColumnType {
        DateColumnType() {
            super(CODE_DATE, true);
        }

        @Override
        public Object tryConvert(Object o) {
            Object result = null;
            try {
                Date parsedDate = DateParser.parse(o.toString());
                result = parsedDate;
            } catch (ParseException pe) {
                result = null;
            }
            return result;
        }
    }

    static final class IntegerColumnType extends ColumnType {
        IntegerColumnType() {
            super(CODE_INTEGER, true);
        }

        @Override
        public Object tryConvert(Object o) {
            Object result = null;
            try {
                Integer parsedInteger = Integer.parseInt(o.toString());
                result = parsedInteger;
            } catch (NumberFormatException nfe) {
                result = null;
            }
            return result;
        }
    }

    static final class DoubleColumnType extends ColumnType {
        DoubleColumnType() {
            super(CODE_DOUBLE, true);
        }

        @Override
        public Object tryConvert(Object o) {
            Object result = null;
            try {
                Double parsedDouble = Double.parseDouble(o.toString());
                result = parsedDouble;
            } catch (NumberFormatException nfe) {
                result = null;
            }
            return result;
        }
    }

    static final class BooleanColumnType extends ColumnType {
        BooleanColumnType() {
            super(CODE_BOOLEAN, false);
        }

        @Override
        public Object tryConvert(Object o) {
            return o == null ? null : Boolean.parseBoolean(o.toString());
        }
    }
}
