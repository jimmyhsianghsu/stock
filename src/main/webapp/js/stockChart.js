function stockChart(div){
	var offsetWidth=div.offsetWidth;
	var offsetHeight=div.offsetHeight;
	div.innerHTML = "<canvas width='"+offsetWidth+"' height='"+offsetHeight+"' style='border:solid 1px blue'/>";
	var ctx = div.childNodes[0].getContext('2d');
	var stock,end,period,list,current;
	var stockUpdate,stockNo,showMark;
	var flagM20,flagBand,flagLevel;
	var rect,flagRect;
	var currentY;
	
	this.setStockUpdate=function(_stockUpdate){stockUpdate=_stockUpdate;}
	this.setStockNo=function(_stockNo){stockNo=_stockNo;}
	this.setShowMark=function(_showMark){showMark=_showMark;}
	
	var draw = function(_stock,_end,_period){
		stock=_stock;
		end=_end*1;
		period=_period;
		if(end<0)end+=stockUpdate.update(stock,stockNo,1);
		if(end<0)end=0;
		if(stock.length-end-period<0)stockUpdate.update(stock,stockNo,-1);
		if(stock.length-end-period<0)end=stock.length-period;
		if(end<0)period=stock.length-(end=_end);

		stock.sort(function(a,b){return a.d-b.d});
		list=[];
		var t2=stock.length-end-1,t1=t2-period+1,len=period;
		var x0=offsetWidth*0.1;
		var y0=offsetHeight*0.7;
		var dx=offsetWidth*0.9/len,dy;
		var low=10000,hi=0;
		var vhi=0;
		var bhi=0;
		for (var i=t1;i<=t2;i++){ // t1t2
			if (stock[i].h*1>hi*1)hi=stock[i].h;
			if (stock[i].l*1<low*1)low=stock[i].l;
			if (stock[i].m20*1>hi*1)hi=stock[i].m20;
			if (stock[i].m20*1<low*1)low=stock[i].m20;
			if (stock[i].bbh*1>hi*1)hi=stock[i].bbh;
			if (stock[i].bbl*1<low*1)low=stock[i].bbl;
			if(stock[i].v*1>vhi*1)vhi=stock[i].v;
			if(Math.abs(stock[i].bs*1)>bhi*1)bhi=Math.abs(stock[i].bs);
		}
		vhi*=1.1;bhi*=1.1;
		low=Math.floor(low)-2;
		hi=Math.ceil(hi)+2;
		dy=y0/(hi-low);
		ctx.clearRect(0,0,offsetWidth,offsetHeight); // t1t2
		ctx.strokeStyle='#000000';
		ctx.beginPath();
		ctx.moveTo(x0,0);
		ctx.lineTo(x0,y0);
		ctx.lineTo(offsetWidth,y0);
		ctx.stroke();
		var y10=y0+(offsetHeight-y0)/2;
		var y20=offsetHeight;
		var dy1=(y10-y0)/vhi;
		var dy2=(y20-y10)/bhi;
		ctx.beginPath();
		ctx.moveTo(x0,y0);
		ctx.lineTo(x0,y10);
		ctx.lineTo(offsetWidth,y10);
		ctx.stroke();
		ctx.beginPath();
		ctx.moveTo(x0,y10);
		ctx.lineTo(x0,y20);
		ctx.lineTo(offsetWidth,y20);
		ctx.stroke();
		for (var i=t1;i<=t2;i++){ // t1t2
			var v0=x0+(i-t1+0.5)*dx;
			var v1=y0-(stock[i].h-low)*dy;
			var v2=y0-(stock[i].l-low)*dy;
			var rh,rl;
			if (stock[i].o*1>stock[i].y*1){
				ctx.strokeStyle=ctx.fillStyle='#000000';
				rh=stock[i].o;
				rl=stock[i].y;
			}else{
				ctx.strokeStyle=ctx.fillStyle='#ff0000';
				rh=stock[i].y;
				rl=stock[i].o;
			}
			var v3=x0+(i-t1+0.3)*dx;
			var v4=y0-(rh-low)*dy;
			var v5=dx*0.4;
			var v6=(rh-rl)*dy;
			
			ctx.beginPath();
			ctx.moveTo(v0,v1);
			ctx.lineTo(v0,v2);
			ctx.rect(v3,v4,v5,v6);
			ctx.stroke();
			ctx.fill();
			
			ctx.rect(v3,y10-stock[i].v*dy1,v5,stock[i].v*dy1);
			ctx.fill();
			ctx.beginPath();
			ctx.fillStyle=stock[i].bs*1>0?'#ff0000':'#000000';
			ctx.rect(v3,y20-Math.abs(stock[i].bs)*dy2,v5,Math.abs(stock[i].bs)*dy2);
			ctx.fill();
			if(flagM20 && i>t1){
				ctx.strokeStyle='#FFCC00';
				ctx.beginPath();
				ctx.moveTo(x0+(i-t1+0.5-1)*dx,y0-(stock[i-1].m20-low)*dy);
				ctx.lineTo(v0,y0-(stock[i].m20-low)*dy);
				ctx.stroke();
			}
			if(flagBand && i>t1){
				ctx.strokeStyle='#CC6699';
				ctx.beginPath();
				ctx.moveTo(x0+(i-t1+0.5-1)*dx,y0-(stock[i-1].bbh-low)*dy);
				ctx.lineTo(v0,y0-(stock[i].bbh-low)*dy);
				ctx.stroke();
				ctx.beginPath();
				ctx.moveTo(x0+(i-t1+0.5-1)*dx,y0-(stock[i-1].bbl-low)*dy);
				ctx.lineTo(v0,y0-(stock[i].bbl-low)*dy);
				ctx.stroke();
			}
			list.push(x0+(i-t1)*dx);
		}
		if(flagLevel && level && level.d1 && level.d2){
			var levH=0,levL=10000,i1=-1,i2=-1,iH=-1,iL=-1;
			for (var i=0;i<stock.length;i++){
				if(level.d1*1==stock[i].d*1)i1=i;
				if(level.d2*1==stock[i].d*1)i2=i;
			}
			for (var i=i1;i<=i2 && i1!=-1 && i2!=-1;i++){
				if(stock[i].h*1>levH*1){levH=stock[i].h*1;iH=i;}
				if(stock[i].l*1<levL*1){levL=stock[i].l*1;iL=i;}
			}
			ctx.strokeStyle='#00FF00';
			ctx.fillStyle='#0000FF';
			ctx.font='12px Georgia';
			if(iH!=-1 && iH*1>=t1*1 && iH*1<=t2*1){
			ctx.beginPath();
			ctx.moveTo(x0+(iH-t1+0.5)*dx,0);
			ctx.lineTo(x0+(iH-t1+0.5)*dx,(hi*1-levH*1)*0.9*dy);
			ctx.stroke();
			ctx.fillText(levH,x0+(iH-t1+0.5)*dx+2,(hi*1-levH*1)*0.9*dy-5);
			}
			if(iL!=-1 && iL*1>=t1*1 && iL*1<=t2*1){
			ctx.beginPath();
			ctx.moveTo(x0+(iL-t1+0.5)*dx,y0);
			ctx.lineTo(x0+(iL-t1+0.5)*dx,y0-(levL*1-low*1)*0.9*dy);
			ctx.stroke();
			ctx.fillText(levL,x0+(iL-t1+0.5)*dx+2,y0-(levL*1-low*1)*0.9*dy+5);
			}
			if(iH*1>iL*1)iL=iH;
			if(iL!=-1 && iL*1<=t2*1){
				if(iL*1<t1)iL=t1;
			ctx.beginPath();
			ctx.moveTo(x0+(iL-t1+0.5)*dx,y0-(level.l1*1-low*1)*dy);
			ctx.lineTo(offsetWidth,y0-(level.l1*1-low*1)*dy);
			ctx.stroke();
			ctx.fillText(Math.round(level.l1*100)/100,x0+(iL-t1+0.5)*dx,y0-(level.l1*1-low*1)*dy-5);
			ctx.beginPath();
			ctx.moveTo(x0+(iL-t1+0.5)*dx,y0-(level.l2*1-low*1)*dy);
			ctx.lineTo(offsetWidth,y0-(level.l2*1-low*1)*dy);
			ctx.stroke();
			ctx.fillText(Math.round(level.l2*100)/100,x0+(iL-t1+0.5)*dx,y0-(level.l2*1-low*1)*dy-5);
			ctx.beginPath();
			ctx.moveTo(x0+(iL-t1+0.5)*dx,y0-(level.l3*1-low*1)*dy);
			ctx.lineTo(offsetWidth,y0-(level.l3*1-low*1)*dy);
			ctx.stroke();
			ctx.fillText(Math.round(level.l3*100)/100,x0+(iL-t1+0.5)*dx,y0-(level.l3*1-low*1)*dy-5);
			}
		}
		ctx.strokeStyle=ctx.fillStyle='#000000';
		ctx.font='12px Georgia';
		var divx=x0;divH=100;
		for (var i=t1;i<=t2;i++){ // t1t2
			var v0=x0+(i-t1)*dx;
			if (v0>=divx){
				ctx.fillText(stock[i].d.toString().substring(3,7),v0,y0-5);
				divx=v0+divH;
			}
		}
		var divy=y0,divV=10;
		for (var i=low+1;i<hi;i++){
			var v0=y0-(i-low)*dy;
			if (v0<=divy){
				ctx.beginPath();
				ctx.moveTo(x0-5,v0);
				ctx.lineTo(x0,v0);
				ctx.stroke();
				ctx.fillText(i,x0-30,v0+5);
				divy=v0-divV;
			}
		}
		ctx.fillText(Math.round(vhi/1.1/1000),x0-45,y0+15);
		ctx.fillText(Math.round(bhi/1.1*10)/10,x0-30,y10+15);
		showMark(null,[end,period,stock.length,t1,t2]);
		if(flagRect && (rect && rect.d1 && rect.d2)){
			var r1=-1,r2=-1;
			for(i=0;i<stock.length;i++){
				if(rect.d1*1==stock[i].d*1)r1=i;
				if(rect.d2*1==stock[i].d*1)r2=i;
			}
			if(r1>=t1 && r1<=t2 && r2>=t1 && r2<=t2){
				ctx.strokeStyle='#00FF00';
				ctx.rect(x0+(r1-t1+0.5)*dx,rect.y1,(r2-r1)*dx,rect.y2-rect.y1);
				ctx.stroke();
			}
		}
		level.y0=y0;level.hi=hi;level.low=low;
	}
	var level={};
	this.setLevel=function(_level){
		if(_level){
			if(_level.d1)level.d1=_level.d1;
			if(_level.d2)level.d2=_level.d2;
			if(_level.l1)level.l1=_level.l1;
			if(_level.l2)level.l2=_level.l2;
			if(_level.l3)level.l3=_level.l3;
			if(stock && typeof(end)!='undefined' && typeof(period)!='undefined')draw(stock,end,period);
		}
	}
	this.saveLevel=function(_stockNo,_end,_period){
		_stockNo=_stockNo||stockNo;
		_end=_end||end;
		_period=_period||period;
		var t2=stock.length-_end-1,t1=t2-_period+1;
		flagLevel=true;
		if(!(rect && rect.d1 && rect.d2))
		this.setLevel(stockUpdate.saveLevel(_stockNo,stock[t1].d,stock[t2].d));
		else this.setLevel(stockUpdate.saveLevel(_stockNo,rect.d1,rect.d2));
	}
	this.drawOption = function(_flagM20,_flagBand,_flagLevel,_flagRect){
		if(_flagM20!=null)flagM20=_flagM20;
		if(_flagBand!=null)flagBand=_flagBand;
		if(_flagLevel!=null)flagLevel=_flagLevel;
		if(typeof(_flagRect)!='undefined'&&_flagRect!=null){
		flagRect=_flagRect;rect=null;
		}
		draw(stock,end,period);
	}
	this.drawRect=function(){
		if(rect && rect.d1 && rect.d2){
			var _end=0,_period=0;
			for(var i=stock.length-1;i>=0;i--){
				if(stock[i].d*1==rect.d2*1)_end=stock.length-i-1;
				if(stock[i].d*1==rect.d1*1)_period=stock.length-i-_end;
			}
			if(_end>=0 && _period>0)stockUpdate.setEndPeriod(_end,_period);
		}
	}
	var onmousedown=function(e){
		var x=e.offsetX;
		for(var i=0;i<list.length;i++){
			if(x>=list[i] && (i==list.length-1 || x<list[i+1])){
				current=i;
				break;
			}
		}
		if(flagRect)rect={d1:stock[stock.length-end-period+current].d,y1:e.offsetY};
		div.onmousemove=onmousemove2;
		currentY=e.offsetY;
		showMark(null,null,level.low*1+(level.y0-e.offsetY)*(level.hi-level.low)/level.y0);
	}
	var onmouseup=function(){div.onmousemove=onmousemove1;}
	var onmousemove1=function(e){
		var x=e.offsetX;
		var y=e.offsetY;
		draw(stock,end,period);
		ctx.strokeStyle=ctx.fillStyle='#0000ff';
		ctx.beginPath();
		ctx.moveTo(x,0);
		ctx.lineTo(x,offsetHeight);
		ctx.stroke();
		ctx.beginPath();
		ctx.moveTo(0,y);
		ctx.lineTo(offsetWidth,y);
		ctx.stroke();
		var current1=-1;
		for(var i=0;i<list.length;i++){
			if(x>=list[i] && (i==list.length-1 || x<list[i+1])){
				current1=i;
				break;
			}
		}
		if(current1!=-1)showMark(stock[stock.length-end-period+current1]);
	}
	var onmousemove2=function(e){
		var x=e.offsetX;
		var current1=-1;
		for(var i=0;i<list.length;i++){
			if(x>=list[i] && (i==list.length-1 || x<list[i+1])){
				current1=i;
				break;
			}
		}
		if(current1!=-1 && current!=current1){
			if(flagRect){
				rect.d2=stock[stock.length-end-period+current1].d;
				rect.y2=e.offsetY;
			}else
			end=end+current1-current;
			draw(stock,end,period);
			current=current1;
		}
		onmousemove1(e);
		var currentY1=e.offsetY;
		if(!flagRect)
		if(currentY1-currentY>40){
			currentY=currentY1;
			period-=20;
			period=period<1?1:period;
			stockUpdate.setEndPeriod(end,period);
		}else if(currentY1-currentY<-40){
			currentY=currentY1;
			stockUpdate.setEndPeriod(end,(period+=20));
		}
	}
	var onmouseout=function(){
		draw(stock,end,period);
		div.onmousemove=onmousemove1;
	}
	this.draw=draw;
	div.onmousedown=onmousedown;
	div.onmouseup=onmouseup;
	div.onmousemove=onmousemove1;
	div.onmouseout=onmouseout;
}