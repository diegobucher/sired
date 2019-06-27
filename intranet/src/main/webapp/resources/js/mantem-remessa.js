$(document).ready(function() {
	var mais = '<a href="#"><img src="imagens/mais.gif"  class="maismenos" /></a>'		
		
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
	$('img', $('.sub th')).click(function(event) {
		event.preventDefault();
		if (($(this).attr('src')) == 'imagens/menos.gif') {
			$(this).attr('src', 'imagens/mais.gif').parents().siblings('tr').hide();
		} else {
			$(this).parents('tbody').siblings('tbody').children('tr:not(tr.sub)').hide()
			$('.maismenos').attr('src', 'imagens/mais.gif')
			$(this).attr('src', 'imagens/menos.gif').parents().siblings('tr').show();
		}
		;
	});
	
});
