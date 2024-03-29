var setupName = document.getElementById('setupName').innerHTML;
var benchmarkDescription = document.getElementById('benchmarkDescription');
var versionVisible = document.getElementById('versionVisible');
var benchmarkConfigID = document.getElementById('benchmarkConfigID');
var versionDropdown = document.getElementById('benchmarkVersions')
var selectElem = document.getElementById('benchmarkingMode');
var benchmarkingParams = document.getElementById('benchmarkingParams');
var benchmarkParamsInput = document.getElementById('benchmarkParamsInput');
var requestBody = document.getElementById('requestBody');
var benchmarkExperimentDesign = document.getElementById('benchmarkExperimentDesign');
var awsBenchmarkDescription = document.getElementById('awsBenchmarkDescription');
var awsBenchmarkConfigRegion = document.getElementById('awsBenchmarkConfigRegion');
var awsBenchmarkConfigRuntime = document.getElementById('awsBenchmarkConfigRuntime');
var awsBenchmarkConfigARNRole = document.getElementById('awsBenchmarkConfigARNRole');
var awsBenchmarkConfigHandlerName = document.getElementById('awsBenchmarkConfigHandlerName');
var awsBenchmarkConfigTimeout = document.getElementById('awsBenchmarkConfigTimeout');
var awsBenchmarkConfigMemorySizes = document.getElementById('awsBenchmarkConfigMemorySizes');
var awsBenchmarkConfigDeploymentSizes = document.getElementById('awsBenchmarkConfigDeploymentSizes');
var awsBenchmarkConfigPathToZip = document.getElementById('awsBenchmarkConfigPathToZip');
var awsBenchmarkConfigTargetUrl = document.getElementById('awsBenchmarkConfigTargetUrl');
var awsBenchmarkConfigApiKey = document.getElementById('awsBenchmarkConfigApiKey');
var awsBenchmarkConfigRestApiId = document.getElementById('awsBenchmarkConfigRestApiId');
var awsBenchmarkConfigApiKeyId = document.getElementById('awsBenchmarkConfigApiKeyId');
var awsBenchmarkConfigUsagePlanId = document.getElementById('awsBenchmarkConfigUsagePlanId');
var openFaaSResourceSetting = document.getElementById('openFaaSResourceSetting');
var openFaaSBaseUrl = document.getElementById('openFaaSBaseUrl');
var openFaaSNumberOfRuns = document.getElementById('openFaaSNumberOfRuns');

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
var fetchDataOpenFaas = document.getElementById('fetchDataOpenFaas');

// chart
var benchmarkChart = document.getElementById('benchmarkChart');
var removeDataset = document.getElementById('removeDataset');

function disableButtons(boolDisabled) {
    updateConfigButton.disabled = boolDisabled;
    deployButton.disabled = boolDisabled;
    undeployButton.disabled = boolDisabled;
    executeBenchmark.disabled = boolDisabled;
//    fetchData.disabled = boolDisabled;
}

function disableAllInteraction(boolDisabled) {
    versionDropdown.disabled = boolDisabled;
    disableButtons(boolDisabled);
}

// When the version visible property is changed
versionVisible.addEventListener('change', function () {
    console.log("change visibility of selected version");
    $.ajax({
        type: "POST",
        url: "/semode/v1/" + setupName + "/benchmark/visible/" + versionDropdown.value,
        success: function () {
            document.getElementById('versionVisibleText').innerHTML = 'Changed visible property to ' + versionVisible.checked;
        }
    });
});


// When a new <option> is selected
selectElem.addEventListener('change', function () {
    var tag = selectElem.value;
    if (selectElem.selectedIndex > 0) {
        $.ajax({
            url: "/semode/v1/benchmark/mode/" + tag,
            success: function (result) {
                var parameters = "";
                for (let i = 0; i < result.parameters.length; i++) {
                    parameters = parameters + '<b>Parameter ' + i + ':</b> ' + result.parameters[i] + '<br/>';
                }
                benchmarkingParams.innerHTML = parameters;
            }
        });
    } else {
        benchmarkingParams.innerHTML = ""
    }
});

// when the description changes
function updateDescription() {
    console.log(benchmarkDescription.value);
    $.ajax({
        type: "POST",
        url: "/semode/v1/" + setupName + "/benchmark/description/" + versionDropdown.value,
        data: {newDescription: benchmarkDescription.value},
        success: function () {
            document.getElementById('versionVisibleText').innerHTML = 'Changed visible property to ' + versionVisible.checked;
        }
    });
}

