(function() {

	var THIS = {};


    var Dragger = THIS.Dragger = function(dragEl, moveEl) {
        var me = this;
        this.dragging = false;
        var $dragel = $(moveEl).find(dragEl);

        $dragel.mousedown(function(event) {
            event.preventDefault();
            event.stopPropagation();
            me.movePos = $(moveEl).position();
            me.dragx = event.pageX;
            me.dragy = event.pageY;
            me.dragging = true;
        });

        $dragel.mousemove(function(event) {
            event.preventDefault();
            event.stopPropagation();
            if(me.dragging) {
                var dragx = event.pageX;
                var dragy = event.pageY;
                var offset = { left: me.movePos.left + (dragx - me.dragx), top: me.movePos.top + (dragy - me.dragy) };
                $(moveEl).offset(offset);
            }
        });

        $dragel.mouseup(function(event) {
            event.preventDefault();
            event.stopPropagation();
            me.dragging = false;
        });
    }

    // This is an HTML dialog handler
    var Dialog = THIS.Dialog = function(dialogSel, buttons, onDone, ctx) {
        this.dialog = $(dialogSel);
        this.overlay = $("#dialogOverlay");
        this.dragger = null;

        this.show = function () {
            if(! this.dragger) {
                this.dragger = new Dragger(".dialogTitle", "#messageBox");
            }
            var ol = this.overlay;
            var dialog = this.dialog;
            var w_ol = ol.outerWidth();
            var w_dl =  dialog.outerWidth();
            var h_ol = ol.outerHeight();
            var h_dl =  dialog.outerHeight();
            var top = (h_ol-h_dl)/2;
            var left = (w_ol-w_dl)/2;
            dialog.offset({top:top, left:left});
            var buttonDiv = dialog.find(".dialogButtons");
            buttonDiv.empty();
            _.each(
                buttons,
                function(info, index, list) {
                    var b = $('<button type="submit" value="'+info[0]+'">'+info[1]+'</button>');
                    buttonDiv.append(b);
                    var me = this;
                    b.click(function(event) {
                        event.preventDefault();
                        event.stopPropagation();
                        me.hide();
                        onDone.call(ctx || this, event.target.value)
                    });
                },
                this
            )
            $ae.show(ol);
            $ae.show(dialog);
        }

         this.hide = function() {
            $ae.hide(this.overlay);
            $ae.hide(this.dialog);
        }

        // Now show the dialog
        this.show();
    };

    Dialog.OK = [["ok", "OK"]];
    Dialog.OK_CANCEL = [["ok", "OK"], ["cancel", "Cancel"]];
    Dialog.YES_NO = [["yes", "Yes"], ["no", "No"]];

    Dialog.messageBox = function(title, message, buttons, onDone) {
        var sel = $("#messageBox");
        var dialog = new Dialog(sel, button, ondone);
        sel.find(".dialogTitle").html(title);
        sel.find(".dialogBody").html(message);
        dialog.show();
    }

    Dialog.confirm = function(title, message, onDone, ctx) {
        var sel = $("#messageBox");
        var dialog = new Dialog(sel, Dialog.YES_NO, onDone, ctx);
        sel.find(".dialogTitle").html(title);
        sel.find(".dialogBody").html(message);
        dialog.show();
    }

	window.$ae = _.extend({}, window.$ae, THIS);


}).call(this);