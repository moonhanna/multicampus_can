<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
<meta charset="EUC-KR">
<title>fcm</title>
</head>
<body>
<h1>fcm web</h1>
<form action="fcm" method="post">
IP<input type="text" name="ip"><br>
SPEED<input type="text" name="speed"><br>
<input type="submit" value="send">
</form>
</body>
</html>
