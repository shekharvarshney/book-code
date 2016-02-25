<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<meta charset="utf-8">
	<title>Historic Candles</title>
	<link href="resources/css/jquery/jquery-ui.css" rel="stylesheet">
	<link href="resources/css/jquery/jquery-ui-timepicker-addon.css" rel="stylesheet">
	<link href="resources/css/jquery/jquery.dataTables.min.css" rel="stylesheet">
	<script src="resources/js/jquery/jquery.min.js"></script>
	<script src="resources/js/jquery/jquery-ui.min.js"></script>
	<script src="resources/js/jquery/jquery-ui-timepicker-addon.js"></script>
	<script src="resources/js/jquery/jquery.dataTables.min.js"></script>
	<style type="text/css">
	body{
		font: 62.5% "Trebuchet MS", sans-serif;
		margin: 25px;
	}
	select {
		width: 175px;
	}
	</style>
</head>
<body>
	<form action="getCandles" method="get" id="histcandles">
	<table>
	<tr>
	<td><label>Instrument:</label></td>
	<td><select id="currencyPair" name="currencyPair">
		<c:forEach var="instrument" items="${instruments}">
			<option value="${instrument.instrument}">${instrument.instrument}</option>
		</c:forEach>
	</select>
	</td>
	<td><label>Granularity:</label></td>
	<td><select id="granularity" name="granularity">
		<c:forEach var="g" items="${granularities}">
			<option value="${g.name}">${g.label}</option>
		</c:forEach>
	</select>
	</td>
	<td><label>From:</label></td>
	<td>
		<input type="text" name="fromDate" id="fromDate" value="" />
	</td>
	<td><label>To:</label></td>
	<td>
		<input type="text" name="toDate" id="toDate" value="" />
	</td>
	<td><label>Last N:</label></td>
	<td><input type="text" name="count" id="count" size="5" maxlength="5"
	onkeypress='return event.charCode >= 48 && event.charCode <= 57'/> </td>
	
	<td>
	<button id="fetch" type="button" onclick="fetchData()">Fetch</button>
	</td>
	</tr>
	</table>
	<table id="candledata" class="display">
		<thead>
			<tr>
				<td>DateTime</td>
				<td>Open Price</td>
				<td>Close Price</td>
				<td>High Price</td>
				<td>Low Price</td>
			</tr>
		</thead>
	</table>
	</form>
</body>


<script type="text/javascript">

$( "#currencyPair" ).selectmenu();
$( "#granularity" ).selectmenu();
$('#fromDate').datetimepicker({
	timeFormat: 'HH:mm:ss',
	dateFormat: 'dd/mm/yy',
	stepHour: 1,
	stepMinute: 1,
	stepSecond: 1
});
$('#toDate').datetimepicker({
	timeFormat: 'HH:mm:ss',
	dateFormat: 'dd/mm/yy',
	stepHour: 1,
	stepMinute: 1,
	stepSecond: 1
});
$( "#fetch" ).button();
function fetchData() {
	var url="jsonCandles?currencyPair="+$('#currencyPair').val() 
			+ "&granularity=" + $('#granularity').val()+"&fromDate="+$('#fromDate').val()
			+ "&toDate="+$('#toDate').val()+"&count="+$('#count').val();
// 	$.post( url, function( data ) {
// 		  alert(data);
// 	});
	$("#candledata").dataTable().fnClearTable();
	$("#candledata").dataTable().fnDestroy();
	$(document).ready(function() {
	    $('#candledata').DataTable( {
	    	"aLengthMenu": [[10, 50, 100], [10, 50, 100]],
	        "iDisplayLength": 50,
	        "iDisplayStart" : 0,
	    	"sAjaxSource": url,
	        "bProcessing": true,
	        "bServerSide": false,
	        "sServerMethod": "POST",
	        "sAjaxDataProp" : "",
	        "aoColumns": [
	            { "data": "eventDate" },
	            { "data": "openPrice" },
	            { "data": "closePrice" },
	            { "data": "highPrice" },
	            { "data": "lowPrice" }
	        ]
	    } );
	} );
}
</script>
</html>