package ru.mail.jira.plugins.jsincluder;

import net.java.ao.Entity;
import net.java.ao.schema.Index;
import net.java.ao.schema.Indexed;
import net.java.ao.schema.Indexes;

@Indexes({
        @Index(name = "projectIssueTypesCreateContext", methodNames = {"getProjectId", "getIssueTypeIds", "isCreateContextEnabled"}),
        @Index(name = "projectIssueTypesEditContext", methodNames = {"getProjectId", "getIssueTypeIds", "isEditContextEnabled"}),
        @Index(name = "projectIssueTypesTransitionContext", methodNames = {"getProjectId", "getIssueTypeIds", "isTransitionContextEnabled"}),
        @Index(name = "projectIssueTypesViewContext", methodNames = {"getProjectId", "getIssueTypeIds", "isViewContextEnabled", "isEditContextEnabled", "isTransitionContextEnabled"})
})
public interface Binding extends Entity {
    Script getScript();
    void setScript(Script script);

    @Indexed
    Long getProjectId();
    void setProjectId(Long projectId);

    @Indexed
    String getIssueTypeIds();
    void setIssueTypeIds(String issueTypeIds);

    boolean isCreateContextEnabled();
    void setCreateContextEnabled(boolean createContextEnabled);

    boolean isViewContextEnabled();
    void setViewContextEnabled(boolean viewContextEnabled);

    boolean isEditContextEnabled();
    void setEditContextEnabled(boolean editContextEnabled);

    boolean isTransitionContextEnabled();
    void setTransitionContextEnabled(boolean transitionContextEnabled);

    @Deprecated
    String getProjectKeys();
    @Deprecated
    void setProjectKeys(String projectKeys);
}
