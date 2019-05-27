require(['jquery', 'jira/util/events', 'jira/util/events/types', 'jira/util/events/reasons'],
    function($, Events, EventsTypes, EventsReasons) {
        var RESTRICTION_RULES = [
            {
                fieldNames: ['priority'],
                projectIds: ['10068'], //JC
                issueTypeIds: ['3'],
                blockedGroups: [],
                blockedProjectRoles: ['Administrators']
            }
        ];

        function isIntersectionEmpty(array1, array2) {
            for (var i = 0; i < array1.length; i++)
                for (var j = 0; j < array2.length; j++)
                    if (array1[i] == array2[j])
                        return false;
            return true;
        }

        var restrictedFieldNames = [];
        for (var ruleIndex = 0; ruleIndex < RESTRICTION_RULES.length; ruleIndex++) {
            var projectCondition = !isIntersectionEmpty(RESTRICTION_RULES[ruleIndex].projectIds, [JS_INCLUDER.params.projectId]);
            var issueTypeCondition = !isIntersectionEmpty(RESTRICTION_RULES[ruleIndex].issueTypeIds, [JS_INCLUDER.params.issueTypeId]);
            var groupCondition = !isIntersectionEmpty(RESTRICTION_RULES[ruleIndex].blockedGroups, JS_INCLUDER.params.userDetails.groupNames);
            var projectRoleCondition = !isIntersectionEmpty(RESTRICTION_RULES[ruleIndex].blockedProjectRoles, JS_INCLUDER.params.userDetails.projectRoleNames);

            if (projectCondition && issueTypeCondition && (groupCondition || projectRoleCondition))
                for (var i = 0; i < RESTRICTION_RULES[ruleIndex].fieldNames.length; i++) {
                    var fieldName = RESTRICTION_RULES[ruleIndex].fieldNames[i];
                    if (/^\d+$/.test(fieldName))
                        fieldName = 'customfield_' + fieldName;
                    restrictedFieldNames.push(fieldName);
                }
        }

        for (i = 0; i < restrictedFieldNames.length; i++) {
            /* Hides field from create and edit screens */
            $(JS_INCLUDER.contextObject).find('.qf-field, .field-group').has('[name="' + restrictedFieldNames[i] + '"]').hide();

            /* Prevents inline editing, needs testing - may be harmful */
            function getFieldSelector(id) {
                if (id === 'issuetype') {
                    return '#type-val';
                } else if (id === 'fixVersions') {
                    return '#fixfor-val';
                } else if (id === 'summary') {
                    return '#summary-val';
                } else if (id === 'labels') {
                    return '#wrap-labels .value';
                } else if (id === 'duedate') {
                    return '#due-date';
                } else {
                    return '#' + id + '-val';
                }
            }

            $(JS_INCLUDER.contextObject).find(getFieldSelector(restrictedFieldNames[i])).attr('id', '');
        }

        /* Prevent Configure Fields popup activity */
        Events.unbind('.fieldSecurity');
        var contextObjectParent = $(JS_INCLUDER.contextObject);
        Events.bind(EventsTypes.NEW_CONTENT_ADDED + '.fieldSecurity', function(e, context, reason) {
            if (contextObjectParent.has(context).length)
                for (i = 0; i < restrictedFieldNames.length; i++)
                    $(context).find('.qf-field, .field-group').add($(context).filter('.qf-field, .field-group')).has('[name="' + restrictedFieldNames[i] + '"]').hide();
        });
    });
