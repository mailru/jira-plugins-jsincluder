package ru.mail.jira.plugins.jsincluder;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.transaction.TransactionCallback;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("controller")
@Produces({MediaType.APPLICATION_JSON})
public class ControllerResource {
    private final ActiveObjects ao;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final IssueManager issueManager;
    private final ProjectManager projectManager;
    private final IssueTypeManager issueTypeManager;
    private final GroupManager groupManager;
    private final ProjectRoleManager projectRoleManager;

    private enum Context {
        CREATE, VIEW, EDIT, TRANSITION;

        static Context parseContext(String contextValue) {
            try {
                return valueOf(contextValue.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }
    }

    public ControllerResource(ActiveObjects ao, JiraAuthenticationContext jiraAuthenticationContext, IssueManager issueManager,
                              ProjectManager projectManager, IssueTypeManager issueTypeManager, GroupManager groupManager,
                              ProjectRoleManager projectRoleManager) {
        this.ao = ao;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.issueTypeManager = issueTypeManager;
        this.groupManager = groupManager;
        this.projectRoleManager = projectRoleManager;
    }

    private String[] splitCommaString(String s) {
        if (StringUtils.isEmpty(s))
            return new String[0];
        else
            return s.trim().split("\\s*,\\s*");
    }

    public ScriptsEntity getScriptsEntity(Project project, IssueType issueType, Context context) {
        ApplicationUser user = jiraAuthenticationContext.getUser();

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

        Script[] allScripts = ao.executeInTransaction(new TransactionCallback<Script[]>() {
            @Override
            public Script[] doInTransaction() {
                return ao.find(Script.class);
            }
        });

        outer:
        for (Script script : allScripts) {
            boolean viewScriptAdded = false, editScriptAdded = false, transitionScriptAdded = false;

            for (Binding binding : script.getBindings()) {
                Long projectId = binding.getProjectId();
                if (projectId != null && !projectId.equals(project.getId()))
                    continue;

                List<String> issueTypes = Arrays.asList(splitCommaString(binding.getIssueTypeIds()));
                if (!issueTypes.isEmpty() && !issueTypes.contains(issueType.getId()))
                    continue;

                switch (context) {
                    case CREATE:
                        if (binding.isCreateContextEnabled()) {
                            result.addCreateScript(script.getCode());
                            continue outer;
                        }
                        break;
                    case VIEW:
                        if (!viewScriptAdded && binding.isViewContextEnabled()) {
                            result.addViewScript(script.getCode());
                            viewScriptAdded = true;
                        }
                        if (!editScriptAdded && binding.isEditContextEnabled()) {
                            result.addEditScript(script.getCode());
                            editScriptAdded = true;
                        }
                        if (!transitionScriptAdded && binding.isTransitionContextEnabled()) {
                            result.addTransitionScript(script.getCode());
                            transitionScriptAdded = true;
                        }
                        if (viewScriptAdded && editScriptAdded && transitionScriptAdded)
                            continue outer;
                        break;
                    case EDIT:
                        if (binding.isEditContextEnabled()) {
                            result.addEditScript(script.getCode());
                            continue outer;
                        }
                        break;
                    case TRANSITION:
                        if (binding.isTransitionContextEnabled()) {
                            result.addTransitionScript(script.getCode());
                            continue outer;
                        }
                        break;
                }
            }
        }

        return result;
    }

    public ScriptsEntity getScriptsEntity(Issue issue, Context context) {
        ScriptsEntity result = getScriptsEntity(issue.getProjectObject(), issue.getIssueTypeObject(), context);
        result.putParam("parentId", issue.getParentId());
        result.putParam("issueStatusId", issue.getStatusObject().getId());
        return result;
    }

    @GET
    @AnonymousAllowed
    @Path("/getCreateScripts")
    public Response getCreateScripts(@QueryParam("projectId") final long projectId,
                                     @QueryParam("issueTypeId") final String issueTypeId) {
        Project project = projectManager.getProjectObj(projectId);
        IssueType issueType = issueTypeManager.getIssueType(issueTypeId);
        if (project == null || issueType == null)
            return Response.serverError().build();
        return Response.ok(getScriptsEntity(project, issueType, Context.CREATE)).build();
    }

    @GET
    @AnonymousAllowed
    @Path("/getIssueScripts")
    public Response getIssueScripts(@QueryParam("issueId") final long issueId,
                                    @QueryParam("context") final String contextValue) {
        Issue issue = issueManager.getIssueObject(issueId);
        Context context = Context.parseContext(contextValue);
        if (issue == null || context == null)
            return Response.serverError().build();
        return Response.ok(getScriptsEntity(issue, context)).build();
    }
}
