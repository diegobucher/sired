function somenteNumero(e) {
	var tecla = e.charCode;
	if (tecla == undefined) { // Validação para o IE
		tecla = e.keyCode;
	}
	tecla = String.fromCharCode(tecla);
	if (e.which == 8 || e.which == 0 || /^\-?([0-9]+|Infinity)$/.test(tecla)) {
		return;
	} else {
		e.keyCode = 0;
		e.charCode = 0;
		e.returnValue = false;
		return false;
	}
}

function somenteNumerosSeparadosPorVirgula(e) {
	var tecla = e.charCode;
	
	console.log(tecla);
	
	if (tecla == 0) {
		return true;
	}
	
	if (tecla == undefined) { // Validação para o IE
		tecla = e.keyCode;
	}
	
	if (tecla == 99 || tecla == 118 || tecla == 120) {
		return;
	}
	
	tecla = String.fromCharCode(tecla);
	if (/^\-?([0-9,]+|Infinity)$/.test(tecla)) {
		return;
	} else {
		e.keyCode = 0;
		e.charCode = 0;
		e.returnValue = false;
		return false;
	}
}

function somenteNumeroCampoCodigoUnidade(e) {
	var tecla = e.charCode;
	
	if (tecla == undefined) { // Validação para o IE
		tecla = e.keyCode;
	}
	
	var ctrl=window.event.ctrlKey;
	var teclaCopiar=window.event.keyCode;
	alert(ctrl );
	
	if (ctrl && teclaCopiar == 67) {
		event.keyCode = 0;
		event.returnValue = false;
		e.keyCode = 0;
		e.charCode = 0;
		e.returnValue = false;
		return false;
	}
	if (ctrl && teclaCopiar == 86) {
		event.keyCode = 0;
		event.returnValue = false;
		e.keyCode = 0;
		e.charCode = 0;
		e.returnValue = false;
		return false;
	}

	tecla = String.fromCharCode(tecla);
	if (e.which == 8 || e.which == 0 || /^\-?([0-9]+|Infinity)$/.test(tecla)) {
		return;
	} else {
		e.keyCode = 0;
		e.charCode = 0;
		e.returnValue = false;
		return false;
	}
}

function overSomenteNumeros(element) {
	var valor = $(element).val();
	if (valor != "" && valor != undefined && !/^\-?([0-9]+|Infinity)$/.test(valor)) {
		$(element).val("");

	} else {
		return;
	}
}

function overSomenteNumerosSeparadosPorVirgula(campo) {

	campo.value = campo.value.toString().replace(/[^0-9,]/g, '').replace(/,,+/g, ',').replace(/^,/,'').replace(/,$/,'');
	return true;
}

function somenteLetrasRemoveEspacoInicio(e, campo) {
	var tecla = e.charCode;
	if (tecla == undefined) { // Validação para o IE
		tecla = e.keyCode;
	}
	tecla = String.fromCharCode(tecla);
	if (e.which == 8 || e.which == 0
			|| /^\-?([a-z A-Z]+|Infinity)$/.test(tecla)) {
		return removeEspacoInicio(e, campo);
	} else {
		e.keyCode = 0;
		e.charCode = 0;
		e.returnValue = false;
		return false;
	}
}

function somenteLetrasOuNumerosRemoveEspacoInicio(e, campo) {
	var tecla = e.charCode;
	if (tecla == undefined) { // Validação para o IE
		tecla = e.keyCode;
	}
	tecla = String.fromCharCode(tecla);
	if ((e.which == 8 || e.which == 0 || /^\-?([a-z A-Z]+|Infinity)$/.test(tecla))
			|| (e.which == 8 || e.which == 0 || /^\-?([0-9]+|Infinity)$/.test(tecla)) ) {
		return removeEspacoInicio(e, campo);
	} else {
		e.keyCode = 0;
		e.charCode = 0;
		e.returnValue = false;
		return false;
	}
}

function removeEspacoInicio(e, campo) {
	var tecla = e.charCode;
	if (tecla == undefined) { // Validação para o IE
		tecla = e.keyCode;
	}
	tecla = String.fromCharCode(tecla);
	valorCampo = campo.value.concat(tecla);
	if (valorCampo.length == 1 && tecla == " ")
		return false;
	return true;

}

function previneCharDeExportacao(e) {
		
	var tecla = e.charCode;
	if (tecla == undefined) { // Validação para o IE
		tecla = e.keyCode;
	}
	tecla = String.fromCharCode(tecla);
	if (/;/g.test(tecla) || /:/g.test(tecla)) {
		e.keyCode = 0;
		e.charCode = 0;
		e.returnValue = false;
		return false;
	}
}

