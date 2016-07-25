package ru.mail.jira.plugins.jsincluder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@XmlRootElement
public class ProjectDto {
    @XmlElement
    private Long id;
    @XmlElement
    private String key;
    @XmlElement
    private String name;
    @XmlElement
    private String avatarUrl;

    public ProjectDto() {

    }

    public ProjectDto(Long id, String key, String name, String avatarUrl) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
