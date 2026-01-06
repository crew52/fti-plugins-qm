/**
 * ============================================================================
 * Tổng quan file
 * ============================================================================
 *
 * File này cấu hình jqGrid để hiển thị và chỉnh sửa samples của
 * lệnh kiểm tra chất lượng (QIC):
 *
 * - Tạo directive Angular ngJqGrid để gắn jqGrid vào DOM.
 *
 * - Định nghĩa controller GridController để:
 *   + Tính toán lại kích thước grid (responsive).
 *   + Cấu hình colModel, editOptions, nguồn dữ liệu, toolbar.
 *   + Gọi REST /rest/rest/qualityStandardSamplesRes/... để lấy
 *     config cột + data.
 *   + Xử lý inline-edit (lưu hàng, hiển thị thông báo).
 *
 * - Thêm logic nghiệp vụ auto-đánh giá quantitativeEvaluation
 *   dựa trên quantitativeResult, upValue, downValue.
 *
 * ============================================================================
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

/**
 * ============================================================================
 * Các hàm tiện ích chung (ngoài controller)
 * ============================================================================
 **/
var myApp = angular.module('gridApp', []);

/**
 * gridRunner(action)
 *
 * Bọc một action() trong loading indicator của Qcadoo:
 * - Gọi QCD.components.elements.utils.LoadingIndicator.blockElement(...) trước,
 * - Chạy action(),
 * - Gọi ...unblockElement(...) sau.
 *
 * Dùng khi cần thực hiện thao tác lâu, muốn chặn UI tạm thời
 * (ở file này hiện chưa dùng nhiều, nhưng là pattern từ
 * materialFlowResources).
 */
function gridRunner(action) {
    QCD.components.elements.utils.LoadingIndicator.blockElement(parent.$('body'));
    action();
    QCD.components.elements.utils.LoadingIndicator.unblockElement(parent.$('body'));
}

/**
 * parseAndValidateInputNumber($element)
 *
 * Chuẩn hóa và validate input số (dùng chung cho các input kiểu customNumber):
 * - Loại bỏ 0 ở đầu (00012.3 → 12.3).
 * - Cho phép format 12, 12.3, 12,3.
 * - Kiểm tra phần nguyên/fraction là số hợp lệ; nếu không,
 *   đánh dấu input bằng CSS class error-grid.
 * - Nếu hợp lệ, chuẩn hóa dấu phẩy thành dấu chấm,
 *   cập nhật lại giá trị.
 *
 * Trả về giá trị sau khi chuẩn hóa (hoặc nguyên gốc nếu lỗi).
 */

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
/**
 * Directive ngJqGrid
 *
 * js
 * myApp.directive('ngJqGrid', function ($window) { ... });
 *
 * Directive này chịu trách nhiệm:
 * - Gắn jqGrid vào DOM khi config có giá trị:
 *   + Xóa nội dung cũ của element,
 *   + Thêm <table id="grid"> và <div id="jqGridPager">,
 *   + Gọi $(table).jqGrid(newValue) để khởi tạo grid
 *     với config đã chuẩn bị trong controller.
 *
 * - Thêm tiêu đề samplesHeader và hiển thị tổng số rows.
 *
 * - Gọi filterToolbar để bật filter trên header.
 *
 * - Reset trạng thái samplesGrid là “chưa thay đổi”
 *   (setComponentChanged(false)).
 */
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

/**
 * validateSerializeData(data)
 *
 * Thu thập các element có class error-grid trong form edit
 * (#FrmGrid_grid hoặc #gridContainer).
 *
 * Hiện tại chỉ đơn giản trả về JSON.stringify(data).
 *
 * Được dùng trong serializeRowData để biến object postdata
 * thành JSON string gửi lên server.
 */
function validateSerializeData(data) {
    var elements = null;
    if ($('#FrmGrid_grid').length) {
        elements = $('.error-grid', '#FrmGrid_grid');

    } else {
        elements = $('.error-grid', '#gridContainer');
    }

    return JSON.stringify(data);
}

/**
 * roundTo(n)
 *
 * Làm tròn số n đến 5 chữ số thập phân,
 * bằng kỹ thuật e+places / e-places.
 *
 * Dùng để đảm bảo các giá trị số (nếu cần)
 * không bị quá nhiều chữ số.
 */
function roundTo(n) {
    var places = 5;
    return +(Math.floor(parseFloat(n) + "e+" + places) + "e-" + places);
}

/**
 * validatorNumber(val)
 *
 * validatorNumber: trả về true nếu val rỗng
 * hoặc là số bằng roundTo(val).
 */
function validatorNumber(val) {
    if (val === '') {
        return true;
    }

    return parseFloat(val) === roundTo(val);
}

