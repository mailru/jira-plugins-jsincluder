require(['jquery', 'jira/util/events', 'jira/util/events/types', 'jira/util/events/reasons'],
    function($, Events, EventsTypes, EventsReasons) {
        AJS.toInit(function() {
            var issueId = $('#key-val').attr('rel');
            if (issueId)
                JS_INCLUDER.executeIssueScripts(issueId, JS_INCLUDER.CONTEXT_VIEW, $(document));

            Events.bind(EventsTypes.ISSUE_REFRESHED, function(e, context) {
                JS_INCLUDER.executeIssueScripts(context, JS_INCLUDER.CONTEXT_VIEW, $(document));
            });

            Events.bind(EventsTypes.NEW_CONTENT_ADDED, function(e, $context, reason) {
                if (reason === EventsReasons.dialogReady) {
                    if ($context.parent('#edit-issue-dialog').length)
                        JS_INCLUDER.executeIssueScripts($context.find('input[name="id"]').val(), JS_INCLUDER.CONTEXT_EDIT, $context);
                }

                //GH details view
                if (reason === EventsReasons.panelRefreshed && ($context.is('#attachmentmodule') || $context.is('#file_attachments') || $context.is('#attachment_thumbnails'))) {
                    var ghIssueId = $('#ghx-detail-issue').data('issueid');
                    if (ghIssueId)
                        JS_INCLUDER.executeIssueScripts(ghIssueId, JS_INCLUDER.CONTEXT_VIEW, $(document));
                }
            });
        });
    });
