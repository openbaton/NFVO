
/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var $chart1;
var $chart2;


function initCharts(){


	$chart1 = new Highcharts.Chart({
	    chart: {
	        renderTo: 'upperchart',
	        defaultSeriesType: 'spline',
	        animation : 
	        {
				duration : 1800	        	
	        },
	        events: {
	            load: refresh1
	        }
	    },
	    
	    
	    
	    title: {
	        text: controlers[0].name
	    },
	    xAxis: {
	        type: 'datetime',
	        tickPixelInterval: 150,
	        maxZoom: 20 * 100
	    },
	    yAxis: {
	        minPadding: 0.2,
	        maxPadding: 0.2,
	        title: {
	            text: 'Kbit/s',
	            margin: 80
	        }
	    },
	    
	    plotOptions:{
	    	series:{
	    		
	    		refreshanimation : 
		        {
					duration : 2000	        	
		        },
	    		
	    		
	    		marker:{
	    			enabled:false
	    		}
	    	}
	    }
	    
	    
	    ,series: []
	});	
	
	$chart1.controlerid = controlers[0].id;
	
	
	
$chart2 = new Highcharts.Chart({
    chart: {
        renderTo: 'lowerchart',
        defaultSeriesType: 'spline',
        animation : 
        {
			duration : 1800	        	
        },
        events: {
            load: refresh2
        }
    },
    
    
    
    title: {
        text: controlers[1].name
    },
    xAxis: {
        type: 'datetime',
        tickPixelInterval: 100,
        maxZoom: 20 * 100
    },
    yAxis: {
        minPadding: 0.2,
        maxPadding: 0.2,
        title: {
            text: 'Kbit/s',
            margin: 80
        }
    },
    
    plotOptions:{
    	series:{
    		
    		refreshanimation : 
	        {
				duration : 2000	        	
	        },
    		
    		
    		marker:{
    			enabled:false
    		}
    	}
    }
    
    
    ,series: [
              
              ]
});	
$chart2.controlerid = controlers[1].id;

$(window).trigger('resize');
}





function refresh1()
{
//	$(window).trigger('resize');
	$.get(path + "/api/rest/monitoring/v1/switches/" +controlers[0].id, function(data){
		 _.each(data, function(swt){
			//add div in the respective controller button box
			 divid = controlers[0].id+ swt;
			 //first check if already exists
			
			 if($('#'+ divid ).length==0)
			 {
				 $newswStat =$('<div name=<"'+ swt + '" controler="'+controlers[0].id+'" dpid="'+swt+'" id="'+divid+'" class="dpidButton"><label class="dpidLabel">'+ controlers[0].switchname +'\n' + swt +'</label></div>');
				 $('div#buttons0').append($newswStat);
			 } 
		 });
		 
	 	});
	
	$.get(path + "/api/rest/monitoring/v1/flows/controller/"+ controlers[0].id, function(flowCollection)
        {
        
			
        	var d = new Date();
        	var n = d.getTime();
        	var series = $chart1.series;
        	

        	flowArray = flowCollection;
        	
        	var inSeries = false;
        	
        	for(var j = 0;j<flowArray.length;j++)
        	{
        		
        		inSeries = false;
		    	for (var i=0;i<series.length;i++)
		    	{
		    		if(series[i].name == flowArray[j].name)
		    		//series already exists, just add Point
		    		{
		    			inSeries = true;
		    			if(flowArray[j].active = true)
		    			{
		    				
		    				series[i].addPoint([ n ,flowArray[j].bandwidth*100000 ],true, series[i].data.length > 20 );
		    			}
		    			else
		    			{
		    				series[i].hide();
		    			}
		    			break;
		    		}	    		
		    	}
		    	
		    	if(!inSeries)
		    	{
		    		
		    		var flow = flowArray[j]; 
		    		var series = $chart1.addSeries({ 
           				data:[ [ n, flow.bw*100000 ]  ], 
           				name:flow.name,
           				animation: true, 
        				});
		    		
		    		series.dpid = flow.dpid;
		    		
		    		
		    		
		    		
		    		
		    	}
		    	
        	}
        	
        	/*var serie = series[0];
        	serie.addPoint( [n,  val] ,true, serie.data.length > 30);
        	*/
        	$chart1.redraw();
    	 	setTimeout(refresh1, 3000);	 
        });
	

};
        

function refresh2()
{
	
//	$(window).trigger('resize');
	$.get(path + "/api/rest/monitoring/v1/switches/" +controlers[1].id, function(data){
		 _.each(data, function(swt){
			//add div in the respective controller button box
			 
			 //first check if already exists
			 if($('#'+controlers[1].id + swt  ).length==0){
				 $newswStat =$('<div name="'+ swt + '" controler="'+controlers[1].id+'" dpid="'+swt+'" id="'+controlers[1].id + swt + '" class="dpidButton"><label class="dpidLabel">'+ controlers[1].switchname+ '\n' + swt +'  </label></div>');
				 $('div#buttons1').append($newswStat);
			 }
		 });
		 
	 	});
	
	$.get(path + "/api/rest/monitoring/v1/flows/controller/"+ controlers[1].id, function(flowCollection)
			 {
        
		
    	var d = new Date();
    	var n = d.getTime();
    	var series = $chart2.series;
    	

    	flowArray = flowCollection;
    	
    	var inSeries = false;
    	
    	for(var j = 0;j<flowArray.length;j++)
    	{
    		
    		inSeries = false;
	    	for (var i=0;i<series.length;i++)
	    	{
	    		if(series[i].name == flowArray[j].name)
	    		//series already exists, just add Point
	    		{
	    			inSeries = true;
	    			
	    			
	    			series[i].addPoint([ n ,flowArray[j].bandwidth*10000 ],true, series[i].data.length > 10 );
	    			$chart2.redraw();
	    		}	    		
	    	}
	    	
	    	if(!inSeries)
	    	{
	    		
	    		var flow = flowArray[j]; 
	    		var serie = $chart2.addSeries({ 
       				data:[ [ n, flow.bw*10000 ]  ], 
       				name:flow.name,
       				animation: true, 
    				});
	    		serie.dpid = flow.dpid;

	    	}
	    	
    	}
        	
        	/*var serie = series[0];
        	serie.addPoint( [n,  val] ,true, serie.data.length > 30);
        	*/
        	$chart2.redraw();
    	 	setTimeout(refresh2, 3000);	 
        });




}

$('#buttons0').on('click', '.dpidButton', function(){

		dpid = $(this).attr('dpid');
		//now get all the reals series
	_.each($chart1.series, function(chartSeries){	
		chartSeries.hide();
		if($(this).attr('dpid') == dpid){
		//	alert('showing series' + thisSeries.name); 
			chartSeries.show();
		}
	});

});


$('#buttons1').on('click', '.dpidButton', function(){
	
		dpid = $(this).attr('dpid');
			//now get all the reals series
		_.each($chart2.series, function(chartSeries){	
			chartSeries.hide();
			if($(this).attr('dpid') == dpid){
			//	alert('showing series' + thisSeries.name); 
				chartSeries.show();
			}
		});

});






        
