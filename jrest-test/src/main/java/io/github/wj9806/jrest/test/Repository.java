package io.github.wj9806.jrest.test;

/**
 * Repository实体类
 */
public class Repository {
    private long id;
    private String name;
    private String full_name;
    private String description;
    private String url;
    private String html_url;
    private boolean isPrivate;
    private boolean fork;
    private int stargazers_count;
    private int watchers_count;
    private int forks_count;
    
    // getter and setter methods
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFull_name() {
        return full_name;
    }
    
    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getHtml_url() {
        return html_url;
    }
    
    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
    
    public boolean isFork() {
        return fork;
    }
    
    public void setFork(boolean fork) {
        this.fork = fork;
    }
    
    public int getStargazers_count() {
        return stargazers_count;
    }
    
    public void setStargazers_count(int stargazers_count) {
        this.stargazers_count = stargazers_count;
    }
    
    public int getWatchers_count() {
        return watchers_count;
    }
    
    public void setWatchers_count(int watchers_count) {
        this.watchers_count = watchers_count;
    }
    
    public int getForks_count() {
        return forks_count;
    }
    
    public void setForks_count(int forks_count) {
        this.forks_count = forks_count;
    }
    
    @Override
    public String toString() {
        return "Repository{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", full_name='" + full_name + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", html_url='" + html_url + '\'' +
                ", isPrivate=" + isPrivate +
                ", fork=" + fork +
                ", stargazers_count=" + stargazers_count +
                ", watchers_count=" + watchers_count +
                ", forks_count=" + forks_count +
                '}';
    }
}