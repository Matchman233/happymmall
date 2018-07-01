<%@ page contentType="text/html; charset=utf-8" language="java" %>
<html>
<body>
<h2>Hello World!</h2>

springMVC上传文件
<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="uploadFile"/>
    <input type="submit" value="上传文件"/>
</form>

富文本上传文件
<form name="form2" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="uploadFile"/>
    <input type="submit" value="上传图片"/>
</body>
</html>
