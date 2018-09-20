$(document).ready(function() {
	/** $grid is table body */
	let $grid = $('ddd');
	let $postInput = $();
	let $output = $('');
	
	/**
	 * display response into grid
	 */
	let display = function(data) {
		$grid.empty();
	};
	
	let post = function(data){
		$.post('/data/orders', data, function(response){
			display(response);
		});
	};
});