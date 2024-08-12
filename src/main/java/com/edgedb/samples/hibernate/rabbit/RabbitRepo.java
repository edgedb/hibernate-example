package com.edgedb.samples.hibernate.rabbit;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;
import com.edgedb.samples.hibernate.position.Position;

public interface RabbitRepo extends Repository<Rabbit, Long> {
    Rabbit findById(long id);

    int countByPositionIsNotNull();

    @EntityGraph(attributePaths = {"position"})
    List<Rabbit> findByPositionIsNotNull();

    @EntityGraph(attributePaths = {"position"})
    List<Rabbit> findByPositionIn(Collection<Position> positions);

    void save(Rabbit rabbit);
}

