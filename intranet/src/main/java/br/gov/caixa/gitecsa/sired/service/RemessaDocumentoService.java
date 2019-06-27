package br.gov.caixa.gitecsa.sired.service;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.PaginatorService;
import br.gov.caixa.gitecsa.sired.dao.RemessaDocumentoDAO;
import br.gov.caixa.gitecsa.sired.enumerator.TipoUnidadeEnum;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless(name = "remessaDocumentoServiceRefactory")
public class RemessaDocumentoService extends PaginatorService<RemessaDocumentoVO>{

  /** Serial */
  private static final long serialVersionUID = 135946985149783993L;
  

  @Inject
  private RemessaDocumentoDAO remessaDocumentoDAO;
  
  @Inject
  private UnidadeService unidadeService;
  
  @Override
  public Integer count(Map<String, Object> filters) throws DataBaseException {
    return null;
  }

  @Override
  public List<RemessaDocumentoVO> pesquisar(Integer offset, Integer limit, Map<String, Object> filters) throws DataBaseException {
    return null;
  }

  @Override
  protected void validaCamposObrigatorios(RemessaDocumentoVO entity) throws BusinessException {
    
  }

  @Override
  protected void validaRegras(RemessaDocumentoVO entity) throws BusinessException {
    
  }

  @Override
  protected void validaRegrasExcluir(RemessaDocumentoVO entity) throws BusinessException {
    
  }

  @Override
  protected GenericDAO<RemessaDocumentoVO> getDAO() {
    return null;
  }

  public RemessaDocumentoVO salvar(RemessaDocumentoVO remessaDocumento) {
    if (remessaDocumento.getId() != null) {      
      remessaDocumentoDAO.update(remessaDocumento);
    } else {
      remessaDocumentoDAO.save(remessaDocumento);
    }
    return remessaDocumento;
  }

  public void excluirremessaDocumento(RemessaDocumentoVO doc) {
    remessaDocumentoDAO.delete(doc);
  }

  public void validarRemessaDocumento(RemessaDocumentoVO remessaDocumentoVO) throws BusinessException, DataBaseException {
    this.validarDigitoVerificadorConta(remessaDocumentoVO);
    this.validarCpf(remessaDocumentoVO);
    this.validarCnpj(remessaDocumentoVO);
  }
  
  /**
   * Esta validação está relacionada ao campo NU_DIGITO_VERIFICADOR da tabela REDTBC15_REQUISICAO_DOCUMENTO, e se baseia nos valores
   * informados para os campos NU_UNIDADE_GERADORA_A02 (Agência), NU_OPERACAO_A11 (Operação) e NU_CONTA (Conta). Caso o dígito verificador
   * da conta seja diferente do calculado pelo sistema, o sistema deve exibir a mensagem MA034 e não permitir a continuidade da operação.
   * 
   * @param requisicao
   * @throws BusinessException
   */
  public void validarDigitoVerificadorConta(RemessaDocumentoVO remessaDocumentoVO) throws BusinessException {

      if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuDigitoVerificador())
              && !ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuConta())
              && !ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuOperacao())) {
          String numContaProcessado = remessaDocumentoVO.getNuConta().replace("_", "");
          String numeroConta = String.format("%04d%s%08d", remessaDocumentoVO.getUnidadeGeradora().getId(), remessaDocumentoVO
                  .getNuOperacao(), Long.valueOf(numContaProcessado));
          if (!SiredUtils.isDigitoVerificadorContaCaixa(numeroConta, remessaDocumentoVO.getNuDigitoVerificador())) {
              throw new BusinessException(MensagemUtils.obterMensagem("MA034"));
          }
      }
  }

  /**
   * Caso a dezena contendo os dígitos verificadores do CPF seja diferente do calculado pelo sistema, o sistema deve exibir a mensagem
   * MA035 e não permitir a continuidade da operação.
   * 
   * @param requisicao
   * @throws BusinessException
   */
  public void validarCpf(RemessaDocumentoVO remessaDocumentoVO) throws BusinessException {
      if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroCpf())
              && !SiredUtils.isCpfValido(remessaDocumentoVO.getNumeroCpf())) {
          throw new BusinessException(MensagemUtils.obterMensagem("MA035"));
      }
  }

  /**
   * Caso a dezena contendo os dígitos verificadores do CNPJ seja diferente do calculado pelo sistema, o sistema deve exibir a mensagem
   * MA036 e não permitir a continuidade da operação.
   * 
   * @param requisicao
   * @throws BusinessException
   */
  public void validarCnpj(RemessaDocumentoVO remessaDocumentoVO) throws BusinessException {
      if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroCnpj())
              && !SiredUtils.isCnpjValido(remessaDocumentoVO.getNumeroCnpj())) {
          throw new BusinessException(MensagemUtils.obterMensagem("MA036"));
      }
  }
  
  public void validarUnidadeGeradora(RemessaDocumentoVO remessaDocumentoVO) throws BusinessException, DataBaseException {

    UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
    if (usuario != null) {

        UnidadeVO unidadeLotacao = this.unidadeService.findUnidadeLotacaoUsuarioLogado();
        if (unidadeLotacao.getTipoUnidade().getIndicadorUnidade().equals(TipoUnidadeEnum.PV)) {
            this.unidadeService.findUnidadePV(unidadeLotacao, usuario);
        } else {
            this.unidadeService.findUnidadeAutorizada(remessaDocumentoVO.getUnidadeGeradora(), usuario);
        }
    }
  }

}
