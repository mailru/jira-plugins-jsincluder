require(['jquery', 'underscore', 'backbone', 'jsincluder/configuration-dialog', 'jsincluder/confirm-dialog'], function($, _, Backbone, ConfigurationDialog, ConfirmDialog) {
    AJS.toInit(function() {
        /* Models */
        var Script = Backbone.Model.extend();
        var ScriptView = Backbone.Model.extend({urlRoot: AJS.contextPath() + '/rest/jsincluder/1.0/configuration/script/'});
        var Binding = Backbone.Model.extend();
        var filters = {};

        /* Collections */
        var ScriptCollection = Backbone.Collection.extend({
            model: Script,
            url: AJS.contextPath() + '/rest/jsincluder/1.0/configuration/script'
        });

        var BindingCollection = Backbone.Collection.extend({
            model: Binding,
            initialize: function(models, options) {
                this.scriptId = options.scriptId;
            },
            url: function() {
                return AJS.contextPath() + '/rest/jsincluder/1.0/configuration/script/' + this.scriptId + '/binding';
            }
        });

        /* Instances */
        var scriptCollection = new ScriptCollection();

        /* View */
        var MainView = Backbone.View.extend({
            el: '#content',
            events: {
                'click #jsincluder-addScript': 'showAddScriptDialog',
                'click .jsincluder-editScript': 'showEditScriptDialog',
                'click .jsincluder-deleteScript': 'showDeleteScriptDialog',
                'click .jsincluder-disableScript': 'disableScript',
                'click .jsincluder-expandBindings': 'expandBindings',
                'input #jsincluder-filters-project-name': 'debouncedFilterScripts',
                'change #jsincluder-filter-context': 'debouncedFilterScripts',
                'change #jsincluder-filter-issueTypes': 'debouncedFilterScripts',
                'change #jsincluder-filter-project': 'debouncedFilterScripts',
            },
            issueTypesFilterOptions: [],
            projectsFilterOptions: [{id: -1,text:"All projects", name: "All projects", avatarUrl: ""}],
            initialize: function() {
                this.userData = {};
                this.collection.on('request', this.startLoadingScriptsCallback);
                this.collection.on('sync', this.finishLoadingScriptsCallback);
                this.collection.on('add', this._addScript, this);
                this.collection.on('change', this._changeScript, this);
                this.collection.on('remove', this._removeScript, this);
                var jsincluderFilters = $(".jsincluder-filters");
                this._initProjectField(jsincluderFilters);
                this._initIssueTypesField(jsincluderFilters);
                this._initContexSelect(jsincluderFilters);
                this.debouncedFilterScripts = _.debounce(this._filterScripts.bind(this), 100);
            },
            startLoadingScriptsCallback: function() {
                AJS.dim();
                JIRA.Loading.showLoadingIndicator();
            },
            finishLoadingScriptsCallback: function() {
                JIRA.Loading.hideLoadingIndicator();
                AJS.undim();
            },
            showAddScriptDialog : function(e) {
                e.preventDefault();

                var configurationDialogView = new ConfigurationDialog({
                    model: new ScriptView(),
                    collection: new BindingCollection([], {}),
                    scripts: mainView.collection
                });
                configurationDialogView.show();
            },
            showEditScriptDialog: function(e){
                e.preventDefault();

                var scriptId = $(e.currentTarget).parents('.jsincluder-script').attr('id');
                var scriptView = new ScriptView({id: scriptId});
                var bindingCollection = new BindingCollection([], {scriptId: scriptId});
                bindingCollection.fetch({
                    success: function(collection) {
                        scriptView.fetch({
                            success: $.proxy(function(model) {
                                var configurationDialogView = new ConfigurationDialog({
                                    model: model,
                                    collection: collection,
                                    scripts: mainView.collection
                                });
                                configurationDialogView.show();
                            }, this),
                            error: function(request) {
                                console.error("JsIncluder", request.responseText);
                            }
                        });
                    },
                    error: function(request) {
                        console.error("JsIncluder", request.responseText);
                    }
                });
            },
            showDeleteScriptDialog: function(e) {
                e.preventDefault();

                var script = this.collection.get($(e.currentTarget).parents('.jsincluder-script').attr('id'));
                var confirmDialog = new ConfirmDialog({
                    okText: AJS.I18n.getText('common.words.delete'),
                    header: AJS.I18n.getText('ru.mail.jira.plugins.jsincluder.configuration.deleteScript'),
                    text: AJS.format('<p>{0}</p>', AJS.I18n.getText('ru.mail.jira.plugins.jsincluder.configuration.deleteScript.confirmation', '<b>' + script.get('name') + '</b>')),
                    okHandler: $.proxy(function() {
                        $.ajax({
                            url: AJS.contextPath() + '/rest/jsincluder/1.0/configuration/script/' + script.id,
                            type: 'DELETE',
                            error: $.proxy(function(xhr) {
                                console.error("JsIncluder", xhr.responseText || 'Internal error');
                            }, this),
                            success: $.proxy(function() {
                                this.collection.remove(script);
                            }, this)
                        });
                    }, this)
                });

                confirmDialog.show();
            },
            disableScript: function(e) {
                var $toggleButton = $(e.currentTarget);
                var script = this.collection.get($toggleButton.parents('.jsincluder-script').attr('id'));
                $toggleButton.busy = true;
                var scriptView = new ScriptView({id: script.id});
                scriptView.save(
                    {
                        name: script.attributes.name,
                        code: script.attributes.code,
                        css: script.attributes.css,
                        bindings: script.attributes.bindings,
                        disabled: !script.attributes.disabled
                    },
                    {
                        success: $.proxy(function(response) {
                            this.collection.add(response, {merge: true});
                        }, this),
                        error: $.proxy(this._ajaxErrorHandler, this),
                        complete: $.proxy(function() {
                            $toggleButton.busy = false;
                        }, this)
                    }
                );
            },
            expandBindings: function(e) {
                e.preventDefault();

                var script = this.collection.get($(e.currentTarget).parents('.jsincluder-script').attr('id'));
                this.userData[script.id].expandBindings = !this.userData[script.id].expandBindings;
                this._changeScript(script);
            },
            _addScript: function(script) {
                this.userData[script.id] = {expandBindings: true};
                $('#jsincluder-scripts').append(JIRA.Templates.Plugins.JsIncluder.scriptEntry({script: script.toJSON(), expandBindings: true}));
                this._addIssueTypesFilter(script);
                this._addProjectFilter(script);
            },
            _changeScript: function(script) {
                $('#jsincluder-scripts tr[id="' + script.id + '"]').replaceWith(JIRA.Templates.Plugins.JsIncluder.scriptEntry({script: script.toJSON(), expandBindings: this.userData[script.id].expandBindings}));
            },
            _removeScript: function(script) {
                $('#jsincluder-scripts tr[id="' + script.id + '"]').remove();
            },
            _addIssueTypesFilter(script) {
                var canAdd = true;
                script.attributes.bindings.forEach((binding) => {
                    binding.issueTypes.forEach(bindingIssueType => {
                        canAdd = true;
                        this.issueTypesFilterOptions.forEach((issueType) => {
                            if (bindingIssueType.id === issueType.id &&
                                bindingIssueType.name === issueType.name &&
                                bindingIssueType.iconUrl === issueType.iconUrl) {
                                canAdd = false;
                            }
                        })
                        if (canAdd)
                            this.issueTypesFilterOptions.push({
                                    id: bindingIssueType.id,
                                    text: bindingIssueType.name,
                                    name: bindingIssueType.name,
                                    iconUrl: bindingIssueType.iconUrl});
                    });
                });
            },
            _addProjectFilter(script) {
                var canAdd = true;
                script.attributes.bindings.forEach((binding) => {
                    this.projectsFilterOptions.forEach((project) => {
                        if(binding.project !== undefined) {
                            if (binding.project.id === project.id &&
                                binding.project.name === project.name &&
                                binding.project.avatarUrl === project.avatarUrl) {
                                canAdd = false;
                            }
                        }
                    })
                    if(canAdd && binding.project !== undefined)
                        this.projectsFilterOptions.push({
                            id: binding.project.id,
                            text: binding.project.name,
                            name: binding.project.name,
                            avatarUrl: binding.project.avatarUrl});
                });
            },
            _filterScripts: function (event) {
                switch (event.target.id){
                    case 'jsincluder-filters-project-name':
                        Object.assign(filters, {projectName: event.target.value});
                        break;
                    case 'jsincluder-filter-context':
                        Object.assign(filters,  {contextSelected: event.added});
                        break;
                    case 'jsincluder-filter-issueTypes':
                        Object.assign(filters,  {issueType:event.val});
                        break;
                    case 'jsincluder-filter-project':
                        Object.assign(filters,  {projectId:event.val});
                        break;
                }
                if(this.collection && this.collection.models) {
                    this.collection.each(function(model) {
                        var attributes = model.attributes;
                        var hideResult = false;
                        if(this._scriptNameFilter(filters.projectName,attributes.name)) {
                            hideResult = true;
                        } else {
                            hideResult = false;
                            var bindings = attributes.bindings;
                            bindings.forEach( (binding) => {
                                hideResult = this._projectFilter(filters.projectId, binding)
                                    || this._contextFilter(filters.contextSelected, binding.enabledContexts)
                                    || this._issueTypesFilter(filters.issueType, binding);
                            })
                        }
                        if(hideResult) {
                            $('#jsincluder-scripts tr[id="' + attributes.id + '"]').hide();
                        } else {
                            $('#jsincluder-scripts tr[id="' + attributes.id + '"]').show();
                        }
                    },{
                        _scriptNameFilter:this._scriptNameFilter,
                        _projectFilter:this._projectFilter,
                        _issueTypesFilter:this._issueTypesFilter,
                        _contextFilter:this._contextFilter,
                    });
                }

            },
            _scriptNameFilter: function (neededProjectName, projectName){
                return neededProjectName !== undefined
                    && neededProjectName !== ""
                    && !projectName.toLowerCase().includes(neededProjectName.toLowerCase());
            },
            _projectFilter: function (projectId, binding){
                if(binding && binding.project){
                    return projectId !== undefined && projectId !== "" && projectId !== "-1" && binding.project.id !== parseInt(projectId);
                } else return false;
            },
            _issueTypesFilter: function (issueType, binding) {
                if(issueType !== undefined && issueType !== "") {
                    var result = true;
                    if (binding && _.isArray(binding.issueTypes)) {
                        if(binding.issueTypes.length === 0) return false;
                        binding.issueTypes.forEach(bindingIssueType => {
                            if (issueType === bindingIssueType.id) {
                                result = false;
                            }
                        });
                    }
                    return result;
                } else return false;
            },
            _contextFilter: function (contextSelected,enabledContexts){
                return contextSelected !== undefined
                    && contextSelected.text !== "Select context"
                    && !enabledContexts.includes(contextSelected.text)
            },
            _initProjectField: function($row) {
                $row.find('#jsincluder-filter-project').auiSelect2({
                    placeholder: AJS.I18n.getText('common.words.project'),
                    allowClear: true,
                    multiple: false,
                    data: this.projectsFilterOptions,
                    dropdownAutoWidth: false,
                    formatResult: function(project) {
                        return JIRA.Templates.Plugins.JsIncluder.projectField({
                            project: project
                        });
                    },
                    formatSelection: function(project) {
                        return JIRA.Templates.Plugins.JsIncluder.projectField({
                            project: project
                        });
                    }
                });
            },
            _initIssueTypesField: function($row) {
                $row.find("#jsincluder-filter-issueTypes").auiSelect2({
                    placeholder: AJS.I18n.getText('ru.mail.jira.plugins.jsincluder.configuration.tab.bindings.issueTypes.all'),
                    allowClear: true,
                    multiple: false,
                    data: this.issueTypesFilterOptions,
                    dropdownAutoWidth: false,
                    formatResult: function (issueType) {
                        return JIRA.Templates.Plugins.JsIncluder.issueTypeField({
                            issueType: issueType,
                        });
                    },
                    formatSelection: function (issueType) {
                        return JIRA.Templates.Plugins.JsIncluder.issueTypeField({
                            issueType: issueType,
                        });
                    }
                });
            },
            _initContexSelect: function($row) {
                $row.find("#jsincluder-filter-context").auiSelect2({
                    placeholder: AJS.I18n.getText( "ru.mail.jira.plugins.jsincluder.filter.select.context" ),
                    dropdownAutoWidth: false,
                    allowClear: true,
                    data: [{id:0,text:'Create'},{id:1,text:'View'},{id:2,text:'Edit'},{id:3,text:'Transition'}],
                });
            }

        });

        var mainView = new MainView({collection: scriptCollection});

        /* Fetch data */
        scriptCollection.fetch();
    });
});
