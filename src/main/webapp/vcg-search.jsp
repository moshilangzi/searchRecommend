<%@ page contentType="text/html;charset=utf-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"><!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<html>
<head>
    <link rel="stylesheet" type="text/css" class="ui" href="dist/semantic.css">
    <script src="js/jquery.min.js"></script>

    <script src="dist/semantic.js"></script>
    <title>TODO supply a title</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script type="text/javascript">
        $(document).ready(function () {
            $('#progressBar').css('visibility', 'hidden');
        });
        function search() {
            $('.content').remove();

            $('#progressBar').css('visibility', 'visible');
            inputText = $('#queryStr').val();
            serviceName = $('#serviceName').val();
            //  alert(textType);
            pageNum = $('#pageNum').val();
            fetchSize = $('#fetchSize').val();
            newImageMeragePolicy = $('#newImageMeragePolicy').val();
            ifUseSecondSortBasedDate = $('#ifUseSecondSortBasedDate').is(":checked");

            //alert(scoreScript)


            //  alert(text);
            paramsObj = new Object();
            paramsObj.queryText = inputText;
            paramsObj.fetchSize = fetchSize;
            paramsObj.pageNum = pageNum;
            paramsObj.newImageMeragePolicy = newImageMeragePolicy;
            paramsObj.ifUseSecondSortBasedDate = ifUseSecondSortBasedDate;
            paramsStr = JSON.stringify(paramsObj);


            alert(paramsStr)
            paramsStr = encodeURIComponent(paramsStr);
            //  alert(paramsStr)

            $.ajax({
                url: "http://localhost:8080/inner/srservice/search.json",
                dataType: "json",
                type: "GET",
                data: "serviceName="+serviceName+"&params=" + paramsStr,
                success: function (data) {
                    $('#progressBar').css('visibility', 'hidden');
                    processSearchResult(data);
                },

                error: function (e) {
                    //called when there is an error
                    alert("error to request");
                    $('#progressBar').css('visibility', 'hidden');

                }
            });
        }

        function processSearchResult(result) {
            $('#imgList').empty();

            code = result.code;
            if (code == 200) {

                groups = result.data.result;
                if (groups.length == 0) {
                    alert("结果为空!");
                }
                alert(result.toString)
                alert(groups.length)
                content = ''
                for (index = 0; index < groups.length; index++) {


                    image = groups[index];
                    alert(image.url);
                    dateStr=image.uploadTime;


                    content=content+"<div class='card'><div class='image'><img src='"+image.url+"'></div>";
                    content=content+"<div class='meta'>上传日期: "+dateStr+" </div>";
                    content=content+"<div class='meta'>id: "+image.id+" </div>";
                    content=content+"<div class='meta'>score: "+image.score+" </div>";
                    content=content+"<div class='meta'>imageId: "+image.id+" </div></div>";
                    content=content+"<div class='meta'>imageStatistic: "+image.imageKwIdStatistics+" </div></div>";

                    //content+="</tr>"


                }
                $('#imgList').append(content)
                message="<li>"+result.data.exInfo+"</li>"
                $('#queryAnalyzerMessage').empty();
                $('#queryAnalyzerMessage').append(message)




            }
            else {
                alert(result.msg);
            }


        }

    </script>
</head>
<body>
<h1 class="ui header centered blue">vcg搜索</h1>

<div class="ui message">
    <div class="header">
        当前查询解析：
    </div>
    <ul class="list" id="queryAnalyzerMessage">



    </ul>
</div>
<div class="ui input">



    <select class="ui dropdown" id="serviceName" disabled>
        <option value="vcgOnlineMockService">vcg线上搜索结果验证服务</option>

        <option value="vcgSearchTestService">vcgSearchTestService</option>
    </select>
</div>
<div class="ui input">


    <input type="text" value="100" placeholder="每页数量" id="fetchSize" disabled>
</div>
<div class="ui input">

    <input type="text" placeholder="页数" id="pageNum">
</div>
<%--<div class="ui input">--%>

    <%--<input type="text" placeholder="每页新图百分比" id="newImagePercentage">--%>
<%--</div>--%>
<div class="ui input ">

    <input type="text" placeholder="请输入查询内容" id="queryStr">
</div>

<%--<select class="ui dropdown" id="newImageMeragePolicy">--%>
    <%--<option value="0">随机打乱</option>--%>
    <%--<option value="1">新图在后好图在前</option>--%>
    <%--&lt;%&ndash;<option value="2">间隔交叉</option>&ndash;%&gt;--%>
<%--</select>--%>

<%--<div class="ui  checkbox hidden">--%>
    <%--<input type="checkbox" id="ifUseSecondSortBasedDate">--%>
    <%--<label>时间二次排序</label>--%>
<%--</div>--%>




<button class="ui primary button" onclick="javascript:search();">查询</button>
<div id="progressBar" class="ui active medium inline loader"></div>

<%--<div class="ui eight column grid" id="imgList">--%>
    <%--&lt;%&ndash;<div class="column"><div class="ui segment"><img></div></div>&ndash;%&gt;--%>


<%--</div>--%>

<div class="ui six  cards" id="imgList">
    <%--<div class="ui card"><div class="image"><img></div>--%>
    </div>
<%--<table class="ui single small compact line table">--%>


    <%--<tbody id="terms_trs">--%>


    <%--</tbody>--%>


<%--</table>--%>


</body>
</html>
