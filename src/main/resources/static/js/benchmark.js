var selectElem = document.getElementById('benchmarkingMode')
var benchmarkingParams = document.getElementById('benchmarkingParams');

// When a new <option> is selected
selectElem.addEventListener('change', function() {
      var tag = selectElem.value;
      if(selectElem.selectedIndex > 0) {
      console.log(selectElem.selectedIndex)
          $.ajax({
            url: "/benchmark/mode/" + tag,
            success: function(result) {
                var parameters = "";
                for(let i = 0 ; i < result.parameters.length; i++){
                    parameters = parameters + '<b>Parameter ' + i + ':</b> ' + result.parameters[i] + '<br/>';
                    console.log(parameters);
                }
                benchmarkingParams.innerHTML = parameters;
          }});
      } else {
          benchmarkingParams.innerHTML = ""
      }
});


var deployButton = document.getElementById('deployFunction')

deployButton.addEventListener('click', function() {
      console.log("deploy...")
      var deployDecision = confirm("Do you really want to deploy the setup?")
      if(deployDecision == true) {
          $.ajax({
            url: "/deploy",
            success: function(result) {
                console.log('Refresh page to see the infos - deployment successful')
          }});
      }
});


var undeployButton = document.getElementById('undeployFunction')
undeployButton.addEventListener('click', function() {
      console.log("deploy...")
      var deployDecision = confirm("Do you really want to undeploy the setup?")
      if(deployDecision == true) {
          $.ajax({
            url: "/undeploy",
            success: function(result) {
                console.log('Refresh page to see the infos - undeployment successful')
          }});
      }
});

var executeBenchmark = document.getElementById('executeBenchmark')
executeBenchmark.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to execute benchmark?")
      if(deployDecision == true) {
          $.ajax({
            url: "/benchmark",
            success: function(result) {
                console.log('Refresh page to see the infos - undeployment successful')
          }});
      }
});

var fetchData = document.getElementById('fetchData')
fetchData.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to fetch benchmark?")
      if(deployDecision == true) {
          $.ajax({
            url: "/fetch",
            success: function(result) {
                console.log('Refresh page to see the infos - undeployment successful')
          }});
      }
});