package prueba.Listly.lists.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import prueba.Listly.lists.dto.CreateItemRequest;
import prueba.Listly.lists.dto.CreateListRequest;
import prueba.Listly.lists.dto.ItemResponse;
import prueba.Listly.lists.dto.ListResponse;
import prueba.Listly.lists.dto.ListSummaryResponse;
import prueba.Listly.lists.dto.UpdateItemCompletionRequest;
import prueba.Listly.lists.dto.UpdateListRequest;
import prueba.Listly.lists.service.ListService;

@RestController
@RequestMapping("/api/lists")
public class ListController {

	private final ListService listService;

	public ListController(ListService listService) {
		this.listService = listService;
	}

	@GetMapping
	public List<ListSummaryResponse> getLists(
			@RequestParam(required = false) String categoria,
			@RequestParam(required = false) String q
	) {
		return listService.getLists(categoria, q);
	}

	@PostMapping
	public ResponseEntity<ListResponse> createList(@Valid @RequestBody CreateListRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(listService.createList(request));
	}

	@GetMapping("/{listId}")
	public ListResponse getList(@PathVariable String listId) {
		return listService.getList(listId);
	}

	@PutMapping("/{listId}")
	public ListResponse updateList(@PathVariable String listId, @Valid @RequestBody UpdateListRequest request) {
		return listService.updateList(listId, request);
	}

	@DeleteMapping("/{listId}")
	public ResponseEntity<Void> deleteList(@PathVariable String listId) {
		listService.deleteList(listId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{listId}/items")
	public List<ItemResponse> getItems(@PathVariable String listId) {
		return listService.getItems(listId);
	}

	@PostMapping("/{listId}/items")
	public ResponseEntity<ItemResponse> addItem(@PathVariable String listId, @Valid @RequestBody CreateItemRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(listService.addItem(listId, request));
	}

	@PatchMapping("/{listId}/items/{itemId}")
	public ItemResponse updateItemCompletion(
			@PathVariable String listId,
			@PathVariable String itemId,
			@Valid @RequestBody UpdateItemCompletionRequest request
	) {
		return listService.updateItemCompletion(listId, itemId, request);
	}

	@DeleteMapping("/{listId}/items/{itemId}")
	public ResponseEntity<Void> deleteItem(@PathVariable String listId, @PathVariable String itemId) {
		listService.deleteItem(listId, itemId);
		return ResponseEntity.noContent().build();
	}
}
