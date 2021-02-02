/* (C)2021 */
package ru.mail.jira.plugins.jsincluder.audit;

import static com.atlassian.audit.entity.CoverageArea.LOCAL_CONFIG_AND_ADMINISTRATION;
import static com.atlassian.audit.entity.CoverageLevel.BASE;
import static com.atlassian.jira.auditing.AuditingCategory.AUDITING;

import com.atlassian.audit.api.AuditService;
import com.atlassian.audit.entity.AuditAttribute;
import com.atlassian.audit.entity.AuditEvent;
import com.atlassian.audit.entity.AuditResource;
import com.atlassian.audit.entity.AuditType;
import com.atlassian.audit.entity.ChangedValue;
import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JsincluderAuditServiceImpl implements JsincluderAuditService {
  private final AuditService auditService;

  private static final String PLUGIN_NAME = "Mail.ru Plugin: JsIncluder";
  private static final String PLUGIN_KEY = "ru.mail.jira.plugins.jsincluder";

  private static final AuditType SCRIPT_CREATED =
      AuditType.fromI18nKeys(
              LOCAL_CONFIG_AND_ADMINISTRATION,
              BASE,
              AUDITING.getNameI18nKey(),
              "ru.mail.jira.plugins.jsincluder.audit.script.created")
          .build();

  private static final AuditType SCRIPT_UPDATED =
      AuditType.fromI18nKeys(
              LOCAL_CONFIG_AND_ADMINISTRATION,
              BASE,
              AUDITING.getNameI18nKey(),
              "ru.mail.jira.plugins.jsincluder.audit.script.updated")
          .build();

  private static final AuditType SCRIPT_DELETED =
      AuditType.fromI18nKeys(
              LOCAL_CONFIG_AND_ADMINISTRATION,
              BASE,
              AUDITING.getNameI18nKey(),
              "ru.mail.jira.plugins.jsincluder.audit.script.deleted")
          .build();

  @Autowired
  public JsincluderAuditServiceImpl(@ComponentImport AuditService auditService) {
    this.auditService = auditService;
  }

  @Override
  public void adminCreateScript(JsincluderAuditChangedValue newValue) {
    List<ChangedValue> changedValues = new ArrayList<>();
    changedValues.add(
        ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.configuration.script.name")
            .from(null)
            .to(newValue.getName())
            .build());
    changedValues.add(
        ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.configuration.script.binding")
            .from(null)
            .to(newValue.getBindings())
            .build());
    if (StringUtils.isNotBlank(newValue.getJavascript())) {
      changedValues.add(
          ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.audit.script.javascript")
              .from(null)
              .to(newValue.getJavascript())
              .build());
    }
    if (StringUtils.isNotBlank(newValue.getCss())) {
      changedValues.add(
          ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.audit.script.css")
              .from(null)
              .to(newValue.getCss())
              .build());
    }
    auditService.audit(
        AuditEvent.builder(SCRIPT_CREATED)
            .affectedObject(
                AuditResource.builder(PLUGIN_NAME, AssociatedItem.Type.PLUGIN.name())
                    .id(PLUGIN_KEY)
                    .build())
            .appendExtraAttributes(
                Collections.singletonList(
                    AuditAttribute.fromI18nKeys(
                            "ru.mail.jira.plugins.jsincluder.audit.script.id",
                            String.format("%d (%s)", newValue.getId(), newValue.getName()))
                        .build()))
            .changedValues(changedValues)
            .build());
  }

  @Override
  public void adminEditScript(
      JsincluderAuditChangedValue oldValue, JsincluderAuditChangedValue newValue) {
    List<ChangedValue> changedValues = new ArrayList<>();
    if (!(newValue.getName()).equals(oldValue.getName())) {
      changedValues.add(
          ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.configuration.script.name")
              .from(oldValue.getName())
              .to(newValue.getName())
              .build());
    }
    if (!(newValue.getBindings()).equals(oldValue.getBindings()))
      changedValues.add(
          ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.configuration.script.binding")
              .from(oldValue.getBindings())
              .to(newValue.getBindings())
              .build());
    if (!(newValue.getJavascript()).equals(oldValue.getJavascript())) {
      changedValues.add(
          ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.audit.script.javascript")
              .from(oldValue.getJavascript())
              .to(newValue.getJavascript())
              .build());
    }
    if (!(newValue.getCss()).equals(oldValue.getCss())) {
      changedValues.add(
          ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.audit.script.css")
              .from(oldValue.getCss())
              .to(newValue.getCss())
              .build());
    }
    if (changedValues.size() > 1) {
      auditService.audit(
          AuditEvent.builder(SCRIPT_UPDATED)
              .affectedObject(
                  AuditResource.builder(PLUGIN_NAME, AssociatedItem.Type.PLUGIN.name())
                      .id(PLUGIN_KEY)
                      .build())
              .appendExtraAttributes(
                  Collections.singletonList(
                      AuditAttribute.fromI18nKeys(
                              "ru.mail.jira.plugins.jsincluder.audit.script.id",
                              String.format("%d (%s)", newValue.getId(), newValue.getName()))
                          .build()))
              .changedValues(changedValues)
              .build());
    }
  }

  @Override
  public void adminDeleteScript(JsincluderAuditChangedValue oldValue) {
    List<ChangedValue> changedValues = new ArrayList<>();
    changedValues.add(
        ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.configuration.script.name")
            .from(oldValue.getName())
            .to(null)
            .build());
    changedValues.add(
        ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.configuration.script.binding")
            .from(oldValue.getBindings())
            .to(null)
            .build());
    if (StringUtils.isNotBlank(oldValue.getJavascript())) {
      changedValues.add(
          ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.audit.script.javascript")
              .from(oldValue.getJavascript())
              .to(null)
              .build());
    }
    if (StringUtils.isNotBlank(oldValue.getCss())) {
      changedValues.add(
          ChangedValue.fromI18nKeys("ru.mail.jira.plugins.jsincluder.audit.script.css")
              .from(oldValue.getCss())
              .to(null)
              .build());
    }
    auditService.audit(
        AuditEvent.builder(SCRIPT_DELETED)
            .affectedObject(
                AuditResource.builder(PLUGIN_NAME, AssociatedItem.Type.PLUGIN.name())
                    .id(PLUGIN_KEY)
                    .build())
            .appendExtraAttributes(
                Collections.singletonList(
                    AuditAttribute.fromI18nKeys(
                            "ru.mail.jira.plugins.jsincluder.audit.script.id",
                            String.format("%d (%s)", oldValue.getId(), oldValue.getName()))
                        .build()))
            .changedValues(changedValues)
            .build());
  }
}
