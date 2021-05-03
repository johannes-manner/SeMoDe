var setupName = document.getElementById('setupName').value;

// buttons
var startLocalCalibration = document.getElementById('startLocalCalibration');
var deployCalibration = document.getElementById('deployCalibration');
var startCalibration = document.getElementById('startCalibration');
var undeployCalibration = document.getElementById('undeployCalibration');
var computeMapping = document.getElementById('computeMapping');
var computeMappingInfo = document.getElementById('computeMappingInfo');
var runFunction = document.getElementById('runFunction');

startLocalCalibration.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to fetch benchmark?")
      if(deployDecision == true) {
          $.ajax({
            url: "/calibration/start/local",
            success: function(result) {
                location.reload();
          }});
      }
});



deployCalibration.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to deploy calibration?")
      if(deployDecision == true) {
          $.ajax({
            url: "/calibration/deploy/aws",
            success: function(result) {
                location.reload();
          }});
      }
});



startCalibration.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to deploy calibration?")
      if(deployDecision == true) {
          $.ajax({
            url: "/calibration/start/aws",
            success: function(result) {
                location.reload();
          }});
      }
});


undeployCalibration.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to undeploy calibration?")
      if(deployDecision == true) {
          $.ajax({
            url: "/calibration/undeploy/aws",
            success: function(result) {
                location.reload();
          }});
      }
});


computeMapping.addEventListener('click', function() {
      var deployDecision = confirm("Do you really want to undeploy calibration?")
      if(deployDecision == true) {
          $.ajax({
            url: "/"+ setupName + "/calibration/mapping",
            success: function(result) {
                computeMappingInfo.innerHTML = result;
          }});
      }
});



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