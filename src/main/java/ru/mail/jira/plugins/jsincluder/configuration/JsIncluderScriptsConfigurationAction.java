/* (C)2020 */
package ru.mail.jira.plugins.jsincluder.configuration;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoNotRequired;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.commons.RestFieldException;
import ru.mail.jira.plugins.commons.RestUtils;
import ru.mail.jira.plugins.jsincluder.*;
import ru.mail.jira.plugins.jsincluder.audit.JsincluderAuditChangedValue;
import ru.mail.jira.plugins.jsincluder.audit.JsincluderAuditService;

@Path("configuration")
@WebSudoRequired
@Produces({MediaType.APPLICATION_JSON})
public class JsIncluderScriptsConfigurationAction extends JiraWebActionSupport {
  private final GlobalPermissionManager globalPermissionManager;
  private final I18nHelper i18nHelper;
  private final IssueTypeManager issueTypeManager;
  private final JsincluderAuditService jsincluderAuditService;
  private final ProjectManager projectManager;
  private final ProjectService projectService;
  private final ScriptManager scriptManager;

  private final String baseUrl;

  public JsIncluderScriptsConfigurationAction(
      @ComponentImport ApplicationProperties applicationProperties,
      @ComponentImport GlobalPermissionManager globalPermissionManager,
      @ComponentImport I18nHelper i18nHelper,
      @ComponentImport IssueTypeManager issueTypeManager,
      JsincluderAuditService jsincluderAuditService,
      @ComponentImport ProjectManager projectManager,
      @ComponentImport ProjectService projectService,
      ScriptManager scriptManager) {
    this.globalPermissionManager = globalPermissionManager;
    this.i18nHelper = i18nHelper;
    this.issueTypeManager = issueTypeManager;
    this.jsincluderAuditService = jsincluderAuditService;
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
      throw new RestFieldException(
          i18nHelper.getText("issue.field.required", i18nHelper.getText("common.words.name")),
          "name");
    if (StringUtils.trimToNull(code) == null && StringUtils.trimToNull(css) == null)
      throw new RestFieldException(
          i18nHelper.getText("ru.mail.jira.plugins.jsincluder.configuration.script.code.required"),
          "code");
    if (bindings.size() == 0)
      throw new RestFieldException(
          i18nHelper.getText(
              "issue.field.required",
              i18nHelper.getText("ru.mail.jira.plugins.jsincluder.configuration.script.binding")),
          "binding");
  }

