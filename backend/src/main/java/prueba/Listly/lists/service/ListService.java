package prueba.Listly.lists.service;

import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import prueba.Listly.lists.dto.CreateItemRequest;
import prueba.Listly.lists.dto.CreateListRequest;
import prueba.Listly.lists.dto.ItemResponse;
import prueba.Listly.lists.dto.ListResponse;
import prueba.Listly.lists.dto.ListSummaryResponse;
import prueba.Listly.lists.dto.UpdateItemCompletionRequest;
import prueba.Listly.lists.dto.UpdateItemRequest;
import prueba.Listly.lists.dto.UpdateListRequest;
import prueba.Listly.lists.entity.ListEntity;
import prueba.Listly.lists.entity.ListItemEntity;
import prueba.Listly.lists.repo.ListRepository;

@Service
public class ListService {

	private final ListRepository listRepository;

	public ListService(ListRepository listRepository) {
		this.listRepository = listRepository;
	}

	public List<ListSummaryResponse> getLists(String categoria, String q) {
		List<ListEntity> lists;

		if (q != null && !q.isBlank()) {
			lists = listRepository.findByTituloContainingIgnoreCaseOrderByFechaObjetivoAscCreadaEnDesc(q.trim());
		} else if (categoria != null && !categoria.isBlank()) {
			lists = listRepository.findByCategoriaIgnoreCaseOrderByFechaObjetivoAscCreadaEnDesc(categoria.trim());
		} else {
			lists = listRepository.findAllByOrderByFechaObjetivoAscCreadaEnDesc();
		}

		return lists.stream().map(ListService::toSummaryResponse).toList();
	}

	public ListResponse createList(CreateListRequest request) {
		ListEntity entity = new ListEntity();
		entity.setTitulo(request.titulo().trim());
		entity.setCategoria(normalizeCategoria(request.categoria()));
		entity.setFechaObjetivo(request.fechaObjetivo());
		entity.setDescripcion(normalizeOptional(request.descripcion()));

		ListEntity saved = listRepository.save(entity);
		return toResponse(saved, List.of());
	}

	public ListResponse getList(String listId) {
		ListEntity entity = listRepository.findById(listId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));

		return toResponse(
				entity,
				entity.getItems().stream().map(ListService::toItemResponse).toList()
		);
	}

	public ListResponse updateList(String listId, UpdateListRequest request) {
		ListEntity entity = listRepository.findById(listId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));

		entity.setTitulo(request.titulo().trim());
		entity.setCategoria(normalizeCategoria(request.categoria()));
		entity.setFechaObjetivo(request.fechaObjetivo());
		entity.setDescripcion(normalizeOptional(request.descripcion()));

		ListEntity saved = listRepository.save(entity);
		return toResponse(
				saved,
				saved.getItems().stream().map(ListService::toItemResponse).toList()
		);
	}

	public void deleteList(String listId) {
		if (!listRepository.existsById(listId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada");
		}
		listRepository.deleteById(listId);
	}

	public List<ItemResponse> getItems(String listId) {
		ListEntity list = listRepository.findById(listId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));

		return list.getItems().stream().map(ListService::toItemResponse).toList();
	}

	public ItemResponse addItem(String listId, CreateItemRequest request) {
		ListEntity list = listRepository.findById(listId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));

		ListItemEntity item = new ListItemEntity();
		item.setId(new ObjectId().toHexString());
		item.setTexto(request.texto().trim());
		item.setCompletado(false);
		item.setIntegrante(normalizeOptional(request.integrante()));
		item.setEstado(normalizeEstado(request.estado()));
		item.setPrioridad(normalizePrioridad(request.prioridad()));

		list.getItems().add(item);
		listRepository.save(list);
		return toItemResponse(item);
	}

	public ItemResponse updateItemCompletion(String listId, String itemId, UpdateItemCompletionRequest request) {
		ListEntity list = listRepository.findById(listId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));

		ListItemEntity item = list.getItems().stream()
				.filter(i -> itemId.equals(i.getId()))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ítem no encontrado"));

		item.setCompletado(request.completado());
		listRepository.save(list);
		return toItemResponse(item);
	}

	public ItemResponse updateItem(String listId, String itemId, UpdateItemRequest request) {
		ListEntity list = listRepository.findById(listId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));

		ListItemEntity item = list.getItems().stream()
				.filter(i -> itemId.equals(i.getId()))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ítem no encontrado"));

		if (request.texto() != null) {
			String t = request.texto().trim();
			if (!t.isBlank()) {
				item.setTexto(t);
			}
		}
		if (request.integrante() != null) {
			item.setIntegrante(normalizeOptional(request.integrante()));
		}
		if (request.estado() != null) {
			item.setEstado(normalizeEstado(request.estado()));
		}
		if (request.prioridad() != null) {
			item.setPrioridad(normalizePrioridad(request.prioridad()));
		}
		if (request.completado() != null) {
			item.setCompletado(request.completado());
		}

		listRepository.save(list);
		return toItemResponse(item);
	}

	public void deleteItem(String listId, String itemId) {
		ListEntity list = listRepository.findById(listId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));

		boolean removed = list.getItems().removeIf(i -> itemId.equals(i.getId()));
		if (!removed) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ítem no encontrado");
		}

		listRepository.save(list);
	}

	private static String normalizeCategoria(String categoria) {
		if (categoria == null || categoria.isBlank()) {
			return "General";
		}
		return categoria.trim();
	}

	private static String normalizeOptional(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
	private static String normalizeEstado(String estado) {
		if (estado == null || estado.isBlank()) {
			return "Idea";
		}
		String e = estado.trim();
		return switch (e.toLowerCase()) {
			case "idea" -> "Idea";
			case "por comprar", "porcomprar" -> "Por comprar";
			case "comprado" -> "Comprado";
			default -> "Idea";
		};
	}
	private static int normalizePrioridad(Integer prioridad) {
		if (prioridad == null) return 2;
		int p = Math.max(1, Math.min(3, prioridad));
		return p;
	}

	private static ListSummaryResponse toSummaryResponse(ListEntity entity) {
		return new ListSummaryResponse(
				entity.getId(),
				entity.getTitulo(),
				entity.getCategoria(),
				entity.getFechaObjetivo(),
				entity.getDescripcion()
		);
	}

	private static ListResponse toResponse(ListEntity entity, List<ItemResponse> items) {
		return new ListResponse(
				entity.getId(),
				entity.getTitulo(),
				entity.getCategoria(),
				entity.getFechaObjetivo(),
				entity.getDescripcion(),
				entity.getCreadaEn(),
				entity.getActualizadaEn(),
				items
		);
	}

	private static ItemResponse toItemResponse(ListItemEntity entity) {
		return new ItemResponse(
				entity.getId(),
				entity.getTexto(),
				entity.isCompletado(),
				entity.getIntegrante(),
				entity.getEstado(),
				entity.getPrioridad()
		);
	}
}
