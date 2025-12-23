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
    $.event.special[j] = {setup: function () {
            if (!e[f] && this[k]) {
                return false
            }
            var l = $(this);
            a = a.add(l);
            $.data(this, d, {w: l.width(), h: l.height()});
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
        }};
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
    }}
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

                    var positionsHeader = QCD.translate('documentGrid.gridHeader.positions');

                    var gridTitle = '<div class="gridTitle">' + positionsHeader + ' <span id="rows-num">(0)</span></div>';

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

function documentIdChanged(id) {
    saveAllRows();
    angular.element($("#GridController")).scope().documentIdChanged(id);
    return false;
}

function getSelectedRowId() {
    return jQuery('#grid').jqGrid('getGridParam', 'selarrrow');
}

function addNewRow() {
    angular.element($("#GridController")).scope().addNewRow();
}

function deleteRow() {
    angular.element($("#GridController")).scope().deleteRow();
}

function openLookup(name, parameters) {
    var lookupHtml = '/lookup.html'
    if (name == 'attribute') {
        mainController.openModal('body', '../' + name + "/" + parameters.custom_attr_name + lookupHtml, false, function onModalClose() {
        }, function onModalRender(modalWindow) {
        }, {width: 1000, height: 560});
    } else {
        if (parameters) {
            var urlParams = $.param(parameters);
            lookupHtml = lookupHtml + "?" + urlParams;
        }
        mainController.openModal('body', '../' + name + lookupHtml, false, function onModalClose() {
        }, function onModalRender(modalWindow) {
        }, {width: 1000, height: 560});
    }
}

