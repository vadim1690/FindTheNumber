package com.example.findthenumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactRecord {

    private String phone;
    private List<Name> names;

    public ContactRecord() {
    }
    public ContactRecord(String phone) {
        this.phone = phone;
        names = new ArrayList<>();
    }
    public ContactRecord(String phone, List<Name>  names) {
        this.phone = phone;
        this.names = names;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Name>  getNames() {
        return names;
    }

    public void setNames(List<Name>  names) {
        this.names = names;
    }
}
