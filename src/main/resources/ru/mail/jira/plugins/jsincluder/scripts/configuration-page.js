AJS.$(function ($) {

    function openPopup(link, popup) {
        link.addClass('jsincluder-popupLink-active');

        var top = link.position().top + link.outerHeight() + 15 - parseInt(link.css('border-bottom-width'));
        var left = Math.min(link.position().left - 100, link.offsetParent().innerWidth() - popup.outerWidth() - 10);

        popup.css({
            top: top + 'px',
            left: left + 'px'
        }).slideDown(function () {
            popup.find('input[type=text]:first').focus();
        });
    }

    function closeAllPopups(onceCalledCallback) {
        var popup = $('.jsincluder-popup:visible');
        popup.slideUp(function () {
            $(this).find('input[type=hidden], input[type=text], textarea').val('');
            $(this).find('input[type=checkbox]').attr('checked', false);
        }).promise().done(function () {
            $('.jsincluder-popupLink').removeClass('jsincluder-popupLink-active');
            if (onceCalledCallback)
                onceCalledCallback();
        });
    }

    function togglePopup(link, popup, beforeOpenCallback) {
        if (!link.hasClass('jsincluder-popupLink-active'))
            closeAllPopups(function () {
                if (beforeOpenCallback)
                    beforeOpenCallback();
                openPopup(link, popup);
            });
        else
            closeAllPopups();
    }

    $('#jsincluder-addScript').click(function (e) {
        e.preventDefault();
        togglePopup($(this), $('#jsincluder-popup-script'), function () {
            $('#jsincluder-popup-script-apply').hide();
        });
    });

    $(document).on('click', '.jsincluder-editScript', function (e) {
        e.preventDefault();

        var link = $(this);
        var tr = link.parents('tr.jsincluder-script');
        var id = tr.attr('id');
        var name = tr.find('.jsincluder-script-name').text();
        var code = tr.find('.jsincluder-script-code').val();

        togglePopup(link, $('#jsincluder-popup-script'), function () {
            $('#jsincluder-popup-script-id').val(id);
            $('#jsincluder-popup-script-name').val(name);
            $('#jsincluder-popup-script-code').val(code);
            $('#jsincluder-popup-script-apply').attr('disabled', 'disabled').show();
        });
    });

    $(document).on('click', '.jsincluder-deleteScript', function (e) {
        e.preventDefault();

        var link = $(this);
        var tr = link.parents('tr.jsincluder-script');
        var id = tr.attr('id');
        var name = tr.find('.jsincluder-script-name').text();

        if (confirm('Are you sure want to delete the script "' + name + '"?'))
            $.get(AJS.contextPath() + '/rest/jsincluder/1.0/configuration/deleteScript', {
                id: id
            }, function () {
                $('.jsincluder-script#' + id).remove();
            });
    });

    function savePopupScriptChanges() {
        var id = $('#jsincluder-popup-script-id').val();
        var name = $('#jsincluder-popup-script-name').val();
        var code = $('#jsincluder-popup-script-code').val();

        if (id == '')
            $.post(AJS.contextPath() + '/rest/jsincluder/1.0/configuration/addScript', {
                name: name,
                code: code
            }, function (data) {
                var html = '';
                html += '<tr id="' + data.id + '" class="jsincluder-script">';
                html += '<td>';
                html += '<strong class="jsincluder-script-name">' + name + '</strong>';
                html += '<textarea class="jsincluder-script-code">' + code + '</textarea>';
                html += '</td>';
                html += '<td>';
                html += '<ul class="jsincluder-bindings"></ul>';
                html += '<a href="#" class="jsincluder-addBinding jsincluder-popupLink">Add</a>';
                html += '</td>';
                html += '<td>';
                html += '<ul class="operations-list">';
                html += '<li><a href="#" class="jsincluder-editScript jsincluder-popupLink">Edit</a></li> ';
                html += '<li><a href="#" class="jsincluder-deleteScript">Delete</a></li>';
                html += '</ul>';
                html += '</td>';
                html += '</tr>';
                $(html).appendTo('#jsincluder-scripts');
            });
        else
            $.post(AJS.contextPath() + '/rest/jsincluder/1.0/configuration/editScript', {
                id: id,
                name: name,
                code: code
            }, function () {
                var tr = $('.jsincluder-script#' + id);
                tr.find('.jsincluder-script-name').text(name);
                tr.find('.jsincluder-script-code').val(code);
            });
    }

    $('#jsincluder-popup-script-name, #jsincluder-popup-script-code').on('input', function () {
        $('#jsincluder-popup-script-apply').removeAttr('disabled');
    });

    $('#jsincluder-popup-script-ok').click(function (e) {
        e.preventDefault();
        savePopupScriptChanges();
        closeAllPopups();
    });

    $('#jsincluder-popup-script-apply').click(function (e) {
        e.preventDefault();
        savePopupScriptChanges();
        $('#jsincluder-popup-script-apply').attr('disabled', 'disabled');
    });

    $(document).on('click', '.jsincluder-addBinding', function (e) {
        e.preventDefault();

        var link = $(this);
        var tr = link.parents('tr.jsincluder-script');
        var scriptId = tr.attr('id');

        togglePopup(link, $('#jsincluder-popup-binding'), function () {
            $('#jsincluder-popup-binding-scriptId').val(scriptId);
        });
    });

    $(document).on('click', '.jsincluder-editBinding', function (e) {
        e.preventDefault();

        var link = $(this);
        var tr = link.parents('tr.jsincluder-script');
        var li = link.parents('li.jsincluder-binding');
        var scriptId = tr.attr('id');
        var id = li.attr('id');
        var projectKeys = li.find('.jsincluder-binding-projectKeys').text();
        var issueTypeIds = li.find('.jsincluder-binding-issueTypeIds').text();
        var createContextEnabled = li.find('.jsincluder-binding-createContextEnabled').text();
        var viewContextEnabled = li.find('.jsincluder-binding-viewContextEnabled').text();
        var editContextEnabled = li.find('.jsincluder-binding-editContextEnabled').text();
        var transitionContextEnabled = li.find('.jsincluder-binding-transitionContextEnabled').text();

        togglePopup(link, $('#jsincluder-popup-binding'), function () {
            $('#jsincluder-popup-binding-scriptId').val(scriptId);
            $('#jsincluder-popup-binding-id').val(id);
            $('#jsincluder-popup-binding-projectKeys').val(projectKeys);
            $('#jsincluder-popup-binding-issueTypeIds').val(issueTypeIds);
            $('#jsincluder-popup-binding-createContextEnabled').attr('checked', createContextEnabled == 'true');
            $('#jsincluder-popup-binding-viewContextEnabled').attr('checked', viewContextEnabled == 'true');
            $('#jsincluder-popup-binding-editContextEnabled').attr('checked', editContextEnabled == 'true');
            $('#jsincluder-popup-binding-transitionContextEnabled').attr('checked', transitionContextEnabled == 'true');
        });
    });

    $(document).on('click', '.jsincluder-deleteBinding', function (e) {
        e.preventDefault();

        var link = $(this);
        var li = link.parents('li.jsincluder-binding');
        var id = li.attr('id');

        if (confirm('Are you sure want to delete the binding?'))
            $.get(AJS.contextPath() + '/rest/jsincluder/1.0/configuration/deleteBinding', {
                id: id
            }, function () {
                $('.jsincluder-binding#' + id).remove();
            });
    });

    $('#jsincluder-popup-binding-ok').click(function (e) {
        e.preventDefault();

        var scriptId = $('#jsincluder-popup-binding-scriptId').val();
        var id = $('#jsincluder-popup-binding-id').val();
        var projectKeys = $('#jsincluder-popup-binding-projectKeys').val();
        var issueTypeIds = $('#jsincluder-popup-binding-issueTypeIds').val();
        var createContextEnabled = $('#jsincluder-popup-binding-createContextEnabled').is(':checked');
        var viewContextEnabled = $('#jsincluder-popup-binding-viewContextEnabled').is(':checked');
        var editContextEnabled = $('#jsincluder-popup-binding-editContextEnabled').is(':checked');
        var transitionContextEnabled = $('#jsincluder-popup-binding-transitionContextEnabled').is(':checked');

        if (id == '')
            $.get(AJS.contextPath() + '/rest/jsincluder/1.0/configuration/addBinding', {
                scriptId: scriptId,
                projectKeys: projectKeys,
                issueTypeIds: issueTypeIds,
                createContextEnabled: createContextEnabled,
                viewContextEnabled: viewContextEnabled,
                editContextEnabled: editContextEnabled,
                transitionContextEnabled: transitionContextEnabled
            }, function (data) {
                var html = '';
                html += '<li id="' + data.id + '" class="jsincluder-binding">';
                html += '<strong>Projects:</strong> <span class="jsincluder-binding-projectKeys">' + projectKeys + '</span><br/>';
                html += '<strong>Issue Types:</strong> <span class="jsincluder-binding-issueTypeIds">' + issueTypeIds + '</span><br/>';
                html += '<strong>Contexts:</strong> <span class="jsincluder-binding-enabledContexts">' + data.enabledContexts + '</span><br/>';
                html += '<div class="jsincluder-binding-createContextEnabled" style="display: none;">' + createContextEnabled + '</div>';
                html += '<div class="jsincluder-binding-viewContextEnabled" style="display: none;">' + viewContextEnabled + '</div>';
                html += '<div class="jsincluder-binding-editContextEnabled" style="display: none;">' + editContextEnabled + '</div>';
                html += '<div class="jsincluder-binding-transitionContextEnabled" style="display: none;">' + transitionContextEnabled + '</div>';
                html += '<ul class="operations-list">';
                html += '<li><a href="#" class="jsincluder-editBinding jsincluder-popupLink">Edit</a></li> ';
                html += '<li><a href="#" class="jsincluder-deleteBinding">Delete</a></li>';
                html += '</ul>';
                html += '</li>';
                $(html).appendTo('.jsincluder-script#' + scriptId + ' .jsincluder-bindings');
            });
        else
            $.get(AJS.contextPath() + '/rest/jsincluder/1.0/configuration/editBinding', {
                id: id,
                projectKeys: projectKeys,
                issueTypeIds: issueTypeIds,
                createContextEnabled: createContextEnabled,
                viewContextEnabled: viewContextEnabled,
                editContextEnabled: editContextEnabled,
                transitionContextEnabled: transitionContextEnabled
            }, function (data) {
                var li = $('.jsincluder-binding#' + id);
                li.find('.jsincluder-binding-projectKeys').text(projectKeys);
                li.find('.jsincluder-binding-issueTypeIds').text(issueTypeIds);
                li.find('.jsincluder-binding-enabledContexts').text(data.enabledContexts);
                li.find('.jsincluder-binding-createContextEnabled').text(createContextEnabled);
                li.find('.jsincluder-binding-viewContextEnabled').text(viewContextEnabled);
                li.find('.jsincluder-binding-editContextEnabled').text(editContextEnabled);
                li.find('.jsincluder-binding-transitionContextEnabled').text(transitionContextEnabled);
            });

        closeAllPopups();
    });

    $('.jsincluder-popup .cancel').click(function (e) {
        e.preventDefault();
        closeAllPopups();
    });
});
