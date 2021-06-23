var setupName = document.getElementById('setupName').value;
var calibrationConfigID = document.getElementById('calibrationConfigID');
var calibrationVersions = document.getElementById('calibrationVersions');
var localSteps = document.getElementById('localSteps');
var numberOfLocalCalibration = document.getElementById('numberOfLocalCalibration');
var localCalibrationDockerSourceFolder = document.getElementById('localCalibrationDockerSourceFolder');
var awsBucketName = document.getElementById('awsBucketName');
var numberOfAWSExecutions = document.getElementById('numberOfAWSExecutions');
var awsRegion = document.getElementById('awsRegion');
var awsRuntime = document.getElementById('awsRuntime');
var awsLambdaRole = document.getElementById('awsLambdaRole');
var awsHandlerName = document.getElementById('awsHandlerName');
var awsTimeout = document.getElementById('awsTimeout');
var awsPathToZip = document.getElementById('awsPathToZip');
var awsTargetUrl = document.getElementById('awsTargetUrl');
var awsApiKey = document.getElementById('awsApiKey');
var awsRestApiId = document.getElementById('awsRestApiId');
var awsApiKeyId = document.getElementById('awsApiKeyId');
var awsUsagePlanId = document.getElementById('awsUsagePlanId');
var mappingProviderPlatform = document.getElementById('mappingProviderPlatform');
var runConfigDockerSourceFolder = document.getElementById('runConfigDockerSourceFolder');
var runConfigEnvironmentVariableFile = document.getElementById('runConfigEnvironmentVariableFile');
var runConfigNumberOfProfiles = document.getElementById('runConfigNumberOfProfiles');
var executedProfilesSelection = document.getElementById('executedProfilesSelection');
var machineName = document.getElementById('machineName');
var cpuModelName = document.getElementById('cpuModelName');
var modelNr = document.getElementById('modelNr');
var operatingSystem = document.getElementById('operatingSystem');

// pipeline buttons
var startLocalCalibration = document.getElementById('startLocalCalibration');
var deployCalibration = document.getElementById('deployCalibration');
var startCalibration = document.getElementById('startCalibration');
var undeployCalibration = document.getElementById('undeployCalibration');
var computeMapping = document.getElementById('computeMapping');
var computeMappingInfo = document.getElementById('computeMappingInfo');
var runFunction = document.getElementById('runFunction');

// charts
//  dropdowns
var localCalibrationConfig = document.getElementById('localCalibrationConfig');
var providerCalibrationConfig = document.getElementById('providerCalibrationConfig');
//  charts and remove buttons
var localCalibrationChart = document.getElementById('localCalibrationChart');
var removeDatasetLocalCalibration = document.getElementById('removeDatasetLocalCalibration');
var providerCalibrationChart = document.getElementById('providerCalibrationChart');
var removeDatasetProviderCalibration = document.getElementById('removeDatasetProviderCalibration');
var profilesChart = document.getElementById('profilesChart');
var removeDatasetProfiles = document.getElementById('removeDatasetProfiles');

function setButtonDisabledProperty(boolDisabled) {
    startLocalCalibration.disabled = boolDisabled;
    deployCalibration.disabled = boolDisabled;
    startCalibration.disabled = boolDisabled;
    undeployCalibration.disabled = boolDisabled;
    computeMapping.disabled = boolDisabled;
    computeMappingInfo.disabled = boolDisabled;
    runFunction.disabled = boolDisabled;
}

calibrationVersions.addEventListener('change', function () {
    var version = calibrationVersions.value;
    if (calibrationVersions.selectedIndex == 0) {
        setButtonDisabledProperty(false);
    } else {
        setButtonDisabledProperty(true);
    }

    $.ajax({
        url: "/semode/v1/" + setupName + "/calibration/version/" + version,
        success: function (result) {
            console.log(result);
            calibrationConfigID.innerHTML = result.id;
            localSteps.value = result.localConfig.localSteps;
            numberOfLocalCalibration.value = result.localConfig.numberOfLocalCalibrations;
            localCalibrationDockerSourceFolder.value = result.localConfig.calibrationDockerSourceFolder;
            awsBucketName.value = result.awsCalibrationConfig.bucketName;
            numberOfAWSExecutions.value = result.awsCalibrationConfig.numberOfAWSExecutions;
            awsRegion.value = result.awsCalibrationConfig.benchmarkConfig.region;
            awsRuntime.value = result.awsCalibrationConfig.benchmarkConfig.runtime;
            awsLambdaRole.value = result.awsCalibrationConfig.benchmarkConfig.awsArnLambdaRole;
            awsHandlerName.value = result.awsCalibrationConfig.benchmarkConfig.functionHandler;
            awsTimeout.value = result.awsCalibrationConfig.benchmarkConfig.timeout;
            awsMemorySizes.value = result.awsCalibrationConfig.benchmarkConfig.memorySizes;
            awsPathToZip.value = result.awsCalibrationConfig.benchmarkConfig.pathToSource;
            awsTargetUrl.value = result.awsCalibrationConfig.benchmarkConfig.targetUrl;
            awsApiKey.value = result.awsCalibrationConfig.benchmarkConfig.apiKey;
            awsRestApiId.value = result.awsCalibrationConfig.benchmarkConfig.restApiId;
            awsApiKeyId.value = result.awsCalibrationConfig.benchmarkConfig.apiKeyId;
            awsUsagePlanId.value = result.awsCalibrationConfig.benchmarkConfig.usagePlanId;
            mappingProviderPlatform.value = result.mappingCalibrationConfig.memorySizesCalibration;
            runConfigDockerSourceFolder.value = result.runningCalibrationConfig.functionDockerSourceFolder;
            runConfigEnvironmentVariableFile.value = result.runningCalibrationConfig.environmentVariablesFile;
            runConfigNumberOfProfiles.value = result.runningCalibrationConfig.numberOfProfiles;
            machineName.value = result.machineConfig.machineName;
            cpuModelName.value = result.machineConfig.cpuModelName;
            modelNr.value = result.machineConfig.modelNr;
            operatingSystem.value = result.machineConfig.operatingSystem;
        }
    });
});

