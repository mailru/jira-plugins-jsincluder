/* (C)2021 */
package ru.mail.jira.plugins.jsincluder.audit;

public interface JsincluderAuditService {
  void adminCreateScript(JsincluderAuditChangedValue newValue);

  void adminEditScript(JsincluderAuditChangedValue oldValue, JsincluderAuditChangedValue newValue);

  void adminDeleteScript(JsincluderAuditChangedValue oldValue);
}
