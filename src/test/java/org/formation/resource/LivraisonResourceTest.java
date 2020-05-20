package org.formation.resource;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.formation.model.Livraison;
import org.formation.repository.LivraisonRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;


@RunWith(SpringRunner.class)
@WebMvcTest(value=LivraisonResource.class)
public class LivraisonResourceTest {

	@Autowired
	ApplicationContext context;
	
	@Autowired
	private MockMvc mvc;

	@MockBean
	private LivraisonRepository livraisonRepository;

	Livraison aLivraison;
	
	public static String API_PREFIX="/api/livraison";

	@Before
	public void setUp() {
		aLivraison = new Livraison();
		aLivraison.setId(1);
		aLivraison.setNoCommande("1234");


	}

	@Test
	public void testGetLivraison() throws Exception {
		given(this.livraisonRepository.findById(1l)).willReturn(Optional.of(aLivraison));
		ResultActions result = mvc.perform(get(API_PREFIX + "/1"));
		MvcResult mvcResult = result.andReturn();
		System.out.println(mvcResult.getResponse().getContentAsString());
		mvc.perform(get(API_PREFIX + "/1")).andExpect(status().isOk()).andExpect(jsonPath("$.noCommande").value("1234"));
	}
	
	@Test
	public void testFindAll() throws Exception {
		assert true;
		
	}
	
	@Test
	public void testChangeStatus() throws Exception {
		assert true;
		
	}
	@Test
	public void testCreation() throws Exception {
		assert true;
		
	}
}
