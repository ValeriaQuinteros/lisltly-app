package prueba.Listly.lists.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ListControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void shouldCreateListAndManageItems() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/lists")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "titulo": "Compras",
								  "categoria": "Casa",
								  "fechaObjetivo": "2025-12-25",
								  "descripcion": "Supermercado"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isString())
				.andExpect(jsonPath("$.titulo").value("Compras"))
				.andExpect(jsonPath("$.categoria").value("Casa"))
				.andExpect(jsonPath("$.fechaObjetivo").value("2025-12-25"))
				.andExpect(jsonPath("$.items").isArray())
				.andReturn();

		String listId = extractId(created);

		mockMvc.perform(get("/api/lists"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", hasItem(listId)));

		mockMvc.perform(get("/api/lists/{listId}", listId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(listId))
				.andExpect(jsonPath("$.items").isArray())
				.andExpect(jsonPath("$.items.length()").value(0));

		mockMvc.perform(put("/api/lists/{listId}", listId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "titulo": "Compras semana",
								  "categoria": "Casa",
								  "fechaObjetivo": "2025-12-26",
								  "descripcion": "Actualizada"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Compras semana"))
				.andExpect(jsonPath("$.fechaObjetivo").value("2025-12-26"));

		MvcResult itemCreated = mockMvc.perform(post("/api/lists/{listId}/items", listId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "texto": "Leche" }
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isString())
				.andExpect(jsonPath("$.texto").value("Leche"))
				.andExpect(jsonPath("$.completado").value(false))
				.andReturn();

		String itemId = extractId(itemCreated);

		mockMvc.perform(get("/api/lists/{listId}/items", listId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(itemId))
				.andExpect(jsonPath("$[0].texto").value("Leche"))
				.andExpect(jsonPath("$[0].completado").value(false));

		mockMvc.perform(patch("/api/lists/{listId}/items/{itemId}", listId, itemId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "completado": true }
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(itemId))
				.andExpect(jsonPath("$.completado").value(true));

		mockMvc.perform(delete("/api/lists/{listId}/items/{itemId}", listId, itemId))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/lists/{listId}/items", listId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));

		mockMvc.perform(delete("/api/lists/{listId}", listId))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/lists/{listId}", listId))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldValidateRequests() throws Exception {
		mockMvc.perform(post("/api/lists")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "titulo": "   " }
								"""))
				.andExpect(status().isBadRequest());

		MvcResult created = mockMvc.perform(post("/api/lists")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "titulo": "Test" }
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		String listId = extractId(created);

		mockMvc.perform(post("/api/lists/{listId}/items", listId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "texto": "  " }
								"""))
				.andExpect(status().isBadRequest());

		mockMvc.perform(delete("/api/lists/{listId}", listId))
				.andExpect(status().isNoContent());
	}

	@Test
	void shouldApplyFiltersAndDefaults() throws Exception {
		String casaId = extractId(mockMvc.perform(post("/api/lists")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "titulo": "Compras casa", "categoria": "Casa" }
								"""))
				.andExpect(status().isCreated())
				.andReturn());

		String viajeId = extractId(mockMvc.perform(post("/api/lists")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "titulo": "Viaje", "categoria": "Viajes" }
								"""))
				.andExpect(status().isCreated())
				.andReturn());

		MvcResult generalCreated = mockMvc.perform(post("/api/lists")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "titulo": "Sin categoria" }
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.categoria").value("General"))
				.andReturn();

		String generalId = extractId(generalCreated);

		mockMvc.perform(get("/api/lists").queryParam("categoria", "Casa"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", hasItem(casaId)))
				.andExpect(jsonPath("$[*].id", not(hasItem(viajeId))));

		mockMvc.perform(get("/api/lists").queryParam("q", "via"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", hasItem(viajeId)))
				.andExpect(jsonPath("$[*].id", not(hasItem(casaId))));

		mockMvc.perform(delete("/api/lists/{listId}", casaId))
				.andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/lists/{listId}", viajeId))
				.andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/lists/{listId}", generalId))
				.andExpect(status().isNoContent());
	}

	@Test
	void shouldReturnNotFoundForMissingResources() throws Exception {
		mockMvc.perform(get("/api/lists/{listId}", "missing"))
				.andExpect(status().isNotFound());

		mockMvc.perform(delete("/api/lists/{listId}", "missing"))
				.andExpect(status().isNotFound());

		mockMvc.perform(patch("/api/lists/{listId}/items/{itemId}", "missing", "missing")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "completado": true }
								"""))
				.andExpect(status().isNotFound());
	}

	private String extractId(MvcResult result) throws Exception {
		String content = result.getResponse().getContentAsString();
		String id = JsonPath.read(content, "$.id");
		assertThat(id).isNotNull();
		return id;
	}
}
