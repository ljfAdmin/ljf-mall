$(function () {
    // 调用jqGrid分页插件的jqGrid()方法渲染分页展示区域
    // jqGrid分页插件在实现分页功能时必须读取以下数据：当前页的所有数据列表、页码、总页码、总记录数量。
    $("#jqGrid").jqGrid({
        url: '/admin/carousels/list',// 请求后台json数据的url
        datatype: "json",// 后台返回的数据格式
        colModel: [// 列表信息：表头 宽度 是否显示 渲染参数 等属性
            {label: 'id', name: 'carouselId', index: 'carouselId', width: 50, key: true, hidden: true},
            {label: '轮播图', name: 'carouselUrl', index: 'carouselUrl', width: 180, formatter: coverImageFormatter},
            {label: '跳转链接', name: 'redirectUrl', index: 'redirectUrl', width: 120},
            {label: '排序值', name: 'carouselRank', index: 'carouselRank', width: 120},
            {label: '添加时间', name: 'createTime', index: 'createTime', width: 120}
        ],
        height: 560,// 表格高度  可自行调节
        rowNum: 10,// 默认一页显示多少条数据 可自行调节
        rowList: [10, 20, 50],// 翻页控制条中 每页显示记录数可选集合
        styleUI: 'Bootstrap',// 主题 这里选用的是Bootstrap主题
        loadtext: '信息读取中...', // 数据加载时显示的提示信息
        rownumbers: false,// 是否显示行号，默认值是false，不显示
        rownumWidth: 20,// 行号列的宽度
        autowidth: true,// 宽度自适应
        multiselect: true,// 是否可以多选
        pager: "#jqGridPager",// 分页信息DOM，分页导航条
        jsonReader: {// 后端处理后传入的数据data
            // 这里使用的是MybatisPlus里面的对象，records,current,total,pages
            root: "data.records",//数据列表模型
            page: "data.current",//数据当前页码
            total: "data.pages", //数据总页码
            records: "data.total"//数据总记录数
            // root: "data.list",//数据列表模型
            // page: "data.currPage",//数据当前页码
            // total: "data.totalPage", //数据总页码
            // records: "data.totalCount"//数据总记录数
        },
        prmNames: {  // 向后台请求的参数
            page: "page",
            rows: "limit",
            order: "order",
        },
        // 数据加载完成并且DOM创建完毕之后的回调函数
        gridComplete: function () {
            //隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({"overflow-x": "hidden"});
        }
    });

    function coverImageFormatter(cellvalue) {
        return "<img src='" + cellvalue + "' height=\"120\" width=\"160\" alt='coverImage'/>";
    }

    $(window).resize(function () {
        $("#jqGrid").setGridWidth($(".card-body").width());
    });

    new AjaxUpload('#uploadCarouselImage', {
        action: '/admin/upload/file',
        name: 'file',
        autoSubmit: true,
        responseType: "json",
        onSubmit: function (file, extension) {
            if (!(extension && /^(jpg|jpeg|png|gif)$/.test(extension.toLowerCase()))) {
                alert('只支持jpg、png、gif格式的文件！');
                return false;
            }
        },
        onComplete: function (file, r) {
            if (r != null && r.resultCode == 200) {
                $("#carouselImg").attr("src", r.data);
                $("#carouselImg").attr("style", "width: 128px;height: 128px;display:block;");
                return false;
            } else {
                alert("error");
            }
        }
    });
});

/**
 * jqGrid重新加载
 */
function reload() {
    var page = $("#jqGrid").jqGrid('getGridParam', 'page');
    $("#jqGrid").jqGrid('setGridParam', {
        page: page
    }).trigger("reloadGrid");
}

function carouselAdd() {
    reset();
    $('.modal-title').html('轮播图添加');
    $('#carouselModal').modal('show');
}

//绑定modal上的保存按钮
$('#saveButton').click(function () {
    var redirectUrl = $("#redirectUrl").val();
    var carouselRank = $("#carouselRank").val();
    var carouselUrl = $('#carouselImg')[0].src;
    var data = {
        "carouselUrl": carouselUrl,
        "carouselRank": carouselRank,
        "redirectUrl": redirectUrl
    };
    var url = '/admin/carousels/save';
    var id = getSelectedRowWithoutAlert();
    if (id != null) {
        url = '/admin/carousels/update';
        data = {
            "carouselId": id,
            "carouselUrl": carouselUrl,
            "carouselRank": carouselRank,
            "redirectUrl": redirectUrl
        };
    }
    $.ajax({
        type: 'POST',//方法类型
        url: url,
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function (result) {
            if (result.resultCode == 200) {
                $('#carouselModal').modal('hide');
                swal("保存成功", {
                    icon: "success",
                });
                reload();
            } else {
                $('#carouselModal').modal('hide');
                swal(result.message, {
                    icon: "error",
                });
            }
            ;
        },
        error: function () {
            swal("操作失败", {
                icon: "error",
            });
        }
    });
});

function carouselEdit() {
    reset();
    var id = getSelectedRow();
    if (id == null) {
        return;
    }
    //请求数据
    $.get("/admin/carousels/info/" + id, function (r) {
        if (r.resultCode == 200 && r.data != null) {
            //填充数据至modal
            $("#carouselImg").attr("src", r.data.carouselUrl);
            $("#carouselImg").attr("style", "height: 64px;width: 64px;display:block;");
            $("#redirectUrl").val(r.data.redirectUrl);
            $("#carouselRank").val(r.data.carouselRank);
        }
    });
    $('.modal-title').html('轮播图编辑');
    $('#carouselModal').modal('show');
}

function deleteCarousel() {
    var ids = getSelectedRows();
    if (ids == null) {
        return;
    }
    swal({
        title: "确认弹框",
        text: "确认要删除数据吗?",
        icon: "warning",
        buttons: true,
        dangerMode: true,
    }).then((flag) => {
            if (flag) {
                $.ajax({
                    type: "POST",
                    url: "/admin/carousels/delete",
                    contentType: "application/json",
                    data: JSON.stringify(ids),
                    success: function (r) {
                        if (r.resultCode == 200) {
                            swal("删除成功", {
                                icon: "success",
                            });
                            $("#jqGrid").trigger("reloadGrid");
                        } else {
                            swal(r.message, {
                                icon: "error",
                            });
                        }
                    }
                });
            }
        }
    )
}


function reset() {
    $("#redirectUrl").val('##');
    $("#carouselRank").val(0);
    $("#carouselImg").attr("src", '/admin/dist/img/img-upload.png');
    $("#carouselImg").attr("style", "height: 64px;width: 64px;display:block;");
    $('#edit-error-msg').css("display", "none");
}
