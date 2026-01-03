package prueba.Listly.lists.dto;

import java.time.LocalDate;

public record ListSummaryResponse(
		String id,
		String titulo,
		String categoria,
		LocalDate fechaObjetivo,
		String descripcion
) {
}
