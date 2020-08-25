require(['jquery'], function($) {
    AJS.toInit(function () {
        var jsdAttempt=0;

        function onFunctionAvailable(issue) {
            if(jsdAttempt++ > 15){
                console.error("JSD page not full load. JSIncluder scripts will not load.");
                return;
            }
            try {
                if ($('#content').children().length > 0)
                    JS_INCLUDER.executeIssueScripts(issue.id, JS_INCLUDER.CONTEXT_VIEW, $(document));
                else
                    throw new Error("JSD page not ready");
            } catch (error) {
                setTimeout(function() {onFunctionAvailable(issue)}, 50);
            }
        }

        var jsonPayload = $('#jsonPayload').text().length > 0 ? JSON.parse($('#jsonPayload').text()) : null;
        if (jsonPayload != null && jsonPayload.hasOwnProperty('reqDetails') && jsonPayload.reqDetails.hasOwnProperty('issue'))
            onFunctionAvailable(jsonPayload.reqDetails.issue); //todo: найти лучшее решение
    });
});
