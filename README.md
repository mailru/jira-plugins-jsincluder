## Description

Includes specified JS-scripts to particular pages.

## Rest API

- **GET** [/rest/jsincluder/1.0/configuration/script]()

public Response getScripts()

- **GET** [/rest/jsincluder/1.0/configuration/script/{id}]()

public Response getScript(@PathParam("id") final int id)

- **POST** [/rest/jsincluder/1.0/configuration/script/]()

public Response createScript(final ScriptDto scriptDto)

- **PUT** [/rest/jsincluder/1.0/configuration/script/{id}]()

public Response updateScript(final ScriptDto scriptDto) 

- **DELETE** [/rest/jsincluder/1.0/configuration/script/{id}]()

public Response deleteScript(@PathParam("id") final int id) 

- **GET** [/rest/jsincluder/1.0/configuration/script/{scriptId}/binding]()

public Response getBindings(@PathParam("scriptId") final String scriptId)

- **GET** [/rest/jsincluder/1.0/configuration/script/{scriptId}/code]()

public Response getCode(@PathParam("scriptId") final String scriptId)

- **GET** [/rest/jsincluder/1.0/configuration/project]()

public Response getProjects(@QueryParam("filter") final String filter)

- **GET** [/rest/jsincluder/1.0/configuration/issuetype]()

public Response getIssueTypes(@QueryParam("projectId") final String projectId, @QueryParam("filter") final String filter)

- **GET** [/rest/jsincluder/1.0/controller/getCreateScripts]()

public Response getCreateScripts(@QueryParam("projectId") final long projectId, @QueryParam("issueTypeId") final String issueTypeId)

- **GET** [/rest/jsincluder/1.0/controller/getIssueScripts]()

public Response getIssueScripts(@QueryParam("issueId") final long issueId, @QueryParam("context") final String contextValue)

