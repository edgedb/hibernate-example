package com.edgedb.samples.hibernate.fox;

import com.edgedb.samples.hibernate.figure.Figure;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"Fox\"")
@PrimaryKeyJoinColumn(name = "figure_id")
public class Fox extends Figure {

    @Override
    public String getStatusString() {
        return "hunting";
    }

	@Override
	public String getFullName() {
		return "Fox " + this.getName();
	}    
}
