var vm = new Vue({
	el:'#rrapp',
	data:{
		configData:{
			generatorBackendPath:"",
			author:"xxx",
			email:"xxx@qq.com",
			tablePrefix:"",
			openLombok:true,
			openSwagger:true,
			serviceInterface:true,
			onlyBackend:false,
			openFrontLowercase:true,
		}
	},
	mounted: function () {
		this.load();
	},
	methods: {
		load:function(){
			$.ajax({
				//几个参数需要注意一下
				type: "GET",//方法类型
				contentType: "application/json",
				dataType: "json",//预期服务器返回的数据类型
				url: "/sys/generator/getconfig" ,//url
				success: function (result) {
					vm.configData = result.config;
				},
				error : function(result) {
					console.log(result)
					alert("异常！");
				}
			});
		},
		config:function(){
			console.log(vm.configData)
			$.ajax({
				//几个参数需要注意一下
				type: "POST",//方法类型
				contentType: "application/json",
				dataType: "json",//预期服务器返回的数据类型
				url: "/sys/generator/config" ,//url
				data: JSON.stringify(vm.configData),
				success: function (result) {
					console.log(result);//打印服务端返回的数据(调试用)
					if (result.code == 200) {
						$('#myModal').modal();
					}
					;
				},
				error : function(result) {
					console.log(result)
					alert("异常！");
				}
			});
		}
	},

});

