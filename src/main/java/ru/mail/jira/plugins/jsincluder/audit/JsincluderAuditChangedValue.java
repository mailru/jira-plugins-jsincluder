/* (C)2021 */
package ru.mail.jira.plugins.jsincluder.audit;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.jsincluder.Binding;
import ru.mail.jira.plugins.jsincluder.Script;

public class JsincluderAuditChangedValue {
  private final int id;
  private final String name;
  private final String bindings;
  private final String javascript;
  private final String css;

  public JsincluderAuditChangedValue(
      int id, String name, String bindings, String javascript, String css) {
    this.id = id;
    this.name = name;
    this.bindings = bindings;
    this.javascript = javascript;
    this.css = css;
  }

  public static JsincluderAuditChangedValue create(
      Script script,
      I18nHelper i18nHelper,
      IssueTypeManager issueTypeManager,
      ProjectManager projectManager) {
    List<String> bindings = new ArrayList<>();
    for (Binding binding : script.getBindings()) {
      bindings.add(
          String.format(
              "%s: %s (%s)",
              getProject(binding, i18nHelper, projectManager),
              getIssueTypes(binding, i18nHelper, issueTypeManager),
              getContext(binding, i18nHelper)));
    }
    return new JsincluderAuditChangedValue(
        script.getID(),
        script.getName(),
        CommonUtils.join(bindings),
        script.getCode(),
        script.getCss());
  }

  private static String getProject(
      Binding binding, I18nHelper i18nHelper, ProjectManager projectManager) {
    if (binding.getProjectId() != null) {
      Project project = projectManager.getProjectObj(binding.getProjectId());
      return project != null
          ? String.format("%s (%s)", project.getName(), project.getKey())
          : String.format("Project with id %d not found", binding.getProjectId());
    } else {
      return i18nHelper.getText(
          "ru.mail.jira.plugins.jsincluder.configuration.tab.bindings.project.all");
    }
  }

  private static String getIssueTypes(
      Binding binding, I18nHelper i18nHelper, IssueTypeManager issueTypeManager) {
    List<IssueType> issueTypes = new ArrayList<>();
    for (String issueTypeId : CommonUtils.split(binding.getIssueTypeIds())) {
      IssueType issueType = issueTypeManager.getIssueType(issueTypeId);
      if (issueType != null) issueTypes.add(issueType);
    }
    if (issueTypes.size() > 0) {
      return CommonUtils.join(
          issueTypes.stream().map(IssueConstant::getName).collect(Collectors.toList()));
    } else {
      return i18nHelper.getText(
          "ru.mail.jira.plugins.jsincluder.configuration.tab.bindings.issueTypes.all");
    }
  }

  private static String getContext(Binding binding, I18nHelper i18nHelper) {
    StringBuilder sb = new StringBuilder();
    if (binding.isCreateContextEnabled()) sb.append("Create");
    if (binding.isViewContextEnabled()) sb.append(sb.length() > 0 ? ", " : "").append("View");
    if (binding.isEditContextEnabled()) sb.append(sb.length() > 0 ? ", " : "").append("Edit");
    if (binding.isTransitionContextEnabled())
      sb.append(sb.length() > 0 ? ", " : "").append("Transition");
    String context = sb.toString();
    return context.length() > 0
        ? context
        : i18nHelper.getText(
            "ru.mail.jira.plugins.jsincluder.configuration.tab.bindings.context.all");
  }

  public int getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getBindings() {
    return this.bindings;
  }

  public String getJavascript() {
    return this.javascript;
  }

  public String getCss() {
    return this.css;
  }
}
