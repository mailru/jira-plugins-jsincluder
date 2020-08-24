package ru.mail.jira.plugins.jsincluder.configuration;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoNotRequired;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.commons.RestExecutor;
import ru.mail.jira.plugins.commons.RestFieldException;
import ru.mail.jira.plugins.jsincluder.Binding;
import ru.mail.jira.plugins.jsincluder.BindingDto;
import ru.mail.jira.plugins.jsincluder.IssueTypeDto;
import ru.mail.jira.plugins.jsincluder.ProjectDto;
import ru.mail.jira.plugins.jsincluder.Script;
import ru.mail.jira.plugins.jsincluder.ScriptDto;
import ru.mail.jira.plugins.jsincluder.ScriptManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("configuration")
@WebSudoRequired
@Produces({MediaType.APPLICATION_JSON})
public class JsIncluderScriptsConfigurationAction extends JiraWebActionSupport {
    private final GlobalPermissionManager globalPermissionManager;
    private final I18nHelper i18nHelper;
    private final IssueTypeManager issueTypeManager;
    private final ProjectManager projectManager;
    private final ProjectService projectService;
    private final ScriptManager scriptManager;

    private String baseUrl;

    public JsIncluderScriptsConfigurationAction(ApplicationProperties applicationProperties, GlobalPermissionManager globalPermissionManager, I18nHelper i18nHelper, IssueTypeManager issueTypeManager, ProjectManager projectManager, ProjectService projectService, ScriptManager scriptManager) {
        this.globalPermissionManager = globalPermissionManager;
        this.i18nHelper = i18nHelper;
        this.issueTypeManager = issueTypeManager;
        this.projectManager = projectManager;
        this.projectService = projectService;
        this.scriptManager = scriptManager;
        this.baseUrl = applicationProperties.getString(APKeys.JIRA_BASEURL);
    }

    @Override
    public String doDefault() throws Exception {
        return SUCCESS;
    }

