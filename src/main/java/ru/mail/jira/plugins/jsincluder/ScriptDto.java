package ru.mail.jira.plugins.jsincluder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@XmlRootElement
public class ScriptDto {
    @XmlElement
    private int id;
    @XmlElement
    private String name;
    @XmlElement
    private String code;
    @XmlElement
    private List<BindingDto> bindings = new ArrayList<BindingDto>();

    public ScriptDto() {
    }

    public ScriptDto(Script script) {
        this.id = script.getID();
        this.name = script.getName();
        this.code = script.getCode();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<BindingDto> getBindings() {
        return this.bindings;
    }

    public void setBinding(List<BindingDto> bindings) {
        this.bindings = bindings;
    }
}
