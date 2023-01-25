package com.example.findthenumber;

import java.util.Objects;

public class Name {
    private String callerName;
    private String contactName;


    public Name(){

    }

    public Name(String callerName, String contactName) {
        this.callerName = callerName;
        this.contactName = contactName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Name name = (Name) o;
        return Objects.equals(callerName, name.callerName) && Objects.equals(contactName, name.contactName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callerName, contactName);
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}
