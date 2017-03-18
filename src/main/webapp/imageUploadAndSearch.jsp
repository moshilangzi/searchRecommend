<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>





    <head>
        <link rel="stylesheet" type="text/css" class="ui" href="dist/semantic.css">
        <script src="js/jquery.min.js"></script>
        <script src="js/simpleUpload.min.js"></script>
        <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
        <script src="dist/semantic.js"></script>
        <title>TODO supply a title</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <script type="text/javascript">
            $(document).ready(function () {













            });
            function showSimi(result1) {
                $('#imgList_similarity').empty();
                result=JSON.parse(result1)

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

            function imageUploadAndSearch(){
                alert("test")
                distanceType=$('#distanceMeasureName').val();
                $('#fileId').simpleUpload("http://localhost:8080/inner/srservice/imageUploadAndSearch.json?distanceType="+distanceType, {

//                        start: function(file){
//                            //upload started
//                            $('#filename').html(file.name);
//                            $('#progress').html("");
//                            $('#progressBar').width(0);
//                        },

//                        progress: function(progress){
//                            //received progress
//                            $('#progress').html("Progress: " + Math.round(progress) + "%");
//                            $('#progressBar').width(progress + "%");
//                        },

                    success: function(data){
                        //upload successful
                        //$('#progress').html("Success!<br>Data: " + JSON.stringify(data));
                        alert("success")
                        showSimi(data)
                        $('.ui.modal')
                                .modal('show')
                        ;

                    },

                    error: function(error){
                        //upload failed
                        alert(error.message)

                        //$('#progress').html("Failure!<br>" + error.name + ": " + error.message);
                    }

                });


            }




        </script>
    </head>
    <body>
        <h1 class="ui header centered blue">以图搜图</h1>













        <div class="ui divider"></div>

            //图片上传表单


                <div class="field">
                    <label>image similarity compute algorithm：</label>
                    <select class="ui dropdown" id="distanceMeasureName" name="distanceType">
                        <option value="chi2">chi2</option>
                        <option value="euclidean">euclidean</option>
                        <option value="canberra">canberra</option>
                        <option value="manhattan">manhattan</option>
                        <option value="chebyshev">chebyshev</option>



                    </select>
                </div>
                <div class="field">
                    <label>上传图片</label>
                    <input type="file" name="file" id="fileId">
                </div>

                <button onclick="javascript:imageUploadAndSearch();" class="ui button" type="button">提交图片</button>
            </form>




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