function updateFieldValue(field, value, rowId) {
    // edit inline
    var selector =  $("[id='"+rowId + '_' + field+"']");

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
        recordName = recordName.replace('attribute/','');
        var rowId = $('#product').length ? null : jQuery('#grid').jqGrid('getGridParam', 'selrow');
        var field = updateFieldValue(recordName, code, rowId);
        if(recordName == "batch") {
            if( row.id == 0) {
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
                callback({entities: [], numberOfResults: 0});
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

        function getDocumentId() {
            if (context) {
                var contextObject = JSON.parse(context);
                if (contextObject && contextObject['window.generalTab.form.id']) {
                    return contextObject['window.generalTab.form.id'];
                }
            }

            var config = angular.element($("#GridController")).scope().config;

            return config ? config.document_id : 0;
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

        // function getColModelOrPrepareForAttribute(columnProperties, c) {
        //     c = c || $scope.config;
        //     var col = c.colModel.filter(function (element, i) {
        //         return element.index === columnProperties.name;
        //     })[0];
        //     if (columnProperties.forAttribute) {
        //         var attrColModel = {};
        //         attrColModel.name = columnProperties.name;
        //         attrColModel.index = "attrs."+columnProperties.name;
        //         attrColModel.jsonmap = "attrs."+columnProperties.name;
        //         attrColModel.editable = true;
        //         if(columnProperties.attributeDataType == '01calculated') {
        //             attrColModel.edittype = 'custom';
        //             var editoptions = {};
        //             editoptions.custom_element = attributeLookup_createElement;
        //             editoptions.custom_value = lookup_value;
        //             editoptions.custom_attr_name = columnProperties.name;
        //             attrColModel.editoptions = editoptions;
        //         } else if(columnProperties.attributeValueType == '02numeric') {
        //             attrColModel.formatter = numberFormatter;
        //             attrColModel.unformat = numberUnformat;
        //              attrColModel.edittype = 'custom';
        //              var editoptions = {};
        //              editoptions.custom_element = attribute_createElement;
        //              editoptions.custom_value = input_value;
        //              editoptions.custom_attr_name = columnProperties.name;
        //              attrColModel.editoptions = editoptions;
        //         } else {
        //              var editoptions = {};
        //              attrColModel.editoptions = editoptions;
        //         }

        //         col = attrColModel;
        //     } else if (!col) {
        //         console.error(index);
        //     }
        //     return col;
        // }

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

        function sampleNumber_createElement(value, options) {
            var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);
            $input.attr('readonly', getColModelByIndex('sampleNumber').editoptions.readonly === 'readonly');

            var priceValue = value;
            var priceValueNew;
            $($input).bind('change keydown paste input', function () {
                var t = $(this);

                window.clearTimeout(t.data("timeout"));
                priceValueNew = t.val();
                if (priceValue !== priceValueNew) {
                    priceValue = priceValueNew;

                    $(this).data("timeout", setTimeout(function () {
                        gridRunner(function () {
                            parseAndValidateInputNumber(t);
                        });
                    }, 500));
                }
            });

            return $input;
        }

        var config = {
            url: '../../rest/rest/qualityStandardSamplesRes/' + getDocumentId() + '.html',
            datatype: "json",
            height: '100%',
            autowidth: true,
            rowNum: 20,
            rowList: [20, 30, 50, 100, 200],
            sortname: 'qualityStandardL',
            toolbar: [true, "top"],
            rownumbers: false,
            altRows: true,
            multiselect: true,
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
                        defaultValue: getDocumentId()
                    }
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
                        // keys: true, // we want use [Enter] key to save the row and [Esc] to cancel editing.
                        // editOptions: gridEditOptions,
                        // url: '../../rest/rest/documentPositions/' + 1 + '.html',
                        // delbutton: false,
                        // onEdit: function (id) {
                        //     if (typeof (lastSel) !== "undefined" && id !== lastSel) {
                        //         cancelEditing(id);
                        //     }
                        //     prepareViewOnStartEdit();
                        //     gridEditOptions.url = '../../rest/rest/documentPositions/' + id + '.html';
                        //     lastSel = id;
                        // },
                        // afterRestore: function () {
                        //     cancelEditing();
                        //     prepareViewOnEndEdit();
                        //     $("#grid").trigger("reloadGrid");
                        //     viewRefresh();
                        // }
                    }
                },
                {
                    name: 'sampleNumber',
                    index: 'sampleNumber',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    // formatter: numberFormatter,
                    // unformat: numberUnformat,
                    editoptions: {
                        custom_element: sampleNumber_createElement,
                        custom_value: input_value,
                    }
                },
            ],
            pager: "#jqGridPager",
            gridComplete: function () {
                var grid = $('#grid');
                var rows = grid.jqGrid('getDataIDs');
                if ($scope.config.readOnly) {
                    for (i = 0; i < rows.length; i++)
                    {
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
                delete postdata.oper;
                postdata.attrs = {};
                angular.forEach(columnConfiguration, function (columnInGrid, key) {
                    if (columnInGrid.forAttribute) {
                        postdata.attrs[columnInGrid.name] = postdata[columnInGrid.name];
                        delete postdata[columnInGrid.name];
                    }
                });

                return validateSerializeData(postdata);
            },
            beforeSubmit: function (postdata, formid) {
                return [false, 'ble'];
            }
        };

        function prepareGridConfig(config) {
            var c = $.cookie("jqgrid_conf");
            if (c){
                $.extend(config, JSON.parse(c));
            }

            var readOnlyInType = function (outDocument, inBufferDocument, columnIndex) {
                if (outDocument && (columnIndex === 'expirationDate' || columnIndex === 'productionDate' ||
                        columnIndex === 'price' ||  columnIndex === 'waste' ||
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
                url: '../../rest/rest/qualityStandardSamplesRes/gridConfig/' + config.document_id + '.html'

            }).then(function successCallback(response) {
                columnConfiguration = response.data.columns;
                config.readOnly = response.data.readOnly;
                config.inBufferDocument = response.data.inBufferDocument;
                config.suggestResource = !response.data.inBufferDocument && response.data.suggestResource;
                config.outDocument = response.data.outDocument;
                config.directionConvertingQuantityAfterChangingConverter = response.data.directionConvertingQuantityAfterChangingConverter;

                var columns = [getColModelByIndex('id', config), getColModelByIndex('qualityInspectionCommandRe', config)];
                var colNames = ['ID', 'qualityInspectionCommandRe'];

                // angular.forEach(response.data.columns, function (columnInGrid, key) {
                //     var gridColModel = getColModelOrPrepareForAttribute(columnInGrid, config);

                //     if (!columnInGrid.checked) {
                //         gridColModel.hidden = true;
                //         gridColModel.editrules = gridColModel.editrules || {};
                //         gridColModel.editrules.edithidden = true;
                //     }
                //     if (gridColModel.editoptions) {
                //         delete gridColModel.editoptions.disabled;
                //         delete gridColModel.editoptions.readonly;
                //     }
                //     if (readOnlyInType(config.outDocument, config.inBufferDocument, columnInGrid.name)) {
                //         gridColModel.editoptions = gridColModel.editoptions || {};
                //         if (gridColModel.edittype === 'select' || gridColModel.edittype === 'checkbox') {
                //             gridColModel.editoptions.disabled = 'disabled';
                //         } else {
                //             gridColModel.editoptions.readonly = 'readonly';
                //         }
                //     }

                //     if (columnInGrid.forAttribute && config.outDocument) {
                //         gridColModel.editoptions.readonly = 'readonly';
                //     }

                //     columns.push(gridColModel);
                //     if(columnInGrid.forAttribute) {
                //         colNames.push(columnInGrid.name);
                //     } else {
                //         colNames.push(QCD.translate('documentGrid.gridColumn.' + columnInGrid.name));
                //     }
                // });

                config.colModel = columns;
                config.colNames = colNames;

                var newConfig = {};
                        newConfig = angular.merge(newConfig, config);
                        $scope.config = newConfig;
                        $('#gridWrapper').unblock();

                // $http({
                //     method: 'GET',
                //     url: '../../rest/typeOfPallets'
                // }).then(function successCallback(response) {
                //     var selectOptionsTypeOfPallets = [':' + translateMessages('documentGrid.allItem')];
                //     var selectOptionsTypeOfPalletsEdit = [':' + translateMessages('documentGrid.emptyItem')];
                //     angular.forEach(response.data, function (value, key) {
                //         selectOptionsTypeOfPallets.push(value.key + ':' + value.value);
                //         selectOptionsTypeOfPalletsEdit.push(value.key + ':' + value.value);
                //     });

                //     getColModelByIndex('typeOfPallet', config).editoptions.value = selectOptionsTypeOfPalletsEdit.join(';');
                //     getColModelByIndex('typeOfPallet', config).searchoptions.value = selectOptionsTypeOfPallets.join(';');

                //     $http({
                //         method: 'GET',
                //         url: '../../rest/units'
                //     }).then(function successCallback(response) {
                //         selectOptionsUnits = [':' + translateMessages('documentGrid.allItem')];
                //         angular.forEach(response.data, function (value, key) {
                //             selectOptionsUnits.push(value.key + ':' + value.value);
                //         });

                //         getColModelByIndex('unit', config).searchoptions.value = selectOptionsUnits.join(';');
                //         getColModelByIndex('givenunit', config).searchoptions.value = selectOptionsUnits.join(';');

                //         var newConfig = {};
                //         newConfig = angular.merge(newConfig, config);
                //         $scope.config = newConfig;
                //         $('#gridWrapper').unblock();

                //     }, errorCallback);
                // }, errorCallback);

            }, errorCallback);

            return config;
        }
        $scope.documentIdChanged = function (id) {
            config.url = '../../rest/rest/qualityStandardSamplesRes/' + id + '.html';
            config.document_id = id;

            config.colModel.filter(function (element, index) {
                return element.index === 'qualityInspectionCommandRe';
            })[0].editoptions.defaultValue = id;

            prepareGridConfig(config);
        };
}]);