    private boolean isUserAllowed() {
        return globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, getLoggedInUser());
    }

    private void checkRequireFields(String name, String code, String css, List<BindingDto> bindings) {
        if (StringUtils.trimToNull(name) == null)
            throw new RestFieldException(i18nHelper.getText("issue.field.required", i18nHelper.getText("common.words.name")), "name");
        if (StringUtils.trimToNull(code) == null && StringUtils.trimToNull(css) == null)
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.jsincluder.configuration.script.code.required") , "code");
        if (bindings.size() == 0)
            throw new RestFieldException(i18nHelper.getText("issue.field.required", i18nHelper.getText("ru.mail.jira.plugins.jsincluder.configuration.script.binding")), "binding");
    }

    private List<BindingDto> buildBindingDtos(int scriptId) {
        Script script = scriptManager.getScript(scriptId);
        List<BindingDto> bindingDtos = new ArrayList<BindingDto>(script.getBindings().length);
        for (Binding binding : script.getBindings()) {
            BindingDto bindingDto = new BindingDto(binding);
            Project project = projectManager.getProjectObj(binding.getProjectId());
            if (project != null)
                bindingDto.setProject(new ProjectDto(project.getId(), project.getKey(), project.getName(), String.format("projectavatar?pid=%d&avatarId=%d&size=xxmall", project.getId(), project.getAvatar().getId())));
            List<IssueTypeDto> issueTypes = new ArrayList<IssueTypeDto>();
            for (String issueTypeId : CommonUtils.split(binding.getIssueTypeIds())) {
                IssueType issueType = issueTypeManager.getIssueType(issueTypeId);
                if (issueType != null)
                    issueTypes.add(new IssueTypeDto(issueType.getId(), issueType.getName(), baseUrl + issueType.getIconUrl()));
            }
            bindingDto.setIssueTypes(issueTypes);
            bindingDtos.add(bindingDto);
        }
        return bindingDtos;
    }

    @GET
    @Path("/script")
    @WebSudoNotRequired
    public Response getScripts() {
        return new RestExecutor<List<ScriptDto>>() {
            @Override
            protected List<ScriptDto> doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();

                List<ScriptDto> result = new ArrayList<ScriptDto>();
                for (Script script : scriptManager.getScripts()) {
                    ScriptDto scriptDto = new ScriptDto(script);
                    scriptDto.setBindings(buildBindingDtos(script.getID()));
                    result.add(scriptDto);
                }
                return result;
            }
        }.getResponse();
    }

    @GET
    @Path("/script/{id}")
    @WebSudoNotRequired
    public Response getScript(@PathParam("id") final int id) {
        return new RestExecutor<ScriptDto>() {
            @Override
            protected ScriptDto doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();

                Script script = scriptManager.getScript(id);
                ScriptDto scriptDto = new ScriptDto(script);
                scriptDto.setBindings(buildBindingDtos(script.getID()));
                return scriptDto;
            }
        }.getResponse();
    }

    @POST
    @Path("/script/")
    @WebSudoNotRequired
    public Response createScript(final ScriptDto scriptDto) {
        return new RestExecutor<ScriptDto>() {
            @Override
            protected ScriptDto doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();
                checkRequireFields(scriptDto.getName(), scriptDto.getCode(), scriptDto.getCss(), scriptDto.getBindings());

                Script script = scriptManager.createScript(scriptDto.getName(), scriptDto.getCode(), scriptDto.getCss());
                for (BindingDto bindingDto : scriptDto.getBindings()) {
                    Long projectId = bindingDto.getProject() != null ? bindingDto.getProject().getId() : null;
                    List<String> issueTypes = new ArrayList<String>();
                    for (IssueTypeDto issueTypeDto : bindingDto.getIssueTypes())
                        issueTypes.add(issueTypeDto.getId());
                    scriptManager.createBinding(script.getID(), projectId, CommonUtils.join(issueTypes), bindingDto.isCreateContextEnabled(), bindingDto.isViewContextEnabled(),
                                                bindingDto.isEditContextEnabled(), bindingDto.isTransitionContextEnabled());
                }

                ScriptDto scriptDtoNew = new ScriptDto(script);
                scriptDtoNew.setBindings(buildBindingDtos(script.getID()));
                return scriptDtoNew;
            }
        }.getResponse();
    }

    @PUT
    @Path("/script/{id}")
    @WebSudoNotRequired
    public Response updateScript(final ScriptDto scriptDto) {
        return new RestExecutor<ScriptDto>() {
            @Override
            protected ScriptDto doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();
                checkRequireFields(scriptDto.getName(), scriptDto.getCode(), scriptDto.getCss(), scriptDto.getBindings());

                Script script = scriptManager.updateScript(scriptDto.getId(), scriptDto.getName(), scriptDto.getCode(), scriptDto.getCss());
                Map<Integer, Binding> oldBindings = new HashMap<Integer, Binding>(script.getBindings().length);
                for (Binding binding : script.getBindings())
                    oldBindings.put(binding.getID(), binding);
                for (BindingDto bindingDto : scriptDto.getBindings()) {
                    Long projectId = bindingDto.getProject() != null ? bindingDto.getProject().getId() : null;
                    List<String> issueTypes = new ArrayList<String>();
                    for (IssueTypeDto issueTypeDto : bindingDto.getIssueTypes())
                        issueTypes.add(issueTypeDto.getId());

                    if (bindingDto.getId().startsWith("temp_")) {
                        scriptManager.createBinding(script.getID(), projectId, CommonUtils.join(issueTypes),
                                                    bindingDto.isCreateContextEnabled(), bindingDto.isViewContextEnabled(),
                                                    bindingDto.isEditContextEnabled(), bindingDto.isTransitionContextEnabled());
                    } else {
                        int bindingId = Integer.parseInt(bindingDto.getId());
                        if (oldBindings.get(bindingId) != null) {
                            scriptManager.updateBinding(bindingId, projectId, CommonUtils.join(issueTypes),
                                                        bindingDto.isCreateContextEnabled(), bindingDto.isViewContextEnabled(),
                                                        bindingDto.isEditContextEnabled(), bindingDto.isTransitionContextEnabled());
                            oldBindings.remove(bindingId);
                        }
                    }
                }

                for (Integer oldBindingId : oldBindings.keySet())
                    scriptManager.deleteBinding(oldBindingId);

                scriptDto.setBindings(buildBindingDtos(script.getID()));
                return scriptDto;
            }
        }.getResponse();
    }

    @DELETE
    @Path("/script/{id}")
    @WebSudoNotRequired
    public Response deleteScript(@PathParam("id") final int id) {
        return new RestExecutor<Void>() {
            @Override
            protected Void doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();

                scriptManager.deleteScript(id);
                return null;
            }
        }.getResponse();
    }

    @GET
    @Path("/script/{scriptId}/binding")
    @WebSudoNotRequired
    public Response getBindings(@PathParam("scriptId") final String scriptId) {
        return new RestExecutor<List<BindingDto>>() {
            @Override
            protected List<BindingDto> doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();

                return buildBindingDtos(scriptManager.getScript(Integer.parseInt(scriptId)).getID());
            }
        }.getResponse();
    }

    @GET
    @Path("/script/{scriptId}/code")
    @WebSudoNotRequired
    public Response getCode(@PathParam("scriptId") final String scriptId) {
        return new RestExecutor<String>() {
            @Override
            protected String doAction() throws Exception {
                return scriptManager.getScript(Integer.parseInt(scriptId)).getCode();
            }
        }.getResponse();
    }

    @GET
    @Path("/project")
    @WebSudoNotRequired
    public Response getProjects(@QueryParam("filter") final String filter) {
        return new RestExecutor<List<ProjectDto>>() {
            @Override
            protected List<ProjectDto> doAction() throws Exception {
                List<ProjectDto> result = new ArrayList<ProjectDto>();
                ApplicationUser user = getLoggedInUser();

                String formattedFilter = filter.trim().toLowerCase();
                List<Project> allProjects = isUserAllowed() ? projectManager.getProjectObjects() : projectService.getAllProjects(user).get();
                if (StringUtils.isEmpty(formattedFilter))
                    for (Project project : allProjects) {
                        result.add(new ProjectDto(project.getId(), project.getKey(), project.getName(), String.format("projectavatar?pid=%d&avatarId=%d&size=xxmall", project.getId(), project.getAvatar().getId())));
                        if (result.size() >= 10)
                            break;
                    }
                else
                    for (Project project : allProjects)
                        if (StringUtils.containsIgnoreCase(project.getName(), formattedFilter) || StringUtils.containsIgnoreCase(project.getKey(), formattedFilter))
                            if (result.size() < 10)
                                result.add(new ProjectDto(project.getId(), project.getKey(), project.getName(), String.format("projectavatar?pid=%d&avatarId=%d&size=xxmall", project.getId(), project.getAvatar().getId())));
                return result;
            }
        }.getResponse();
    }

    @GET
    @Path("/issuetype")
    @WebSudoNotRequired
    public Response getIssueTypes(@QueryParam("projectId") final String projectId,
                                  @QueryParam("filter") final String filter) {
        return new RestExecutor<List<IssueTypeDto>>() {
            @Override
            protected List<IssueTypeDto> doAction() throws Exception {
                List<IssueTypeDto> result = new ArrayList<IssueTypeDto>();

                Collection<IssueType> allIssueType = new ArrayList<IssueType>();
                if (StringUtils.isEmpty(projectId))
                    allIssueType = issueTypeManager.getIssueTypes();
                else {
                    Project project = projectManager.getProjectObj(Long.parseLong(projectId));
                    if (project != null)
                        allIssueType = project.getIssueTypes();
                }

                String formattedFilter = filter.trim().toLowerCase();
                if (StringUtils.isEmpty(formattedFilter))
                    for (IssueType issueType : allIssueType) {
                        result.add(new IssueTypeDto(issueType.getId(), issueType.getName(), baseUrl + issueType.getIconUrl()));
                        if (result.size() >= 10)
                            break;
                    }
                else
                    for (IssueType issueType : allIssueType)
                        if (StringUtils.containsIgnoreCase(issueType.getName(), formattedFilter))
                            if (result.size() < 10)
                                result.add(new IssueTypeDto(issueType.getId(), issueType.getName(), baseUrl + issueType.getIconUrl()));
                return result;
            }
        }.getResponse();
    }
}
