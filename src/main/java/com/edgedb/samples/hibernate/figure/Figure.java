package com.edgedb.samples.hibernate.figure;

import java.util.UUID;
import com.edgedb.samples.hibernate.position.Position;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "\"Figure\"")
public abstract class Figure {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int score;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    private Position position;

    @Column(nullable = false, name = "stat_moves")
    private int statMoves;

    // methods

    public abstract String getStatusString();
    
    public abstract String getFullName();

    // getters and setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Position getPosition() {
        return this.position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getScore() {
        return score;
    }

    public void increaseScore(int score) {
        this.score += score;
    }

    public int getStatMoves() {
        return statMoves;
    }

    public void setStatMoves(int statHops) {
        this.statMoves = statHops;
    }
}
