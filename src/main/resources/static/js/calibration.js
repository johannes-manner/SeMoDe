var setupName = document.getElementById('setupName').value;
var calibrationConfigID = document.getElementById('calibrationConfigID');
var calibrationVersions = document.getElementById('calibrationVersions');
var localSteps = document.getElementById('localSteps');
var numberOfLocalCalibration = document.getElementById('numberOfLocalCalibration');
var localCalibrationDockerSourceFolder = document.getElementById('localCalibrationDockerSourceFolder');
var openFaasBaseURL = document.getElementById('openFaasBaseURL');
var openFaasFunctionname = document.getElementById('openFaasFunctionname');
var numberOfOpenFaasCalibration = document.getElementById('numberOfOpenFaasCalibration');
var openFaasIncrements = document.getElementById('openFaasIncrements');
var dockerHubUsername = document.getElementById('dockerHubUsername');
var openFaasUsername = document.getElementById('openFaasUsername');
var openFaasPassword = document.getElementById('openFaasPassword');
var openFaasFileTransferURL = document.getElementById('fileTransferURL');
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
var gflopsSizesCalibration = document.getElementById('gflopsSizesCalibration');
var runConfigDockerSourceFolder = document.getElementById('runConfigDockerSourceFolder');
var runConfigEnvironmentVariableFile = document.getElementById('runConfigEnvironmentVariableFile');
var runConfigNumberOfProfiles = document.getElementById('runConfigNumberOfProfiles');
var executedProfilesSelection = document.getElementById('executedProfilesSelection');
var machineName = document.getElementById('machineName');
var cpuModelName = document.getElementById('cpuModelName');
var cores = document.getElementById('cores');
var modelNr = document.getElementById('modelNr');
var operatingSystem = document.getElementById('operatingSystem');
var providerCalibrationFunction = document.getElementById('providerCalibrationFunction');
var localCalibrationFunction = document.getElementById('localCalibrationFunction');

// pipeline buttons
var startLocalCalibration = document.getElementById('startLocalCalibration');
var startOpenFaasCalibration = document.getElementById('startOpenFaasCalibration');
var deployCalibration = document.getElementById('deployCalibration');
var startCalibration = document.getElementById('startCalibration');
var undeployCalibration = document.getElementById('undeployCalibration');
var computeMapping = document.getElementById('computeMapping');
var computeMappingInfo = document.getElementById('computeMappingInfo');
var runFunction = document.getElementById('runFunction');

// charts
//  dropdowns
var localCalibrationConfig = document.getElementById('localCalibrationConfig');
var localCalibrationStats = document.getElementById('localCalibrationStats');
var providerCalibrationConfig = document.getElementById('providerCalibrationConfig');
var providerCalibrationStats = document.getElementById('providerCalibrationStats');
var averageAProfileSelection = document.getElementById('averageAProfileSelection');
var profileFunctionSelection = document.getElementById('profileFunctionSelection');
//  charts and remove buttons
var localCalibrationChart = document.getElementById('localCalibrationChart');
var removeDatasetLocalCalibration = document.getElementById('removeDatasetLocalCalibration');
var providerCalibrationChart = document.getElementById('providerCalibrationChart');
var removeDatasetProviderCalibration = document.getElementById('removeDatasetProviderCalibration');
var profilesChart = document.getElementById('profilesChart');

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
            openFaasBaseURL.value = result.openFaasConfig.baseUrl;
            openFaasFunctionname.value = result.openFaasConfig.functionName;
            numberOfOpenFaasCalibration.value = result.openFaasConfig.numberOfCalibrations;
            openFaasIncrements.value = result.openFaasConfig.increments;
            dockerHubUsername.value = result.openFaasConfig.dockerUsername;
            openFaasUsername.value = result.openFaasConfig.username;
            openFaasPassword.value = result.openFaasConfig.password;
            openFaasFileTransferURL.value = result.openFaasConfig.fileTransferURL;
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
            gflopsSizesCalibration.value = result.mappingCalibrationConfig.gflopsSizesCalibration;
            runConfigDockerSourceFolder.value = result.runningCalibrationConfig.functionDockerSourceFolder;
            runConfigEnvironmentVariableFile.value = result.runningCalibrationConfig.environmentVariablesFile;
            runConfigNumberOfProfiles.value = result.runningCalibrationConfig.numberOfProfiles;
            machineName.value = result.machineConfig.machineName;
            cpuModelName.value = result.machineConfig.cpuModelName;
            cores.value = result.machineConfig.noCPUs;
            modelNr.value = result.machineConfig.modelNr;
            operatingSystem.value = result.machineConfig.operatingSystem;
        }
    });
});