  private List<BindingDto> buildBindingDtos(int scriptId) {
    Script script = scriptManager.getScript(scriptId);
    List<BindingDto> bindingDtos = new ArrayList<BindingDto>(script.getBindings().length);
    for (Binding binding : script.getBindings()) {
      BindingDto bindingDto = new BindingDto(binding);
      Project project = projectManager.getProjectObj(binding.getProjectId());
      if (project != null)
        bindingDto.setProject(
            new ProjectDto(
                project.getId(),
                project.getKey(),
                project.getName(),
                String.format(
                    "projectavatar?pid=%d&avatarId=%d&size=xxmall",
                    project.getId(), project.getAvatar().getId())));

      ProjectCategory projectCategory =
          projectManager.getProjectCategory(binding.getProjectCategoryId());
      if (projectCategory != null) {
        bindingDto.setProjectCategory(
            new ProjectCategoryDto(
                projectCategory.getId(),
                projectCategory.getName(),
                projectCategory.getDescription()));
      }

      List<IssueTypeDto> issueTypes = new ArrayList<IssueTypeDto>();
      for (String issueTypeId : CommonUtils.split(binding.getIssueTypeIds())) {
        IssueType issueType = issueTypeManager.getIssueType(issueTypeId);
        if (issueType != null)
          issueTypes.add(
              new IssueTypeDto(
                  issueType.getId(), issueType.getName(), baseUrl + issueType.getIconUrl()));
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

    if (!isUserAllowed()) throw new SecurityException();

    List<ScriptDto> result = new ArrayList<ScriptDto>();
    for (Script script : scriptManager.getScripts()) {
      ScriptDto scriptDto = new ScriptDto(script);
      scriptDto.setBindings(buildBindingDtos(script.getID()));
      result.add(scriptDto);
    }
    return RestUtils.success(result);
  }

  @GET
  @Path("/script/{id}")
  @WebSudoNotRequired
  public Response getScript(@PathParam("id") final int id) {

    if (!isUserAllowed()) throw new SecurityException();

    Script script = scriptManager.getScript(id);
    ScriptDto scriptDto = new ScriptDto(script);
    scriptDto.setBindings(buildBindingDtos(script.getID()));
    return RestUtils.success(scriptDto);
  }

  @POST
  @Path("/script/")
  @WebSudoNotRequired
  public Response createScript(final ScriptDto scriptDto) {

    if (!isUserAllowed()) throw new SecurityException();
    checkRequireFields(
        scriptDto.getName(), scriptDto.getCode(), scriptDto.getCss(), scriptDto.getBindings());

    Script script =
        scriptManager.createScript(scriptDto.getName(), scriptDto.getCode(), scriptDto.getCss());
    for (BindingDto bindingDto : scriptDto.getBindings()) {
      Long projectId = bindingDto.getProject() != null ? bindingDto.getProject().getId() : null;
      Long projectCategoryId =
          bindingDto.getProjectCategory() != null ? bindingDto.getProjectCategory().getId() : null;
      List<String> issueTypes = new ArrayList<String>();
      for (IssueTypeDto issueTypeDto : bindingDto.getIssueTypes())
        issueTypes.add(issueTypeDto.getId());
      scriptManager.createBinding(
          script.getID(),
          projectId,
          projectCategoryId,
          CommonUtils.join(issueTypes),
          bindingDto.isCreateContextEnabled(),
          bindingDto.isViewContextEnabled(),
          bindingDto.isEditContextEnabled(),
          bindingDto.isTransitionContextEnabled());
    }
    jsincluderAuditService.adminCreateScript(
        JsincluderAuditChangedValue.create(
            scriptManager.getScript(script.getID()), i18nHelper, issueTypeManager, projectManager));

    ScriptDto scriptDtoNew = new ScriptDto(script);
    scriptDtoNew.setBindings(buildBindingDtos(script.getID()));
    return RestUtils.success(scriptDtoNew);
  }

  @PUT
  @Path("/script/{id}")
  @WebSudoNotRequired
  public Response updateScript(final ScriptDto scriptDto) {
    if (!isUserAllowed()) throw new SecurityException();
    checkRequireFields(
        scriptDto.getName(), scriptDto.getCode(), scriptDto.getCss(), scriptDto.getBindings());

    JsincluderAuditChangedValue oldValue =
        JsincluderAuditChangedValue.create(
            scriptManager.getScript(scriptDto.getId()),
            i18nHelper,
            issueTypeManager,
            projectManager);
    Script script =
        scriptManager.updateScript(
            scriptDto.getId(), scriptDto.getName(), scriptDto.getCode(), scriptDto.getCss());
    Map<Integer, Binding> oldBindings = new HashMap<Integer, Binding>(script.getBindings().length);
    for (Binding binding : script.getBindings()) oldBindings.put(binding.getID(), binding);
    for (BindingDto bindingDto : scriptDto.getBindings()) {
      Long projectId = bindingDto.getProject() != null ? bindingDto.getProject().getId() : null;
      Long projectCategoryId =
          bindingDto.getProjectCategory() != null ? bindingDto.getProjectCategory().getId() : null;
      List<String> issueTypes = new ArrayList<String>();
      for (IssueTypeDto issueTypeDto : bindingDto.getIssueTypes())
        issueTypes.add(issueTypeDto.getId());

      if (bindingDto.getId().startsWith("temp_")) {
        scriptManager.createBinding(
            script.getID(),
            projectId,
            projectCategoryId,
            CommonUtils.join(issueTypes),
            bindingDto.isCreateContextEnabled(),
            bindingDto.isViewContextEnabled(),
            bindingDto.isEditContextEnabled(),
            bindingDto.isTransitionContextEnabled());
      } else {
        int bindingId = Integer.parseInt(bindingDto.getId());
        if (oldBindings.get(bindingId) != null) {
          scriptManager.updateBinding(
              bindingId,
              projectId,
              projectCategoryId,
              CommonUtils.join(issueTypes),
              bindingDto.isCreateContextEnabled(),
              bindingDto.isViewContextEnabled(),
              bindingDto.isEditContextEnabled(),
              bindingDto.isTransitionContextEnabled());
          oldBindings.remove(bindingId);
        }
      }
    }

    for (Integer oldBindingId : oldBindings.keySet()) scriptManager.deleteBinding(oldBindingId);

    JsincluderAuditChangedValue newValue =
        JsincluderAuditChangedValue.create(
            scriptManager.getScript(scriptDto.getId()),
            i18nHelper,
            issueTypeManager,
            projectManager);
    jsincluderAuditService.adminEditScript(oldValue, newValue);

    scriptDto.setBindings(buildBindingDtos(script.getID()));
    return RestUtils.success(scriptDto);
  }

  @DELETE
  @Path("/script/{id}")
  @WebSudoNotRequired
  public Response deleteScript(@PathParam("id") final int id) {
    if (!isUserAllowed()) throw new SecurityException();
    JsincluderAuditChangedValue oldValue =
        JsincluderAuditChangedValue.create(
            scriptManager.getScript(id), i18nHelper, issueTypeManager, projectManager);
    scriptManager.deleteScript(id);
    jsincluderAuditService.adminDeleteScript(oldValue);
    return RestUtils.success(null);
  }

  @GET
  @Path("/script/{scriptId}/binding")
  @WebSudoNotRequired
  public Response getBindings(@PathParam("scriptId") final String scriptId) {
    if (!isUserAllowed()) throw new SecurityException();

    return RestUtils.success(
        buildBindingDtos(scriptManager.getScript(Integer.parseInt(scriptId)).getID()));
  }

  @GET
  @Path("/script/{scriptId}/code")
  @WebSudoNotRequired
  public Response getCode(@PathParam("scriptId") final String scriptId) {
    return RestUtils.success(scriptManager.getScript(Integer.parseInt(scriptId)).getCode());
  }

  @GET
  @Path("/project")
  @WebSudoNotRequired
  public Response getProjects(@QueryParam("filter") final String filter) {
    List<ProjectDto> projectsDtos = new ArrayList<ProjectDto>();
    ApplicationUser user = getLoggedInUser();

    String formattedFilter = filter.trim().toLowerCase();
    List<Project> allProjects =
        isUserAllowed()
            ? projectManager.getProjectObjects()
            : projectService.getAllProjects(user).get();

    Map<Long, ProjectCategoryDto> allProjectCategoryDtos = new HashMap<>();
    allProjects.forEach(
        project -> {
          ProjectCategory category = project.getProjectCategory();
          if (category != null
              && StringUtils.containsIgnoreCase(category.getName(), formattedFilter)) {
            allProjectCategoryDtos.put(
                category.getId(),
                new ProjectCategoryDto(
                    category.getId(), category.getName(), category.getDescription()));
          }
        });

    if (StringUtils.isEmpty(formattedFilter))
      for (Project project : allProjects) {
        projectsDtos.add(
            new ProjectDto(
                project.getId(),
                project.getKey(),
                project.getName(),
                String.format(
                    "projectavatar?pid=%d&avatarId=%d&size=xxmall",
                    project.getId(), project.getAvatar().getId())));
        if (projectsDtos.size() >= 10) break;
      }
    else
      for (Project project : allProjects)
        if (StringUtils.containsIgnoreCase(project.getName(), formattedFilter)
            || StringUtils.containsIgnoreCase(project.getKey(), formattedFilter))
          if (projectsDtos.size() < 10)
            projectsDtos.add(
                new ProjectDto(
                    project.getId(),
                    project.getKey(),
                    project.getName(),
                    String.format(
                        "projectavatar?pid=%d&avatarId=%d&size=xxmall",
                        project.getId(), project.getAvatar().getId())));
    return RestUtils.success(
        new HashMap<String, Object>() {
          {
            put("projects", projectsDtos);
            put("categories", allProjectCategoryDtos.values());
          }
        });
  }

  @GET
  @Path("/issuetype")
  @WebSudoNotRequired
  public Response getIssueTypes(
      @QueryParam("projectId") final String projectId, @QueryParam("filter") final String filter) {
    List<IssueTypeDto> result = new ArrayList<IssueTypeDto>();
    Collection<IssueType> allIssueType = new ArrayList<IssueType>();
    if (StringUtils.isEmpty(projectId)) allIssueType = issueTypeManager.getIssueTypes();
    else {
      Project project = projectManager.getProjectObj(Long.parseLong(projectId));
      if (project != null) allIssueType = project.getIssueTypes();
    }

    String formattedFilter = filter.trim().toLowerCase();
    if (StringUtils.isEmpty(formattedFilter))
      for (IssueType issueType : allIssueType) {
        result.add(
            new IssueTypeDto(
                issueType.getId(), issueType.getName(), baseUrl + issueType.getIconUrl()));
        if (result.size() >= 10) break;
      }
    else
      for (IssueType issueType : allIssueType)
        if (StringUtils.containsIgnoreCase(issueType.getName(), formattedFilter))
          if (result.size() < 10)
            result.add(
                new IssueTypeDto(
                    issueType.getId(), issueType.getName(), baseUrl + issueType.getIconUrl()));
    return RestUtils.success(result);
  }
}
