package com.edgedb.samples.hibernate.fox;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;
import com.edgedb.samples.hibernate.position.Position;

public interface FoxRepo extends Repository<Fox, Long> {
    Fox findById(long id);

    int countByPositionIsNotNull();

    @EntityGraph(attributePaths = {"position"})
    List<Fox> findByPositionIn(Collection<Position> positions);

	void save(Fox fox);
}

