package prueba.Listly.lists.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ListResponse(
		String id,
		String titulo,
		String categoria,
		LocalDate fechaObjetivo,
		String descripcion,
		Instant creadaEn,
		Instant actualizadaEn,
		List<ItemResponse> items
) {
}
