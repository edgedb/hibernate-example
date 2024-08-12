package com.edgedb.samples.hibernate.carrot;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;
import com.edgedb.samples.hibernate.position.Position;

public interface CarrotRepo extends Repository<Carrot, Long> {
    int count();

    @EntityGraph(attributePaths = {"position"})    
    List<Carrot> findAll();

	void save(Carrot carrot);

    void delete(Carrot carrot);

    List<Carrot> findByPositionIn(Collection<Position> positions);
}

