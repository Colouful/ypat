$(function () {
    var imgpath = $("#imgpath").val();
    if(imgpath!=""){
        document.getElementById("viewImg").innerHTML = "<img style='width: 30%;height: 30%; float: left' src='"+imgpath+"'>";
    }

    var ue = UE.getEditor('editor');
    ue.ready(function() {
        var content = $("#content").val();
        ue.setContent(content);
    });

    UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
    UE.Editor.prototype.getActionUrl = function(action) {
        if (action == 'config') {
            return '/ueditor/config';
        } else if (action == 'uploadimage') {
            return '/ueditor/uploadImage';
        } else {
            return this._bkGetActionUrl.call(this, action);
        }
    }

    $('#saveForm').validate({
        rules: {
            title: {required: true},
            describ: {required: true}
        }, messages: {
            title: {required: "必填"},
            describ: {required: "必填"}
        }
    });
    $('.saveBtn').click(function () {
        if ($('#saveForm').valid()) {
            var formData = new FormData($('#saveForm')[0]);
            $.ajax({
                type: "POST",
                url: "./save",
                data: formData,
                cache: false,
                processData: false,
                contentType: false,
                //headers: {"Content-type": "application/x-www-form-urlencoded;charset=UTF-8"},
                success: function (data) {
                    if (data.code == 200) {
                        alert("保存成功");
                        location.href = '/article/index';
                    } else {
                        alert(data);
                    }
                }
            });
        } else {
            alert('数据验证失败，请检查！');
        }
    });
});

function backBtn() {
    location.href = '/article/index';
}

function changImg(e){
    for (var i = 0; i < e.target.files.length; i++) {
        var file = e.target.files.item(i);
        if (!(/^image\/.*$/i.test(file.type))) {
            continue;
        }
        //实例化FileReader API
        var freader = new FileReader();
        freader.readAsDataURL(file);
        freader.onload = function(e) {
            document.getElementById("viewImg").innerHTML = "<img style='width: 30%;height: 30%; float: left' src='"+e.target.result+"'>";
        }
    }
}