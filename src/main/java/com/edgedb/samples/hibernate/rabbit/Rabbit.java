package com.edgedb.samples.hibernate.rabbit;

import com.edgedb.samples.hibernate.figure.Figure;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"Rabbit\"")
@PrimaryKeyJoinColumn(name = "figure_id")
public class Rabbit extends Figure {

    public static final int CARROT_NUTRITION = 15;

    @Column(nullable = false)
    private int nutrition;

    public int getNutrition() {
        return nutrition;
    }

    public void setNutrition(int nutrition) {
        this.nutrition = nutrition;
    }

    @Override
	public String getFullName() {
		return "Rabbit " + this.getName();
	}

    public String getStatusString() {
        var pos = this.getPosition();
        if (pos == null) {
            return "deceased";
        }
        if (this.nutrition < 10) {
            return "hungry";
        }
        return "hopping around";
    }
}
