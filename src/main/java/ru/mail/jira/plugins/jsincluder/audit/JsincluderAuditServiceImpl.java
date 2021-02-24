/* (C)2021 */
package ru.mail.jira.plugins.jsincluder.audit;

import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.AuditingManager;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.auditing.handlers.ChangedValuesBuilder;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JsincluderAuditServiceImpl implements JsincluderAuditService {
  private static final String PLUGIN_NAME = "Mail.ru Plugin: JsIncluder";

  private final AuditingManager auditingManager;
  private final I18nHelper i18nHelper;

  @Autowired
  public JsincluderAuditServiceImpl(
      @ComponentImport AuditingManager auditingManager, @ComponentImport I18nHelper i18nHelper) {
    this.auditingManager = auditingManager;
    this.i18nHelper = i18nHelper;
  }

  @Override
  public void adminCreateScript(JsincluderAuditChangedValue newValue) {
    ChangedValuesBuilder changedValues = new ChangedValuesBuilder();
    changedValues.add(
        "ru.mail.jira.plugins.jsincluder.configuration.script.name", null, newValue.getName());
    changedValues.add(
        "ru.mail.jira.plugins.jsincluder.configuration.script.binding",
        null,
        newValue.getBindings());
    if (StringUtils.isNotBlank(newValue.getJavascript())) {
      changedValues.add(
          "ru.mail.jira.plugins.jsincluder.audit.script.javascript",
          null,
          newValue.getJavascript());
    }
    if (StringUtils.isNotBlank(newValue.getCss())) {
      changedValues.add(
          "ru.mail.jira.plugins.jsincluder.audit.script.css", null, newValue.getCss());
    }

    auditingManager.store(
        (new RecordRequest(
                AuditingCategory.AUDITING,
                i18nHelper.getText("ru.mail.jira.plugins.jsincluder.audit.script.created"),
                PLUGIN_NAME))
            .withChangedValues(changedValues.build()));
  }

  @Override
  public void adminEditScript(
      JsincluderAuditChangedValue oldValue, JsincluderAuditChangedValue newValue) {
    ChangedValuesBuilder changedValues = new ChangedValuesBuilder();
    if (!(newValue.getName()).equals(oldValue.getName())) {
      changedValues.add(
          "ru.mail.jira.plugins.jsincluder.configuration.script.name",
          oldValue.getName(),
          newValue.getName());
    }
    if (!(newValue.getBindings()).equals(oldValue.getBindings())) {
      changedValues.add(
          "ru.mail.jira.plugins.jsincluder.configuration.script.binding",
          oldValue.getBindings(),
          newValue.getBindings());
    }
    if (!(newValue.getJavascript()).equals(oldValue.getJavascript())) {
      if (StringUtils.isNotBlank(newValue.getJavascript())) {
        changedValues.add(
            "ru.mail.jira.plugins.jsincluder.audit.script.javascript",
            oldValue.getJavascript(),
            newValue.getJavascript());
      }
    }
    if (!(newValue.getCss()).equals(oldValue.getCss())) {
      if (StringUtils.isNotBlank(newValue.getCss())) {
        changedValues.add(
            "ru.mail.jira.plugins.jsincluder.audit.script.css",
            oldValue.getCss(),
            newValue.getCss());
      }
    }

    if (changedValues.build().size() > 0) {
      auditingManager.store(
          (new RecordRequest(
                  AuditingCategory.AUDITING,
                  i18nHelper.getText("ru.mail.jira.plugins.jsincluder.audit.script.updated"),
                  PLUGIN_NAME))
              .withChangedValues(changedValues.build()));
    }
  }

  @Override
  public void adminDeleteScript(JsincluderAuditChangedValue oldValue) {
    ChangedValuesBuilder changedValues = new ChangedValuesBuilder();
    changedValues.add(
        "ru.mail.jira.plugins.jsincluder.configuration.script.name", oldValue.getName(), null);
    changedValues.add(
        "ru.mail.jira.plugins.jsincluder.configuration.script.binding",
        oldValue.getBindings(),
        null);
    if (StringUtils.isNotBlank(oldValue.getJavascript())) {
      changedValues.add(
          "ru.mail.jira.plugins.jsincluder.audit.script.javascript",
          oldValue.getJavascript(),
          null);
    }
    if (StringUtils.isNotBlank(oldValue.getCss())) {
      changedValues.add(
          "ru.mail.jira.plugins.jsincluder.audit.script.css", oldValue.getCss(), null);
    }

    auditingManager.store(
        (new RecordRequest(
                AuditingCategory.AUDITING,
                i18nHelper.getText("ru.mail.jira.plugins.jsincluder.audit.script.deleted"),
                PLUGIN_NAME))
            .withChangedValues(changedValues.build()));
  }
}
