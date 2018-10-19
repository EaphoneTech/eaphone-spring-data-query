$(document).ready(function () {
	/** $grid is the table body containing rendered <tr>s */
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
			filters: {
				orderNumber: {
					eq: 'O10001'
				}
			}
		}
	}, {
		summary: 'Single comparison filter',
		description: 'Comparison filter on single column',
		value: {
			filters: {
				price: {
					type: 'double',
					lt: 20.5
				}
			}
		}
	}, {
		summary: 'Range filter on one column',
		description: 'More filters on single column',
		value: {
			filters: {
				price: {
					type: 'double',
					gte: 10,
					lt: 20.5
				}
			}
		}
	}, {
		summary: 'Multiple columns',
		description: 'More filters on multiple columns',
		value: {
			filters: {
				date: {
					type: 'date',
					gt: '2012-01-01'
				},
				price: {
					type: 'double',
					gte: 10,
					lt: 20.5
				}
			}
		}
	}, {
		summary: 'Pagination',
		description: 'Customize page size and start page',
		value: {
			filters: {
				price: {
					type: 'double',
					gte: 10
				}
			},
			start: 14,
			length: 7
		}
	}, {
		summary: 'Orders',
		description: 'Multiple orders',
		value: {
			orders: [{
				field: 'amount',
				dir: 'desc'
			}, {
				field: 'price',
				dir: 'asc'
			}]
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
		draw: 1,
		start: 0,
		length: 10
	};

	/** This is the (last successful) request model */
	let model = {};

	/**
	 * display response into grid
	 * @param {object} data server response 
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
		let currentPage = Math.ceil((model.start || DEFAULT.start) / (model.length || DEFAULT.length)) + 1;
		let totalPage = Math.ceil(data.filtered / (model.length || DEFAULT.length));
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
	 * @param {string} data request body
	 */
	let post = function (data) {
		let jsonModel = JSON.parse(data);
		$.ajax({
			url: '/data/orders',
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
		let newStart = (Number.parseInt($(this).text()) - 1) * (model.length || DEFAULT.length);
		model.start = newStart;
		$postInput.val(JSON.stringify(model, null, 2));
		$('#btn-post').click();
		return false;
	});
});
