<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<html>





    <head>
        <link rel="stylesheet" type="text/css" class="ui" href="dist/semantic.css">
        <script src="js/jquery.min.js"></script>
        <%--<link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">--%>
        <%--<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>--%>
        <script src="dist/semantic.js"></script>
        <title>TODO supply a title</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <script type="text/javascript">

            $(document).ready(function () {

                //alert('<spring:eval expression="@srProperties.getProperty('redis.ip')" />')
                pageSearch();







            });

            function processSearchResult(result) {
                $('#imgList').empty();

                code = result.code;
                if (code == 200) {

                    groups = result.data.result;
                    if (groups.length == 0) {
                        alert("结果为空!");
                    }
//
                    content = ''
                    for (index = 0; index < groups.length; index++) {


                        image = groups[index];

                        content=content+"<div class='card'> ";
                        content=content+"<div class='image'>"+"<img src="+image.url+">"+"</div>";



                        content=content+"<div class='content'>";
                        content=content+"<div class='meta'>名称: "+image.imageId+" </div>";
                        content=content+"<div class='meta'><button class='ui primary button' onclick='javascript:search("+image.imageId+");'>相似图片搜索</button></div>";





                        content=content+"</div></div>";


                        //content+="</tr>"


                    }
                    $('#imgList').append(content)



                }
                else {
                    alert(result.msg);
                }


            }
            function pageSearch(){
                pageNum = $('#pageNum').val();
                fetchSize = $('#fetchSize').val();
                paramsObj = new Object();
                paramsObj.fetchSize = fetchSize;
                paramsObj.pageNum = pageNum;
                paramsStr = JSON.stringify(paramsObj);


               // alert(paramsStr)
                paramsStr = encodeURIComponent(paramsStr);

                $.ajax({
                    url: "<spring:eval expression="@srProperties.getProperty('srserviceUrlPrefix')" />"+"/inner/srservice/imageSearch.json",
                    dataType: "json",
                    type: "GET",
                    data: "params=" + paramsStr,
                    success: function (data) {
                        processSearchResult(data);
                    },

                    error: function (e) {
                        //called when there is an error
                        alert(e.data);

                    }
                });


            }



            function search(id) {

                distanceMeasureName = $('#distanceMeasureName').val();
                //  alert(text);
                paramsObj = new Object();
                matchedTopNum=20;
                paramsObj.matchedTopNum = matchedTopNum;
                paramsObj.imageId = id;
                paramsObj.distanceType = distanceMeasureName;
                paramsStr = JSON.stringify(paramsObj);
                //alert(paramsStr)


                $.ajax({
                    url: "<spring:eval expression="@srProperties.getProperty('srserviceUrlPrefix')" />"+"/inner/srservice/imageSearch.json",
                    dataType: "json",
                    type: "GET",
                    data: "params=" + encodeURI(paramsStr),
                    success: function (data) {
                       // alert(data.toString)
                        showSimi(data);
                        $('.ui.modal')
                                .modal('show')
                        ;
                    },

                    error: function (e) {
                        //called when there is an error
                        alert("error to request");


                    }
                });







            }
            function showSimi(result) {
                $('#imgList_similarity').empty();

                code = result.code;
                if (code == 200) {

                    groups = result.data.result;
                    if (groups.length == 0) {
                        alert("结果为空!");
                    }
//
                    content = ''
                    for (index = 0; index < groups.length; index++) {


                        image = groups[index];

                        content=content+"<div class='card'> ";
                        content=content+"<div class='image'>"+"<img src="+image.url+">"+"</div>";



                        content=content+"<div class='content'>";
                        content=content+"<div class='meta'>名称: "+image.imageId+" </div>";





                        content=content+"</div></div>";


                        //content+="</tr>"


                    }
                    $('#imgList_similarity').append(content)



                }
                else {
                    alert(result.msg);
                }


            }



        </script>
    </head>
    <body>
        <h1 class="ui header centered blue">shoe 以图搜图结果验证</h1>
        <div class="ui input">


            <input type="text" value="100" placeholder="每页数量" id="fetchSize" disabled>
        </div>
        <div class="ui input">

            <input type="text" placeholder="页数" id="pageNum" value="1">
        </div>




        <button class="ui primary button" onclick="javascript:pageSearch();">查询</button>


        <label class="ui">image similarity compute algorithm：</label>
        <select class="ui dropdown" id="distanceMeasureName">
            <option value="chi2">chi2</option>
            <option value="euclidean">euclidean</option>
            <option value="canberra">canberra</option>
            <option value="manhattan">manhattan</option>
            <option value="chebyshev">chebyshev</option>



        </select>
        <%--<label>feature type：</label>--%>
        <%--<select class="ui dropdown" id="feature">--%>
            <%--<option value="CNN">CNN</option>--%>
            <%--<option value="shape">shape</option>--%>
            <%--<option value="color">color</option>--%>



        <%--</select>--%>


        <%--<button class="ui primary button" onclick="javascript:clusterImgs();">聚类</button>--%>
        <%--<div id="progressBar" class="ui active medium inline loader"></div>--%>
        <div class="ui divider"></div>


        <div class="ui cards" id="imgList">
            </div>


        <div class="ui modal">
            <i class="close icon"></i>
            <div class="header">
               相似图片
            </div>
            <div class="ui cards" id="imgList_similarity">

            </div>



            <div class="actions">



            </div>
        </div>

        <%--<div id="dialog-message" title="Download complete" hidden>--%>
           <%----%>
        <%--</div>--%>











    </body>
</html>
