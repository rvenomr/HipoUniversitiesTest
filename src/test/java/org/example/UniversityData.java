package org.example;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;

public class UniversityData {
    public String name;
    public List<String> domains;
    public List<String> web_pages;
    public String country;
    public String alpha_two_code;
    public String stateProvince;

    public boolean equals(Object obj){
        UniversityData obj1 = (UniversityData) obj;
        boolean isEqual = Objects.equals(this.name, obj1.name) && Objects.equals(this.domains, obj1.domains) && Objects.equals(this.country, obj1.country) && this.domains.containsAll(obj1.domains) && obj1.domains.containsAll(this.domains);
        return isEqual;
    }
}
