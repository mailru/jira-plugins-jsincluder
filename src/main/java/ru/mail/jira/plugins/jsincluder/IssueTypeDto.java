package ru.mail.jira.plugins.jsincluder;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@Getter @Setter
@XmlRootElement
public class IssueTypeDto {
    @XmlElement
    private String id;
    @XmlElement
    private String name;
    @XmlElement
    private String iconUrl;

    public IssueTypeDto() {
    }

    public IssueTypeDto(String id, String name, String iconUrl) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
    }
}
