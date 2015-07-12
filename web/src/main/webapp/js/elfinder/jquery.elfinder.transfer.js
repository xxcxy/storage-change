"use strict"
elFinder.prototype.commands.transfer = function() {
	this.alwaysEnabled = false;
	var fm = this.fm;
	this.callback = fm.options.transferCallback;
	var self = this;
	this.getstate = function(sel) {
		var sel = this.files(sel), cnt = sel.length;
		return cnt == 1 ? 0 : cnt ? ($.map(sel, function(file) {
			return file.mime == 'directory' ? null : file
		}).length == cnt ? 0 : -1) : -1
	}
	this.exec = function(hashes) {
		var fm    = this.fm;
		var dfrd  = $.Deferred().fail(function(error) { error && fm.error(error); });
		var files = this.files(hashes);
		var cnt   = files.length;
		var cdata = self.callback(fm);
		if (!cnt) {
			return dfrd.reject();
		}
		if (cdata) {
			return fm.request({
				data   : {cmd  : 'transfer', target : cdata.targetHash, source: cdata.sourceHash, targetKeyId: cdata.targetKeyId,type: cdata.type},
				notify : {type : 'upload', cnt : 1},
				syncOnFail : true
			}).done(function(data){
				var uiop = {title:"操作信息",minWidth:880,width:'880',position:{top:160,left:150},close:function(event, ui){$('#operator_dialog').dialog("destroy");}};
				var html_context = "<div class='dialog' id='operator_dialog'><div class='alert alert-success'>成功文件数:"+data.success_count+"<br>成功文件列表:<br>";
				for(var fileIndex in data.success_files)
					html_context = html_context+data.success_files[fileIndex]+"<br>"
				html_context = html_context + "</div><div class='alert alert-warning'>问题文件数:"+data.warn_count+"<br>问题文件列表:<br>";
				for(var fileIndex in data.warn_files)
					html_context = html_context+data.warn_files[fileIndex]+"<br>"
				html_context = html_context + "</div><div class='alert alert-error'>错误文件数:"+data.error_count+"<br>错误文件列表:<br>";
				for(var fileIndex in data.error_files)
					html_context = html_context+data.error_files[fileIndex]+"<br>"
				html_context = html_context+"</div></div>";
				$(html_context).dialog(uiop);
			});
		}	
		return dfrd.resolve(hashes);
	}
};
elFinder.prototype.commands.bucketTransfer = function() {
	this.alwaysEnabled = false;
	var fm = this.fm;
	var self = this;
	this.callback = fm.options.bucketTransferCallback;
    this.getstate = function() {
        var a = this.fm.root();
        var b = this.fm.cwd().hash;
        return a && b && a == b ? 0 : -1
    },
    this.exec = function(hashes) {
    	var fm    = this.fm;
		var dfrd  = $.Deferred().fail(function(error) { error && fm.error(error); });
		var cdata = self.callback(fm);
		if (cdata) {
			return fm.request({
				data   : {cmd  : 'transfer', target : cdata.targetHash, source: cdata.sourceHash, targetKeyId: cdata.targetKeyId,type: cdata.type},
				notify : {type : 'upload', cnt : 1},
				syncOnFail : true
			}).done(function(data){
				var uiop = {title:"操作信息",minWidth:880,width:'880',position:{top:160,left:150},close:function(event, ui){$('#operator_dialog').dialog("destroy");}};
				var html_context = "<div class='dialog' id='operator_dialog'><div class='alert alert-success'>成功文件数:"+data.success_count+"<br>成功文件列表:<br>";
				for(var fileIndex in data.success_files)
					html_context = html_context+data.success_files[fileIndex]+"<br>"
				html_context = html_context + "</div><div class='alert alert-warning'>问题文件数:"+data.warn_count+"<br>问题文件列表:<br>";
				for(var fileIndex in data.warn_files)
					html_context = html_context+data.warn_files[fileIndex]+"<br>"
				html_context = html_context + "</div><div class='alert alert-error'>错误文件数:"+data.error_count+"<br>错误文件列表:<br>";
				for(var fileIndex in data.error_files)
					html_context = html_context+data.error_files[fileIndex]+"<br>"
				html_context = html_context+"</div></div>";
				$(html_context).dialog(uiop);
			});
		}	
		return dfrd.resolve(hashes);
    }
}