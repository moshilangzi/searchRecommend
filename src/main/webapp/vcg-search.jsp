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
            //  alert(textType);
            scoreScript = $('#scoreScript').val();
            fetchSize = $('#fetchSize').val();
            newImageMeragePolicy=$('#newImageMeragePolicy').val();
            ifUseSecondSortBasedDate=$('#ifUseSecondSortBasedDate').is(":checked");

            //alert(scoreScript)


            //  alert(text);
            paramsObj = new Object();
            paramsObj.queryText = inputText;
            paramsObj.fetchSize=fetchSize;
            paramsObj.scoreScript = scoreScript;
            paramsObj.newImageMeragePolicy=newImageMeragePolicy;
            paramsObj.ifUseSecondSortBasedDate=ifUseSecondSortBasedDate;
            paramsStr = JSON.stringify(paramsObj);


          alert(paramsStr)
            paramsStr= encodeURIComponent(paramsStr);
          //  alert(paramsStr)

            $.ajax({
                url: "http://localhost:8080/inner/srservice/search.json",
                dataType: "json",
                type: "GET",
                data: "params=" + paramsStr,
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
            $('#terms_trs').empty();

            code = result.code;
            if (code == 200) {

                groups = result.data;
                if (groups.length == 0) {
                    alert("结果为空!");
                }
                alert(result.toString)
                alert(groups.length)
                content=''
                for (index = 0; index < groups.length; index++) {

                    image = groups[index];
                    alert(image.url);

                        //content+="<tr><td>'"+image.resId+"'</td>"
                        content=content+"<img src ="+image.url+">";
                    //content+="</tr>"









                }
                $('#terms_trs').append(content)


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
       概念定义:
    </div>
    <ul class="list">
        <li>h表示以当前时间为基准,过去多少小时上传的图片被认为最新图片</li>
        <li>时间二次排序表示搜索过程中不对图片进行日期相关的排序,仅仅针对最终的搜索结果进行二次排序将最新图片打乱加入结果集中
        ,打乱的方式：
            1：简单的每页一半新图放在好图后面，2：新图和好图随机打乱
        </li>
        <li>如果不选择二次排序,则系统将默认利用es的score机制基于已有的payload总分加上日期相关的权重进行排序</li>
    </ul>
</div>
<div class="ui input">

    <input type="text" placeholder="返回数量" id="fetchSize">
</div>
<div class="ui input ">

    <input type="text" placeholder="请输入查询内容" id="queryStr">
</div>

<select class="ui dropdown" id="newImageMeragePolicy">
    <option value="0">随机打乱</option>
    <option value="1">新图在后好图在前</option>
    <%--<option value="2">间隔交叉</option>--%>
</select>

<div class="ui  checkbox hidden">
    <input type="checkbox" id="ifUseSecondSortBasedDate"  >
    <label>时间二次排序</label>
</div>

<div class="ui massive icon input">
    <input type="text" placeholder="scoreScript" id="scoreScript">
</div>


<button class="ui primary button" onclick="javascript:search();">查询</button>
<div id="progressBar" class="ui active medium inline loader"></div>
<table class="ui single small compact line table">


    <tbody id="terms_trs">




    </tbody>


</table>









</body>
</html>
