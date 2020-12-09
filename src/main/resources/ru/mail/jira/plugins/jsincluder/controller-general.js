var JS_INCLUDER = {
    CONTEXT_CREATE: 'create',
    CONTEXT_VIEW: 'view',
    CONTEXT_EDIT: 'edit',
    CONTEXT_TRANSITION: 'transition',

    _cache: {},
    params: {},
    $contextObject: null,

    _executeJs: function(scripts) {
        for (var i = 0; i < scripts.length; i++)
            try {
                if (scripts[i].code && scripts[i].code.length)
                    eval(scripts[i].code);
            } catch (e) {
                console.error(AJS.format('Script: {0} \n Error: {1}', scripts[i].name, e.message));
                alert(e.message);
            }
    },

    _executeCss: function(scripts) {
        AJS.$(AJS.format('style.jsincluder-css-{0}', JS_INCLUDER.params.context)).remove();
        var css = '';
        for (var i = 0; i < scripts.length; i++) {
            if (scripts[i].css && scripts[i].css.length) {
                css += AJS.format('\n/* Script {0} css rules */\n', scripts[i].name);
                css += scripts[i].css + '\n';
            }
        }
        if (css.length)
            AJS.$(AJS.format('<style class="jsincluder-css-{0}">', JS_INCLUDER.params.context)).text(css).appendTo('head');
    },

    _execute: function(scripts, params, context, $contextObject) {
        JS_INCLUDER.params = params;
        JS_INCLUDER.params.context = context;
        JS_INCLUDER.$contextObject = $contextObject;
        JS_INCLUDER._executeJs(scripts);
        JS_INCLUDER._executeCss(scripts);
    },

    executeCreateScripts: function(projectId, issueTypeId, $contextObject) {
        if(projectId) {
            AJS.$.ajax({
                url: AJS.contextPath() + '/rest/jsincluder/1.0/controller/getCreateScripts',
                data: {
                    projectId: projectId,
                    issueTypeId: issueTypeId
                },
                async: false,
                success: function (data) {
                    JS_INCLUDER._execute(data[JS_INCLUDER.CONTEXT_CREATE], data.params, JS_INCLUDER.CONTEXT_CREATE, $contextObject);
                }
            });
        } else {
            console.error('Error: no projectId',issueTypeId,$contextObject);
        }
    },

    executeIssueScripts: function(issueId, context, $contextObject) {
        if (JS_INCLUDER._cache[issueId] == null) {
            JS_INCLUDER._cache[issueId] = {};
            AJS.$.ajax({
                url: AJS.contextPath() + '/rest/jsincluder/1.0/controller/getIssueScripts',
                data: {
                    issueId: issueId,
                    context: context
                },
                async: false,
                success: function(data) {
                    JS_INCLUDER._cache[issueId][context] = data[context];
                    if (context === JS_INCLUDER.CONTEXT_VIEW) {
                        JS_INCLUDER._cache[issueId][JS_INCLUDER.CONTEXT_EDIT] = data[JS_INCLUDER.CONTEXT_EDIT];
                        JS_INCLUDER._cache[issueId][JS_INCLUDER.CONTEXT_TRANSITION] = data[JS_INCLUDER.CONTEXT_TRANSITION];
                    }
                    JS_INCLUDER._cache[issueId].params = data.params;
                }
            });
        }
        JS_INCLUDER._execute(JS_INCLUDER._cache[issueId][context], JS_INCLUDER._cache[issueId].params, context, $contextObject);
    }
};

require(['jquery', 'wrm/context-path', 'jira/util/formatter', 'jira/util/events', 'jira/util/events/types', 'jira/util/events/reasons'],
    function($, contextPath, formatter, Events, EventsTypes, EventsReasons) {

        AJS.toInit(function() {
            var createSubTaskPageForm = $('#subtask-create-details');
            if (createSubTaskPageForm.length)
                JS_INCLUDER.executeCreateScripts(createSubTaskPageForm.find('input[name="pid"]').val(), createSubTaskPageForm.find('input[name="issuetype"]').val(), $(document));

            var editPageForm = $('#issue-edit');
            if (editPageForm.length)
                JS_INCLUDER.executeIssueScripts(editPageForm.find('input[name="id"]').val(), JS_INCLUDER.CONTEXT_EDIT, $(document));

            var transitionPageForm = $('#issue-workflow-transition');
            if (transitionPageForm.length)
                JS_INCLUDER.executeIssueScripts(transitionPageForm.find('input[name="id"]').val(), JS_INCLUDER.CONTEXT_TRANSITION, $(document));

            Events.bind(EventsTypes.NEW_CONTENT_ADDED, function(e, $context, reason) {
                if (reason === EventsReasons.dialogReady) {
                    if ($context.parent('#create-issue-dialog').length || $context.parent('#create-subtask-dialog').length || $context.parent('#prefillable-create-issue-dialog').length)
                        JS_INCLUDER.executeCreateScripts($context.find('#project').val(), $context.find('#issuetype').val(), $context);

                    if ($context.children('#issue-workflow-transition').length)
                        JS_INCLUDER.executeIssueScripts($context.find('input[name="id"]').val(), JS_INCLUDER.CONTEXT_TRANSITION, $context);
                }
            });
        });

        return JS_INCLUDER;
    });
