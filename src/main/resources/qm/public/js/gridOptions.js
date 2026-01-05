/*
 * jQuery resize event - v1.1 - 3/14/2010
 * http://benalman.com/projects/jquery-resize-plugin/
 * 
 * Copyright (c) 2010 "Cowboy" Ben Alman
 * Dual licensed under the MIT and GPL licenses.
 * http://benalman.com/about/license/
 */
(function ($, h, c) {
    var a = $([]), e = $.resize = $.extend($.resize, {}), i, k = "setTimeout", j = "resize", d = j + "-special-event", b = "delay", f = "throttleWindow";
    e[b] = 250;
    e[f] = true;
    $.event.special[j] = {
        setup: function () {
            if (!e[f] && this[k]) {
                return false
            }
            var l = $(this);
            a = a.add(l);
            $.data(this, d, { w: l.width(), h: l.height() });
            if (a.length === 1) {
                g()
            }
        }, teardown: function () {
            if (!e[f] && this[k]) {
                return false
            }
            var l = $(this);
            a = a.not(l);
            l.removeData(d);
            if (!a.length) {
                clearTimeout(i)
            }
        }, add: function (l) {
            if (!e[f] && this[k]) {
                return false
            }
            var n;
            function m(s, o, p) {
                var q = $(this), r = $.data(this, d);
                r.w = o !== c ? o : q.width();
                r.h = p !== c ? p : q.height();
                n.apply(this, arguments)
            }
            if ($.isFunction(l)) {
                n = l;
                return m
            } else {
                n = l.handler;
                l.handler = m
            }
        }
    };
    function g() {
        i = h[k](function () {
            a.each(function () {
                var n = $(this), m = n.width(), l = n.height(), o = $.data(this, d);
                if (m !== o.w || l !== o.h) {
                    n.trigger(j, [o.w = m, o.h = l])
                }
            });
            g()
        }, e[b])
    }
}
)(jQuery, this);

var myApp = angular.module('gridApp', []);

function gridRunner(action) {
    QCD.components.elements.utils.LoadingIndicator.blockElement(parent.$('body'));
    action();
    QCD.components.elements.utils.LoadingIndicator.unblockElement(parent.$('body'));
}

function parseAndValidateInputNumber($element) {
    function countEmptyElements(arr) {
        var counterOfEmptyElements = 0;
        for (var index in arr) {
            if (!arr[index]) {
                counterOfEmptyElements++;
            }
        }

        return counterOfEmptyElements;
    }
    var element = $element[0];
    var rawValue = element.value.replace(/^0+(?!\.|,|$)/, '');

    try {
        if (!rawValue) {
            throw 'Empty element value';
        }

        var valueArray = rawValue.split(/[.,]/);

        if (!((valueArray.length === 1 || valueArray.length === 2) && countEmptyElements(valueArray) === 0)) {
            throw 'Value with wrong separator';
        }

        var value = rawValue.replace(',', '.');
        var unitPart = valueArray[0];
        var fractionPart = '1' + (valueArray[1] || '0');

        if (unitPart !== parseFloat(unitPart).toString()) {
            throw 'Invalid unit part';
        }
        if (fractionPart !== parseFloat(fractionPart).toString()) {
            throw 'Invalid fraction part';
        }

        $element.removeClass('error-grid');
        if (value !== $element.val()) {
            $element.val(value);
        }

    } catch (exception) {
        $element.addClass('error-grid');
        $element.val(rawValue);
    }

    return $element.val();
}

