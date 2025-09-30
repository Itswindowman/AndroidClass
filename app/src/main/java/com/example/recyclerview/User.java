package com.example.recyclerview;

public class User {
    private String id,name,password;
    private Object pic;

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPic(Object pic) {
        this.pic = pic;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Object getPic() {
        return pic;
    }

    public User(String id, String name, String password, Object pic) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.pic = pic;
    }
}
