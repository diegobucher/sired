/**
 * Faz o bind do evento para Validar envio de documento digital
 */
function validarArquivoPdfZip(evt) {
	var f = evt.target.files[0];
	rmcValidarTipoDocumento([{name: 'filename', value: f.name}]);
}

/**
 * Faz o bind do evento para Validar envio de documento digital
 */
$(document).on('change', '#uploadForm\\:documento', function (e) {
	validarArquivoPdfZip(e);
});

/**
 * Habilita/desabilita os filtros de acordo com o preenchimento do campo nº requisição
 */
$(document).on('keyup', '#formConsulta\\:numeroRequisicoes', function () {
	// Chamado sempre que um número de requisição for informado. Ao utilizar $(documento).on(...), 
	// os eventos do jQuery não são perdidos ao realizar update em um componente JSF, ao contrário do $(document).ready().
	toggleEditable($(this));
});

/**
 * Alterna a exibição do formulário de upload dos arquivos de atendimento
 */
$(document).on('click', '.btnAtendimento', function (e){
	e.preventDefault();
	$('div[id*="pnlAtendimentoLote"]').toggle();
});

/**
 * Realiza a validação prévia do arquivo de atendimento e exibe o modal com o resumo do arquivo caso esteja válido
 */
$(document).on('click', '#btnEnviarArqAtendimento', function (e) {
	if (formUploadIsValid()) {
		modalEnviarArquivo.show();
	}
});

/**
 * Envia o arquivo de atendimento para processamento online
 */
$(document).on('click', 'a[id*=btnEnviarArquivo]', function (e) {
	e.preventDefault();
	
//	$(this)
//		.prop('disabled', true)
//		.addClass('ui-state-disabled');
	
	prepareUploadAjax();
	$('#formConsulta').submit();
});

$(document).on('click', '#btnEnviarArqAtendimento', function (e){
	e.preventDefault();
	
	$(this)
	.addClass("ui-state-disabled")
	.replaceWith(function () { 
		return $("<span></span>", {
    		html: $(this).html(), 
    		class: $(this).prop("class")
    	}) 
	});
});

$(document).ready(function(e){
	
	// Utilizado para a tela "Minhas Requisições" que já deve iniciar com os campos desabilitados. 
	toggleEditable($('#formConsulta\\:numeroRequisicoes'));
	applyStylesResultsDataTable();
	//ajustarTamanhoCamposDinamicos();
	
	$('div[id*="pnlAtendimentoLote"]').hide();
});

/**
 * Executado no onshow do modal de confirmação de upload dos arquivos de atendimento
 */
var onShowModalEnviarArquivo = function() {
	var reader = new FileReader();
	reader.onload = function(e) {
		var lines = e.target.result.trim().replace("\r\n\r\n", "\r\n").split(/[\r\n]+/g);
		$('#resumoAtendimento').text("Foi(ram) identificado(s) " + lines.length + " item(ns) nesse lote para importação. Confirma atendimento(s)?");
	}
	
	reader.readAsText($('#arquivo')[0].files[0]);
	
//	$('a[id*=btnEnviarArquivo]')
//		.prop('disabled', false)
//		.removeClass('ui-state-disabled');
}

/**
 * Executado no bean ao exibir mensagens através do remote command
 */
var bindOnChangeMessage = function () {
	$('a[data-log]').click(function (e){
		e.preventDefault();
		location.href = $(this).prop('href') + '?report=' + $(this).data('log');
	});
}

var toggleEditable = function (component) {
	
	var state = ( !component.val() ) ? false : true;

	// InputText
	$('input.filtro-opcional').prop('disabled', state);
	toggleEditableClass($('input.filtro-opcional'), 'input-disabled');

	// Calendar
	$('span.filtro-opcional input').prop('disabled', state);
	toggleEditableClass($('span.filtro-opcional input'), 'input-disabled');

	// SelectOneMenu
	$('select.filtro-opcional')
		.prop('disabled', state);
	toggleEditableClass($('select.filtro-opcional'), 'select-disabled');
	
	$('#formConsulta\\:tabela_data tr.ui-datatable-empty-message td')
	.attr('style', 'color: #F00 !important; font-size: 12px !important; font-weight: bold;');
}; 	
	
var toggleEditableClass = function(component, className) {

	if (component.prop('disabled')) {
		component.addClass(className);
	} else {
		component.removeClass(className);
	}
};

var applyStylesResultsDataTable = function () {
	$('#formConsulta\\:tabela table[role=grid], #formConsulta\\:tblRelatorios table[role=grid]')
	.prop('class', 'zebra gridPesquisar')
	.css('border', '1px dotted rgb(102, 102, 102)');
		
	$('#formConsulta\\:tabela_head tr th[role=columnheader], #formConsulta\\:tblRelatorios_head tr th[role=columnheader]')
		.prop('class', 'borderLeft marginLeft center')
		.first()
		.removeClass('borderLeft');
			
	$('#formConsulta\\:tabela_data tr td[role=gridcell], #formConsulta\\:tblRelatorios_data tr td[role=gridcell]')
		.prop('class', 'borderLeft marginLeft center btnInsDocumento')
		.first()
		.removeClass('borderLeft');

	$('#formConsulta\\:tabela_data tr.ui-datatable-empty-message td, #formConsulta\\:tblRelatorios_data tr.ui-datatable-empty-message td')
		.attr('style', 'color: #F00 !important; font-size: 12px !important; font-weight: bold;');
}

//ajustarTamanhoCamposDinamicos = function() {
//	// Alpha
//    $(".mask-alpha, .mask-custom").each(function(){
//		var l = $(this).prop('maxlength') + 1;
//		l *= 8;
//		
//		var w = Math.max($(this).closest('label').width(), l);
//		$(this).width(w + 'px');
//    });           		
//	// Money
//	$(".mask-money").priceFormat({
//	    centsSeparator: ',',
//	    thousandsSeparator: '.'
//	});
//	
//	$('.mask-money, .mask-date input').each(function(){
//		// 80 pixels é a largura padrão para campos do tipo moeda
//		var LARGURA_PADRAO = 80;
//		
//		$(this).width(LARGURA_PADRAO);
//		var w = Math.max($(this).closest('label').width(), $(this).width());
//		$(this).width(w + 'px');
//	});
//	
//	// Necessário buscar a máscara no input hidden pois não foi possível nessa versão, utilizar atributos customizados ("data-*")
//	// e nem setar id dinâmicos por causa do ciclo de vida do JSF.
//	$.each($('.mask-custom'), function () {
//		var mask = $(this).next().val();
//		$(this).inputmask(mask);
//	});
//}