package prueba.Listly.lists.repo;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import prueba.Listly.lists.entity.ListEntity;

public interface ListRepository extends MongoRepository<ListEntity, String> {

	List<ListEntity> findByCategoriaIgnoreCaseOrderByFechaObjetivoAscCreadaEnDesc(String categoria);

	List<ListEntity> findByTituloContainingIgnoreCaseOrderByFechaObjetivoAscCreadaEnDesc(String titulo);

	List<ListEntity> findAllByOrderByFechaObjetivoAscCreadaEnDesc();
}
