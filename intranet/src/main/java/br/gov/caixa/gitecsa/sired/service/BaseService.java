package br.gov.caixa.gitecsa.sired.service;

import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.BaseAtendimentoDAO;
import br.gov.caixa.gitecsa.sired.dao.BaseDAO;
import br.gov.caixa.gitecsa.sired.dao.EmpresaContratoDAO;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.BaseAtendimentoVO;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless(name = "baseArquivoService")
public class BaseService extends AbstractService<BaseVO> {

    private static final int NUM_CHARS_SEQUENCIA = 4;
    private static final int NUM_CHARS_LOTE_SEQUENCIA = 15;

    private static final long serialVersionUID = -5756580844519809453L;

    @Inject
    private BaseDAO dao;

    @Inject
    private EmpresaContratoDAO contratoDao;
    
    @Inject
    private BaseAtendimentoDAO baseAtendimentoDao;

    @Override
    protected void validaCamposObrigatorios(BaseVO entity) {

    }

    @Override
    protected void validaRegras(BaseVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(BaseVO entity) {

    }

    @Override
    protected GenericDAO<BaseVO> getDAO() {
        return this.dao;
    }

    /**
     * Realiza a validação do lote/sequência conforme documentação suplementar:
     * 
     * O sistema deve validar o campo número Lote/Seqüência, conforme o número de Lote/Seqüência associado à Base de Arquivos de vinculação
     * da unidade geradora do documento. Caso os primeiros 4 algarismos do 1º campo de Lote/Seqüência (formado por 15 algarismos, por
     * exemplo: 00091020075/0243 e 00580820211/0012) não corresponda à Base de Arquivos de vinculação da unidade solicitante, o sistema
     * deverá exibir a mensagem MI023,, informando a base de arquivos de vinculação do usuário e não permitir a continuidade da operação.
     * 
     * @param requisicaoDocumento
     * @return
     * @throws BusinessException
     */
    public BaseVO validarLoteSequencia(RequisicaoDocumentoVO requisicaoDocumento) throws BusinessException {

        if (!ObjectUtils.isNullOrEmpty(requisicaoDocumento.getNuLoteSequencia())) {

            String nuLoteSequencia = StringUtils.leftPad(requisicaoDocumento.getNuLoteSequencia(), NUM_CHARS_LOTE_SEQUENCIA, '0');
            BaseVO base = this.dao.findByLoteSequenciaEUnidade(nuLoteSequencia.substring(0, NUM_CHARS_SEQUENCIA), requisicaoDocumento.getUnidadeGeradora());

            if (!ObjectUtils.isNullOrEmpty(base)) {
                if (!ObjectUtils.isNullOrEmpty(base.getLoteSequencia())) {
                    return base;
                }

                throw new BusinessException(MensagemUtils.obterMensagem("MI023", base.getNome(), StringUtils.join(base.getLoteSequencia(), ", ")));
            }

        }

        return null;
    }

    /**
     * Obtém a empresa responsável pelo atendimento e a base responsável pela área onde o documento está localizado
     * 
     * @param unidade
     * @return O contrato empresa contendo a base, o contrato vigente e a empresa
     * @throws DataBaseException 
     */
    public EmpresaContratoVO findContratoBaseByUnidadeEager(UnidadeVO unidade) throws DataBaseException {
        return this.contratoDao.findByAbrangenciaUnidadeEager(unidade);
    }
    
	/**
	 * Verifica se há Base de Atendimento específica para o Documento. Caso
	 * haja, verifica se foi definida Data de Início. Caso negativo, assume que
	 * todos os atendimentos devem ser direcionados para a Base específica,
	 * independente da Data de Geração. Caso positivo, e a data informada seja
	 * válida, verifica se ela é igual ou posterior a Data de Início.
	 * 
	 * @param documento
	 * @param data
	 * @return
	 * @throws DataBaseException 
	 */
	public EmpresaContratoVO findContratoBaseByDocumentoEager(DocumentoVO documento, Date data) throws DataBaseException {
		BaseAtendimentoVO baseAtendimento = this.baseAtendimentoDao.findByIdEager(documento.getId());

		if (!ObjectUtils.isNullOrEmpty(baseAtendimento)) {
			if (ObjectUtils.isNullOrEmpty(baseAtendimento.getDtInicioAtendimento())) {
				// todos os atendimentos devem ser direcionados para a Base
				// específica
				return this.contratoDao.findByBaseAtendimento(baseAtendimento);
			} else if (!ObjectUtils.isNullOrEmpty(data)) {
				Calendar calParam = Calendar.getInstance();
				calParam.setTime(DateUtils.fitAtStart(data));

				Calendar calAtendimento = Calendar.getInstance();
				calAtendimento.setTime(DateUtils.fitAtStart(baseAtendimento.getDtInicioAtendimento()));

				if (calParam.after(calAtendimento) || calParam.equals(calAtendimento)) {
					return this.contratoDao.findByBaseAtendimento(baseAtendimento);
				}
			}
		}

		return null;
	}

}
