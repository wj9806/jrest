package io.github.wj9806.jrest.spring.test.entity;

/**
 * User实体类
 */
public class User {
    private String login;
    private long id;
    private String avatar_url;
    private String name;
    private String company;
    private String location;
    private String email;
    private int public_repos;
    private int followers;
    private int following;
    
    // getter and setter methods
    public String getLogin() {
        return login;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getAvatar_url() {
        return avatar_url;
    }
    
    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public int getPublic_repos() {
        return public_repos;
    }
    
    public void setPublic_repos(int public_repos) {
        this.public_repos = public_repos;
    }
    
    public int getFollowers() {
        return followers;
    }
    
    public void setFollowers(int followers) {
        this.followers = followers;
    }
    
    public int getFollowing() {
        return following;
    }
    
    public void setFollowing(int following) {
        this.following = following;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", id=" + id +
                ", avatar_url='" + avatar_url + '\'' +
                ", name='" + name + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", email='" + email + '\'' +
                ", public_repos=" + public_repos +
                ", followers=" + followers +
                ", following=" + following +
                '}';
    }
}