var setupName = document.getElementById('setupName').innerHTML;
var benchmarkConfigID = document.getElementById('benchmarkConfigID');
var versionDropdown = document.getElementById('benchmarkVersions')
var selectElem = document.getElementById('benchmarkingMode');
var benchmarkingParams = document.getElementById('benchmarkingParams');
var benchmarkParamsInput = document.getElementById('benchmarkParamsInput');
var requestBody = document.getElementById('requestBody');
var benchmarkExperimentDesign = document.getElementById('benchmarkExperimentDesign');
var awsBenchmarkConfigRegion = document.getElementById('awsBenchmarkConfigRegion');
var awsBenchmarkConfigRuntime = document.getElementById('awsBenchmarkConfigRuntime');
var awsBenchmarkConfigARNRole = document.getElementById('awsBenchmarkConfigARNRole');
var awsBenchmarkConfigHandlerName = document.getElementById('awsBenchmarkConfigHandlerName');
var awsBenchmarkConfigTimeout = document.getElementById('awsBenchmarkConfigTimeout');
var awsBenchmarkConfigMemorySizes = document.getElementById('awsBenchmarkConfigMemorySizes');
var awsBenchmarkConfigPathToZip = document.getElementById('awsBenchmarkConfigPathToZip');
var awsBenchmarkConfigTargetUrl = document.getElementById('awsBenchmarkConfigTargetUrl');
var awsBenchmarkConfigApiKey = document.getElementById('awsBenchmarkConfigApiKey');
var awsBenchmarkConfigRestApiId = document.getElementById('awsBenchmarkConfigRestApiId');
var awsBenchmarkConfigApiKeyId = document.getElementById('awsBenchmarkConfigApiKeyId');
var awsBenchmarkConfigUsagePlanId = document.getElementById('awsBenchmarkConfigUsagePlanId');

// buttons
var updateConfigButton = document.getElementById('updateBenchmarkConfigButton');
var deployButton = document.getElementById('deployFunction');
var deployButtonInfo = document.getElementById('deployButtonInfo');
var undeployButton = document.getElementById('undeployFunction');
var undeployButtonInfo = document.getElementById('undeployButtonInfo');
var executeBenchmark = document.getElementById('executeBenchmark');
var executeButtonInfo = document.getElementById('executeButtonInfo');
var fetchData = document.getElementById('fetchData');
var fetchButtonInfo = document.getElementById('fetchButtonInfo');

// chart
var benchmarkChart = document.getElementById('benchmarkChart');
var removeDataset = document.getElementById('removeDataset');

function disableButtons(boolDisabled) {
    updateConfigButton.disabled = boolDisabled;
    deployButton.disabled = boolDisabled;
    undeployButton.disabled = boolDisabled;
    executeBenchmark.disabled = boolDisabled;
    fetchData.disabled = boolDisabled;
}

function disableAllInteraction(boolDisabled){
    versionDropdown.disabled = boolDisabled;
    disableButtons(boolDisabled);
}


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

