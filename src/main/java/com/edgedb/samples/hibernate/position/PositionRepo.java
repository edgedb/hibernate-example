package com.edgedb.samples.hibernate.position;

import java.util.List;
import org.springframework.data.repository.Repository;

public interface PositionRepo extends Repository<Position, Long> {
    Position findByXAndY(int x, int y);

	void save(Position position);

    void delete(Position position);

    List<Position> findAllByXGreaterThanAndXLessThanAndYGreaterThanAndYLessThan(int xLower, int xUpper, int yLower, int yUpper);
}
