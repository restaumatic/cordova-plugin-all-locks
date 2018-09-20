exports.acquire = function () {
  cordova.exec(null, null, 'AllLocks', 'acquire', []);
};

exports.release = function () {
  cordova.exec(null, null, 'AllLocks', 'release', []);
};

exports.batteryOptimization = function () {
  cordova.exec(null, null, 'AllLocks', 'battery-optimization', []);
};