// when the version changes
versionDropdown.addEventListener('change', function() {
      var version = versionDropdown.value;
      if(versionDropdown.selectedIndex == 0) {
        console.log(versionDropdown.selectedIndex)
        disableButtons(false);
      } else {
        console.log(versionDropdown.selectedIndex);
        disableButtons(true);
      }
      $.ajax({
          url: "/benchmark/version/" + version,
          success: function(result) {
              console.log(result);
              benchmarkConfigID.innerHTML = result.id;
              selectElem.value = result.benchmarkMode;
              benchmarkParamsInput.value = result.benchmarkParameters;
              requestBody.value = result.postArgument;
              benchmarkExperimentDesign.innerHTML = result.startTime + " - " + result.endTime;
              awsBenchmarkConfigRegion.value = result.awsBenchmarkConfig.region;
              awsBenchmarkConfigRuntime.value = result.awsBenchmarkConfig.runtime;
              awsBenchmarkConfigARNRole.value = result.awsBenchmarkConfig.awsArnLambdaRole;
              awsBenchmarkConfigHandlerName.value = result.awsBenchmarkConfig.functionHandler;
              awsBenchmarkConfigTimeout.value = result.awsBenchmarkConfig.timeout;
              awsBenchmarkConfigMemorySizes.value = result.awsBenchmarkConfig.memorySizes;
              awsBenchmarkConfigPathToZip.value = result.awsBenchmarkConfig.pathToSource;
              awsBenchmarkConfigTargetUrl.value = result.awsBenchmarkConfig.targetUrl;
              awsBenchmarkConfigApiKey.value = result.awsBenchmarkConfig.apiKey;
              awsBenchmarkConfigRestApiId.value = result.awsBenchmarkConfig.restApiId;
              awsBenchmarkConfigApiKeyId.value = result.awsBenchmarkConfig.apiKeyId;
              awsBenchmarkConfigUsagePlanId.value = result.awsBenchmarkConfig.usagePlanId;
        }});
      addBenchmarkDataToChart(version, myChart);
});


deployButton.addEventListener('click', function() {
      console.log("deploy...")
      var deployDecision = confirm("Do you really want to deploy the setup?")
      if(deployDecision == true) {
          deployButtonInfo.innerHTML = "This may take some time, page will be refreshed automatically...";
          disableAllInteraction(true);
          $.ajax({
            url: "/benchmark/deploy",
            success: function(result) {
                location.reload();
          }});
      }
});



undeployButton.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to undeploy the setup?")
      if(deployDecision == true) {
          undeployButtonInfo.innerHTML = "This may take some time, page will be refreshed automatically...";
          disableAllInteraction(true);
          $.ajax({
            url: "/benchmark/undeploy",
            success: function(result) {
                location.reload();
          }});
      }
});

executeBenchmark.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to execute benchmark?")
      if(deployDecision == true) {
          disableAllInteraction(true);
          executeButtonInfo.innerHTML = "This is not the recommended way to execute the benchmark. Use the CLI feature instead... Benchmark is under execution, this may take a while... Page will be refreshed";
          $.ajax({
            url: "/benchmark/execute",
            statusCode: {
              500: function() {
                executeButtonInfo.innerHTML = "Server Error occurred... Wait a second until AWS Lambda is ready. You executed to early."
              }
            },
            success: function(result) {
                location.reload();
          }});
      }
});

fetchData.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to fetch benchmark?")
      if(deployDecision == true) {
          disableAllInteraction(true);
          fetchButtonInfo.innerHTML = "This may take some time, page will be refreshed automatically...";
          $.ajax({
            url: "/benchmark/fetch",
            success: function(result) {
                location.reload();
          }});
      }
});

// CHART HANDLING

function addBenchmarkDataToChart(version, chart){
    $.ajax({
        url: "/"+ setupName + "/benchmark/version/" + version + "/data",
        success: function(result) {
            if(result.length > 1){
                console.log(result);
                const RGB = 255;
                var benchmarkDataOf = {
                    label: 'Version ' + version,
                    data:  result,
                    backgroundColor: 'rgb('+ Math.random() * RGB + ', '+ Math.random() * RGB + ', '+ Math.random() * RGB + ')'
                };
                chart.data.datasets.push(benchmarkDataOf);
                chart.update();
            }
        }
    });
}
var myChart = new Chart(benchmarkChart, {
    type: 'scatter',
      data: {
        datasets: [],
      },
      options: {
        responsive: true,
        scales: {
          x: {
            beginAtZero: true,
            type: 'linear',
            position: 'bottom'
          },
          y: {
            beginAtZero: true
          }
        }
      }
});

removeDataset.addEventListener('click', function(){
    myChart.data.datasets.pop();
    myChart.update();
});

addBenchmarkDataToChart(versionDropdown.value, myChart);
