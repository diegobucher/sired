package br.gov.caixa.gitecsa.sired.service;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.caixa.gitecsa.sired.dao.RemessaMovimentoDiarioDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;

@Ignore
public class RemessaMovimentoDiarioServiceTest {
  
  @InjectMocks
  private RemessaMovimentoDiarioService remessaMovimentoDiarioService;
  
  RemessaMovimentoDiarioVO remessaMov;
  
  @Mock
  private RemessaMovimentoDiarioDAO remessaMovimentoDiarioDAO;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    remessaMov = new RemessaMovimentoDiarioVO();
    remessaMov.setId(1L);
  }

  @Test
  public void testFindByUnidadeDtMovimento() {
    Mockito.when(remessaMovimentoDiarioDAO.findByUnidadeDtMovimento(remessaMov)).thenReturn(remessaMov);
    RemessaMovimentoDiarioVO remessaMovimento = remessaMovimentoDiarioService.findByUnidadeDtMovimento(remessaMov);
    assertTrue(remessaMovimento.getId().equals(remessaMov.getId()));
  }

}
