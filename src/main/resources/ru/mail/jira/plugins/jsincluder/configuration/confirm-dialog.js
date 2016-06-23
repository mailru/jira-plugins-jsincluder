define('jsincluder/confirm-dialog', ['jquery', 'backbone'], function($, Backbone) {
    return Backbone.View.extend({
        el: '#jsincluder-confirm-dialog',
        events: {
            'click #jsincluder-confirm-dialog-ok': '_ok',
            'click #jsincluder-confirm-dialog-cancel': '_cancel'
        },
        initialize: function(options) {
            this.dialog = AJS.dialog2('#jsincluder-confirm-dialog');
            this.header = this.$('.aui-dialog2-header-main');
            this.text = this.$('.aui-dialog2-content');
            this.okBtn = this.$('#jsincluder-confirm-dialog-ok');

            this.header.empty();
            this.text.empty();

            options.okText && this.okBtn.text(options.okText);
            this.header.append(options.header);
            this.text.append(options.text);
            this._okHandler = options.okHandler;
            this._cancelHandler = options.cancelHandler;

            this.dialog.on('hide', $.proxy(this.destroy, this));
        },
        destroy: function() {
            this.stopListening();
            this.undelegateEvents();
            this.dialog.off();
        },
        show: function() {
            this.dialog.show();
        },
        _ok: function() {
            this.dialog.hide();
            this._okHandler && this._okHandler.call();
        },
        _cancel: function() {
            this.dialog.hide();
            this._cancelHandler && this._cancelHandler.call();
        }
    });
});