/* (C)2021 */
package ru.mail.jira.plugins.jsincluder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@Getter
@Setter
@XmlRootElement
public class ProjectCategoryDto {
  @XmlElement private String description;
  @XmlElement private Long id;
  @XmlElement private String name;

  public ProjectCategoryDto() {}

  public ProjectCategoryDto(Long id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }
}
