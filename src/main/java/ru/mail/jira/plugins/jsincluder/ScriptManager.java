package ru.mail.jira.plugins.jsincluder;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;

public class ScriptManager {
    private final ActiveObjects ao;

    public ScriptManager(ActiveObjects ao) {
        this.ao = ao;
    }

    public Script getScript(final int id) {
        return ao.executeInTransaction(new TransactionCallback<Script>() {
            @Override
            public Script doInTransaction() {
                Script script = ao.get(Script.class, id);
                if (script == null)
                    throw new IllegalArgumentException(String.format("Script is not found by id %s", id));
                return script;
            }
        });
    }

    public Script[] getScripts() {
        return ao.executeInTransaction(new TransactionCallback<Script[]>() {
            @Override
            public Script[] doInTransaction() {
                return ao.find(Script.class);
            }
        });
    }

    public Binding getBinding(final int id) {
        return ao.executeInTransaction(new TransactionCallback<Binding>() {
            @Override
            public Binding doInTransaction() {
                Binding binding = ao.get(Binding.class, id);
                if (binding == null)
                    throw new IllegalArgumentException(String.format("Binding is not found by id %s", id));
                return binding;
            }
        });
    }

    public Binding[] findBindings(final Long projectId, final String issueTypeId) {
        return ao.executeInTransaction(new TransactionCallback<Binding[]>() {
            @Override
            public Binding[] doInTransaction() {
                return ao.find(Binding.class, Query.select().where("(PROJECT_ID = ? OR PROJECT_ID IS NULL) AND (ISSUE_TYPE_IDS LIKE ? OR ISSUE_TYPE_IDS = ?)", projectId, "%" + issueTypeId + "%", StringUtils.EMPTY));
            }
        });
    }

    public Script createScript(final String name, final String code, final String css) {
        return ao.executeInTransaction(new TransactionCallback<Script>() {
            @Override
            public Script doInTransaction() {
                Script script = ao.create(Script.class);
                script.setName(name);
                script.setCode(code);
                script.setCss(css);
                script.save();
                return script;
            }
        });
    }

    public Script updateScript(final int id, final String name, final String code, final String css) {
        return ao.executeInTransaction(new TransactionCallback<Script>() {
            @Override
            public Script doInTransaction() {
                Script script = getScript(id);
                script.setName(name);
                script.setCode(code);
                script.setCss(css);
                script.save();
                return script;
            }
        });
    }

    public void deleteScript(final int id) {
        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                Script script = getScript(id);
                for (Binding binding : script.getBindings())
                    ao.delete(binding);
                ao.delete(script);
                return null;
            }
        });
    }

    public Binding createBinding(final int scriptId, final Long projectId, final String issueTypeIds, final boolean createContextEnabled, final boolean viewContextEnabled, final boolean editContextEnabled, final boolean transitionContextEnabled) {
        return ao.executeInTransaction(new TransactionCallback<Binding>() {
            @Override
            public Binding doInTransaction() {
                Binding binding = ao.create(Binding.class);
                binding.setScript(getScript(scriptId));
                binding.setProjectId(projectId);
                binding.setIssueTypeIds(issueTypeIds);
                binding.setCreateContextEnabled(createContextEnabled);
                binding.setViewContextEnabled(viewContextEnabled);
                binding.setEditContextEnabled(editContextEnabled);
                binding.setTransitionContextEnabled(transitionContextEnabled);
                binding.save();
                return binding;
            }
        });
    }

    public Binding updateBinding(final int id, final Long projectId, final String issueTypeIds, final boolean createContextEnabled, final boolean viewContextEnabled, final boolean editContextEnabled, final boolean transitionContextEnabled) {
        return ao.executeInTransaction(new TransactionCallback<Binding>() {
            @Override
            public Binding doInTransaction() {
                Binding binding = getBinding(id);
                binding.setProjectId(projectId);
                binding.setIssueTypeIds(issueTypeIds);
                binding.setCreateContextEnabled(createContextEnabled);
                binding.setViewContextEnabled(viewContextEnabled);
                binding.setEditContextEnabled(editContextEnabled);
                binding.setTransitionContextEnabled(transitionContextEnabled);
                binding.save();
                return binding;
            }
        });
    }

    public void deleteBinding(final int id) {
        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                ao.delete(getBinding(id));
                return null;
            }
        });
    }
}
