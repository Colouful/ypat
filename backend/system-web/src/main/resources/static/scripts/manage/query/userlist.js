$(function () {
    $('#searchBtn').click(function(){
        pageaction();
    });
    //初始化分页
    pageaction();
    var pg = $('.pagination');
    $('#pageSelect').live("change",function(){
        pg.trigger('setPage', [$(this).val()-1]);
    });
});

//分页的参数设置
var getOpt = function(){
    var opt = {
        items_per_page: 10,	//每页记录数
        num_display_entries: 3, //中间显示的页数个数 默认为10
        current_page:0,	//当前页
        num_edge_entries:1, //头尾显示的页数个数 默认为0
        link_to:"javascript:void(0)",
        prev_text:"上页",
        next_text:"下页",
        load_first_page:true,
        show_total_info:true ,
        show_first_last:true,
        first_text:"首页",
        last_text:"尾页",
        hasSelect:false,
        callback: pageselectCallback //回调函数
    }
    return opt;
}
//分页开始
var currentPageData = null ;
var pageaction = function(){
    $.get('/manage/user/list?t='+new Date().getTime(),{
        nickname:$("#nickname").val(),
        regisdate:$("#regisdate").val(),
        mobile:$("#mobile").val(),
        gender:$("#gender").val(),
        id:$("#id").val()
    },function(data){
        currentPageData = data.res.content;
        $(".pagination").pagination(data.res.totalElements, getOpt());
    });
}

var pageselectCallback = function(page_index, jq, size){
    var html = "" ;
    if(currentPageData!=null){
        fillData(currentPageData);
        currentPageData = null;
    }else
        $.get('/manage/user/list?t='+new Date().getTime(),{
            size:size,
            page:page_index,
            nickname:$("#nickname").val(),
            regisdate:$("#regisdate").val(),
            mobile:$("#mobile").val(),
            gender:$("#gender").val(),
            id:$("#id").val()
        },function(data){
            fillData(data.res.content);
        });
}
//填充分页数据
function fillData(data){
    var $list = $('#tbodyContent').empty();
    $.each(data,function(k,v){
        var html = "" ;
        html += '<tr> ' +
            '<td>'+ (v.id) +'</td>' +
            '<td>'+ (v.nickname) +'</td>' +
            '<td>'+ (v.mobile) +'</td>' +
            '<td>'+ (v.wx==null?"":v.wx) +'</td>' +
            '<td>'+ (v.qq==null?"":v.qq) +'</td>' +
            '<td>'+ (v.professTxt==null?"":v.professTxt) +'</td>' +
            '<td>'+ (v.genderTxt==null?"":v.genderTxt) +'</td>' +
            '<td>'+ (v.birthday==null?"":v.birthday) +'</td>' +
            '<td>'+ (v.regisdate==null?"":v.regisdate) +'</td>' +
            '<td>'+ (v.city==null?"":v.city) +'</td>' +
            '<td>'+ (v.channelTxt==null?"":v.channelTxt) +'</td>';
        html +='</tr>' ;
        $list.append($(html));
    });
}
//分页结束
var artdialog ;
function detail(id){

}

function closeDialog() {
    artdialog.close();
}
