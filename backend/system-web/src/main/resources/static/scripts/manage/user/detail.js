$(function(){
	$('#saveForm').validate({
		rules: {
			name       :{required:true},
			email      :{required:true}
        },messages:{
            name :{required:"必填"},
            email :{required:"必填"}
        }
 	});
	$('.tgBtn').click(function(){
	   if($('#saveForm').valid()){
           $.ajax({
               type: "POST",
               url: "./audit?flag=2",
               data: $("#saveForm").serialize(),
               headers: {"Content-type": "application/x-www-form-urlencoded;charset=UTF-8"},
               success: function (data) {
                   if (data.code == 200) {
                       alert("审核成功");
                       pageaction();
                       closeDialog();
                   } else {
                       alert(data);
                   }
               }
           });
	   }else{
		   alert('审核失败，请检查！');
	   }
	});
    $('.btgBtn').click(function(){
        if($('#saveForm').valid()){
            $.ajax({
                type: "POST",
                url: "./audit?flag=3",
                data: $("#saveForm").serialize(),
                headers: {"Content-type": "application/x-www-form-urlencoded;charset=UTF-8"},
                success: function (data) {
                    if (data.code == 200) {
                        alert("审核成功");
                        pageaction();
                        closeDialog();
                    } else {
                        alert(data);
                    }
                }
            });
        }else{
            alert('审核失败，请检查！');
        }
    });
});	
