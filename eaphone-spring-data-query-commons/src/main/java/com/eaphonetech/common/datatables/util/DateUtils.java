package com.eaphonetech.common.datatables.util;

import java.text.ParseException;
import java.util.Date;

import com.eaphonetech.common.datatables.model.mapping.DateParser;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtils {

	public Date tryParse(String text) {
		try {
			return DateParser.parse(text);
		} catch (ParseException e) {
			return null;
		}
	}
}