/**
 * validateElement(el, validator)
 *
 * validateElement: dùng một validator (vd validatorNumber)
 * để đặt/bỏ class error-grid trên element.
 */
function validateElement(el, validator) {
    el = $(el);
    if (validator(el.val())) {
        el.removeClass('error-grid');

    } else {
        el.addClass('error-grid');
    }
}

/**
 * translateMessages(messages)
 *
 * Chuyển các message nội bộ (string, có thể là nhiều dòng)
 * thành message hiển thị cho user:
 * - Tách theo \n,
 * - Thay " thành &#039;,
 * - Dùng QCD.translate để dịch,
 * - Xử lý nội dung dạng ["something"] thành something,
 * - Gộp các dòng lại bằng \n.
 *
 * Được dùng cho text lỗi và text trên UI.
 */
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

/**
 * saveAllRows()
 *
 * Lấy tất cả id của rows trong grid và gọi saveRow(id)
 * cho từng row.
 *
 * Đảm bảo mọi edit inline được lưu khi QIC thay đổi
 * hoặc khi chuyển context.
 */
function saveAllRows() {
    var grid = $("#grid");
    var ids = grid.jqGrid('getDataIDs');

    for (var i = 0; i < ids.length; i++) {
        grid.saveRow(ids[i]);
    }
}

/**
 * viewRefresh()
 *
 * viewRefresh: gọi cancelEditing() trên scope GridController
 * để hủy edit inline.
 */
function viewRefresh() {
    angular.element($("#GridController")).scope().cancelEditing();
}

/**
 * refreshForm()
 *
 * refreshForm: gọi performRefresh() trên main view component
 * (form hoặc grid) để reload lại form/grid từ server.
 */
function refreshForm() {
    var mainViewComponent = mainController.getComponentByReferenceName("form") || mainController.getComponentByReferenceName("grid");
    if (mainViewComponent) {
        mainViewComponent.performRefresh();
    }
}

/**
 * qicIdChanged(id)
 *
 * Khi ID của QIC (quality inspection command) thay đổi:
 * - Gọi saveAllRows() để lưu edit hiện tại,
 * - Gọi $scope.qicIdChanged(id) trong controller để:
 *   + Đổi URL data config.url,
 *   + Đặt qic_id mới,
 *   + Gọi prepareGridConfig(config) để tải lại
 *     config cột & grid.
 */
function qicIdChanged(id) {
    saveAllRows();
    angular.element($("#GridController")).scope().qicIdChanged(id);
    return false;
}

/**
 * getSelectedRowId()
 *
 * Trả về danh sách selarrrow hiện tại của jqGrid,
 * dùng nếu cần biết row nào đang được chọn.
 */
function getSelectedRowId() {
    return jQuery('#grid').jqGrid('getGridParam', 'selarrrow');
}

// Controller GridController
var messagesController = new QCD.MessagesController();
var columnConfiguration;

