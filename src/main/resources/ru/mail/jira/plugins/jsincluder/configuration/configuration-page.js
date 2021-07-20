require(['jquery', 'backbone', 'jsincluder/configuration-dialog', 'jsincluder/confirm-dialog'], function($, Backbone, ConfigurationDialog, ConfirmDialog) {
    AJS.toInit(function() {
        /* Models */
        var Script = Backbone.Model.extend();
        var ScriptView = Backbone.Model.extend({urlRoot: AJS.contextPath() + '/rest/jsincluder/1.0/configuration/script/'});
        var Binding = Backbone.Model.extend();

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
                'click .jsincluder-expandBindings': 'expandBindings'
            },
            initialize: function() {
                this.userData = {};
                this.collection.on('request', this.startLoadingScriptsCallback);
                this.collection.on('sync', this.finishLoadingScriptsCallback);
                this.collection.on('add', this._addScript, this);
                this.collection.on('change', this._changeScript, this);
                this.collection.on('remove', this._removeScript, this);
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
            },
            _changeScript: function(script) {
                $('#jsincluder-scripts tr[id="' + script.id + '"]').replaceWith(JIRA.Templates.Plugins.JsIncluder.scriptEntry({script: script.toJSON(), expandBindings: this.userData[script.id].expandBindings}));
            },
            _removeScript: function(script) {
                $('#jsincluder-scripts tr[id="' + script.id + '"]').remove();
            }
        });

        var mainView = new MainView({collection: scriptCollection});

        /* Fetch data */
        scriptCollection.fetch();
    });
});
