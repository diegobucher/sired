function alterarNumeroTerminal(terminal, size){
	rmcNumeroTerminal([
		{name:'linha', value: size}, 
		{name:'numeroTerminal', value: terminal.value} 
	]);
}

function alterarGrupo1(valor, size){
	rmcAlterarGrupo1([
		{name:'linha', value: size}, 
		{name:'grupo', value: valor.value} 
		]);
}

function alterarGrupo2(valor, size){
	rmcAlterarGrupo2([
		{name:'linha', value: size}, 
		{name:'grupo', value: valor.value} 
		]);
}

function alterarGrupo3(valor, size){
	rmcAlterarGrupo3([
		{name:'linha', value: size}, 
		{name:'grupo', value: valor.value} 
		]);
}
