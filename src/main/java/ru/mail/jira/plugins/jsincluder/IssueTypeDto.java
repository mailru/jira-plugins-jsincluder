/* (C)2020 */
package ru.mail.jira.plugins.jsincluder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@Getter
@Setter
@XmlRootElement
public class IssueTypeDto {
  @XmlElement private String id;
  @XmlElement private String name;
  @XmlElement private String iconUrl;

  public IssueTypeDto() {}

  public IssueTypeDto(String id, String name, String iconUrl) {
    this.id = id;
    this.name = name;
    this.iconUrl = iconUrl;
  }
}
