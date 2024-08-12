package com.edgedb.samples.hibernate.fox;

import com.edgedb.samples.hibernate.figure.Figure;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"Fox\"")
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
