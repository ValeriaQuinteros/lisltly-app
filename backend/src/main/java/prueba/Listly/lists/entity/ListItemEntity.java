package prueba.Listly.lists.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ListItemEntity {

	private String id;

	private String texto;

	private boolean completado;

	private String integrante;

	private String estado;

	private int prioridad;
}
