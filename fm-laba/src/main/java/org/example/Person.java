package org.example;

public class Person {
    private String personId;
    // TODO реализовать работу через UUID person.setPersonId(UUID.randomUUID().toString());
    public void setPersonId(String personId) {
        this.personId = personId;
    }

    private String name;
    public void setName(String name) {
        this.name = name;
    }

    public Person(String personId, String name) {
        this.personId = personId;
        this.name = name;
    }

    //standard getters and setters

    @Override
    public String toString() {
        return String.format("Person{personId='%s', name='%s'}",
                personId, name);
    }


}