startLocalCalibration.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to fetch benchmark?")
    if (deployDecision == true) {
        $.ajax({
            url: "/semode/v1/" + setupName + "/calibration/start/local",
            success: function (result) {
                location.reload();
            }
        });
    }
});


deployCalibration.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to deploy calibration?")
    if (deployDecision == true) {
        $.ajax({
            url: "/semode/v1/" + setupName + "/calibration/deploy/aws",
            success: function (result) {
                location.reload();
            }
        });
    }
});


startCalibration.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to deploy calibration?")
    if (deployDecision == true) {
        $.ajax({
            url: "/semode/v1/" + setupName + "/calibration/start/aws",
            success: function (result) {
                location.reload();
            }
        });
    }
});


undeployCalibration.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to undeploy calibration?")
    if (deployDecision == true) {
        $.ajax({
            url: "/semode/v1/" + setupName + "/calibration/undeploy/aws",
            success: function (result) {
                location.reload();
            }
        });
    }
});


computeMapping.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to undeploy calibration?")
    if (deployDecision == true) {
        $.ajax({
            url: "/semode/v1/" + setupName + "/calibration/mapping",
            success: function (result) {
                computeMappingInfo.innerHTML = result;
            }
        });
    }
});


runFunction.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to undeploy calibration?")
    if (deployDecision == true) {
        $.ajax({
            url: "/semode/v1/" + setupName + "/simulation/run",
            success: function (result) {
                console.log('Refresh page to see the infos - undeployment successful')
            }
        });
    }
});

// CHART HANDLING

function addCalibrationDataToChart(calibrationId, chart) {
    $.ajax({
        url: "/semode/v1/" + setupName + "/calibration/" + calibrationId + "/data",
        success: function (result) {
            if (result.length > 1) {
                console.log(result);
                const RGB = 255;
                var calibrationDataOf = {
                    label: 'Id ' + calibrationId,
                    data: result,
                    backgroundColor: 'rgb(' + Math.random() * RGB + ', ' + Math.random() * RGB + ', ' + Math.random() * RGB + ')'
                };
                chart.data.datasets.push(calibrationDataOf);
                chart.update();
            }
        }
    });
}

var chartConfig = {
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
};

var localCalibrationChartHandle = new Chart(localCalibrationChart, chartConfig);
var providerCalibrationChartHandle = new Chart(providerCalibrationChart, JSON.parse(JSON.stringify(chartConfig)));
var profilesChartHandle = new Chart(profilesChart, JSON.parse(JSON.stringify(chartConfig)))

removeDatasetLocalCalibration.addEventListener('click', function () {
    localCalibrationChartHandle.data.datasets.pop();
    localCalibrationChartHandle.update();
});

removeDatasetProviderCalibration.addEventListener('click', function () {
    providerCalibrationChartHandle.data.datasets.pop();
    providerCalibrationChartHandle.update();
});

removeDatasetProfiles.addEventListener('click', function () {
    profilesChartHandle.data.datasets.pop();
    profilesChartHandle.update();
});

localCalibrationConfig.addEventListener('change', function () {
    addCalibrationDataToChart(localCalibrationConfig.value, localCalibrationChartHandle);
});

providerCalibrationConfig.addEventListener('change', function () {
    addCalibrationDataToChart(providerCalibrationConfig.value, providerCalibrationChartHandle);
});

addCalibrationDataToChart(localCalibrationConfig.value, localCalibrationChartHandle);
addCalibrationDataToChart(providerCalibrationConfig.value, providerCalibrationChartHandle);

// update already executed profiles selection
$.ajax({
    url: "/semode/v1/" + setupName + "/profiles",
    success: function (result) {
        for (const cId of result) {
            var option = document.createElement("option");
            option.text = "Profile from Calibration " + cId.calibrationId;
            option.value = cId.calibrationId;
            executedProfilesSelection.add(option);
        }
    }
});

executedProfilesSelection.addEventListener('change', function () {
    var selectedProfileCalibration = executedProfilesSelection.value;
    if (selectedProfileCalibration > 0) {
        $.ajax({
            url: "/semode/v1/" + setupName + "/profiles/" + selectedProfileCalibration,
            success: function (result) {
                console.log(result);
                const RGB = 255;
                var calibrationDataOf = {
                    label: 'Profile ' + selectedProfileCalibration,
                    data: result,
                    backgroundColor: 'rgb(' + Math.random() * RGB + ', ' + Math.random() * RGB + ', ' + Math.random() * RGB + ')'
                };
                profilesChartHandle.data.datasets.push(calibrationDataOf);
                profilesChartHandle.update();
            }
        });
    }
});