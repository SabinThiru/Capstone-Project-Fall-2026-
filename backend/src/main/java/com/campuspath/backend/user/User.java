package com.campuspath.backend.user;

import com.campuspath.backend.university.University;

import jakarta.persistence.*;

@Entity
@Table(name = "users", schema = "public")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // DB generates the id
    @Column(name = "user_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Column(name = "cognito_sub", nullable = false, unique = true, length = 128)
    private String cognitoSub;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "euid", nullable = false, length = 50)
    private String euid;

    protected User() { }   // JPA needs an empty constructor

    public User(String cognitoSub, University university, String email, String fullName, String euid) {
        this.cognitoSub = cognitoSub;
        this.university = university;
        this.email = email;
        this.fullName = fullName;
        this.euid = euid;
    }

    public Integer getId() { return id; }
    public University getUniversity() { return university; }
    public String getCognitoSub() { return cognitoSub; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getEuid() { return euid; }
}