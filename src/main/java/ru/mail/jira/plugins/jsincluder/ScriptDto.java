package ru.mail.jira.plugins.jsincluder;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@Getter @Setter
@XmlRootElement
public class ScriptDto {
    @XmlElement
    private int id;
    @XmlElement
    private String name;
    @XmlElement
    private String code;
    @XmlElement
    private String css;
    @XmlElement
    private List<BindingDto> bindings = new ArrayList<>();

    public ScriptDto() {
    }

    public ScriptDto(Script script) {
        this.id = script.getID();
        this.name = script.getName();
        this.code = script.getCode();
        this.css = script.getCss();
    }
}
