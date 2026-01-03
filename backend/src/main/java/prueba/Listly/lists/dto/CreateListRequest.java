package prueba.Listly.lists.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateListRequest(
		@NotBlank @Size(max = 120) String titulo,
		@Size(max = 40) String categoria,
		LocalDate fechaObjetivo,
		@Size(max = 500) String descripcion
) {
}

