package edu.usf.business.softwaretesting.mborkland.runner.test;

import com.example.Vehicle;
import edu.usf.business.softwaretesting.mborkland.model.NotAPerson;
import edu.usf.business.softwaretesting.mborkland.model.Person;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class TestContext {

    private final List<String> messages = new ArrayList<>();

    private final List<Person> people = new ArrayList<>();

    private final List<NotAPerson> notPeople = new ArrayList<>();

    private final List<Object> deadLetterQueue = new ArrayList<>();

    private final List<Vehicle> vehicles = new ArrayList<>();

    private final String[] partitions = new String[4];

    @Setter
    private boolean exceptionThrown = false;

    public String addMessage(String message) {
        messages.add(message);
        return message;
    }

    public void addMessages(List<String> additionalMessages) {
        messages.addAll(additionalMessages);
    }

    public void addPerson(Person person) {
        people.add(person);
    }

    public void addNotAPerson(NotAPerson notAPerson) {
        notPeople.add(notAPerson);
    }

    public void addToQueue(Object deadLetter) {
        deadLetterQueue.add(deadLetter);
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public void addPartition(String message, int index) {
        Assert.isTrue(index > -1 && index < 4, "Index must be between 0 and 3 inclusive");
        partitions[index] = message;
    }

    public void clear() {
        messages.clear();
        people.clear();
        notPeople.clear();
        deadLetterQueue.clear();
        vehicles.clear();
        exceptionThrown = false;
    }
}
