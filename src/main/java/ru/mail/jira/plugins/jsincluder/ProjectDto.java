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
public class ProjectDto {
  @XmlElement private Long id;
  @XmlElement private String key;
  @XmlElement private String name;
  @XmlElement private String avatarUrl;

  public ProjectDto() {}

  public ProjectDto(Long id, String key, String name, String avatarUrl) {
    this.id = id;
    this.key = key;
    this.name = name;
    this.avatarUrl = avatarUrl;
  }
}
