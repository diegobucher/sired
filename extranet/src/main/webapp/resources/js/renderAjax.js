/**
 * Função chamada em toda requisição ajax
 * */
 
function onRenderAjax() {
	adicionaRemoveClassBtnSelected();
	
	if($(".ui-fileupload").length > 0) {
//		adicionaRemoveClassBtnSelectedFileUpload();
//		adicionaTooltipe();
	}
}