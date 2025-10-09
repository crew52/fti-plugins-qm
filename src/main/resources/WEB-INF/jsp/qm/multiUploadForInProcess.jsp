<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
String ctx = request.getContextPath();
%>

<script src="/basic/public/js/jquery.form.js"></script>
<script src="/basic/public/js/jquery.ui.widget.js"></script>
<script src="/basic/public/js/jquery.iframe-transport.js"></script>
<script src="/basic/public/js/jquery.fileupload.js"></script>
<script src="/qm/public/js/multiuploadforinprocessfunctions.js"></script>
<script src="/basic/public/js/bootstrap.min.js"></script>

<link href="/basic/public/css/dropzone.css" type="text/css" rel="stylesheet" />



<form id="fileupload" action="../../../rest/qm/multiUploadForInProcessFiles.html" method="POST" enctype="multipart/form-data">

    <input type="file" name="files[]" multiple>

    <div id="dropzone">${requestScope.translationsMap['qcadooView.fileupload.dropzone']}</div>

    <div id="maxUploadFileMessage" style="display: none;">${requestScope.translationsMap['qcadooView.errorPage.error.uploadException.maxSizeExceeded.explanation']}</div>

	<div id="progress" class="progress">
        <div class="progress-bar progress-bar-success"> </div>
    </div>

</form>
