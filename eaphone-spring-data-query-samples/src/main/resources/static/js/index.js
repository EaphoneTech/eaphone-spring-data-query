$(document).ready(function () {
	/** $grid is table body */
	let $grid = $('#table-body');
	/** $postInput is the element containing data to post */
	let $postInput = $('#input-post-data');
	/** $output is the element to display server response */
	let $output = $('#server-response');
	let $pagination = $('#pagination');

	const DEFAULT = {
		draw: 1,
		start: 1,
		length: 10
	};

	/** This is the (last successful) request model */
	let model = {
		filters: {
			orderNumber: {
				eq: 'O10001'
			}
		}
	};

	/**
	 * display response into grid
	 */
	let display = function (data) {
		// generate table rows
		$.each(data.data, function (idx, val) {
			$('<tr>')
				.append($('<td>').text(val.id))
				.append($('<td>').text(val.date))
				.append($('<td>').text(val.orderNumber))
				.append($('<td>').text(val.isValid))
				.append($('<td>').text(val.amount))
				.append($('<td>').text(val.price))
				.appendTo($grid);
		});

		// draw pagination
		let currentPage = Math.ceil(model.start || DEFAULT.start) / (model.length || DEFAULT.length);
		let totalPage = Math.ceil(data.filtered / (model.length || DEFAULT.length));
		let startPage = Math.max(1, currentPage - 2);
		let endPage = Math.min(currentPage + 2, totalPage);
		for (let i = startPage; i <= endPage; i++) {
			$pagination.append($('<a href="#" class="page-link">' + i + '</a>'));
		}

		// display server output 
		$output.text(JSON.stringify(data, null, 2));
		hljs.highlightBlock($output[0]);
	};

	let post = function (data) {
		let jsonModel = JSON.parse(data);
		$.ajax({
			url: '/data/orders',
			type: 'post',
			data: data,
			contentType: 'application/json; charset=UTF-8',
			success: function (response) {
				display(response);
				model = jsonModel;
			},
			dataType: 'json'
		});
	};

	/** event initialization */
	$('#btn-post').click(function () {
		$grid.empty();
		$pagination.empty();
		$output.empty();

		let requestData = $postInput.val();
		post(requestData);
		return false;
	});

	/** all pagination links */
	$('a.page-link').on('click', function () {
		let newStart = Number.parseInt($(this).text()) * (model.length || DEFAULT.length);
		model.start = newStart;
		$postInput.text(JSON.stringify(model, null, 2));
		$('#btn-post').click();
		return false;
	});
});
