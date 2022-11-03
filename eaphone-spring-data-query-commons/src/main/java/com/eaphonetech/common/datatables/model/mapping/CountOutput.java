package com.eaphonetech.common.datatables.model.mapping;

import lombok.Data;

@Data
public class CountOutput {

	/**
	* Total records, before filtering (i.e. the total number of records in the database)
	*/
	private long total = 0L;

	/**
	 * Total records, after filtering (i.e. the total number of records after filtering has been
	 * applied - not just the number of records being returned for this page of data).
	 */
	private long filtered = 0L;

	/**
	* Optional: If an error occurs during the running of the server-side processing script, you can
	* inform the user of this error by passing back the error message to be displayed using this
	* parameter. Do not include if there is no error.
	*/
	private String error;

}