startLocalCalibration.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to execute local calibration?")
    if (deployDecision == true) {
        $.ajax({
            url: "/semode/v1/" + setupName + "/calibration/start/local",
            success: function (result) {
                location.reload();
            }
        });
    }
});

startOpenFaasCalibration.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to execute OpenFaaS calibration?")
    if (deployDecision == true) {
        $.ajax({
            url: "/semode/v1/" + setupName + "/calibration/start/openfaas",
            success: function (result) {
                location.reload();
            }
        });
    }
});


deployCalibration.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to deploy AWS calibration?")
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
    var deployDecision = confirm("Do you really want to start AWS calibration?")
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
    var deployDecision = confirm("Do you really want to undeploy AWS calibration?")
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
    var deployDecision = confirm("Do you really want to compute mapping?")
    if (deployDecision == true) {
        $.ajax({
            url: "/semode/v1/" + setupName + "/calibration/mapping",
            success: function (result) {
                computeMappingInfo.innerHTML = result;
            }
        });
    }
});

gflopsSizesCalibration.addEventListener("blur", function () {
    $.ajax({
        url: "/semode/v1/" + setupName + "/mapping/gflops?gflops=" + gflopsSizesCalibration.value,
        success: function (result) {
            document.getElementById('gflopsSizesCalibrationInfo').innerHTML = result;
        }
    });
});


