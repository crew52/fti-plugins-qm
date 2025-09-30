$(function () {
    $('#fileupload').fileupload({
        pasteZone: null,
        dataType: 'json',
        acceptFileTypes: QCDMultiUpload.acceptFileTypes,

        submit: function (e, data) {
            const locale = window.mainController
                .getComponentByReferenceName("iQSHMultiUploadLocale")
                .getValue().content.value;

            const entityIdComp = window.mainController
                .getComponentByReferenceName("iQSHIdForMultiUpload")
                .getValue();

            const entityId = entityIdComp.content;

            if (!entityId.value || entityId.value === 0) {
                $.each(data.files, function (index, file) {
                    if (locale === "pl_PL" || locale === "pl") {
                        showMessage("failure", "Obiekt niezapisany", "Pominięto wgranie pliku: " + file.name);
                    } else {
                        showMessage("failure", "Entity not saved", "Omitted file upload: " + file.name);
                    }
                });
                return false;
            }
        },

        done: function (e, data) {
            const mainViewComponent =
                window.mainController.getComponentByReferenceName("form") ||
                window.mainController.getComponentByReferenceName("grid");

            if (mainViewComponent) {
                mainViewComponent.performRefresh();
            }

            const locale = window.mainController
                .getComponentByReferenceName("iQSHMultiUploadLocale")
                .getValue().content.value;

            $.each(data.files, function (index, file) {
                if (QCDMultiUpload.acceptFileTypes.test(file.name)) {
                    if (locale === "pl_PL" || locale === "pl") {
                        showMessage("success", "Wgrywanie zakończone", "Wgrano plik: " + file.name);
                    } else {
                        showMessage("success", "Upload complete", "Uploaded file: " + file.name);
                    }
                }
            });
        },

        progressall: function (e, data) {
            const progress = parseInt(data.loaded / data.total * 100, 10);
            $('#progress .progress-bar').css('width', progress + '%');
        },

        dropZone: $('#dropzone')
    })
    .bind('fileuploadsubmit', function (e, data) {
        const entityIdComp = window.mainController
            .getComponentByReferenceName("iQSHIdForMultiUpload")
            .getValue();

        const entityId = entityIdComp.content;
        data.formData = {
            techId: entityId.value   // vẫn dùng tên 'techId' để backend hiểu
        };
    })
    .bind('fileuploadadd', function (e, data) {
        const filetype = QCDMultiUpload.acceptFileTypes;
        const maxUploadFileMessage = $('#maxUploadFileMessage').text();
        const maxSize = parseInt(maxUploadFileMessage.replace(/^\D+/g, '')); // lấy số MB

        const locale = window.mainController
            .getComponentByReferenceName("iQSHMultiUploadLocale")
            .getValue().content.value;

        $.each(data.files, function (index, file) {
            if (!filetype.test(file.name)) {
                if (locale === "pl_PL" || locale === "pl") {
                    showMessage("failure", "Pominięto wgranie pliku", "Niedopuszczalny typ pliku: " + file.name);
                } else {
                    showMessage("failure", "Upload skipped", "Invalid file type: " + file.name);
                }
                return false;
            }

            if (file.size / 1048576 > maxSize) {
                if (locale === "pl_PL" || locale === "pl") {
                    showMessage("failure", "Pominięto wgranie pliku", "Plik: " + file.name + " " + maxUploadFileMessage);
                } else {
                    showMessage("failure", "Upload skipped", "File: " + file.name + " " + maxUploadFileMessage);
                }
                return false;
            }
        });
    });
});

function showMessage(type, title, content) {
    window.mainController.showMessage({
        type: type,
        title: title,
        content: content
    });
}