myApp.directive('ngJqGrid', function ($window) {
    return {
        restrict: 'E',
        scope: {
            config: '=',
            data: '=',
        },
        link: function (scope, element, attrs) {
            var table;

            scope.$watch('config', function (newValue) {
                if (newValue) {
                    $(element).empty();
                    table = angular.element('<table id="grid"></table>');
                    element.append(table);
                    element.append(angular.element('<div id="jqGridPager"></div>'));
                    $(table).jqGrid(newValue);

                    var samplesHeader = QCD.translate('samplesGrid.gridHeader.samples');

                    var gridTitle = '<div class="gridTitle">' + samplesHeader + ' <span id="rows-num">(0)</span></div>';

                    $('#t_grid').append('<div class="t_grid__container"></div>');
                    $('#t_grid .t_grid__container').append(gridTitle);

                    $(table).jqGrid('filterToolbar');
                    mainController.getComponentByReferenceName("samplesGrid").setComponentChanged(false);
                }
            });
        }
    };
});

function validateSerializeData(data) {
    var elements = null;
    if ($('#FrmGrid_grid').length) {
        elements = $('.error-grid', '#FrmGrid_grid');

    } else {
        elements = $('.error-grid', '#gridContainer');
    }

    return JSON.stringify(data);
}

function roundTo(n) {
    var places = 5;
    return +(Math.floor(parseFloat(n) + "e+" + places) + "e-" + places);
}

function validatorNumber(val) {
    if (val === '') {
        return true;
    }

    return parseFloat(val) === roundTo(val);
}

function validateElement(el, validator) {
    el = $(el);
    if (validator(el.val())) {
        el.removeClass('error-grid');

    } else {
        el.addClass('error-grid');
    }
}

function translateMessages(messages) {
    var message = [];
    if (messages) {
        var messageArray = messages.split('\n');
        for (var i in messageArray) {
            var msg = messageArray[i].split('"').join("&#039;");
            msg = QCD.translate(msg);
            if (msg.substr(0, 1) === '[' && msg.substr(-1, 1) === ']') {
                msg = msg.substr(1, msg.length - 2);
            }
            message.push(msg);
        }
    }
    message = message.join('\n');

    return message;
}

function saveAllRows() {
    var grid = $("#grid");
    var ids = grid.jqGrid('getDataIDs');

    for (var i = 0; i < ids.length; i++) {
        grid.saveRow(ids[i]);
    }
}

function viewRefresh() {
    angular.element($("#GridController")).scope().cancelEditing();
}

function refreshForm() {
    var mainViewComponent = mainController.getComponentByReferenceName("form") || mainController.getComponentByReferenceName("grid");
    if (mainViewComponent) {
        mainViewComponent.performRefresh();
    }
}

function qicIdChanged(id) {
    saveAllRows();
    angular.element($("#GridController")).scope().qicIdChanged(id);
    return false;
}

function getSelectedRowId() {
    return jQuery('#grid').jqGrid('getGridParam', 'selarrrow');
}

function addNewRow() {
    angular.element($("#GridController")).scope().addNewRow();
}

// function deleteRow() {
//     angular.element($("#GridController")).scope().deleteRow();
// }

function openLookup(name, parameters) {
    var lookupHtml = '/lookup.html'
    if (name == 'attribute') {
        mainController.openModal('body', '../' + name + "/" + parameters.custom_attr_name + lookupHtml, false, function onModalClose() {
        }, function onModalRender(modalWindow) {
        }, { width: 1000, height: 560 });
    } else {
        if (parameters) {
            var urlParams = $.param(parameters);
            lookupHtml = lookupHtml + "?" + urlParams;
        }
        mainController.openModal('body', '../' + name + lookupHtml, false, function onModalClose() {
        }, function onModalRender(modalWindow) {
        }, { width: 1000, height: 560 });
    }
}

function updateFieldValue(field, value, rowId) {
    // edit inline
    var selector = $("[id='" + rowId + '_' + field + "']");

    var element = $(selector);
    if (element.length && element[0].tagName.toLowerCase() === 'span') {
        element = $('input', element);
    }

    if (element.is(':checkbox')) {
        return element.prop('checked', value);
    } else {
        return element.val(value);
    }
}

function clearSelect(field, rowId) {
    var selector = $('#' + rowId + '_' + field);
    $(selector).empty();
    $(selector).val([]);
}

