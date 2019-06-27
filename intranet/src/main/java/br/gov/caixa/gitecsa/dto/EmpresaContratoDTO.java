package br.gov.caixa.gitecsa.dto;

import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;

public class EmpresaContratoDTO {

    private EmpresaVO empresa;

    private EmpresaContratoVO empresaContrato;

    public EmpresaVO getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaVO empresa) {
        this.empresa = empresa;
    }

    public EmpresaContratoVO getEmpresaContrato() {
        return empresaContrato;
    }

    public void setEmpresaContrato(EmpresaContratoVO empresaContrato) {
        this.empresaContrato = empresaContrato;
    }

}
