(function ($) {
    AJS.toInit(function () {
        var issueId = $('#key-val').attr('rel');
        if (issueId)
            JS_INCLUDER.executeIssueScripts(issueId, JS_INCLUDER.CONTEXT_VIEW, $(document));

        JIRA.bind(JIRA.Events.ISSUE_REFRESHED, function (e, context) {
            JS_INCLUDER.executeIssueScripts(context, JS_INCLUDER.CONTEXT_VIEW, $(document));
        });

        JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, $context, reason) {
            if (reason == JIRA.CONTENT_ADDED_REASON.dialogReady) {
                if ($context.parent('#edit-issue-dialog').length)
                    JS_INCLUDER.executeIssueScripts($context.find('input[name="id"]').val(), JS_INCLUDER.CONTEXT_EDIT, $context);
            }
        });
    });
})(AJS.$);
