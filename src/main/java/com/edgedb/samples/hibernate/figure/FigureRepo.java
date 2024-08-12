package com.edgedb.samples.hibernate.figure;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;
import com.edgedb.samples.hibernate.position.Position;

public interface FigureRepo extends Repository<Figure, Long> {

    Figure findById(long personId);

    @EntityGraph(attributePaths = {"position"})
    List<Figure> findAll();

    @EntityGraph(attributePaths = {"position"})
    List<Figure> findTop10ByPositionIsNotNullOrderByScoreDescName();

    @EntityGraph(attributePaths = {"position"})
    List<Figure> findByPositionIn(Collection<Position> positions);

    void save(Figure figure);
}

