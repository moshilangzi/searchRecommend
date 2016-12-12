<%@ page contentType="text/html;charset=utf-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>test</title>

    <script src="http://static4.comm.com/3rd-lib/jquery-1.8.3.min.js" type="text/javascript"
            charset="utf-8"></script>
    <script type="text/javascript">
        $(function () {

            $('#call').click(function () {
                var action = $('#action option:selected').val();
                var target = $('input:radio:checked').val();
                $('#api').attr('action', action)
                        .attr('target', target)
                        .submit();
            });


        });
    </script>

</head>

<body>
<H1>API Test</H1>

<form id="api" action="/inner/searchRecommend/recommend.json" method="post">
    <table>
        <tr>
            <td>接口:</td>
            <td>
                <select id="action" name="action" style="width: 300px; height: 30px;">
                    <option value="/inner/searchRecommend/recommend.json">
                        用户推荐：/inner/searchRecommend/recommend.json
                    </option>
                    <option value="/inner/searchRecommend/search.json">
                        用户搜索：/inner/searchRecommend/search.json
                    </option>


                </select>
            </td>
        </tr>
        <tr>
            <td>traceID:</td>
            <td><input type="text" style="width: 300px; height: 25px;" name="traceID"/></td>
        </tr>
        <tr>
            <td>systemID:</td>
            <td><input type="text" style="width: 300px; height: 25px;" name="systemID"/></td>
        </tr>
        <tr>
            <td>params:</td>
            <td><textarea rows="5" cols="5" name="params" style="width: 300px"></textarea></td>
        </tr>
        <tr>
            <td>调用窗口打开方式:</td>
            <td>
                <label><input type="radio" name="target" value="_blank"
                              checked="checked"/>新窗口</label>&nbsp;&nbsp;&nbsp;&nbsp;
                <label><input type="radio" name="target" value="_self"/>本窗口</label>&nbsp;&nbsp;&nbsp;&nbsp;
            </td>
        </tr>
    </table>
    <br/>
    <input id="call" type="submit" value="调用"/><br/>
</form>

</body>
</html>



