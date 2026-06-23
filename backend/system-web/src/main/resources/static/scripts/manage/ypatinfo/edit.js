$(function () {
    $('#saveForm').validate({
        rules: {
            title: {required: true}
        }, messages: {
            title: {required: "必填"}
        }
    });
    $('.saveBtn').click(function () {
        if ($('#saveForm').valid()) {
            var formData = new FormData($('#saveForm')[0]);
            $.ajax({
                type: "POST",
                url: "./submit",
                data: formData,
                cache: false,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.code == 200) {
                        alert("保存成功");
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

