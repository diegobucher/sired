$(document).ready(function() {
	ajustarTamanhoCamposDinamicos();
});

ajustarTamanhoCamposDinamicos = function() {
	// Alpha
    $(".mask-alpha, .mask-custom").each(function(){
		var l = $(this).prop('maxlength') + 1;
		l *= 8;
		
		var w = Math.max($(this).closest('label').width(), l);
		$(this).width(w + 'px');
    });           		
	// Money
	$(".mask-money").priceFormat({
	    centsSeparator: ',',
	    thousandsSeparator: '.'
	});
	
	$('.mask-money, .mask-date input').each(function(){
		// 80 pixels é a largura padrão para campos do tipo moeda
		var LARGURA_PADRAO = 80;
		
		$(this).width(LARGURA_PADRAO);
		var w = Math.max($(this).closest('label').width(), $(this).width());
		$(this).width(w + 'px');
	});
	
	// Necessário buscar a máscara no input hidden pois não foi possível nessa versão, utilizar atributos customizados ("data-*")
	// e nem setar id dinâmicos por causa do ciclo de vida do JSF.
	$.each($('.mask-custom'), function () {
		var mask = $(this).next().val();
		$(this).inputmask(mask);
	});
}