function overPrevineCharDeExportacao(campo) {
	
	campo.value = campo.value.toString().replace(/;/g, '').replace(/:/g, '');
}

function formatarCnpj() {
	// $("input[id*='Cnpj']").mask('99.999.999/9999-99');

	var valorCnpj = $("input[id*='Cnpj']").val();
	if (valorCnpj != null && valorCnpj != "") {

		exp = /\-|\.|\/|\(|\)| /g;

		valorCnpj = valorCnpj.replace(exp, "");

		valorCnpj = valorCnpj.substring(0, 2) + "." + valorCnpj.substring(2, 5)
				+ "." + valorCnpj.substring(5, 8) + "/"
				+ valorCnpj.substring(8, 12) + "-"
				+ valorCnpj.substring(12, 14);

		$("input[id*='Cnpj']").val(valorCnpj);
	}
}

/**
 * Metodo submete o form ao tecla enter
 * 
 * @param event
 */
function submitForm(event) {
	if (event.keyCode == 13) {
		// Submete o form se os elementos ativos forem da tela de ConsultarSimulacao
		if ($(document.activeElement).attr("id").indexOf("formAbertura") != -1) {
			$('.submitForm').click();
		}
	}
}

function executaEnterCampo(e, campo, id) {	
    if (e.keyCode) {  
        tecla = e.keyCode;  
    } else {  
        tecla = e.which;  
    }  
    
    shift = e.shiftKey;
    
    if ((tecla >= 65 && tecla <= 90) ||
    	(tecla >= 97 && tecla <= 122)||	
        (!shift && tecla >= 37 && tecla <= 40) ||  
        (tecla >= 48 && tecla <= 57) ||  
        (tecla == 08) || (tecla == 09) || 
        (tecla == 13) || (tecla == 32) ||
        (tecla == 46)) {  
    	if (tecla == 13){
    		$(campo).val($(campo).val().replace(/[^a-zA-Z 0-9]+/g,''));
    		
    		forceClick(id);
    		
    		return false;
    	}
    } else {  
        if (e.keyCode) {  
            e.returnValue = false;  
        } else {  
            e.preventDefault();  
        }  
        
        return false;
    }     
}

function apenasNumeros(e, id) {	
    if (e.keyCode) {  
        tecla = e.keyCode;  
    } else {  
        tecla = e.which;  
    }  
    
    if ((tecla >= 48 && tecla <= 57)   ||
    	(tecla >= 37 && tecla <= 40)   ||
        (tecla == 08) || (tecla == 46) || 
        (tecla == 13) || (tecla == 32) ||
        (tecla == 09)) {  
    	if (tecla == 13){
    		forceClick(id);
    		return false;
    	}
    } else {  
        if (e.keyCode) {  
            e.returnValue = false;  
        } else {  
            e.preventDefault();  
        }  
        return false;
    }     
	
}

function formatarSemCaracterEspecial(event, campo) {	
	var tecla = event.keyCode || event.witch;	
	if (tecla != 37 && tecla != 39)  {	
		$(campo).val($(campo).val().replace(/[^a-zA-Z 0-9]+/g,''));
		return true;
	} else {
		return true;
	}
}

function limitarTextArea(limitField, limitNum) {
	/*
	 * limitField.value = limitField.value.replace(new RegExp("\r", "g"), "
	 * ").replace(new RegExp("\n", "g"), " "); if (limitField.value.length >
	 * limitNum) { limitField.value = limitField.value.substring(0, limitNum); }
	 */
	var length = 0;
	for ( var i = 0; i < limitField.value.length; i++) {
		if (limitField.value[i] == '\r' || limitField.value[i] == '\n') {
			length += 2;
			if (i < limitNum) {
				limitNum = limitNum - 1;
			}
		} else {
			length++;
		}
	}
	if (length > limitNum) {
		limitField.value = limitField.value.substring(0, limitNum);
	}
}

function formatarCpf() {

	var valorCpf = $("input[id*='Cpf']").val();
	valorCpf = valorCpf.replace(/\-/g, "").replace(/\./g, '');

	if (valorCpf != "" && valorCpf != undefined
			&& !/^\-?([0-9]+|Infinity)$/.test(valorCpf)) {
		$("input[id*='Cpf']").val("");
	} else if (valorCpf != null && valorCpf != "") {

		exp = /\-|\.|\/|\(|\)| /g;

		valorCpf = valorCpf.replace(exp, "");

		valorCpf = valorCpf.substring(0, 3) + "." + valorCpf.substring(3, 6)
				+ "." + valorCpf.substring(6, 9) + "-"
				+ valorCpf.substring(9, 11);

		$("input[id*='Cpf']").val(valorCpf);
	}
}

