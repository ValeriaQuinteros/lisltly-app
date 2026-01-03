package prueba.Listly.lists.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "lists")
public class ListEntity {

	@Id
	private String id;

	private String titulo;

	private String categoria;

	private LocalDate fechaObjetivo;

	private String descripcion;

	@CreatedDate
	private Instant creadaEn;

	@LastModifiedDate
	private Instant actualizadaEn;

	private List<ListItemEntity> items = new ArrayList<>();
}
