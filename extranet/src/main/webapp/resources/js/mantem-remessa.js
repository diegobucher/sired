$(document).ready(function() {
	
	var mais = '<a href="#"><img src="imagens/mais.gif"  class="maismenos" /></a>';
	$('table.gridTree tbody tr:not(.sub)').hide();
	
	$('.sub td').css({
		background : '#ebebeb',
		borderBottom : '1px dotted #666',
		borderTop : '1px dotted #666'
	});
	
	$('.sub th').css({
		background : '#ebebeb',
		borderBottom : '1px dotted #666',
		borderTop : '1px dotted #666'
	}).prepend(mais);
	
});
