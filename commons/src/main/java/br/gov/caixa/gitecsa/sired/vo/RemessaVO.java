package br.gov.caixa.gitecsa.sired.vo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.comparator.RemessaDocumentoCodigoRemessaComparator;
import br.gov.caixa.gitecsa.sired.dto.MovimentoDiarioRemessaCDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc17_remessa", schema = Constantes.SCHEMADB_NAME)
public class RemessaVO extends BaseEntity {

    private static final long serialVersionUID = 6846806660219543014L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_remessa", nullable = false, columnDefinition = "serial")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nu_documento_tipo_c_c01", columnDefinition = "int2")
    private DocumentoVO documento;

    @ManyToOne
    @JoinColumn(name = "nu_unidade_solicitante_a02", columnDefinition = "int2")
    private UnidadeVO unidadeSolicitante;

    @ManyToOne
    @JoinColumn(name = "nu_empresa_contrato_c13", columnDefinition = "int2")
    private EmpresaContratoVO empresaContrato;

    @ManyToOne
    @JoinColumn(name = "nu_base_c08", columnDefinition = "int2")
    private BaseVO base;

    @Column(name = "co_usuario_abertura", columnDefinition = "bpchar")
    private String codigoUsuarioAbertura;

    @Column(name = "nu_lacre_tipo_c", columnDefinition = "int8")
    private Long lacre;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_abertura")
    private Date dataHoraAbertura;

    @OneToMany(mappedBy = "remessa", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
    private List<RemessaDocumentoVO> remessaDocumentos;
    
    @OneToMany(mappedBy = "remessa", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
    private List<RemessaMovimentoDiarioVO> movimentosDiarioList;

    @OneToOne
    @JoinColumn(name = "nu_trmte_rmsa_atual_s03", columnDefinition = "int4")
    private TramiteRemessaVO tramiteRemessaAtual;

    @OneToMany(mappedBy = "remessa")
    private List<TramiteRemessaVO> tramiteRemessas;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_agendamento", nullable = true)
    private Date dataAgendamento;
    
    @Column(name = "co_remessa_tipo_c", columnDefinition= "numeric")
    private Long codigoRemessaTipoC;
    
    @Transient
    private List<MovimentoDiarioRemessaCDTO> dataMovimentosList;
    

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (Long) id;
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public DocumentoVO getDocumento() {
        return documento;
    }

    public void setDocumento(DocumentoVO documento) {
        this.documento = documento;
    }

    public UnidadeVO getUnidadeSolicitante() {
        return unidadeSolicitante;
    }

    public void setUnidadeSolicitante(UnidadeVO unidadeSolicitante) {
        this.unidadeSolicitante = unidadeSolicitante;
    }

    public EmpresaContratoVO getEmpresaContrato() {
        return empresaContrato;
    }

    public void setEmpresaContrato(EmpresaContratoVO empresaContrato) {
        this.empresaContrato = empresaContrato;
    }

    public String getCodigoUsuarioAbertura() {
        return codigoUsuarioAbertura;
    }

    public void setCodigoUsuarioAbertura(String codigoUsuarioAbertura) {
        this.codigoUsuarioAbertura = codigoUsuarioAbertura;
    }

    public Date getDataHoraAbertura() {
        return dataHoraAbertura;
    }

    public void setDataHoraAbertura(Date dataHoraAbertura) {
        this.dataHoraAbertura = dataHoraAbertura;
    }

    public List<RemessaDocumentoVO> getRemessaDocumentos() {
        return remessaDocumentos;
    }

    public void setRemessaDocumentos(List<RemessaDocumentoVO> remessaDocumentos) {
        this.remessaDocumentos = remessaDocumentos;
    }

    public TramiteRemessaVO getTramiteRemessaAtual() {
        return tramiteRemessaAtual;
    }

    public void setTramiteRemessaAtual(TramiteRemessaVO tramiteRemessaAtual) {
        this.tramiteRemessaAtual = tramiteRemessaAtual;
    }
    
    public List<RemessaMovimentoDiarioVO> getMovimentosDiarioList() {
      return movimentosDiarioList;
    }

    public void setMovimentosDiarioList(List<RemessaMovimentoDiarioVO> movimentosDiarioList) {
      this.movimentosDiarioList = movimentosDiarioList;
    }

    public Long getLacre() {
      return lacre;
    }

    public void setLacre(Long lacre) {
      this.lacre = lacre;
    }
    
    public Long getCodigoRemessaTipoC() {
      return codigoRemessaTipoC;
    }

    public void setCodigoRemessaTipoC(Long codigoRemessaTipoC) {
      this.codigoRemessaTipoC = codigoRemessaTipoC;
    }
    
    public List<MovimentoDiarioRemessaCDTO> getDataMovimentosList() {
      return dataMovimentosList;
    }

    public void setDataMovimentosList(List<MovimentoDiarioRemessaCDTO> dataMovimentosList) {
      this.dataMovimentosList = dataMovimentosList;
    }

    public BaseVO getBase() {
      return base;
    }

    public void setBase(BaseVO base) {
      this.base = base;
    }
    
    @Transient
    public Boolean getTipoRemessaMoviMentoDiario() {
      if(this.getCodigoRemessaTipoC() == null) {        
        return Boolean.FALSE;
      }
      return Boolean.TRUE;
    }
    
    @Transient
    public String getNovosMovimentos() {
      List<String> novos = new ArrayList<String>();
      return StringUtils.join(novos, "; ");
    }
    
    @Transient
    public String getMovimentosUltimos90Dias() {
        List<String> complementares = new ArrayList<String>();
        return StringUtils.join(complementares, "; ");
    }
    
    @Transient
    public String getMovimentosApos90Dias() {
        List<String> complementares = new ArrayList<String>();
        return StringUtils.join(complementares, "; ");
    }

    public String getDataHoraAberturaFormatada() {

        if (dataHoraAbertura != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(dataHoraAbertura);
        }
        return "";

    }

    public List<TramiteRemessaVO> getTramiteRemessas() {
        return tramiteRemessas;
    }

    public void setTramiteRemessas(List<TramiteRemessaVO> tramiteRemessas) {
        this.tramiteRemessas = tramiteRemessas;
    }

    public Date getDataAgendamento() {
      return dataAgendamento;
    }

    public void setDataAgendamento(Date dataAgendamento) {
      this.dataAgendamento = dataAgendamento;
    }
    
    public String getDataAgendamentoFormatada() {
      if (dataAgendamento != null) {
          SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
          return sdf.format(dataAgendamento);
      }
      return null;
  }
    
    public List<RemessaDocumentoVO> remessaDocumentosTratados() {
      List<RemessaDocumentoVO> listaAux = new ArrayList<RemessaDocumentoVO>();
      for (RemessaDocumentoVO remessaDocumentoVO : remessaDocumentos) {
        if(!remessaDocumentoVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO) &&
            !remessaDocumentoVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_BASE_ARQUIVO)) {
         listaAux.add(remessaDocumentoVO); 
        }
      }
      Collections.sort(listaAux, new RemessaDocumentoCodigoRemessaComparator());
      return listaAux;
    }
    
    public List<RemessaMovimentoDiarioVO> remessaMovimentoTratado() {
      List<RemessaMovimentoDiarioVO> listaAux = new ArrayList<RemessaMovimentoDiarioVO>();
      
      for(RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : movimentosDiarioList) {
        if(remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO)) {
          listaAux.add(remessaMovimentoDiarioVO);
        }
      }
      return listaAux;
    }
    
    public List<MovimentoDiarioRemessaCDTO> remessaDTOTratado(){
      List<MovimentoDiarioRemessaCDTO> listaAux = new ArrayList<MovimentoDiarioRemessaCDTO>();
     
      for(MovimentoDiarioRemessaCDTO remessaMovimento : dataMovimentosList) {
        if(remessaMovimento.tamanhoRemessaTratada() > 0) {
          listaAux.add(remessaMovimento);
        }
      }
      return listaAux; 
    }
    
}
