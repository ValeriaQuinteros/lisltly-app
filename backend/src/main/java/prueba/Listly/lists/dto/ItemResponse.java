package prueba.Listly.lists.dto;

public record ItemResponse(
		String id,
		String texto,
		boolean completado,
		String integrante,
		String estado,
		int prioridad
) {
}
