$(function() {
    $('#fileupload').fileupload({
        pasteZone: null,
        dataType: 'json',
        acceptFileTypes: QCDMultiUpload.acceptFileTypes,

        submit: function(e, data) {
            var locale = window.mainController.getComponentByReferenceName("iQSHMultiUploadLocale").getValue().content.value;
            var idField = window.mainController.getComponentByReferenceName("iQSHIdForMultiUpload").getValue();
            var idValue = idField.content;

            if (!idValue.value || 0 === idValue.value) {
                $.each(data.files, function(index, file) {
                    showMessage("failure", "IncomingQualityStandardH is not saved", "Omitted file upload: " + file.name);
                });
                return false;
            }
        },

        done: function(e, data) {
            var form = mainController.getComponentByReferenceName("form");
            if (form) form.performRefresh();

            var locale = window.mainController.getComponentByReferenceName("iQSHMultiUploadLocale").getValue().content.value;
            $.each(data.files, function(index, file) {
                showMessage("success", "Upload complete", "Loaded file: " + file.name);
            });
        },

        progressall: function(e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            $('#progress .progress-bar').css('width', progress + '%');
        },

        dropZone: $('#dropzone')
    }).bind('fileuploadsubmit', function(e, data) {
        var idField = window.mainController.getComponentByReferenceName("iQSHIdForMultiUpload").getValue();
        var idValue = idField.content;
        data.formData = {
            iQSHId: idValue.value
        };
    });
});

function showMessage(type, title, content) {
    mainController.showMessage({
        type: type,
        title: title,
        content: content
    });
}
