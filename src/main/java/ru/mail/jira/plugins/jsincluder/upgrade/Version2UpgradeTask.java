package ru.mail.jira.plugins.jsincluder.upgrade;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.external.ActiveObjectsUpgradeTask;
import com.atlassian.activeobjects.external.ModelVersion;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.transaction.TransactionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.jsincluder.Binding;

import java.util.List;

public class Version2UpgradeTask implements ActiveObjectsUpgradeTask {
    private final ProjectManager projectManager;

    private final static Logger log = LoggerFactory.getLogger(Version2UpgradeTask.class);

    public Version2UpgradeTask(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    @Override
    public ModelVersion getModelVersion() {
        return ModelVersion.valueOf("2");
    }

    @Override
    public void upgrade(ModelVersion currentVersion, final ActiveObjects ao) {
        log.info("Current version " + currentVersion.toString());
        if (currentVersion.isOlderThan(getModelVersion())) {
            ao.migrate(Binding.class);
            log.info("Run upgrade task to version 2");
            ao.executeInTransaction(new TransactionCallback<Void>() {
                @Override
                public Void doInTransaction() {
                    for (Binding oldBinding : ao.find(Binding.class)) {
                        List<String> projectKeys = CommonUtils.split(oldBinding.getProjectKeys());
                        for (int i = 0; i < projectKeys.size(); i++) {
                            Project project = projectManager.getProjectObjByKey(projectKeys.get(i));
                            if (project != null) {
                                if (i == 0) {
                                    oldBinding.setProjectId(project.getId());
                                    oldBinding.save();
                                    continue;
                                }

                                Binding binding = ao.create(Binding.class);
                                binding.setScript(oldBinding.getScript());
                                binding.setProjectId(project.getId());
                                binding.setIssueTypeIds(oldBinding.getIssueTypeIds());
                                binding.setCreateContextEnabled(oldBinding.isCreateContextEnabled());
                                binding.setViewContextEnabled(oldBinding.isViewContextEnabled());
                                binding.setEditContextEnabled(oldBinding.isEditContextEnabled());
                                binding.setTransitionContextEnabled(oldBinding.isTransitionContextEnabled());
                                binding.save();
                            }
                        }
                    }
                    return null;
                }
            });
        }
    }


}