runFunction.addEventListener('click', function () {
    var deployDecision = confirm("Do you really want to simulte the function run?")
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

function addCalibrationDataToChart(calibrationId, chart, regressionInfoField) {
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
    displayRegressionFunction(calibrationId, regressionInfoField);
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

localCalibrationConfig.addEventListener('change', function () {
    addCalibrationDataToChart(localCalibrationConfig.value, localCalibrationChartHandle, localCalibrationStats);
    displayRegressionFunction(localCalibrationConfig.value, localCalibrationFunction);
});

providerCalibrationConfig.addEventListener('change', function () {
    addCalibrationDataToChart(providerCalibrationConfig.value, providerCalibrationChartHandle, providerCalibrationStats);
    displayRegressionFunction(providerCalibrationConfig.value, providerCalibrationFunction);
});

averageAProfileSelection.addEventListener('change', function () {
    updateSimulationProfilingGraph();
});

profileFunctionSelection.addEventListener('change', function () {
    updateSimulationProfilingGraph();
});

addCalibrationDataToChart(localCalibrationConfig.value, localCalibrationChartHandle, localCalibrationStats);
addCalibrationDataToChart(providerCalibrationConfig.value, providerCalibrationChartHandle, providerCalibrationStats)

function displayRegressionFunction(calibrationId, labelForDisplayingInfo) {
    $.ajax({
        url: "/semode/v1/" + setupName + "/calibration/" + calibrationId + "/mapping",
        success: function (result) {
            labelForDisplayingInfo.textContent = result;
        }
    });
}

// update already executed profiles selection
$.ajax({
    url: "/semode/v1/" + setupName + "/profiles",
    success: function (result) {
        for (const cId of result) {
            var option = document.createElement("option");
            option.text = "Profile from Calibration-Version " + cId.calibrationId + "-" + cId.version;
            option.value = cId.calibrationId;
            executedProfilesSelection.add(option);
        }
    }
});

executedProfilesSelection.addEventListener('change', function () {
    var selectedProfileCalibration = executedProfilesSelection.value;
    if (selectedProfileCalibration > 0) {
        // get Functions
        $.ajax({
            url: "/semode/v1/" + setupName + "/profiles/" + executedProfilesSelection.value + "/names",
            success: function (result) {
                console.log(result)
                for (const functionName of result) {
                    var option = document.createElement("option");
                    option.text = "Function: " + functionName;
                    option.value = functionName;
                    profileFunctionSelection.add(option);
                }
            }
        });
    }
});

function updateSimulationProfilingGraph() {
    var selectedProfileCalibration = executedProfilesSelection.value;
    $.ajax({
        url: "/semode/v1/" + setupName + "/profiles/" + selectedProfileCalibration + "?avg=" + averageAProfileSelection.value + "&function=" + profileFunctionSelection.value,
        success: function (result) {
            console.log(result);

            const cpuMemoryEquivalents = new Map(Object.entries(result.cpuMemoryEquivalents));
            const providerCpuMemoryEquivalents = new Map(Object.entries(result.providerCpuMemoryEquivalents));
            const verticalLines = [];

            cpuMemoryEquivalents.forEach((value, key) => {
                console.log(key + " - " + value);
                var lineConfig = {
                    type: 'line',
                    xMin: value,
                    xMax: value,
                    borderColor: 'rgb(65,105,225)',
                    borderWidth: 2,
                }
                verticalLines.push(lineConfig);
            });

            providerCpuMemoryEquivalents.forEach((value, key) => {
                console.log(key + " - " + value);
                var lineConfig = {
                    type: 'line',
                    xMin: value,
                    xMax: value,
                    borderColor: 'rgb(255,193,99)',
                    borderWidth: 2,
                }
                verticalLines.push(lineConfig);
            });

            profilesChartHandle.destroy();

            var plugins = {
                annotation: {
                    annotations: verticalLines
                }
            }

            var config = {
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
                            beginAtZero: true,
                            display: true,
                            position: 'left'
                        },
                        y1: {
                            beginAtZero: true,
                            display: true,
                            position: 'right',
                            grid: {
                                drawOnChartArea: false, // only want the grid lines for one axis to show up
                            }
                        }
                    },
                    plugins
                }
            };

            // create new graph
            profilesChartHandle = new Chart(profilesChart, config);

            // fill the selection for selecting an a for the ideal curve
            averageAProfileSelection.length = 0;
            for (i = 0; i < result.avgOptions.length; i++) {
                var option = document.createElement("option");
                option.text = "a = " + result.avgOptions[i];
                option.value = result.avgOptions[i];
                averageAProfileSelection.add(option);
                if (result.avgOptions[i] == result.avg) {
                    averageAProfileSelection.selectedIndex = i;
                }
            }

            // add simulation data
            const RGB = 255;
            var calibrationDataOf = {
                label: 'Profile ' + selectedProfileCalibration,
                data: result.profileData,
                backgroundColor: 'rgb(50,205,50)',
                yAxisID: 'y'
            };

            // add ideal average curve based on the selected a
            var avgData = {
                type: 'line',
                label: 'AVG ' + selectedProfileCalibration + ', ' + result.avg,
                pointRadius: 1,
                pointHoverRadius: 1,
                data: result.avgData,
                backgroundColor: 'rgb(0,100,0)',
                fill: false,
                yAxisID: 'y'
            };

            var priceSimulation = {
                type: 'line',
                label: 'Simulated Price',
                data: result.simulatedPrice,
                backgroundColor: 'rgb(255,0,255)',
                yAxisID: 'y1'
            }

            profilesChartHandle.data.datasets.push(avgData);
            profilesChartHandle.data.datasets.push(calibrationDataOf);
            profilesChartHandle.data.datasets.push(priceSimulation);
            profilesChartHandle.update();
        }
    });
}
