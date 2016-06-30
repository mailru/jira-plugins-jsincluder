package ru.mail.jira.plugins.jsincluder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@XmlRootElement
public class BindingDto {
    @XmlElement
    private String id;
    @XmlElement
    private ProjectDto project;
    @XmlElement
    private List<IssueTypeDto> issueTypes;
    @XmlElement
    private boolean createContextEnabled;
    @XmlElement
    private boolean viewContextEnabled;
    @XmlElement
    private boolean editContextEnabled;
    @XmlElement
    private boolean transitionContextEnabled;
    @XmlElement
    private String enabledContexts;

    public BindingDto() {
    }

    public BindingDto(Binding binding) {
        this.id = String.valueOf(binding.getID());
        this.createContextEnabled = binding.isCreateContextEnabled();
        this.viewContextEnabled = binding.isViewContextEnabled();
        this.editContextEnabled = binding.isEditContextEnabled();
        this.transitionContextEnabled = binding.isTransitionContextEnabled();
        this.enabledContexts = buildEnabledContexts();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ProjectDto getProject() {
        return this.project;
    }

    public void setProject(ProjectDto project) {
        this.project = project;
    }

    public List<IssueTypeDto> getIssueTypes() {
        return this.issueTypes;
    }

    public void setIssueTypes(List<IssueTypeDto> issueTypes) {
        this.issueTypes = issueTypes;
    }

    public boolean getCreateContextEnabled() {
        return this.createContextEnabled;
    }

    public void setCreateContextEnabled(boolean createContextEnabled) {
        this.createContextEnabled = createContextEnabled;
    }

    public boolean getEditContextEnabled() {
        return this.editContextEnabled;
    }

    public void setEditContextEnabled(boolean editContextEnabled) {
        this.editContextEnabled = editContextEnabled;
    }

    public boolean getViewContextEnabled() {
        return this.viewContextEnabled;
    }

    public void setViewContextEnabled(boolean viewContextEnabled) {
        this.viewContextEnabled = viewContextEnabled;
    }

    public boolean getTransitionContextEnabled() {
        return this.transitionContextEnabled;
    }

    public void setTransitionContextEnabled(boolean transitionContextEnabled) {
        this.transitionContextEnabled = transitionContextEnabled;
    }

    public String getEnabledContexts() {
        return this.enabledContexts;
    }

    public void setEnabledContexts(String enabledContexts) {
        this.enabledContexts = enabledContexts;
    }

    private String buildEnabledContexts() {
        StringBuilder sb = new StringBuilder();
        if (createContextEnabled)
            sb.append("Create");
        if (viewContextEnabled)
            sb.append(sb.length() > 0 ? ", " : "").append("View");
        if (editContextEnabled)
            sb.append(sb.length() > 0 ? ", " : "").append("Edit");
        if (transitionContextEnabled)
            sb.append(sb.length() > 0 ? ", " : "").append("Transition");
        return sb.toString();
    }
}
