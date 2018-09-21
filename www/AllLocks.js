exports.acquire = function () {
  cordova.exec(null, null, 'AllLocks', 'acquire', []);
};

exports.release = function () {
  cordova.exec(null, null, 'AllLocks', 'release', []);
};

exports.batteryOptimization = function (title, text) {
  cordova.exec(null, null, 'AllLocks', 'battery-optimization', [title, text]);
};
