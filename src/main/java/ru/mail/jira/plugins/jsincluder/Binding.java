package ru.mail.jira.plugins.jsincluder;

import net.java.ao.Entity;

@SuppressWarnings("unused")
public interface Binding extends Entity {
    Script getScript();
    void setScript(Script script);
    String getProjectKeys();
    void setProjectKeys(String projectKeys);
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
}