function onSelectLookupRow(row, recordName) {
    if (row) {
        var code = row.code || row.number || row.value;
        recordName = recordName.replace('attribute/', '');
        var rowId = $('#product').length ? null : jQuery('#grid').jqGrid('getGridParam', 'selrow');
        var field = updateFieldValue(recordName, code, rowId);
        if (recordName == "batch") {
            if (row.id == 0) {
                var fieldBatchId = updateFieldValue("batchId", null, rowId);
                fieldBatchId.trigger('change');
            } else {
                var fieldBatchId = updateFieldValue("batchId", row.id, rowId);
                fieldBatchId.trigger('change');
            }
        }
        field.trigger('change');
    }

    mainController.closeThisModalWindow();
}

var messagesController = new QCD.MessagesController();
var columnConfiguration;

myApp.controller('GridController', ['$scope', '$window', '$http', function ($scope, $window, $http) {

    var _this = this;
    var quantities = {};
    var conversionModified = true;
    var lastSel;
    var firstLoad = true;
    var hasAdditionalUnit = false;

    function getJsonByQuery(url, params, callback) {
        if (params && params.query) {
            return $.ajax({
                dataType: "json",
                url: url,
                data: params,
                success: function (data) {
                    callback(data);
                }
            });

        } else {
            callback({ entities: [], numberOfResults: 0 });
        }
    }

    function getRowIdFromElement(el) {
        var rowId = el.attr('rowId');
        if ('_empty' === rowId) {
            rowId = 0;
        }

        return rowId;
    }

    function showMessage(type, title, content) {
        mainController.showMessage({
            type: type,
            title: title,
            content: content
        });
    }

    function getFieldValue(field, rowId) {
        return getField(field, rowId).val();
    }

    function getField(field, rowId) {
        // edit inline
        var selector = $('#' + rowId + '_' + field);

        var element = $(selector);
        if (element.length && element[0].tagName.toLowerCase() === 'span') {
            element = $('input', element);
        }

        return element;
    }

    function quantitativeResult_createElement(value, options) {
        var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
        $input.val(value);

        $($input).bind('change keydown paste input', function () {
            var t = $(this);

            window.clearTimeout(t.data("timeout"));
            t.data("timeout", setTimeout(function () {
                var rowId = t.attr('rowId'); // từ options.rowId
                var qrRaw = t.val();
                if (qrRaw === '') {
                    // nếu quantitativeResult thì quantitativeEvaluation sẽ trống
                    var evalSelector = '#' + rowId + '_quantitativeEvaluation';
                    var $evalEl = $(evalSelector);

                    if ($evalEl.length && $evalEl[0].tagName.toLowerCase() === 'span') {
                        $evalEl = $('select,input', $evalEl);
                    }

                    if ($evalEl.length) {
                        $evalEl.val('');          // hoặc null
                        $evalEl.trigger('change');
                    }
                    return;
                }
                var qr = parseFloat(qrRaw);
                if (isNaN(qr)) {
                    return;
                }

                // Lấy full dữ liệu hàng từ jqGrid
                var rowData = $('#grid').jqGrid('getRowData', rowId);
                var upRaw = rowData.upValue;
                var downRaw = rowData.downValue;

                if (!upRaw || !downRaw) {
                    // nếu 1 trong 2 không có dữ liệu hiển thị -> bỏ qua
                    return;
                }

                var up = parseFloat(upRaw);
                var down = parseFloat(downRaw);
                if (isNaN(up) || isNaN(down)) {
                    return;
                }

                var newEval = (qr >= down && qr <= up) ? '01pass' : '02fail';

                // set vào ô quantitativeEvaluation của hàng đang edit
                var evalSelector = '#' + rowId + '_quantitativeEvaluation';
                var $evalEl = $(evalSelector);
                if ($evalEl.length && $evalEl[0].tagName.toLowerCase() === 'span') {
                    $evalEl = $('select,input', $evalEl);
                }
                if ($evalEl.length) {
                    $evalEl.val(newEval);
                    $evalEl.trigger('change');
                }
            }, 300));
        });

        return $input;
    }

    // Lấy id của QIC từ view
    function getQICId() {
        if (context) {
            var contextObject = JSON.parse(context);
            if (contextObject && contextObject['window.generalTab.form.id']) {
                return contextObject['window.generalTab.form.id'];
            }
        }

        var config = angular.element($("#GridController")).scope().config;

        return config ? config.qic_id : 0;
    }

    function getColModelByIndex(index, c) {
        c = c || $scope.config;
        var col = c.colModel.filter(function (element, i) {
            return element.name === index;
        })[0];
        if (!col) {
            console.error(index);
        }
        return col;
    }

    function getColModelOrPrepareForAttribute(columnProperties, c) {
        c = c || $scope.config;
        var col = c.colModel.filter(function (element, i) {
            return element.index === columnProperties.name;
        })[0];
        if (columnProperties.forAttribute) {
            var attrColModel = {};
            attrColModel.name = columnProperties.name;
            attrColModel.editable = true;
            if (columnProperties.attributeDataType == '01calculated') {
                attrColModel.edittype = 'custom';
                var editoptions = {};
                editoptions.custom_element = attributeLookup_createElement;
                editoptions.custom_value = lookup_value;
                editoptions.custom_attr_name = columnProperties.name;
                attrColModel.editoptions = editoptions;
            } else if (columnProperties.attributeValueType == '02numeric') {
                attrColModel.formatter = numberFormatter;
                attrColModel.unformat = numberUnformat;
                attrColModel.edittype = 'custom';
                var editoptions = {};
                editoptions.custom_element = attribute_createElement;
                editoptions.custom_value = input_value;
                editoptions.custom_attr_name = columnProperties.name;
                attrColModel.editoptions = editoptions;
            } else {
                var editoptions = {};
                attrColModel.editoptions = editoptions;
            }

            col = attrColModel;
        } else if (!col) {
            console.error(columnProperties.name);
        }
        return col;
    }

    function errorCallback(response) {
        showMessage('failure', QCD.translate('documentGrid.notification.failure'), response.data.message);
    }

    function input_value(elem, operation, value) {
        if (operation === 'get') {
            return $(elem).val();

        } else if (operation === 'set') {
            return $('input', elem).val(value);
        }
    }

    function numberFormatter(cellvalue, options, rowObject) {
        var val = cellvalue || '';
        return '<span class="number-cell">' + val + '</span>';
    }

    function numberUnformat(cellvalue, options, cell) {
        var val = $('span', cell).text();
        return val || '';
    }

    function errorfunc(rowID, response) {
        var message = JSON.parse(response.responseText).message;
        message = translateMessages(message);
        showMessage('failure', QCD.translate('samplesGrid.notification.failure'), message);
        return true;
    }

    function successfunc(rowID, response) {
        prepareViewOnEndEdit();
        showMessage('success', QCD.translate('samplesGrid.notification.success'), QCD.translate('samplesGrid.message.saveMessage'));
        return true;
    }

    function aftersavefunc() {
        refreshForm();
    }

    function prepareViewOnStartEdit() {
        mainController.getComponentByReferenceName("samplesGrid").setComponentChanged(true);
        $("#add_new_row").addClass("disableButton");
        $("#delete_row").addClass("disableButton");
    }

    function prepareViewOnEndEdit() {
        mainController.getComponentByReferenceName("samplesGrid").setComponentChanged(false);
        $("#add_new_row").removeClass("disableButton");
        $("#delete_row").removeClass("disableButton");
    }


    function cancelEditing() {
        var lrid;
        if (typeof lastSel !== "undefined") {
            // cancel editing of the previous selected row if it was in editing state.
            // jqGrid hold intern savedRow array inside of jqGrid object,
            // so it is safe to call restoreRow method with any id parameter
            // if jqGrid not in editing state
            $('#grid').jqGrid('restoreRow', lastSel);

            // now we need to restore the icons in the formatter:"actions"
            lrid = $.jgrid.jqID(lastSel);
            $("tr#" + lrid + " div.ui-inline-edit, " + "tr#" + lrid + " div.ui-inline-del").show();
            $("tr#" + lrid + " div.ui-inline-save, " + "tr#" + lrid + " div.ui-inline-cancel").hide();
        }
    }
    $scope.cancelEditing = cancelEditing;

    // resize: Thiết kế màn hình phù hợp cho mọi màn hình
    $scope.resize = function () {
        var $grid = jQuery('#grid').setGridWidth($("#window\\.samplesGridTab").width() - 23, true);
        if ($grid.is(':visible')) {
            var $flowGridLayout = $('div.flow-grid-layout-item');
            var containerHeight = $flowGridLayout.innerHeight();
            var gridHeight = $('.ui-jqgrid-bdiv', $flowGridLayout).outerHeight();
            var totalGridHeight = $('#gbox_grid', $flowGridLayout).outerHeight();
            var newGridHeightToFillWholeSpace = containerHeight - totalGridHeight + gridHeight;
            $grid.setGridHeight(newGridHeightToFillWholeSpace);
            config.height = newGridHeightToFillWholeSpace;
        }
    };

    $("#window\\.samplesGridTab").resize($scope.resize);

    var gridEditOptions = {
        keys: true,
        url: '../../rest/rest/qualityStandardSamplesRes.html', // base
        mtype: 'PUT',
        errorfunc: errorfunc,
        successfunc: successfunc,
        aftersavefunc: aftersavefunc
    };

    var config = {
        url: '../../rest/rest/qualityStandardSamplesRes/' + getQICId() + '.html',
        datatype: "json",
        height: '100%',
        autowidth: true,
        rowNum: 20,
        rowList: [20, 30, 50, 100, 200],
        sortname: 'qualityStandardL',
        toolbar: [true, "top"],
        rownumbers: false,
        altRows: true,
        multiselect: false,
        altclass: 'qcadooRowClass',
        errorTextFormat: function (response) {
            return translateMessages(JSON.parse(response.responseText).message);
        },
        colModel: [
            {
                name: 'id',
                index: 'id',
                key: true,
                hidden: true
            },
            {
                name: 'qualityInspectionCommandRe',
                index: 'qualityInspectionCommandRe',
                hidden: true,
                editable: true,
                editoptions: {
                    defaultValue: getQICId()
                }
            },
            {
                name: 'number',
                index: 'number',
                width: 50,
                search: false,
                hidden: false,
                editable: false,
            },
            {
                name: 'act',
                index: 'act',
                width: 55,
                align: 'center',
                sortable: false,
                search: false,
                formatter: 'actions',
                formatoptions: {
                    keys: true, // we want use [Enter] key to save the row and [Esc] to cancel editing.
                    editOptions: gridEditOptions,
                    url: '../../rest/rest/qualityStandardSamplesRes/' + 1 + '.html',
                    delbutton: false,
                    onEdit: function (id) {
                        if (typeof (lastSel) !== "undefined" && id !== lastSel) {
                            cancelEditing(id);
                        }
                        prepareViewOnStartEdit();
                        gridEditOptions.url = '../../rest/rest/qualityStandardSamplesRes/' + id + '.html';
                        lastSel = id;
                    },
                    afterRestore: function () {
                        cancelEditing();
                        prepareViewOnEndEdit();
                        $("#grid").trigger("reloadGrid");
                        viewRefresh();
                    }
                },
            },
            {
                name: 'position',
                index: 'position',
                hidden: false,
                editable: false,
                // formatter: numberFormatter
            },
            {
                name: 'qcNumber',
                index: 'qcNumber',
                hidden: false,
                editable: false,
                editoptions: {}
            },
            {
                name: 'qcName',
                index: 'qcName',
                hidden: false,
                editable: false,
                editoptions: {}
            },
            {
                name: 'qcType',
                index: 'qcType',
                hidden: false,
                editable: false,
                editoptions: {},
                stype: 'select',
                searchoptions: {
                    sopt: ['eq'],
                    value:
                        ':;' +
                        '01qualitative:' + translateMessages('samplesGrid.01qualitative') + ';' +
                        '02quantitative:' + translateMessages('samplesGrid.02quantitative')
                }
            },
            {
                name: 'unit',
                index: 'unit',
                hidden: false,
                editable: false,
                stype: 'select',
                editoptions: {},
                searchoptions: {}
            },
            {
                name: 'description',
                index: 'description',
                hidden: false,
                editable: false,
                editoptions: {}
            },
            {
                name: 'sampleSize',
                index: 'sampleSize',
                hidden: false,
                editable: false,
                editoptions: {}
            },
            {
                name: 'qualitativeResult',
                index: 'qualitativeResult',
                hidden: false,
                editable: true,
                editoptions: {},
                edittype: 'select',
                stype: 'select',
                searchoptions: {}
            },
            {
                name: 'quantitativeValue',
                index: 'quantitativeValue',
                hidden: false,
                editable: false,
                editoptions: {}
            },
            {
                name: 'upValue',
                index: 'upValue',
                hidden: false,
                editable: false,
                editoptions: {}
            },
            {
                name: 'downValue',
                index: 'downValue',
                hidden: false,
                editable: false,
                editoptions: {}
            },
            {
                name: 'quantitativeResult',
                index: 'quantitativeResult',
                hidden: false,
                editable: true,
                edittype: 'custom',
                editoptions: {
                    custom_element: quantitativeResult_createElement,
                    custom_value: input_value // đã có sẵn ở trên
                }
            },
            {
                name: 'quantitativeEvaluation',
                index: 'quantitativeEvaluation',
                hidden: false,
                editable: true,
                // loại seach
                edittype: 'select',
                stype: 'select',
                editoptions: {},
                // option search
                searchoptions: {}
            },
            {
                name: 'meName',
                index: 'meName',
                hidden: false,
                editable: false,
                editoptions: {}
            },
            {
                name: 'meMeasuringMethod',
                index: 'meMeasuringMethod',
                hidden: false,
                editable: false,
                editoptions: {}
            },
            {
                name: 'sampleNumber',
                index: 'sampleNumber',
                hidden: false,
                editable: false,
                editoptions: {}
            },
            {
                name: 'qualitativeValue',
                index: 'qualitativeValue',
                hidden: false,
                editable: false,
                editoptions: {},
                stype: 'select',
                searchoptions: {
                    sopt: ['eq'],
                    value:
                        ':;' +
                        '01pass:' + translateMessages('samplesGrid.01pass')
                }
            },
        ],
        pager: "#jqGridPager",
        gridComplete: function () {
            var grid = $('#grid');
            var rows = grid.jqGrid('getDataIDs');
            if ($scope.config.readOnly) {
                for (i = 0; i < rows.length; i++) {
                    $("tr#" + rows[i] + " div.ui-inline-edit").hide();
                }
            }
            $('#rows-num').text('(' + grid.getGridParam('records') + ')');
        },
        onSelectRow: function (rowid, status) {
        },
        beforeSelectRow: function (rowid, e) {
            var $td = $(e.target).closest("tr.jqgrow>td");
            if ($td.length > 0) {
                var $grid = $(this);
                var i = $.jgrid.getCellIndex($td);
                var cm = $grid.jqGrid('getGridParam', 'colModel');
                if (cm[i].name === 'act') {
                    if (e.target.className === 'ui-icon ui-icon-cancel') {
                        return false;
                    }
                    $grid.jqGrid('resetSelection');
                    return true;
                }
                return (cm[i].name === 'cb');
            }
            return false;
        },
        beforeRequest: function () {
            $.cookie("jqgrid_conf", JSON.stringify({
                rowNum: $(this).getGridParam("rowNum")
            }));
        },
        ajaxRowOptions: {
            contentType: "application/json"
        },
        serializeRowData: function (postdata) {
            // 1. Xóa thuộc tính mặc định của jqGrid
            delete postdata.oper;

            // 2. Xóa các cột thuộc tính động (theo logic cũ của bạn)
            angular.forEach(columnConfiguration, function (columnInGrid, key) {
                if (columnInGrid.forAttribute) {
                    delete postdata[columnInGrid.name];
                }
            });

            // 3. THÊM MỚI: Xóa các trường không có dữ liệu (rỗng, null, undefined)
            Object.keys(postdata).forEach(function (key) {
                var value = postdata[key];
                if (value === "" || value === null || value === undefined) {
                    delete postdata[key];
                }
            });

            // 4. Trả về dữ liệu sau khi đã được "làm sạch" qua hàm validate
            return validateSerializeData(postdata);
        },
        beforeSubmit: function (postdata, formid) {
            return [false, 'ble'];
        }
    };

    function prepareGridConfig(config) {
        var c = $.cookie("jqgrid_conf");
        if (c) {
            $.extend(config, JSON.parse(c));
        }

        var readOnlyInType = function (outDocument, inBufferDocument, columnIndex) {
            if (outDocument && (columnIndex === 'expirationDate' || columnIndex === 'productionDate' ||
                columnIndex === 'price' || columnIndex === 'waste' ||
                columnIndex === 'palletNumber' || columnIndex === 'typeOfPallet' || columnIndex === 'storageLocation')) {
                return true;
            }
            if ((columnIndex === 'resource') && (inBufferDocument || !outDocument)) {
                return true;
            }
            if (columnIndex === 'lastResource') {
                return true;
            }
            if (!outDocument && columnIndex === 'sellingPrice') {
                return true;
            }

            return false;
        };

        $http({
            method: 'GET',
            url: '../../rest/rest/qualityStandardSamplesRes/gridConfig/' + config.qic_id + '.html'

        }).then(function successCallback(response) {
            columnConfiguration = response.data.columns;
            config.readOnly = response.data.readOnly;
            config.inBufferDocument = response.data.inBufferDocument;
            config.suggestResource = !response.data.inBufferDocument && response.data.suggestResource;
            config.outDocument = response.data.outDocument;
            config.directionConvertingQuantityAfterChangingConverter = response.data.directionConvertingQuantityAfterChangingConverter;

            var columns = [
                getColModelByIndex('id', config),
                getColModelByIndex('qualityInspectionCommandRe', config)];
            var colNames = ['ID', 'qualityInspectionCommandRe'];

            angular.forEach(response.data.columns, function (columnInGrid, key) {
                var gridColModel = getColModelOrPrepareForAttribute(columnInGrid, config);

                if (!columnInGrid.checked) {
                    gridColModel.hidden = true;
                    gridColModel.editrules = gridColModel.editrules || {};
                    gridColModel.editrules.edithidden = true;
                }
                if (gridColModel.editoptions) {
                    delete gridColModel.editoptions.disabled;
                    delete gridColModel.editoptions.readonly;
                }
                if (readOnlyInType(config.outDocument, config.inBufferDocument, columnInGrid.name)) {
                    gridColModel.editoptions = gridColModel.editoptions || {};
                    if (gridColModel.edittype === 'select' || gridColModel.edittype === 'checkbox') {
                        gridColModel.editoptions.disabled = 'disabled';
                    } else {
                        gridColModel.editoptions.readonly = 'readonly';
                    }
                }

                if (columnInGrid.forAttribute && config.outDocument) {
                    gridColModel.editoptions.readonly = 'readonly';
                }

                columns.push(gridColModel);
                if (columnInGrid.forAttribute) {
                    colNames.push(columnInGrid.name);
                } else {
                    colNames.push(QCD.translate('samplesGrid.gridColumn.' + columnInGrid.name));
                }
            });

            config.colModel = columns;
            config.colNames = colNames;

            $http({
                method: 'GET',
                url: '../../rest/rest/qualityStandardSamplesRes/qualityEvaluationOptions'
            }).then(function successCallback(response) {
                var selectOptionsQualityEvaluationOptions = [':' + translateMessages('samplesGrid.allItem')];
                var selectOptionsQualityEvaluationOptionsEdit = [':' + translateMessages('samplesGrid.emptyItem')];
                angular.forEach(response.data, function (value, key) {
                    selectOptionsQualityEvaluationOptions.push(value.key + ':' + value.value);
                    selectOptionsQualityEvaluationOptionsEdit.push(value.key + ':' + value.value);
                });

                getColModelByIndex('quantitativeEvaluation', config).editoptions.value = selectOptionsQualityEvaluationOptionsEdit.join(';');
                getColModelByIndex('quantitativeEvaluation', config).searchoptions.value = selectOptionsQualityEvaluationOptions.join(';');

                getColModelByIndex('qualitativeResult', config).editoptions.value = selectOptionsQualityEvaluationOptionsEdit.join(';');
                getColModelByIndex('qualitativeResult', config).searchoptions.value = selectOptionsQualityEvaluationOptions.join(';');;

                $http({
                    method: 'GET',
                    url: '../../rest/units'
                }).then(function successCallback(response) {
                    selectOptionsUnits = [':' + translateMessages('samplesGrid.allItem')];
                    angular.forEach(response.data, function (value, key) {
                        selectOptionsUnits.push(value.key + ':' + value.value);
                    });

                    getColModelByIndex('unit', config).searchoptions.value = selectOptionsUnits.join(';');

                    var newConfig = {};
                    newConfig = angular.merge(newConfig, config);
                    $scope.config = newConfig;
                    $('#gridWrapper').unblock();

                }, errorCallback);
            }, errorCallback);
        }, errorCallback);

        return config;
    }
    $scope.qicIdChanged = function (id) {
        config.url = '../../rest/rest/qualityStandardSamplesRes/' + id + '.html';
        config.qic_id = id;

        config.colModel.filter(function (element, index) {
            return element.index === 'qualityInspectionCommandRe';
        })[0].editoptions.defaultValue = id;

        prepareGridConfig(config);
    };

    $scope.data = [];

    // dont close inline edit after fail validations
    // gọi đến server xử lý
    $.extend($.jgrid.inlineEdit, { restoreAfterError: false });

    $.jgrid.edit = $.jgrid.edit || {};
    $.jgrid.edit.addCaption = '';
    $.jgrid.edit.editCaption = '';
    $.jgrid.edit.bSubmit = '<label>' + $.jgrid.edit.bSubmit + '</label>';
    $.jgrid.edit.bCancel = '<label>' + $.jgrid.edit.bCancel + '</label>';

    $.extend(true, $.jgrid.inlineEdit, {
        beforeSaveRow: function (option, rowId) {
            if (rowId === '0') {
                option.url = '../../rest/rest/qualityStandardSamplesRes.html';
                option.errorfunc = errorfunc;
                option.successfunc = successfunc;
                option.aftersavefunc = aftersavefunc;
            } else {
                option.url = '../../rest/rest/qualityStandardSamplesRes/' + rowId + '.html';
                option.errorfunc = errorfunc;
                option.successfunc = successfunc;
                option.aftersavefunc = aftersavefunc;
            }
            option.mtype = 'PUT';
        }
    });

    // disable close modal on off click
    $.jqm.params.closeoverlay = false;
}]);

