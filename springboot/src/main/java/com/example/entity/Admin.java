package com.example.entity;
//import javax.persistence.Id;
//import javax.persistence.Entity;
//import javax.persistence.Table;
import java.io.Serializable;

/**
 * Administrator
*/
//@Entity
//@Table(name = "admin")
public class Admin extends Account implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
//    @Id
    private Integer id;
    /** Username */
    private String username;
    /** Password */
    private String password;
    /** Name */
    private String name;
    /** Phone */
    private String phone;
    /** Email */
    private String email;
    /** Avatar */
    private String avatar;
    /** Role identifier */
    private String role;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public void setRole(String role) {
        this.role = role;
    }
}