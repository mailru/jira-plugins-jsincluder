package ru.mail.jira.plugins.jsincluder;

import net.java.ao.Entity;

@SuppressWarnings("unused")
public interface Binding extends Entity {
    Script getScript();
    void setScript(Script script);

    Long getProjectId();
    void setProjectId(Long projectId);

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
