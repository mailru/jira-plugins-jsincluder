package ru.mail.jira.plugins.jsincluder;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@Getter @Setter
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