// when the version changes
versionDropdown.addEventListener('change', function () {
    var version = versionDropdown.value;
    if (versionDropdown.selectedIndex == 0) {
        disableButtons(false);
    } else {
        disableButtons(true);
    }
    $.ajax({
        url: "/semode/v1/" + setupName + "/benchmark/version/" + version,
        success: function (result) {
            console.log(result);
            benchmarkConfigID.innerHTML = result.id;
            benchmarkDescription.value = result.description;
            versionVisible.checked = result.versionVisible;
            selectElem.value = result.benchmarkMode;
            benchmarkParamsInput.value = result.benchmarkParameters;
            requestBody.value = result.postArgument;
            benchmarkExperimentDesign.innerHTML = result.startTime + " - " + result.endTime;
            awsBenchmarkDescription.value = result.awsBenchmarkConfig.description;
            awsBenchmarkConfigRegion.value = result.awsBenchmarkConfig.region;
            awsBenchmarkConfigRuntime.value = result.awsBenchmarkConfig.runtime;
            awsBenchmarkConfigARNRole.value = result.awsBenchmarkConfig.awsArnLambdaRole;
            awsBenchmarkConfigHandlerName.value = result.awsBenchmarkConfig.functionHandler;
            awsBenchmarkConfigTimeout.value = result.awsBenchmarkConfig.timeout;
            awsBenchmarkConfigMemorySizes.value = result.awsBenchmarkConfig.memorySizes;
            awsBenchmarkConfigDeploymentSizes.value = result.awsBenchmarkConfig.deploymentPackageSizes;
            awsBenchmarkConfigPathToZip.value = result.awsBenchmarkConfig.pathToSource;
            awsBenchmarkConfigTargetUrl.value = result.awsBenchmarkConfig.targetUrl;
            awsBenchmarkConfigApiKey.value = result.awsBenchmarkConfig.apiKey;
            awsBenchmarkConfigRestApiId.value = result.awsBenchmarkConfig.restApiId;
            awsBenchmarkConfigApiKeyId.value = result.awsBenchmarkConfig.apiKeyId;
            awsBenchmarkConfigUsagePlanId.value = result.awsBenchmarkConfig.usagePlanId;
            openFaaSResourceSetting.value = result.openFaasBenchmarkConfig.openFaaSResourceSetting;
            openFaaSBaseUrl.value = result.openFaasBenchmarkConfig.openFaaSBaseUrl;
            openFaaSNumberOfRuns.value = result.openFaasBenchmarkConfig.openFaaSNumberOfRuns;
        }
    });
    addBenchmarkDataToChart(version, myChart);
});


deployButton.addEventListener('click', function () {
    console.log("deploy...")
    var deployDecision = confirm("Do you really want to deploy the setup?")
    if (deployDecision == true) {
        deployButtonInfo.innerHTML = "This may take some time, page will be refreshed automatically...";
        disableAllInteraction(true);
        $.ajax({
            url: "/semode/v1/" + setupName + "/benchmark/deploy",
            success: function (result) {
                location.reload();
            }
        });
    }
});


undeployButton.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to undeploy the setup?")
    if (deployDecision == true) {
        undeployButtonInfo.innerHTML = "This may take some time, page will be refreshed automatically...";
        disableAllInteraction(true);
        $.ajax({
            url: "/semode/v1/" + setupName + "/benchmark/undeploy",
            success: function (result) {
                location.reload();
            }
        });
    }
});

executeBenchmark.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to execute benchmark?")
    if (deployDecision == true) {
        disableAllInteraction(true);
        executeButtonInfo.innerHTML = "This is not the recommended way to execute the benchmark. Use the CLI feature instead... Benchmark is under execution, this may take a while... Page will be refreshed";
        $.ajax({
            url: "/semode/v1/" + setupName + "/benchmark/execute",
            statusCode: {
                500: function () {
                    executeButtonInfo.innerHTML = "Server Error occurred... Wait a second until AWS Lambda is ready. You executed to early."
                }
            },
            success: function (result) {
                location.reload();
            }
        });
    }
});

fetchData.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to fetch benchmark?")
    if (deployDecision == true) {
        disableAllInteraction(true);
        fetchButtonInfo.innerHTML = "This may take some time, page will be refreshed automatically...";
        $.ajax({
            url: "/semode/v1/" + setupName + "/benchmark/" + versionDropdown.value + "/fetch",
            success: function (result) {
                location.reload();
            }
        });
    }
});

fetchDataOpenFaas.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to fetch benchmark?")
    if (deployDecision == true) {
        disableAllInteraction(true);
        document.getElementById('fetchDataOpenFaasInfo').innerHTML = "This may take some time, page will be refreshed automatically...";
        $.ajax({
            url: "/semode/v1/" + setupName + "/benchmark/" + versionDropdown.value + "/fetch/openfaas",
            success: function (result) {
                location.reload();
            }
        });
    }
});

// CHART HANDLING

function addBenchmarkDataToChart(version, chart) {
    $.ajax({
        url: "/semode/v1/" + setupName + "/benchmark/version/" + version + "/data",
        success: function (result) {
            if (result.length > 1) {
                console.log(result);
                const RGB = 255;
                var benchmarkDataOf = {
                    label: 'Version ' + version,
                    data: result,
                    backgroundColor: 'rgb(' + Math.random() * RGB + ', ' + Math.random() * RGB + ', ' + Math.random() * RGB + ')'
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

removeDataset.addEventListener('click', function () {
    myChart.data.datasets.pop();
    myChart.update();
});

addBenchmarkDataToChart(versionDropdown.value, myChart);
