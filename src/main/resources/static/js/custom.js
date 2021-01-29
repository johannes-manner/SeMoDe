


var startLocalCalibration = document.getElementById('startLocalCalibration')
startLocalCalibration.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to fetch benchmark?")
      if(deployDecision == true) {
          $.ajax({
            url: "/calibration/start/local",
            success: function(result) {
                console.log('Refresh page to see the infos - undeployment successful')
          }});
      }
});


var deployCalibration = document.getElementById('deployCalibration')
deployCalibration.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to deploy calibration?")
      if(deployDecision == true) {
          $.ajax({
            url: "/calibration/deploy/aws",
            success: function(result) {
                console.log('Refresh page to see the infos - undeployment successful')
          }});
      }
});


var startCalibration = document.getElementById('startCalibration')
startCalibration.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to deploy calibration?")
      if(deployDecision == true) {
          $.ajax({
            url: "/calibration/start/aws",
            success: function(result) {
                console.log('Refresh page to see the infos - undeployment successful')
          }});
      }
});

var undeployCalibration = document.getElementById('undeployCalibration')
undeployCalibration.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to undeploy calibration?")
      if(deployDecision == true) {
          $.ajax({
            url: "/calibration/undeploy/aws",
            success: function(result) {
                console.log('Refresh page to see the infos - undeployment successful')
          }});
      }
});

var computeMapping = document.getElementById('computeMapping')
computeMapping.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to undeploy calibration?")
      if(deployDecision == true) {
          $.ajax({
            url: "/mapping",
            success: function(result) {
                console.log('Refresh page to see the infos - undeployment successful')
          }});
      }
});


var runFunction = document.getElementById('runFunction')
runFunction.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to undeploy calibration?")
      if(deployDecision == true) {
          $.ajax({
            url: "/run",
            success: function(result) {
                console.log('Refresh page to see the infos - undeployment successful')
          }});
      }
});