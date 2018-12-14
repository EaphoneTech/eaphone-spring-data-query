$(document).ready(function () {
	/**
	 * $grid is the table body containing rendered
	 * <tr>s
	 */
	let $grid = $('#table-body');
	/** $postInput contains data (string) to post */
	let $postInput = $('#input-post-data');
	/** $output is the element to display server response */
	let $output = $('#server-response');
	/** $pagination contains pagination links */
	let $pagination = $('#pagination');
	/** $examples contains example buttons */
	let $examples = $('#examples');

	/** some pre-defined examples */
	const examples = [{
		summary: 'Single text filter',
		description: 'The most basic filter',
		value: {
			where: {
				orderNumber: {
					'_eq': 'O10001'
				}
			}
		}
	}, {
		summary: 'Single comparison filter',
		description: 'Comparison filter on single column',
		value: {
			where: {
				price: {
					'_lt': 20.5
				}
			}
		}
	}, {
		summary: 'Enumeration (in)',
		description: 'Enumeration with string',
		value: {
			where: {
				orderNumber: {
					'_in': ['O10001', 'O10002']
				}
			}
		}
	}, {
		summary: 'Enumeration (in)',
		description: 'Enumeration with number',
		value: {
			where: {
				amount: {
					'_in': [5, 9, 13]
				}
			}
		}
	}, {
		summary: 'Range filter on one column',
		description: 'More where on single column',
		value: {
			where: {
				price: {
					'_gte': 10,
					'_lt': 20.5
				}
			}
		}
	}, {
		summary: 'Multiple columns',
		description: 'More where on multiple columns',
		value: {
			where: {
				date: {
					'_gt': '2012-01-01'
				},
				price: {
					'_gte': 10,
					'_lt': 20.5
				}
			}
		}
	}, {
		summary: 'Pagination',
		description: 'Customize page size and offset page',
		value: {
			where: {
				price: {
					'_gte': 10
				}
			},
			offset: 30,
			limit: 10
		}
	}, {
		summary: 'Orders',
		description: 'Multiple orders',
		value: {
			order_by: [{
				'amount': 'desc'
			}, {
				'price': 'asc'
			}]
		}
	}, {
		summary: 'Like',
		description: 'SQL like',
		value: {
			where: {
				"orderNumber": {
					"_like": "O100%"
				}
			}
		}
	}];
	
	/** Draw example buttons */
	$.each(examples, function(idx, val) {
		$('<button type="button" class="btn btn-outline-primary mr-2 mb-2">')
			.text(val.summary)
			.attr({title: val.description})
			.click(function(){
				$postInput.val(JSON.stringify(val.value, null, 2));
			})
			.appendTo($examples);
	});
	
	/** default value that will apply to each request if not being overridden */
	const DEFAULT = {
		offset: 0,
		limit: 10
	};

	/** This is the (last successful) request model */
	let model = {};

	/**
	 * display response into grid
	 * 
	 * @param {object}
	 *            data server response
	 */
	let display = function (data) {
		// generate table rows
		$.each(data.data, function (idx, val) {
			$('<tr>')
				.append($('<td>').text(idx + 1))
				.append($('<td>').text(val.id))
				.append($('<td>').text(val.date))
				.append($('<td>').text(val.orderNumber))
				.append($('<td>').text(val.isValid))
				.append($('<td>').text(val.amount))
				.append($('<td>').text(val.price))
				.appendTo($grid);
		});

		// draw pagination
		let currentPage = Math.ceil((model.offset || DEFAULT.offset) / (model.limit || DEFAULT.limit)) + 1;
		let totalPage = Math.ceil(data.filtered / (model.limit || DEFAULT.limit));
		let startPage = Math.max(1, currentPage - 3);
		let endPage = Math.min(currentPage + 3, totalPage);
		for (let i = startPage; i <= endPage; i++) {
			$('<li class="page-item">')
				.append($('<a href="#" class="page-link">')
					.text(i)
				)
				.addClass(i === currentPage ? 'active' : '')
				.appendTo($pagination);
		}

		// display server output
		$output.text(JSON.stringify(data, null, 2));
		hljs.highlightBlock($output[0]);
	};

	/**
	 * POST to server
	 * 
	 * @param {string}
	 *            data request body
	 */
	let post = function (data) {
		let jsonModel = JSON.parse(data);
		$.ajax({
			url: '/data/orders/search',
			type: 'post',
			data: data,
			contentType: 'application/json; charset=UTF-8',
			success: function (response) {
				model = jsonModel;
				display(response);
			},
			dataType: 'json'
		});
	};

	/** event initialization */
	$('#btn-post').click(function () {
		// clear output
		$grid.empty();
		$pagination.empty();
		$output.empty();
		
		let requestData = $postInput.val() || '{}';
		post(requestData);
		return false;
	});

	/** all pagination links */
	$('#pagination').on('click', 'a.page-link', function () {
		let newStart = (Number.parseInt($(this).text()) - 1) * (model.limit || DEFAULT.limit);
		model.offset = newStart;
		$postInput.val(JSON.stringify(model, null, 2));
		$('#btn-post').click();
		return false;
	});
});
