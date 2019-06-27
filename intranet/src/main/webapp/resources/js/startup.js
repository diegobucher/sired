
//Novo método para o objeto 'String'
String.prototype.reverse = function(){
       return this.split('').reverse().join('');
};


$(function(){	
	
	$("#dropIdentif").hide();
	$("#btnInfos").mouseover(function() {
		$("#dropIdentif").show();
	}).mouseout(function(){
		$("#dropIdentif").hide();
	});

//	$('.zebra tbody tr:odd').addClass('impar');			
//	$('.zebra tbody tr:even').addClass('par');			
	
	$('.zebra tbody tr').hover(
	function() {
		$(this).addClass('destacar');			
	},
	function() {
		$(this).removeClass('destacar');			
		});
	}
);


$(document).ready(function(){
	// Formata data
	formatarDatas();
	// Formata Cnpj
	formatarCnpj();
	// Esconde elemento com o class carregando
	$(".carregando").hide();
	$.unblockUI();
	
	// Remove lista de anexos do componente de upload de arquivo
	//$(".ui-fileupload-content").remove();
	
	$("form").keyup(function(event){
		 // função submete o form ao teclar enter
		 submitForm(event);
	 });
});



$(window).bind('beforeunload',function(){
	$(".carregando").show();
	$.blockUI({message: null});
});

function removerDivFileUpload() {
	$( ".ui-fileupload-content" ).remove();
}