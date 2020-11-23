/* (C)2020 */
package ru.mail.jira.plugins.jsincluder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@Getter
@Setter
@XmlRootElement
public class ScriptDto {
  @XmlElement private int id;
  @XmlElement private String name;
  @XmlElement private String code;
  @XmlElement private String css;
  @XmlElement private List<BindingDto> bindings = new ArrayList<>();

  public ScriptDto() {}

  public ScriptDto(Script script) {
    this.id = script.getID();
    this.name = script.getName();
    this.code = script.getCode();
    this.css = script.getCss();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    ScriptDto script = (ScriptDto) o;
    return this.id == script.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id);
  }
}
