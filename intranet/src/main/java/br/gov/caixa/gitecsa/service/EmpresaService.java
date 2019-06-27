package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

@Stateless
public class EmpresaService extends AbstractService<EmpresaVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Inject
    private RequisicaoService requisicaoService;

    @Override
    protected void validaCampos(EmpresaVO entity) {
    }

    @Override
    protected void validaRegras(EmpresaVO entity) {
        regraDuplicidade(entity);

    }

    @Override
    protected void validaRegrasExcluir(EmpresaVO entity) throws AppException {
        RequisicaoVO requisicao = new RequisicaoVO();
        requisicao.setEmpresaContrato(entity.getEmpresaContratoExcluir());
        if (entity.getEmpresaContratoExcluir() != null) {
            requisicaoService.findByParameters(requisicao);
            if (requisicao != null) {
                mensagens.add(MensagemUtils.obterMensagem("MA006"));
            }
        }
    }

    private void regraDuplicidade(EmpresaVO entity) {
        List<EmpresaVO> lista = findAll();
        for (EmpresaVO obj : lista) {
            if (entity.getId() == null) {
                if (obj.getNome().equalsIgnoreCase(entity.getNome().trim()) || obj.getCnpj().equals(entity.getCnpj())) {
                    mensagens.add(MensagemUtils.obterMensagem("MI018"));
                    break;
                }
            } else {
                if ((!obj.getId().equals(entity.getId()))
                        && (obj.getNome().trim().equalsIgnoreCase(entity.getNome().trim()) || obj.getCnpj().equals(entity.getCnpj()))) {
                    mensagens.add(MensagemUtils.obterMensagem("MI018"));
                    break;
                }

            }
        }
    }

    public List<EmpresaVO> findAllEager() {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT e FROM EmpresaVO e ");
        hql.append(" Left Join Fetch e.empresaContratos ec ");
        hql.append(" Left Join Fetch ec.base ");

        TypedQuery<EmpresaVO> query = getEntityManager().createQuery(hql.toString(), EmpresaVO.class);

        return query.getResultList();
    }

}
