package prueba.Listly.lists.dto;

import jakarta.validation.constraints.Size;

public record UpdateItemRequest(
		@Size(max = 200) String texto,
		@Size(max = 60) String integrante,
		@Size(max = 20) String estado,
		Integer prioridad,
		Boolean completado
) {
}
