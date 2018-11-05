'use strict';

const uuidv4 = require('uuid/v4');
const containerId = uuidv4();

exports.fibonacci = (params) => {
    var n = params.n;
    
    var requestId = uuidv4();

    if (!n || !isNumeric(n)) {
        throw "Parameter is not a number!";
    }

    n = parseInt(n);
    var result = fib(n);
    console.log("SEMODE::" + createJSON(result, requestId));
	return createJSON(result, requestId);
};

var fib = function(n) {
    if (n <= 1) {
        return 1;
    } else {
        return fib(n - 1) + fib(n - 2);
    }
};

var createJSON = function(message, requestId) {
	var response = {
        result: message,
        platformId: requestId,
        containerId: containerId
    };

    return response;	
}

var isNumeric = function(value) {
    return /^\d+$/.test(value);
};