$(function() {
	if ($(".matriculaCaixa").length > 0) {
		$(".matriculaCaixa").inputmask("a999999");
	}
});

/*
 * function matuser(e){
 * 
 * var tecla = e.charCode;
 * 
 * 
 * 
 * if (tecla == undefined) { // Validação para o IE
 * 
 * tecla = e.keyCode; }
 * 
 * tecla = String.fromCharCode(tecla); // if (e.which == 8 || e.which == 0 ||
 * /^\-?([0-9]+|Infinity)$/.test(tecla)) { if (e.which == 8 || e.which == 0 ||
 * /^\-?([0-9]+|Infinity))$/.test(tecla)) {
 * 
 * 
 * return; } else {
 * 
 * e.keyCode = 0;
 * 
 * e.charCode = 0;
 * 
 * e.returnValue = false;
 * 
 * return false; } }
 */

/**
 * Função Principal
 * 
 * @param w -
 *            O elemento que será aplicado (normalmente this).
 * @param e -
 *            O evento para capturar a tecla e cancelar o backspace.
 * @param m -
 *            A máscara a ser aplicada.
 * @param r -
 *            Se a máscara deve ser aplicada da direita para a esquerda. Veja
 *            Exemplos.
 * @param a -
 * @returns null
 */
function maskIt(w, e, m, r, a) {
	// Cancela se o evento for Backspace
	if (!e) {
		e = window.event;
	}
	if (e.keyCode) {
		code = e.keyCode;
	} else if (e.which) {
		code = e.which;
	}

	// Variáveis da função
	var txt = (!r) ? w.value.replace(/[^\d]+/gi, '') : w.value.replace(/[^\d]+/gi, '').reverse();
	var mask = (!r) ? m : m.reverse();
	var pre = (a) ? a.pre : "";
	var pos = (a) ? a.pos : "";
	var ret = "";

	if (code == 9 || code == 8|| code == 37 || code == 39 || code == 46	|| txt.length == mask.replace(/[^#]+/g, '').length)
		return false;

	// Loop na máscara para aplicar os caracteres
	for ( var x = 0, y = 0, z = mask.length; x < z && y < txt.length;) {
		if (mask.charAt(x) != '#') {
			ret += mask.charAt(x);
			x++;
		} else {
			ret += txt.charAt(y);
			y++;
			x++;
		}
	}

	// Retorno da função
	ret = (!r) ? ret : ret.reverse();
	w.value = pre + ret + pos;
}

PrimeFaces.locales['pt'] = {

	closeText : 'Fechar',

	prevText : 'Anterior',

	nextText : 'Próximo',

	currentText : 'Começo',

	monthNames : [ 'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
			'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro' ],

	monthNamesShort : [ 'Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago',
			'Set', 'Out', 'Nov', 'Dez' ],

	dayNames : [ 'Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta',
			'Sábado' ],

	dayNamesShort : [ 'Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb' ],

	dayNamesMin : [ 'D', 'S', 'T', 'Q', 'Q', 'S', 'S' ],

	weekHeader : 'Semana',

	firstDay : 1,

	isRTL : false,

	showMonthAfterYear : false,

	yearSuffix : '',

	timeOnlyTitle : 'Só Horas',

	timeText : 'Tempo',

	hourText : 'Hora',

	minuteText : 'Minuto',

	secondText : 'Segundo',

	ampm : false,

	month : 'Mês',

	week : 'Semana',

	day : 'Dia',

	allDayText : 'Todo Dia'

};

// Novo método para o objeto 'String'
String.prototype.reverse = function() {
	return this.split('').reverse().join('');
};

function somenteLetras(e) {
	var tecla = e.charCode;
	if (tecla == undefined) { // Validação para o IE
		tecla = e.keyCode;
	}
	tecla = String.fromCharCode(tecla);
	if (e.which == 8 || e.which == 0
			|| /^\-?([a-z A-Z]+|Infinity)$/.test(tecla)) {
		return;
	} else {
		e.keyCode = 0;
		e.charCode = 0;
		e.returnValue = false;
		return false;
	}
}

function somenteLetrasEnumeros(e) {
	var tecla = e.charCode;
	if (tecla == undefined) { // Validação para o IE
		tecla = e.keyCode;
	}
	tecla = String.fromCharCode(tecla);
	if (e.which == 8 || e.which == 0
			|| /^\-?([a-z A-Z 0-9]+|Infinity)$/.test(tecla)) {
		return;
	} else {
		e.keyCode = 0;
		e.charCode = 0;
		e.returnValue = false;
		return false;
	}
}

$(function() {
	$("#dropIdentif").hide();
	$("#btnInfos").mouseover(function() {
		$("#dropIdentif").show();
	}).mouseout(function() {
		$("#dropIdentif").hide();
	});

	// $('.zebr tbody tr:odd').addClass('impar');
	// $('.zebr tbody tr:even').addClass('par');

	/*
	 * $('.zebr tbody tr').hover(function() { $(this).addClass('destacar'); },
	 * function() { $(this).removeClass('destacar'); });
	 */
});

$(document).ready(function() {
	formatarDatas();
	$(".carregando").hide();
	$.unblockUI();
	adicionaRemoveClassBtnSelected();

	// Foco no Menu
	$(".menuLink").focus(function() {
		$(this).addClass('link-menu-selected');
	});

	// Perda de foco do menu
	$(".menuLink").blur(function() {
		$(this).removeClass('link-menu-selected');
	});	

});

function showStatus() {
	$("#idLoad").show();
	/*$.blockUI({
		message : null
	});*/
}

function hideStatus() {
	$("#idLoad").hide();
	$.unblockUI();
}

function pad(n, len, padding) {
	var sign = '', s = n;

	if (typeof n === 'number') {
		sign = n < 0 ? '-' : '';
		s = Math.abs(n).toString();
	}

	if ((len -= s.length) > 0) {
		s = Array(len + 1).join(padding || '0') + s;
	}
	return sign + s;
}

$(window).bind('beforeunload', function() {
	$("input[id*='Data']").mask('99/99/9999');
	$(".carregando").show();
	$.blockUI({
		message : null
	});
});

function forceClick(id) {
	jQuery(PrimeFaces.escapeClientId(id)).click();
}

/**
 * Metodo limapa os campos da sessao que possui as datas inicio e fim
 * 
 * @returns {Boolean}
 */
function limparSessaoDatas() {
	$("input[id='mainFormCadastro:idDataExpedicao_input']").val("");
	$("input[id='mainFormCadastro:idDataNascimento_input']").val("");
	return false;
}

/**
 * Metodo responsavel por formatar datas
 */
function formatarDatas() {
	// $("input[id*='Data']").mask('99/99/9999');

	$("input[id*='Data']").each(
			function() {
				valorData = $(this).val();
				if (valorData != null && valorData != "") {
					valorData = valorData.replace("/", "").replace("/", "");
					valorData = valorData.substring(0, 2) + "/"
							+ valorData.substring(2, 4) + "/"
							+ valorData.substring(4, 8);
					$(this).val(valorData);
				}
			});
}

function guardarIdComponenteController(element) {
	if (element != null) {
		var idComponente = element.id;
		// Função definida pelo p:remoteCommand e esta implementada no
		// ContextoController
		guardarIdComponente([ {
			name : 'idComponente',
			value : idComponente
		} ]);
	} else {
		return null;
	}
}

function giveFocus(elementId) {
	var element = document.getElementById(elementId);
	if (element != null) {
		element.focus();
	}
}

function adicionaRemoveClassBtnSelected() {
	// Foco dos botões
	$(".buttonSelected").focus(function() {
		$(this).addClass('btnSelected');
	});

	// Perda de foco dos botões
	$(".buttonSelected").blur(function() {
		$(this).removeClass('btnSelected');
	});

	// Click dos botões
	$(".buttonSelected").click(function() {
		$(this).removeClass('btnSelected');
	});
}

function noCopy(event) {
	var tecla = String.fromCharCode(event.keyCode).toLowerCase();
	if (event.ctrlKey && (tecla == "c" || tecla == "v")) {
		window.event ? event.returnValue = false : event.preventDefault();
		return false;
	}
}

function overSomenteNumeros(element) {
	var valor = $(element).val();
	if (valor != "" && valor != undefined
			&& !/^\-?([0-9]+|Infinity)$/.test(valor)) {
		$(element).val("");
	} else {
		return;
	}
}

$(document).on('keydown', 'textarea', function(event) {
	if ( event.which == 13 ) {
		event.preventDefault();
	    return false;
	}
});

var TEXTAREA_SELECTOR = 'textarea[maxlength]';

$(document).on('keyup', TEXTAREA_SELECTOR, function () {
	maxlengthIEWorkaround($(this));			        				
})

$(document).on('blur', TEXTAREA_SELECTOR, function () {
	maxlengthIEWorkaround($(this));			        				
})

var maxlengthIEWorkaround = function ($this) {
	var content = $this.val();
	$this.val($this.val().slice(0, $this.prop('maxlength')));
}
