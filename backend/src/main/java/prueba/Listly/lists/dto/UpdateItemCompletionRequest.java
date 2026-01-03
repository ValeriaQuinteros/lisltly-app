package prueba.Listly.lists.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateItemCompletionRequest(
		@NotNull Boolean completado
) {
}

