package edu.usf.business.softwaretesting.mborkland.model;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotAPerson {

    private int squareFootage;
    private int bedrooms;
    private int bathrooms;
    private int garageSize;
    private Person owner;

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
