package ru.mail.jira.plugins.jsincluder.configuration;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import ru.mail.jira.plugins.jsincluder.Binding;
import ru.mail.jira.plugins.jsincluder.Script;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("configuration")
@Produces({ MediaType.APPLICATION_JSON })
public class JsIncluderConfigurationAction extends JiraWebActionSupport {
    private final ActiveObjects ao;

    public JsIncluderConfigurationAction(ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public String doDefault() throws Exception {
        return SUCCESS;
    }

    @SuppressWarnings("unused")
    public Script[] getScripts() {
        return ao.executeInTransaction(new TransactionCallback<Script[]>() {
            @Override
            public Script[] doInTransaction() {
                return ao.find(Script.class);
            }
        });
    }

    @POST
    @Path("/addScript")
    public Response addScript(@FormParam("name") final String name,
                              @FormParam("code") final String code) {
        Script script = ao.executeInTransaction(new TransactionCallback<Script>() {
            @Override
            public Script doInTransaction() {
                Script script = ao.create(Script.class);
                script.setName(name);
                script.setCode(code);
                script.save();
                return script;
            }
        });
        return Response.ok(String.format("{\"id\": %d}", script.getID())).build();
    }

    @POST
    @Path("/editScript")
    public Response editScript(@FormParam("id") final int id,
                               @FormParam("name") final String name,
                               @FormParam("code") final String code) {
        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                Script script = ao.get(Script.class, id);
                script.setName(name);
                script.setCode(code);
                script.save();
                return null;
            }
        });
        return Response.ok().build();
    }

    @GET
    @Path("/deleteScript")
    public Response deleteScript(@QueryParam("id") final int id) {
        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                Script script = ao.get(Script.class, id);
                for (Binding binding : script.getBindings())
                    ao.delete(binding);
                ao.delete(script);
                return null;
            }
        });
        return Response.ok().build();
    }

    public String getEnabledContexts(Binding binding) {
        StringBuilder sb = new StringBuilder();
        if (binding.isCreateContextEnabled())
            sb.append("Create");
        if (binding.isViewContextEnabled())
            sb.append(sb.length() > 0 ? ", " : "").append("View");
        if (binding.isEditContextEnabled())
            sb.append(sb.length() > 0 ? ", " : "").append("Edit");
        if (binding.isTransitionContextEnabled())
            sb.append(sb.length() > 0 ? ", " : "").append("Transition");
        return sb.toString();
    }

    @GET
    @Path("/addBinding")
    public Response addBinding(@QueryParam("scriptId") final int scriptId,
                               @QueryParam("projectKeys") final String projectKeys,
                               @QueryParam("issueTypeIds") final String issueTypeIds,
                               @QueryParam("createContextEnabled") final boolean createContextEnabled,
                               @QueryParam("viewContextEnabled") final boolean viewContextEnabled,
                               @QueryParam("editContextEnabled") final boolean editContextEnabled,
                               @QueryParam("transitionContextEnabled") final boolean transitionContextEnabled) {
        Binding binding = ao.executeInTransaction(new TransactionCallback<Binding>() {
            @Override
            public Binding doInTransaction() {
                Binding binding = ao.create(Binding.class);
                binding.setScript(ao.get(Script.class, scriptId));
                binding.setProjectKeys(projectKeys);
                binding.setIssueTypeIds(issueTypeIds);
                binding.setCreateContextEnabled(createContextEnabled);
                binding.setViewContextEnabled(viewContextEnabled);
                binding.setEditContextEnabled(editContextEnabled);
                binding.setTransitionContextEnabled(transitionContextEnabled);
                binding.save();
                return binding;
            }
        });
        return Response.ok(String.format("{\"id\": %d, \"enabledContexts\": \"%s\"}", binding.getID(), getEnabledContexts(binding))).build();
    }

    @GET
    @Path("/editBinding")
    public Response editBinding(@QueryParam("id") final int id,
                                @QueryParam("projectKeys") final String projectKeys,
                                @QueryParam("issueTypeIds") final String issueTypeIds,
                                @QueryParam("createContextEnabled") final boolean createContextEnabled,
                                @QueryParam("viewContextEnabled") final boolean viewContextEnabled,
                                @QueryParam("editContextEnabled") final boolean editContextEnabled,
                                @QueryParam("transitionContextEnabled") final boolean transitionContextEnabled) {
        Binding binding = ao.executeInTransaction(new TransactionCallback<Binding>() {
            @Override
            public Binding doInTransaction() {
                Binding binding = ao.get(Binding.class, id);
                binding.setProjectKeys(projectKeys);
                binding.setIssueTypeIds(issueTypeIds);
                binding.setCreateContextEnabled(createContextEnabled);
                binding.setViewContextEnabled(viewContextEnabled);
                binding.setEditContextEnabled(editContextEnabled);
                binding.setTransitionContextEnabled(transitionContextEnabled);
                binding.save();
                return binding;
            }
        });
        return Response.ok(String.format("{\"enabledContexts\": \"%s\"}", getEnabledContexts(binding))).build();
    }

    @GET
    @Path("/deleteBinding")
    public Response deleteBinding(@QueryParam("id") final int id) {
        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                Binding binding = ao.get(Binding.class, id);
                ao.delete(binding);
                return null;
            }
        });
        return Response.ok().build();
    }
}
