package com.eaphonetech.common.datatables.model.mapping;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Date;

import org.springframework.util.ReflectionUtils;

import lombok.Getter;

@Getter
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

	public static ColumnType parse(Class<?> clazz, String fieldName) {
		// default value is STRING
		if (clazz == null || fieldName == null) {
			return STRING;
		}
		Field field = ReflectionUtils.findField(clazz, fieldName);
		// TODO 嵌套关联的支持
		if (field.getType().isAssignableFrom(String.class)) {
			return STRING;
		} else if (field.getType().isAssignableFrom(Integer.class) || field.getType().isAssignableFrom(Long.class)) {
			return INTEGER;
		} else if (field.getType().isAssignableFrom(Float.class) || field.getType().isAssignableFrom(Double.class)) {
			return DOUBLE;
		} else if (field.getType().isAssignableFrom(Boolean.class)) {
			return BOOLEAN;
		} else if (field.getType().isAssignableFrom(Date.class)) {
			return DATE;
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
