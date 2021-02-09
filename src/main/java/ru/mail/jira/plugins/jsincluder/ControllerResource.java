/* (C)2020 */
package ru.mail.jira.plugins.jsincluder;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;

@Path("controller")
@Produces({MediaType.APPLICATION_JSON})
public class ControllerResource {
  private final JiraAuthenticationContext jiraAuthenticationContext;
  private final IssueManager issueManager;
  private final ProjectManager projectManager;
  private final IssueTypeManager issueTypeManager;
  private final GroupManager groupManager;
  private final ProjectRoleManager projectRoleManager;
  private final ScriptManager scriptManager;

  public ControllerResource(
      @ComponentImport JiraAuthenticationContext jiraAuthenticationContext,
      @ComponentImport IssueManager issueManager,
      @ComponentImport ProjectManager projectManager,
      @ComponentImport IssueTypeManager issueTypeManager,
      @ComponentImport GroupManager groupManager,
      @ComponentImport ProjectRoleManager projectRoleManager,
      ScriptManager scriptManager) {
    this.jiraAuthenticationContext = jiraAuthenticationContext;
    this.issueManager = issueManager;
    this.projectManager = projectManager;
    this.issueTypeManager = issueTypeManager;
    this.groupManager = groupManager;
    this.projectRoleManager = projectRoleManager;
    this.scriptManager = scriptManager;
  }

  private String[] splitCommaString(String s) {
    if (StringUtils.isEmpty(s)) return new String[0];
    else return s.trim().split("\\s*,\\s*");
  }

  public ScriptsEntity getScriptsEntity(Project project, IssueType issueType, Context context) {
    ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();

    ScriptsEntity result = new ScriptsEntity();

    result.putParam("projectId", project.getId());
    result.putParam("projectKey", project.getKey());
    result.putParam("issueTypeId", issueType.getId());
    if (user != null) {
      Map<String, Object> userDetails = new HashMap<String, Object>();
      userDetails.put("username", user.getName());
      userDetails.put("email", user.getEmailAddress());
      userDetails.put("groupNames", groupManager.getGroupNamesForUser(user));
      List<String> projectRoleNames = new ArrayList<String>();
      for (ProjectRole projectRole : projectRoleManager.getProjectRoles(user, project))
        projectRoleNames.add(projectRole.getName());
      userDetails.put("projectRoleNames", projectRoleNames);
      result.putParam("userDetails", userDetails);
    }
    ProjectCategory projectCategory = project.getProjectCategory();
    for (Binding binding :
        scriptManager.findBindings(
            project.getId(), projectCategory != null ? projectCategory.getId() : null, context)) {
      List<String> issueTypes = Arrays.asList(splitCommaString(binding.getIssueTypeIds()));
      if (!issueTypes.isEmpty() && !issueTypes.contains(issueType.getId())) continue;

      Script script = binding.getScript();
      switch (context) {
        case CREATE:
          result.addCreateScript(new ScriptDto(script));
          break;
        case VIEW:
          if (binding.isViewContextEnabled()) {
            result.addViewScript(new ScriptDto(script));
          }
          if (binding.isEditContextEnabled()) {
            result.addEditScript(new ScriptDto(script));
          }
          if (binding.isTransitionContextEnabled()) {
            result.addTransitionScript(new ScriptDto(script));
          }
          break;
        case EDIT:
          result.addEditScript(new ScriptDto(script));
          break;
        case TRANSITION:
          result.addTransitionScript(new ScriptDto(script));
          break;
      }
    }

    return result;
  }

  public ScriptsEntity getScriptsEntity(Issue issue, Context context) {
    ScriptsEntity result =
        getScriptsEntity(issue.getProjectObject(), issue.getIssueType(), context);
    result.putParam("parentId", issue.getParentId());
    result.putParam("issueStatusId", issue.getStatusId());
    return result;
  }

  @GET
  @AnonymousAllowed
  @Path("/getCreateScripts")
  public Response getCreateScripts(
      @QueryParam("projectId") final long projectId,
      @QueryParam("issueTypeId") final String issueTypeId) {
    if (projectId == 0 || issueTypeId == null) return Response.serverError().build();
    Project project = projectManager.getProjectObj(projectId);
    IssueType issueType = issueTypeManager.getIssueType(issueTypeId);
    if (project == null || issueType == null) return Response.serverError().build();
    return Response.ok(getScriptsEntity(project, issueType, Context.CREATE)).build();
  }

  @GET
  @AnonymousAllowed
  @Path("/getIssueScripts")
  public Response getIssueScripts(
      @QueryParam("issueId") final long issueId, @QueryParam("context") final String contextValue) {
    if (issueId == 0 || contextValue == null) return Response.serverError().build();
    Issue issue = issueManager.getIssueObject(issueId);
    Context context = Context.parseContext(contextValue);
    if (issue == null || context == null) return Response.serverError().build();
    return Response.ok(getScriptsEntity(issue, context)).build();
  }
}
