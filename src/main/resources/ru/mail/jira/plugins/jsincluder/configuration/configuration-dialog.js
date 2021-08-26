define('jsincluder/configuration-dialog', ['jquery', 'underscore', 'backbone'], function($, _, Backbone) {
    var editorJS;
    var editorCSS;

    var allProjects = {id: -1, name: AJS.I18n.getText('ru.mail.jira.plugins.jsincluder.configuration.tab.bindings.project.all')};
    return Backbone.View.extend({
        el: '#jsincluder-configuration-dialog',
        events: {
            'click #jsincluder-configuration-dialog-ok': '_submit',
            'click #jsincluder-configuration-dialog-cancel': 'hide',
            'click a[href=#jsincluder-configuration-dialog-general-tab]': '_selectGeneralTab',
            'click .jsincluder-configuration-dialog-code-item-name': '_toggleCodeField',
            'click a[href=#jsincluder-configuration-dialog-bindings-tab]': '_selectBindingsTab',
            'click #jsincluder-add-binding button': '_addNewBinding',
            'click #jsincluder-binding-create': '_createBinding',
            'click #jsincluder-binding-create-cancel': '_cancelCreateBinding',
            'click .jsincluder-binding-editable-field': '_editBinding',
            'click .jsincluder-binding-edit-save': '_saveEditBinding',
            'click .jsincluder-binding-edit-cancel': '_cancelEditBinding',
            'click .jsincluder-binding-delete': '_removeBinding',
            'change .jsincluder-binding-project': '_changeProjectField',
            'change .jsincluder-binding-view input.checkbox': '_updateContextBinding'
        },
        initialize: function(options) {
            this.dialog = AJS.dialog2('#jsincluder-configuration-dialog');
            this.$okButton = this.$('#jsincluder-configuration-dialog-ok');
            this.$cancelButton = this.$('#jsincluder-configuration-dialog-cancel');
            this.scripts = options.scripts;
            this.updateHints = _.debounce(this._updateHints.bind(this),100);
            this._fillForm();

            this.dialog.on('hide', $.proxy(this.destroy, this));
        },
        destroy: function() {
            this.stopListening();
            this.undelegateEvents();
            this.$('form').off();
            this.dialog.off();

            this.$okButton.removeAttr('disabled');
            this.$cancelButton.removeAttr('disabled');

            this.$('#jsincluder-configuration-dialog-name').val('');
            this.$('#jsincluder-configuration-dialog-code').val('');
            this.$('div.CodeMirror').remove();
            this.$('#jsincluder-configuration-dialog-bindings-table tbody tr').remove();
            this.$('div.error').text('').addClass('hidden');
            this.$('.jsincluder-configuration-dialog-error-panel').text('').addClass('hidden');
        },
        show: function() {
            this.dialog.show();
            this._selectGeneralTab();
        },
        hide: function() {
            this.dialog.hide();
        },
        _initProjectField: function($row) {
            $row.find('.jsincluder-binding-project').auiSelect2({
                placeholder: AJS.I18n.getText('common.words.project'),
                ajax: {
                    url: AJS.contextPath() + '/rest/jsincluder/1.0/configuration/project',
                    dataType: 'json',
                    data: function(filter) {
                        return {
                            filter: filter
                        };
                    },
                    results: function(data) {
                        results = [{
                            name: "Projects",
                            children: [allProjects]
                        }];

                        if (data.projects) {
                            results[0].children.push(...data.projects.map(c => {
                                c.type = "project";
                                return c;
                            }));
                        }

                        if (data.categories) {
                            results.push({
                                name: "Categories",
                                children: data.categories.map(c => {
                                    c.type = "category";
                                    return c;
                                })
                            });
                        }
                        return {
                            results: results
                        };
                    },
                    cache: true
                },
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
        _changeProjectField: function() {
            var projectData = this.$('.jsincluder-binding-project').auiSelect2('data');
            var projectId = projectData.id != -1? projectData.id : '';
            $.ajax({
                url: AJS.contextPath() + '/rest/jsincluder/1.0/configuration/issuetype?projectId=' + projectId,
                data: {
                    filter: ''
                },
                type: 'GET',
                error: $.proxy(function(xhr) {
                    console.error("JsIncluder", xhr.responseText || 'Internal error');
                }, this),
                success: function(data) {
                    var $issueTypeField = $('.jsincluder-binding-issueTypes');
                    var selectedOldData = $issueTypeField.auiSelect2('data');
                    var selectedData = [];
                    for (var i = 0; i < selectedOldData.length; i++)
                        for (var j = 0; j < data.length; j ++) {
                            if (selectedOldData[i].id == data[j].id) {
                                selectedData.push(selectedOldData[i]);
                                break;
                            }
                        }
                    $issueTypeField.auiSelect2('data', selectedData)
                }
            });
        },
        _initIssueTypesField: function($row) {
            $row.find('.jsincluder-binding-issueTypes').auiSelect2({
                placeholder: AJS.I18n.getText('ru.mail.jira.plugins.jsincluder.configuration.tab.bindings.issueTypes.all'),
                allowClear: true,
                multiple: true,
                ajax: {
                    url: function() {
                        var projectData = $row.find('.jsincluder-binding-project').auiSelect2('data');
                        var projectId = projectData.id != -1? projectData.id : '';
                        var url = AJS.contextPath() + '/rest/jsincluder/1.0/configuration/issuetype';
                        url += (projectData.description ? '?projectCategoryId=' : '?projectId=') + projectId;
                        return url;
                    },
                    dataType: 'json',
                    data: function(filter) {
                        return {
                            filter: filter
                        };
                    },
                    results: function(data) {
                        return {
                            results: data
                        };
                    },
                    cache: true
                },
                dropdownAutoWidth: false,
                formatResult: function(issueType) {
                    return JIRA.Templates.Plugins.JsIncluder.issueTypeField({
                        issueType: issueType
                    });
                },
                formatSelection: function(issueType) {
                    return JIRA.Templates.Plugins.JsIncluder.issueTypeField({
                        issueType: issueType
                    });
                }
            });
        },
        _fillForm: function() {
            if (this.model.id !== undefined) {
                this.$('.aui-dialog2-header-main').text(AJS.I18n.getText('ru.mail.jira.plugins.jsincluder.configuration.editScript'));
                this.$okButton.text(AJS.I18n.getText('common.words.update'));

                this.$('#jsincluder-configuration-dialog-name').val(this.model.get('name'));
                this.$('#jsincluder-configuration-dialog-code-js').val(this.model.get('code'));
                this.$('#jsincluder-configuration-dialog-code-css').val(this.model.get('css'));
                var htmlBindings = '';
                this.collection.each(function(binding) {
                    htmlBindings += JIRA.Templates.Plugins.JsIncluder.bindingEntry({
                        binding: binding.toJSON()
                    });
                });
                if (htmlBindings.length)
                    $('#jsincluder-configuration-dialog-bindings-table tbody').append(htmlBindings);
            } else {
                var i = 1;
                while(true) {
                    if (this.scripts.findWhere({name: AJS.I18n.getText('ru.mail.jira.plugins.jsincluder.configuration.script.default', i)}) != null) {
                        i++;
                        continue;
                    }
                    break;
                }
                this.$('#jsincluder-configuration-dialog-name').val(AJS.I18n.getText('ru.mail.jira.plugins.jsincluder.configuration.script.default', i));
                this.$('#jsincluder-configuration-dialog-code-js').val(AJS.I18n.getText('ru.mail.jira.plugins.jsincluder.configuration.script.code.default'));
                this.$('#jsincluder-configuration-dialog-code-css').val('');
                this.$('.aui-dialog2-header-main').text(AJS.I18n.getText('ru.mail.jira.plugins.jsincluder.configuration.createScript'));
                this.$okButton.text(AJS.I18n.getText('common.words.create'));
            }
        },
        _submit: function(e) {
            e.preventDefault();

            this.$okButton.attr('disabled', 'disabled');
            this.$cancelButton.attr('disabled', 'disabled');
            this.model.save(this._serializeScript(), {
                success: $.proxy(this._ajaxSuccessHandler, this),
                error: $.proxy(this._ajaxErrorHandler, this)
            });
        },
        _serializeScript: function() {
            var name = this.$('#jsincluder-configuration-dialog-name').val();
            var code = editorJS !== undefined ? editorJS.getValue(): '';
            var css = editorCSS !== undefined ? editorCSS.getValue(): '';
            return {
                name: name,
                code: code,
                css: css,
                bindings: this.collection
            };
        },
        _serializeBinding: function(id, $row) {
            var target = $row.find('.jsincluder-binding-project').auiSelect2('data').id != -1? $row.find('.jsincluder-binding-project').auiSelect2('data') : null;
            var issueTypes = $row.find('.jsincluder-binding-issueTypes').auiSelect2('data');
            var createContextEnabled = $row.find('input.jsincluder-binding-createContextEnabled:checked').length ? true : false;
            var editContextEnabled = $row.find('input.jsincluder-binding-editContextEnabled:checked').length ? true : false;
            var viewContextEnabled = $row.find('input.jsincluder-binding-viewContextEnabled:checked').length ? true : false;
            var transitionContextEnabled = $row.find('input.jsincluder-binding-transitionContextEnabled:checked').length ? true : false;
            const bindingType = target ?  target.type : null;
            const res = {
                id: id,
                issueTypes: issueTypes,
                createContextEnabled: createContextEnabled,
                editContextEnabled: editContextEnabled,
                viewContextEnabled: viewContextEnabled,
                transitionContextEnabled: transitionContextEnabled
            }
            switch (bindingType) {
                case "project":
                    res.project = {...target};
                    res.projectCategory = null;
                    delete res.project.type;
                    break;

                case "category":
                    res.projectCategory = {...target};
                    res.project = null;
                    delete res.projectCategory.type;
                    break;
            }
            return res;
        },
        _serializeBindingWithCollection: function(binding) {
            return {
                id: binding.id,
                project:  binding.get('project'),
                projectCategory: binding.get('projectCategory'),
                issueTypes: binding.get('issueTypes'),
                createContextEnabled: binding.get('createContextEnabled'),
                editContextEnabled: binding.get('editContextEnabled'),
                viewContextEnabled: binding.get('viewContextEnabled'),
                transitionContextEnabled: binding.get('transitionContextEnabled')
            }
        },
        _ajaxSuccessHandler: function(model, response) {
            this.scripts.add(response, {merge: true});
            this.model.trigger('change', this.model);
            this.$okButton.removeAttr('disabled');
            this.$cancelButton.removeAttr('disabled');
            this.hide();
        },
        _ajaxErrorHandler: function(model, response) {
            this._selectGeneralTab();
            var field = response.getResponseHeader('X-Atlassian-Rest-Exception-Field');
            if (field) {
                this.$('#jsincluder-configuration-dialog-' + field + '-error').removeClass('hidden').text(response.responseText);
                if (field == 'name')
                    this.$('#jsincluder-configuration-dialog-' + field).focus();
                else if (field == 'code') {
                    editorJS.focus();
                } else if (field == 'binding') {
                    this._selectBindingsTab();
                    this.$('#jsincluder-configuration-dialog-bindings-tab .jsincluder-configuration-dialog-error-panel').removeClass('hidden').text(response.responseText);
                }
            } else
                this.$('#jsincluder-configuration-dialog-general-tab .jsincluder-configuration-dialog-error-panel').removeClass('hidden').text(response.responseText);
            this.$okButton.removeAttr('disabled');
            this.$cancelButton.removeAttr('disabled');
        },
        _selectGeneralTab: function(e) {
            e && e.preventDefault();
            this.$('#jsincluder-configuration-dialog-bindings-tab').toggleClass('hidden', true);
            this.$('a[href=#jsincluder-configuration-dialog-bindings-tab]').closest('li').toggleClass('aui-nav-selected', false);
            this.$('#jsincluder-configuration-dialog-general-tab').toggleClass('hidden', false);
            this.$('a[href=#jsincluder-configuration-dialog-general-tab]').closest('li').toggleClass('aui-nav-selected', true);
            this.$('#jsincluder-configuration-dialog-name').focus();

            this.$('.jsincluder-configuration-dialog-code-item').each((function(index, element) {
                var $codeFieldInputContainer = $(element).find('.jsincluder-configuration-dialog-code-item-input');
                this._initCodeFieldInput($codeFieldInputContainer);
            }).bind(this))
        },
        _initCodeMirrorEditor: function(textarea, mode) {
            return CodeMirror.fromTextArea(textarea, {
                autofocus: true,
                lineNumbers: true,
                lint: true,
                gutters: ["CodeMirror-lint-markers"],
                mode: mode,
                matchBrackets: true,
                indentWithTabs: true,
                tabMode: "shift"
            });
        },
        _zoomDialog: function($zoomedEditorWrapper, codemirrorEditor) {
            if($zoomedEditorWrapper && codemirrorEditor) {
                var body = $('body');
                var CodeMirrorFullscreenTop = $('.CodeMirror-fullscreen-top');
                var CodeMirrorFullscreenBottom = $('.CodeMirror-fullscreen-bottom');
                if(CodeMirrorFullscreenTop.length === 0) {
                    body.append('<div class="CodeMirror-fullscreen-top"><span class="aui-icon aui-icon-small aui-iconfont-vid-full-screen-off"></span></div>');
                    CodeMirrorFullscreenTop = $('.CodeMirror-fullscreen-top');
                }
                if(CodeMirrorFullscreenBottom.length === 0) {
                    body.append('<div class="CodeMirror-fullscreen-bottom"></div>');
                    CodeMirrorFullscreenBottom = $('.CodeMirror-fullscreen-bottom')
                }


                $zoomedEditorWrapper.after('<div id="zoomedEditorAnchor"></div>');
                $zoomedEditorWrapper.attr('id', 'zoomedEditorAnchorEditor');
                body.append($zoomedEditorWrapper);
                CodeMirrorFullscreenTop.show();

                $(".CodeMirror-fullscreen-bottom").show();
                codemirrorEditor.setOption("fullScreen", true);

                CodeMirrorFullscreenTop.find(".aui-iconfont-vid-full-screen-off").off("click");
                CodeMirrorFullscreenTop.find(".aui-iconfont-vid-full-screen-off").on("click", function () {
                    var zoomedEditorAnchor = $("#zoomedEditorAnchor");
                    if (zoomedEditorAnchor.length > 0) {
                        codemirrorEditor.setOption("fullScreen", false);
                        var zoomedEditorAnchorEditor = $("#zoomedEditorAnchorEditor");
                        zoomedEditorAnchorEditor.removeAttr('id');
                        zoomedEditorAnchor.replaceWith(zoomedEditorAnchorEditor);
                        CodeMirrorFullscreenTop.hide();
                        CodeMirrorFullscreenBottom.hide();
                        $(codemirrorEditor.getWrapperElement()).css("resize", "vertical");
                    }
                });
                $(codemirrorEditor.getWrapperElement()).css("resize", "none");
            }
        },
        _initCodeFieldInput: function($codeFieldInputContainer) {
            if (!$codeFieldInputContainer.hasClass('hidden') && !$codeFieldInputContainer.find('div.CodeMirror').length) {
                var textarea = $codeFieldInputContainer.find('.jsincluder-configuration-dialog-code').get(0);
                if ($codeFieldInputContainer.hasClass('css-field')) {
                    editorCSS = this._initCodeMirrorEditor(textarea, 'css');
                    editorCSS.on("change", this.updateHints);
                    $codeFieldInputContainer.parent().find(".zoom-button").on("click", {zoomDialog:this._zoomDialog}, function (event) {
                        event.data.zoomDialog($(event.target).parents(".jsincluder-configuration-dialog-code-item"),editorCSS);
                    });
                } else {
                    editorJS = this._initCodeMirrorEditor(textarea, 'javascript');
                    editorJS.on("change", this.updateHints);
                    $codeFieldInputContainer.parent().find(".zoom-button").on("click", {zoomDialog:this._zoomDialog}, function (event) {
                        event.data.zoomDialog($(event.target).parents(".jsincluder-configuration-dialog-code-item"),editorJS);
                    });
                }
                this.updateHints();
            } else {
                if ($codeFieldInputContainer.hasClass('css-field')) {
                    $codeFieldInputContainer.parent().find("#error-tooltip-css").hide();
                } else {
                    $codeFieldInputContainer.parent().find("#error-tooltip-js").hide();
                }
            }
        },
        _toggleCodeField: function(e) {
            e && e.preventDefault();
            var $codeField = $(e.target).parents('div.jsincluder-configuration-dialog-code-item');
            var $codeFieldNameContainer = $codeField.find('.jsincluder-configuration-dialog-code-item-name');
            var $codeFieldInputContainer = $codeField.find('.jsincluder-configuration-dialog-code-item-input');

            var isCodeFieldHidden = $codeFieldInputContainer.hasClass('hidden');
            $codeFieldNameContainer.toggleClass('expanded', isCodeFieldHidden);
            $codeFieldNameContainer.find(".jsincluder-configuration-dialog-code-item-left-part>.aui-icon").toggleClass('aui-iconfont-chevron-right', !isCodeFieldHidden).toggleClass('aui-iconfont-chevron-down', isCodeFieldHidden);
            $codeFieldInputContainer.toggleClass('hidden', !isCodeFieldHidden);
            $codeField.find(".zoom-button").toggleClass('hidden', !isCodeFieldHidden);
            this._initCodeFieldInput($codeFieldInputContainer);
        },
        _selectBindingsTab: function(e) {
            e && e.preventDefault();
            this.$('#jsincluder-configuration-dialog-bindings-tab').toggleClass('hidden', false);
            this.$('a[href=#jsincluder-configuration-dialog-bindings-tab]').closest('li').toggleClass('aui-nav-selected', true);
            this.$('#jsincluder-configuration-dialog-general-tab').toggleClass('hidden', true);
            this.$('a[href=#jsincluder-configuration-dialog-general-tab]').closest('li').toggleClass('aui-nav-selected', false);
        },
        _addNewBinding: function(e) {
            e && e.preventDefault();
            this.$('button.jsincluder-binding-edit-save').click();
            if (!this.$('#jsincluder-add-binding-row').length) {
                $('#jsincluder-configuration-dialog-bindings-table tbody').append(JIRA.Templates.Plugins.JsIncluder.bindingAddEntry());
                var $row = $("#jsincluder-add-binding-row");
                this._initProjectField($row);
                this._initIssueTypesField($row);
                $row.find('.jsincluder-binding-project').auiSelect2('data' , allProjects);
                $row.find('.jsincluder-binding-project').auiSelect2('focus');
            } else
                this.$('#jsincluder-add-binding-row').find('.jsincluder-binding-project').auiSelect2('focus');
        },
        _createBinding: function(e) {
            e && e.preventDefault();
            var $row = $(e.target).parents('tr');
            var id = $row.data('id');
            if (!id.length)
                id = 'temp_' + new Date().getTime();
            this.collection.add(this._serializeBinding(id,$row));
            $row.replaceWith(JIRA.Templates.Plugins.JsIncluder.bindingEntry({
                binding: this._serializeBinding(id, $row)
            }));
        },
        _cancelCreateBinding: function(e) {
            e && e.preventDefault();
            this.$('#jsincluder-add-binding-row').remove();
        },
        _editBinding: function(e) {
            e && e.preventDefault();
            this.$('button.jsincluder-binding-edit-save').click();
            this.$('#jsincluder-binding-create').click();

            var $row = $(e.target).parents('tr');
            var bindingId = $row.data('id');
            var binding = this.collection.get(bindingId);

            $row.replaceWith(JIRA.Templates.Plugins.JsIncluder.bindingEditEntry({
                binding: this._serializeBindingWithCollection(binding)
            }));
            var $addedRow = this.$('tr[data-id="' + bindingId + '"]');

            this._initProjectField($addedRow);
            this._initIssueTypesField($addedRow);
            var project = binding.get('project');
            var projectCategory = binding.get('projectCategory');
            if (project) {
                $addedRow.find('.jsincluder-binding-project').auiSelect2('data' , {id: project.id, key: project.key, name: project.name, avatarUrl: project.avatarUrl});
            }
            else if (projectCategory) {
                $addedRow.find('.jsincluder-binding-project').auiSelect2('data' , {id: projectCategory.id, name: projectCategory.name, description: projectCategory.description});
            }
            else
                $addedRow.find('.jsincluder-binding-project').auiSelect2('data' , allProjects);
            var issueTypes = [];
            binding.get('issueTypes').forEach(function(issueType) {
                issueTypes.push({id: issueType.id, name: issueType.name, iconUrl: issueType.iconUrl});
            });
            $addedRow.find('.jsincluder-binding-issueTypes').auiSelect2('data' , issueTypes);
        },
        _saveEditBinding: function(e) {
            e && e.preventDefault();
            var $row = $(e.target).parents('tr');
            this.collection.add(this._serializeBinding($row.data('id'), $row), {merge: true});
            $row.replaceWith(JIRA.Templates.Plugins.JsIncluder.bindingEntry({
                binding: this._serializeBindingWithCollection(this.collection.get($row.data('id')))
            }));
        },
        _cancelEditBinding: function(e) {
            e && e.preventDefault();
            var $row = $(e.target).parents('tr');
            $row.replaceWith(JIRA.Templates.Plugins.JsIncluder.bindingEntry({
                binding: this._serializeBindingWithCollection(this.collection.get($row.data('id')))
            }));
        },
        _removeBinding: function(e) {
            e && e.preventDefault();
            var $row = $(e.target).parents('tr');
            this.collection.remove(this.collection.get($row.data('id')));
            $row.remove();
        },
        _updateContextBinding: function(e) {
            e && e.preventDefault();
            this.$('button.jsincluder-binding-edit-save').click();
            this.$('#jsincluder-binding-create').click();
            var $row = $(e.target).parents('tr');
            var createContextEnabled = $row.find('input.jsincluder-binding-createContextEnabled:checked').length ? true : false;
            var editContextEnabled = $row.find('input.jsincluder-binding-editContextEnabled:checked').length ? true : false;
            var viewContextEnabled = $row.find('input.jsincluder-binding-viewContextEnabled:checked').length ? true : false;
            var transitionContextEnabled = $row.find('input.jsincluder-binding-transitionContextEnabled:checked').length ? true : false;

            var binding = this.collection.get($row.data('id'));
            binding.set('createContextEnabled', createContextEnabled);
            binding.set('editContextEnabled', editContextEnabled);
            binding.set('viewContextEnabled', viewContextEnabled);
            binding.set('transitionContextEnabled', transitionContextEnabled);
        },

        _updateHints: function () {
            var i = 0;
            var tooltipError = {
                hasCssError: false,
                hasJsError:false,
                jsErrorText: "",
                cssErrorText: ""
            };
            if (editorJS) {
                editorJS.operation(function () {
                    JSHINT(editorJS.getValue());
                    var jsTooltip = $("#error-tooltip-js");
                    for (i = 0; i < JSHINT.errors.length; ++i) {
                        var err = JSHINT.errors[i];
                        if (!err) continue;
                        if (typeof err.code === 'string' && err.code.includes('E')) {
                            tooltipError.hasJsError = true;
                            tooltipError.jsErrorText = "JS: Line: " + err.line + " Column: " + err.character + " " + err.reason + "\n";
                            jsTooltip.attr('title', tooltipError.jsErrorText);
                            jsTooltip.show();
                            break;
                        }
                    }
                    if(!tooltipError.hasJsError){
                        jsTooltip.hide();
                    }
                    if(editorJS.getOption("fullScreen")) {
                        var codeMirrorFullscreenBottom = $(".CodeMirror-fullscreen-bottom");
                        codeMirrorFullscreenBottom.html('<span class="aui-icon aui-icon-small aui-icon-wait aui-iconfont-error js-includer-error CodeMirror-fullscreen-bottom-text"></span><span class="CodeMirror-fullscreen-bottom-text"></span>');
                        if(tooltipError.hasJsError) {
                            codeMirrorFullscreenBottom = codeMirrorFullscreenBottom.find(".CodeMirror-fullscreen-bottom-text");
                            codeMirrorFullscreenBottom.text(tooltipError.jsErrorText);
                            codeMirrorFullscreenBottom.attr('title', tooltipError.jsErrorText);
                        } else {
                            codeMirrorFullscreenBottom.text("");
                        }
                    }
                });
            }
            if(editorCSS) {
                editorCSS.operation(function () {
                    var cssRules = CSSLint.verify(editorCSS.getValue());
                    if (cssRules && cssRules.messages) {
                        var cssTooltip = $("#error-tooltip-css");
                        for (i = 0; i < cssRules.messages.length; i++) {
                            if (cssRules.messages[i].type === "error") {
                                tooltipError.hasCssError = true;
                                tooltipError.cssErrorText = "CSS: " + cssRules.messages[i].message + "\n";
                                cssTooltip.attr('title', tooltipError.cssErrorText);
                                cssTooltip.show();
                                break;
                            }
                        }
                        if(!tooltipError.hasCssError) {
                            cssTooltip.hide();
                        }
                        if(editorCSS.getOption("fullScreen")) {
                            var codeMirrorFullscreenBottom = $(".CodeMirror-fullscreen-bottom");
                            codeMirrorFullscreenBottom.html('<span class="aui-icon aui-icon-small aui-icon-wait aui-iconfont-error js-includer-error CodeMirror-fullscreen-bottom-text"></span><span class="CodeMirror-fullscreen-bottom-text"></span>');
                            if(tooltipError.hasCssError) {
                                codeMirrorFullscreenBottom = codeMirrorFullscreenBottom.find(".CodeMirror-fullscreen-bottom-text");
                                codeMirrorFullscreenBottom.text(tooltipError.cssErrorText);
                                codeMirrorFullscreenBottom.attr('title', tooltipError.cssErrorText);
                            } else {
                                codeMirrorFullscreenBottom.text("");
                            }
                        }
                    }
                });
            }
            if (tooltipError.hasCssError || tooltipError.hasJsError) {
                this.$okButton.attr('disabled', 'disabled');
                var submitTooltip = $("#submit-tooltip");
                submitTooltip.attr('title', tooltipError.jsErrorText + tooltipError.cssErrorText);
                submitTooltip.show();
            } else {
                tooltipError.cssErrorText="";
                tooltipError.jsErrorText="";
                this.$okButton.removeAttr('disabled');
                $("#submit-tooltip").hide();
            }
        }
    });
});
