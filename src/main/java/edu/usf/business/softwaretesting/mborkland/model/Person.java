package edu.usf.business.softwaretesting.mborkland.model;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    private String firstName;
    private String lastName;
    private int age;

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