myApp.controller('GridController', ['$scope', '$window', '$http', function ($scope, $window, $http) {

    // Lưu id row đang edit inline gần nhất.
    var lastSel;

    /**
    * showMessage(type, title, content)
    * - Wrapper gọi mainController.showMessage để hiển thị
    *   thông báo (thành công/thất bại).
    */
    function showMessage(type, title, content) {
        mainController.showMessage({
            type: type,
            title: title,
            content: content
        });
    }

    /**
     * quantitativeResult_createElement(value, options)
     *
     * Custom editor cho cột quantitativeResult (inline edit):
     *
     * - Tạo <input type="customNumber" ... rowId="...">.
     *
     * - Bind change/keydown/paste/input:
     *   + Debounce 300ms để user gõ xong.
     *   + Lấy rowId từ attribute rowId trên input.
     *   + Đọc qrRaw (giá trị mới của quantitativeResult).
     *
     * - Nếu qrRaw rỗng:
     *   + Xóa giá trị quantitativeEvaluation
     *     (set '' và trigger change).
     *   + Kết thúc.
     *
     * - Parse qr = parseFloat(qrRaw), nếu NaN thì bỏ qua.
     *
     * - Lấy rowData từ $('#grid').jqGrid('getRowData', rowId):
     *   + upRaw = rowData.upValue
     *   + downRaw = rowData.downValue
     *
     * - Nếu upRaw hoặc downRaw rỗng
     *   → không có dữ liệu chuẩn trên hàng này
     *   → bỏ qua, không auto set.
     *
     * - Parse up, down từ upRaw, downRaw; nếu NaN thì bỏ qua.
     *
     * - Nếu down ≤ qr ≤ up → newEval = '01pass',
     *   ngược lại newEval = '02fail'.
     *
     * - Tìm element #rowId_quantitativeEvaluation
     *   (select đang edit inline cho cột đánh giá):
     *   + Nếu đó là <span> wrapper
     *     → lấy select/input bên trong.
     *
     * - Set .val(newEval) và trigger change.
     *
     * Tóm lại: đây là chỗ hiện thực logic:
     * - Tự động cập nhật quantitativeEvaluation
     *   dựa trên quantitativeResult so với upValue/downValue.
     * - Nếu thiếu upValue/downValue thì không can thiệp.
     */
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

    /**
     * getQICId()
     *
     * Lấy ID của QIC cho grid:
     * - Nếu có global context (JSON string) với key
     *   'window.generalTab.form.id' → dùng giá trị đó.
     * - Nếu không, lấy từ $scope.config.qic_id
     *   (do controller đặt).
     * - Nếu vẫn chưa có, trả 0.
     *
     * Giá trị này được dùng để:
     * - Đặt URL cho grid
     *   (...qualityStandardSamplesRes/{qicId}.html),
     * - Fill defaultValue cho cột
     *   qualityInspectionCommandRe.
     */
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

    /**
     * getColModelByIndex(index, c)
     *
     * Tìm một colModel entry theo name == index trong config:
     * - c mặc định là $scope.config.
     *
     * Nếu không tìm thấy cột, log error
     * console.error(index).
     *
     * Dùng để chỉnh sửa cấu hình cột
     * (thêm searchoptions, editoptions.value, v.v.)
     * sau khi load gridConfig.
     */
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

    /**
     * getColModelOrPrepareForAttribute(columnProperties, c)
     *
     * Được gọi cho mỗi columnInGrid từ backend gridConfig.
     *
     * Nếu columnProperties.forAttribute == true:
     * - Tạo attrColModel cho cột thuộc tính động:
     *   + name = columnProperties.name.
     *   + editable = true.
     *
     * - Nếu attributeDataType == '01calculated':
     *   + Sử dụng custom lookup attributeLookup_createElement.
     *
     * - Nếu attributeValueType == '02numeric':
     *   + Sử dụng numberFormatter và custom number editor
     *     attribute_createElement.
     *
     * - Ngược lại, chỉ set editoptions rỗng.
     *
     * - Trả về attrColModel.
     *
     * Nếu không phải attribute:
     * - Tìm cột hiện có qua index === columnProperties.name.
     *
     * Mục đích:
     * - Map cấu hình meta từ backend
     *   thành colModel thực tế cho jqGrid.
     */
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

    /**
     * errorCallback(response)
     *
     * Dùng chung cho lỗi HTTP của $http:
     * - Lấy response.data.message,
     * - Dùng translateMessages,
     * - Hiển thị bằng showMessage('failure', ...).
     */
    function errorCallback(response) {
        showMessage('failure', QCD.translate('samplesGrid.notification.failure'), response.data.message);
    }

    /**
     * input_value(elem, operation, value)
     *
     * Custom custom_value cho editor jqGrid:
     * - Nếu operation === 'get' → trả $(elem).val().
     * - Nếu operation === 'set' → gán $('input', elem).val(value).
     *
     * Dùng cho quantitativeResult_createElement
     * (và các custom editor khác)
     * để jqGrid biết cách lấy/đặt giá trị.
     */
    function input_value(elem, operation, value) {
        if (operation === 'get') {
            return $(elem).val();

        } else if (operation === 'set') {
            return $('input', elem).val(value);
        }
    }

    /**
     * numberFormatter
     *
     * Format số hiển thị trong cell:
     * - Bọc giá trị trong
     *   <span class="number-cell">...</span>.
     */
    function numberFormatter(cellvalue, options, rowObject) {
        var val = cellvalue || '';
        return '<span class="number-cell">' + val + '</span>';
    }

    /**
     * numberUnformat
     *
     * Unformat số hiển thị trong cell:
     * - Lấy text bên trong span.
     */
    function numberUnformat(cellvalue, options, cell) {
        var val = $('span', cell).text();
        return val || '';
    }

    /**
     * errorfunc(rowID, response)
     *
     * Dùng cho jqGrid inline-edit error:
     * - Parse response.responseText để lấy .message,
     * - Dịch và hiển thị thông báo lỗi “failure”
     *   cho samples grid.
     */
    function errorfunc(rowID, response) {
        var message = JSON.parse(response.responseText).message;
        message = translateMessages(message);
        showMessage('failure', QCD.translate('samplesGrid.notification.failure'), message);
        return true;
    }

    /**
     * successfunc(rowID, response)
     *
     * Hiển thị thông báo “success”
     * khi lưu sample thành công.
     */
    function successfunc(rowID, response) {
        showMessage('success', QCD.translate('samplesGrid.notification.success'), QCD.translate('samplesGrid.message.saveMessage'));
        return true;
    }

    /**
     * aftersavefunc()
     *
     * Gọi refreshForm()
     * để reload lại form sau khi lưu row.
     */
    function aftersavefunc() {
        refreshForm();
    }

    /**
     * cancelEditing()
     *
     * Hủy inline-edit row hiện tại (lastSel):
     * - Gọi restoreRow(lastSel) trên jqGrid.
     * - Hiển thị lại icon edit/del,
     *   ẩn icon save/cancel.
     *
     * Gán lên $scope.cancelEditing
     * để gọi từ bên ngoài
     * (vd viewRefresh).
     */
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

    // Cấu hình grid (config) và các cột quan trọng
    /**
     * gridEditOptions
     * - keys: true → Enter để lưu, Esc để cancel.
     * - mtype: 'PUT' → sử dụng HTTP PUT.
     * - errorfunc, successfunc, aftersavefunc
     *   như mô tả trên.
     */
    var gridEditOptions = {
        keys: true,
        url: '../../rest/rest/qualityStandardSamplesRes.html', // base
        mtype: 'PUT',
        errorfunc: errorfunc,
        successfunc: successfunc,
        aftersavefunc: aftersavefunc
    };

    //  Đối tượng cấu hình jqGrid (URL dữ liệu, phân trang, cột, toolbar, v.v.).
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
                        gridEditOptions.url = '../../rest/rest/qualityStandardSamplesRes/' + id + '.html';
                        lastSel = id;
                    },
                    afterRestore: function () {
                        cancelEditing();
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
        /**
         * serializeRowData(postdata)
         *
         * Chạy trước khi gửi dữ liệu lên server khi lưu row:
         * - Xóa postdata.oper (tham số mặc định của jqGrid).
         * - Xóa các cột thuộc tính động (forAttribute).
         * - Xóa các key có giá trị "", null, undefined.
         *
         * - Gọi validateSerializeData(postdata)
         *   để trả về JSON string.
         *
         * → Mục tiêu: chỉ gửi những field
         *   có giá trị thực sự cần update.
         */
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

    /**
     * prepareGridConfig(config)
     *
     * Gọi backend:
     * - GET ../../rest/rest/qualityStandardSamplesRes/gridConfig/{qic_id}.html
     *
     *   + Lấy meta cấu hình cột
     *     (tên, checked, forAttribute, v.v.).
     *   + Dựa trên đó build config.colModel
     *     và config.colNames.
     *
     * - GET ../../rest/rest/qualityStandardSamplesRes/qualityEvaluationOptions
     *
     *   + Lấy options cho đánh giá
     *     (01pass, 02fail, ...).
     *   + Set editoptions.value
     *     + searchoptions.value cho:
     *       * quantitativeEvaluation
     *       * qualitativeResult
     *
     * - GET ../../rest/units
     *
     *   + Lấy danh sách đơn vị,
     *   + Set searchoptions.value
     *     cho cột unit.
     *
     * Cuối cùng:
     * - Merge config mới vào $scope.config,
     * - $('#gridWrapper').unblock()
     *   để bỏ loading.
     */
    function prepareGridConfig(config) {
        var c = $.cookie("jqgrid_conf");
        if (c) {
            $.extend(config, JSON.parse(c));
        }

        $http({
            method: 'GET',
            url: '../../rest/rest/qualityStandardSamplesRes/gridConfig/' + config.qic_id + '.html'

        }).then(function successCallback(response) {
            columnConfiguration = response.data.columns;
            config.readOnly = response.data.readOnly;

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

                if (columnInGrid.forAttribute) {
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

    /**
     * Inline-edit save hook
     *
     * js
     * $.extend(true, $.jgrid.inlineEdit, {
     *     beforeSaveRow: function (option, rowId) {
     *         if (rowId === '0') {
     *             option.url = '../../rest/rest/qualityStandardSamplesRes.html';
     *             ...
     *         } else {
     *             option.url = '../../rest/rest/qualityStandardSamplesRes/' + rowId + '.html';
     *             ...
     *         }
     *         option.mtype = 'PUT';
     *     }
     * });
     *
     * Trước khi jqGrid gọi saveRow:
     * - Nếu rowId = '0' (row mới)
     *   → gọi URL base không id.
     * - Nếu rowId != '0'
     *   → gọi URL REST với id .html.
     *
     * - Gắn errorfunc, successfunc, aftersavefunc
     *   vào option.
     */

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

